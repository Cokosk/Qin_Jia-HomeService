package com.cokosk.homeserve.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cokosk.homeserve.entity.Order;
import com.cokosk.homeserve.lock.DistributedLock;
import com.cokosk.homeserve.lock.RateLimiter;
import com.cokosk.homeserve.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ListOperations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private DistributedLock distributedLock;

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private RLock rLock;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private ZSetOperations<String, String> zSetOps;

    @Mock
    private ListOperations<String, String> listOps;

    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() throws Exception {
        orderService = new OrderService(distributedLock, rateLimiter, redisTemplate);
        
        Field baseMapperField = OrderService.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(orderService, orderMapper);

        order = new Order();
        order.setId(1L);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        lenient().when(redisTemplate.opsForList()).thenReturn(listOps);
    }

    @Test
    void testGrabOrder_Success() {
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(valueOps.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(anyLong())).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        assertEquals("抢单成功", result);
        verify(orderMapper, times(1)).updateById(any(Order.class));
        verify(valueOps, times(1)).set(anyString(), eq("1"));
        verify(distributedLock, times(1)).unlock(any(RLock.class), anyString());
    }

    @Test
    void testGrabOrder_AlreadyGrabbed() {
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(valueOps.get(anyString())).thenReturn("1");

        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        assertEquals("订单已被抢走", result);
        verify(orderMapper, never()).updateById(any(Order.class));
        verify(distributedLock, times(1)).unlock(any(RLock.class), anyString());
    }

    @Test
    void testGrabOrder_OrderNotFound() {
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(valueOps.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(anyLong())).thenReturn(null);

        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        assertEquals("订单不存在", result);
        verify(orderMapper, never()).updateById(any(Order.class));
        verify(distributedLock, times(1)).unlock(any(RLock.class), anyString());
    }

    @Test
    void testGrabOrder_RateLimited() {
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(false);

        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        assertEquals("请求过于频繁，请稍后再试", result);
        verify(distributedLock, never()).tryLock(anyString(), anyLong(), anyLong());
    }

    @Test
    void testGrabOrder_LockFailure() {
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(null);

        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        assertEquals("抢单失败，请稍后重试", result);
        verify(orderMapper, never()).selectById(anyLong());
    }

    @Test
    void testGrabOrder_AlreadyTakenFromDB() {
        order.setStatus(1);
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(valueOps.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(anyLong())).thenReturn(order);

        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        assertEquals("该订单已被抢或已取消", result);
        verify(orderMapper, never()).updateById(any(Order.class));
        verify(distributedLock, times(1)).unlock(any(RLock.class), anyString());
    }

    @Test
    void testGrabOrder_UpdateFailure() {
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(valueOps.get(anyString())).thenReturn(null);
        when(orderMapper.selectById(anyLong())).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(0);

        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        assertEquals("抢单失败，请重试", result);
        verify(distributedLock, times(1)).unlock(any(RLock.class), anyString());
    }

    @Test
    void testAddToGrabPool() {
        Order newOrder = new Order();
        newOrder.setId(1L);
        newOrder.setCreateTime(LocalDateTime.now());

        orderService.addToGrabPool(newOrder);

        verify(zSetOps, times(1)).add(anyString(), anyString(), anyDouble());
        verify(valueOps, times(1)).set(anyString(), eq("0"));
    }
}
