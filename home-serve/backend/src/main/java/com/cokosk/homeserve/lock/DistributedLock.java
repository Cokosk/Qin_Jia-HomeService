package com.cokosk.homeserve.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * 用于抢单等高并发场景
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {
    
    private final RedissonClient redissonClient;
    
    /**
     * 获取锁
     * @param lockKey 锁的key
     * @param waitTime 等待获取锁的最大时间(毫秒)
     * @param leaseTime 锁的自动释放时间(毫秒)
     * @return 锁对象，获取失败返回null
     */
    public RLock tryLock(String lockKey, long waitTime, long leaseTime) {
        String threadId = UUID.randomUUID().toString();
        try {
            RLock lock = redissonClient.getLock(lockKey);
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            if (acquired) {
                log.info("获取分布式锁成功: {}, threadId: {}", lockKey, threadId);
                return lock;
            }
            log.warn("获取分布式锁失败: {}, threadId: {}", lockKey, threadId);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁异常: {}", lockKey, e);
            return null;
        }
    }
    
    /**
     * 释放锁
     * @param lock 锁对象
     * @param lockValue 锁的唯一标识
     */
    public void unlock(RLock lock, String lockValue) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("释放分布式锁成功: {}", lockValue);
        }
    }
    
    /**
     * 简单释放锁（根据key释放）
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("释放分布式锁成功: {}", lockKey);
        }
    }
    
    /**
     * 检查锁是否被持有
     * @param lockKey 锁的key
     * @return true-被持有
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }
}