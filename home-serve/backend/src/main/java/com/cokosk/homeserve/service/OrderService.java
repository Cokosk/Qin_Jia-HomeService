package com.cokosk.homeserve.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cokosk.homeserve.entity.Order;
import com.cokosk.homeserve.lock.DistributedLock;
import com.cokosk.homeserve.lock.RateLimiter;
import com.cokosk.homeserve.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 订单服务类 - 核心高并发抢单逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrderMapper, Order> {
    
    private final DistributedLock distributedLock;
    private final RateLimiter rateLimiter;
    private final StringRedisTemplate redisTemplate;
    
    /**
     * 抢单核心逻辑
     * 1. 限流检查
     * 2. 获取分布式锁
     * 3. 验证订单状态
     * 4. 更新数据库
     * 5. 更新Redis缓存
     * 6. 异步通知
     *
     * @param orderId 订单ID
     * @param workerId 服务者ID
     * @param clientIp 客户端IP
     * @return 抢单结果
     */
    @Transactional(rollbackFor = Exception.class)
    public String grabOrder(Long orderId, Long workerId, String clientIp) {
        // ========== 1. 限流检查 ==========
        if (!rateLimiter.tryAcquire(clientIp, "/api/order/grab")) {
            return "请求过于频繁，请稍后再试";
        }
        
        // 锁的key
        String lockKey = "order:grab:lock:" + orderId;
        String lockValue = UUID.randomUUID().toString();
        
        // ========== 2. 获取分布式锁 (等待3秒，获取后30秒自动释放) ==========
        RLock lock = distributedLock.tryLock(lockKey, 3000, 30000);
        if (lock == null) {
            log.warn("抢单失败，锁被占用: orderId={}, workerId={}", orderId, workerId);
            return "抢单失败，请稍后重试";
        }
        
        try {
            // ========== 3. 从Redis获取订单状态进行快速校验 ==========
            String orderStatusKey = "order:status:" + orderId;
            String cachedStatus = redisTemplate.opsForValue().get(orderStatusKey);
            
            // 如果Redis中有缓存，检查状态
            if (cachedStatus != null && !cachedStatus.equals("0")) {
                log.warn("订单已被抢: orderId={}, status={}", orderId, cachedStatus);
                return "订单已被抢走";
            }
            
            // ========== 4. 查询数据库验证订单状态 ==========
            Order order = this.getById(orderId);
            if (order == null) {
                return "订单不存在";
            }
            
            // 订单状态必须是0(待抢单)才能抢
            if (order.getStatus() != 0) {
                log.warn("订单状态不可抢: orderId={}, status={}", orderId, order.getStatus());
                return "该订单已被抢或已取消";
            }
            
            // ========== 5. 更新数据库订单状态 ==========
            order.setStatus(1); // 已接单
            order.setWorkerId(workerId);
            order.setGrabTime(LocalDateTime.now());
            
            boolean updated = this.updateById(order);
            if (!updated) {
                log.error("订单更新失败: orderId={}", orderId);
                return "抢单失败，请重试";
            }
            
            // ========== 6. 同步更新Redis缓存 ==========
            redisTemplate.opsForValue().set(orderStatusKey, "1");
            // 从抢单池中移除（使用Sorted Set）
            redisTemplate.opsForZSet().remove("order:grab:pool", orderId.toString());
            
            log.info("抢单成功: orderId={}, workerId={}", orderId, workerId);
            
            // ========== 7. 异步通知（可扩展）============
            // 将订单ID推入异步队列处理通知等
            redisTemplate.opsForList().rightPush("queue:order:grabbed", orderId.toString());
            
            return "抢单成功";
            
        } catch (Exception e) {
            log.error("抢单异常: orderId={}, workerId={}", orderId, workerId, e);
            return "抢单异常，请重试";
        } finally {
            // ========== 8. 释放分布式锁 ==========
            distributedLock.unlock(lock, lockValue);
        }
    }
    
    /**
     * 将新订单添加到Redis抢单池
     */
    public void addToGrabPool(Order order) {
        // 使用Sorted Set按创建时间排序
        String score = String.valueOf(order.getCreateTime().toLocalTime().toSecondOfDay());
        redisTemplate.opsForZSet().add("order:grab:pool", order.getId().toString(), Double.parseDouble(score));
        // 同时更新订单状态缓存
        redisTemplate.opsForValue().set("order:status:" + order.getId(), "0");
    }
    
    /**
     * 从Redis抢单池获取可抢订单
     */
    public java.util.List<Object> getGrabPoolOrders() {
        return redisTemplate.opsForZSet().range("order:grab:pool", 0, -1);
    }
    
    /**
     * 缓存热点数据 - 服务列表
     */
    public void cacheServiceList(String categoryKey, String data) {
        redisTemplate.opsForValue().set("service:category:" + categoryKey, data, Duration.ofMinutes(30));
    }
    
    public String getCachedServiceList(String categoryKey) {
        return redisTemplate.opsForValue().get("service:category:" + categoryKey);
    }
}