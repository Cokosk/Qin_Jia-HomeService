package com.cokosk.homeserve.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DistributedLockTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @InjectMocks
    private DistributedLock distributedLock;

    private String lockKey;
    private String lockValue;

    @BeforeEach
    void setUp() {
        lockKey = "test:lock:key";
        lockValue = UUID.randomUUID().toString();
    }

    @Test
    void testTryLockSuccess() throws InterruptedException {
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        RLock result = distributedLock.tryLock(lockKey, 3000, 30000);

        assertNotNull(result);
        assertEquals(rLock, result);
        verify(rLock, times(1)).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testTryLockFailure() throws InterruptedException {
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        RLock result = distributedLock.tryLock(lockKey, 3000, 30000);

        assertNull(result);
        verify(rLock, times(1)).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testTryLockInterruptedException() throws InterruptedException {
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        // 抛出中断异常
        doThrow(new InterruptedException("Test interruption"))
            .when(rLock).tryLock(anyLong(), anyLong(), any(TimeUnit.class));

        // 分布式锁捕获异常后返回null
        RLock result = distributedLock.tryLock(lockKey, 3000, 30000);

        // 验证返回null
        assertNull(result);
    }

    @Test
    void testUnlockWithLockAndValue() {
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        distributedLock.unlock(rLock, lockValue);

        verify(rLock, times(1)).unlock();
    }

    @Test
    void testUnlockWithLockNotHeldByCurrentThread() {
        when(rLock.isHeldByCurrentThread()).thenReturn(false);

        distributedLock.unlock(rLock, lockValue);

        verify(rLock, never()).unlock();
    }

    @Test
    void testUnlockWithNullLock() {
        distributedLock.unlock((RLock) null, lockValue);

        verify(rLock, never()).isHeldByCurrentThread();
        verify(rLock, never()).unlock();
    }

    @Test
    void testUnlockByKeyWhenHeldByCurrentThread() {
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        distributedLock.unlock(lockKey);

        verify(rLock, times(1)).unlock();
    }

    @Test
    void testUnlockByKeyWhenNotHeldByCurrentThread() {
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.isHeldByCurrentThread()).thenReturn(false);

        distributedLock.unlock(lockKey);

        verify(rLock, never()).unlock();
    }

    @Test
    void testIsLocked() {
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.isLocked()).thenReturn(true);

        boolean result = distributedLock.isLocked(lockKey);

        assertTrue(result);
        verify(rLock, times(1)).isLocked();
    }

    @Test
    void testIsLockedFalse() {
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.isLocked()).thenReturn(false);

        boolean result = distributedLock.isLocked(lockKey);

        assertFalse(result);
        verify(rLock, times(1)).isLocked();
    }
}
