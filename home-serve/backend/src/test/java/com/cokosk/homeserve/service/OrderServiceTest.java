package com.cokosk.homeserve.service;

import com.cokosk.homeserve.entity.Order;
import com.cokosk.homeserve.lock.DistributedLock;
import com.cokosk.homeserve.lock.RateLimiter;
import com.cokosk.homeserve.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setStatus(0); // 待抢单状态
        order.setCreateTime(LocalDateTime.now());
    }

    @Test
    void testGrabOrderSuccess() {
        // Arrange
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null); // Redis中无缓存
        when(orderMapper.selectById(anyLong())).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(true);

        // Act
        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        // Assert
        assertEquals("抢单成功", result);
        verify(orderMapper, times(1)).updateById(any(Order.class));
        verify(redisTemplate, times(1)).opsForValue().set(anyString(), eq("1"));
        verify(distributedLock, times(1)).unlock(any(RLock.class), anyString());
    }

    @Test
    void testGrabOrderRateLimited() {
        // Arrange
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(false);

        // Act
        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        // Assert
        assertEquals("请求过于频繁，请稍后再试", result);
        verify(distributedLock, never()).tryLock(anyString(), anyLong(), anyLong());
    }

    @Test
    void testGrabOrderLockFailure() {
        // Arrange
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(null);

        // Act
        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        // Assert
        assertEquals("抢单失败，请稍后重试", result);
        verify(orderMapper, never()).selectById(anyLong());
    }

    @Test
    void testGrabOrderAlreadyTakenFromCache() {
        // Arrange
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(redisTemplate.opsForValue().get(anyString())).thenReturn("1"); // 订单已被抢

        // Act
        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        // Assert
        assertEquals("订单已被抢走", result);
        verify(orderMapper, never()).updateById(any(Order.class));
        verify(distributedLock, times(1)).unlock(any(RLock.class), anyString());
    }

    @Test
    void testGrabOrderAlreadyTakenFromDatabase() {
        // Arrange
        order.setStatus(1); // 已被抢
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(orderMapper.selectById(anyLong())).thenReturn(order);

        // Act
        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        // Assert
        assertEquals("该订单已被抢或已取消", result);
        verify(orderMapper, never()).updateById(any(Order.class));
        verify(distributedLock, times(1)).unlock(any(RLock.class), anyString());
    }

    @Test
    void testGrabOrderNotFound() {
        // Arrange
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(orderMapper.selectById(anyLong())).thenReturn(null);

        // Act
        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        // Assert
        assertEquals("订单不存在", result);
        verify(orderMapper, never()).updateById(any(Order.class));
        verify(distributedLock, times(1)).unlock(any(RLock.class), anyString());
    }

    @Test
    void testGrabOrderUpdateFailure() {
        // Arrange
        when(rateLimiter.tryAcquire(anyString(), anyString())).thenReturn(true);
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(orderMapper.selectById(anyLong())).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(false);

        // Act
        String result = orderService.grabOrder(1L, 2L, "192.168.1.1");

        // Assert
        assertEquals("抢单失败，请重试", result);
        verify(distributedLock, times(1)).unlock(any(RLock.class), anyString());
    }

    @Test
    void testAddToGrabPool() {
        // Arrange
        Order newOrder = new Order();
        newOrder.setId(1L);
        newOrder.setCreateTime(LocalDateTime.now());

        // Act
        orderService.addToGrabPool(newOrder);

        // Assert
        verify(redisTemplate, times(1)).opsForZSet().add(anyString(), anyString(), anyDouble());
        verify(redisTemplate, times(1)).opsForValue().set(anyString(), eq("0"), any());
    }
}