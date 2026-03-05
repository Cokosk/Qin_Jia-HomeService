package com.cokosk.homeserve.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis限流工具类
 * 使用滑动窗口算法实现接口限流
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiter {
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * 限流检查
     * @param key 限流key (如: ip:api)
     * @param maxRequests 最大请求数
     * @param windowSeconds 时间窗口(秒)
     * @return true-允许通过, false-被限流
     */
    public boolean tryAcquire(String key, int maxRequests, int windowSeconds) {
        String redisKey = "rate:limit:" + key;
        
        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count == null) {
                return false;
            }
            
            // 首次设置过期时间
            if (count == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }
            
            boolean allowed = count <= maxRequests;
            if (!allowed) {
                log.warn("接口限流触发: key={}, count={}, max={}", redisKey, count, maxRequests);
            }
            return allowed;
        } catch (Exception e) {
            log.error("限流检查异常: {}", redisKey, e);
            // 异常时放行，避免影响正常业务
            return true;
        }
    }
    
    /**
     * 简化版限流（每IP每分钟10次）
     * @param ip 客户端IP
     * @param api 接口路径
     * @return true-允许, false-被限流
     */
    public boolean tryAcquire(String ip, String api) {
        return tryAcquire(ip + ":" + api, 10, 60);
    }
}