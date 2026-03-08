package com.cokosk.homeserve.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        // Arrange
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // Act
        RLock result = distributedLock.tryLock(lockKey, 3000, 30000);

        // Assert
        assertNotNull(result);
        assertEquals(rLock, result);
        verify(rLock, times(1)).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testTryLockFailure() throws InterruptedException {
        // Arrange
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // Act
        RLock result = distributedLock.tryLock(lockKey, 3000, 30000);

        // Assert
        assertNull(result);
        verify(rLock, times(1)).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testTryLockInterruptedException() throws InterruptedException {
        // Arrange
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        doThrow(new InterruptedException("Test interruption")).when(rLock)
            .tryLock(anyLong(), anyLong(), any(TimeUnit.class));

        // Act & Assert
        assertThrows(Exception.class, () -> {
            distributedLock.tryLock(lockKey, 3000, 30000);
        });
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted(); // Clear interrupted flag for test
    }

    @Test
    void testUnlockWithLockAndValue() {
        // Arrange
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // Act
        distributedLock.unlock(rLock, lockValue);

        // Assert
        verify(rLock, times(1)).unlock();
    }

    @Test
    void testUnlockWithLockNotHeldByCurrentThread() {
        // Arrange
        when(rLock.isHeldByCurrentThread()).thenReturn(false);

        // Act
        distributedLock.unlock(rLock, lockValue);

        // Assert
        verify(rLock, never()).unlock();
    }

    @Test
    void testUnlockWithNullLock() {
        // Act
        distributedLock.unlock((RLock) null, lockValue);

        // Assert
        verify(rLock, never()).isHeldByCurrentThread();
        verify(rLock, never()).unlock();
    }

    @Test
    void testUnlockByKeyWhenHeldByCurrentThread() {
        // Arrange
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // Act
        distributedLock.unlock(lockKey);

        // Assert
        verify(rLock, times(1)).unlock();
    }

    @Test
    void testUnlockByKeyWhenNotHeldByCurrentThread() {
        // Arrange
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.isHeldByCurrentThread()).thenReturn(false);

        // Act
        distributedLock.unlock(lockKey);

        // Assert
        verify(rLock, never()).unlock();
    }

    @Test
    void testIsLocked() {
        // Arrange
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.isLocked()).thenReturn(true);

        // Act
        boolean result = distributedLock.isLocked(lockKey);

        // Assert
        assertTrue(result);
        verify(rLock, times(1)).isLocked();
    }

    @Test
    void testIsLockedFalse() {
        // Arrange
        when(redissonClient.getLock(eq(lockKey))).thenReturn(rLock);
        when(rLock.isLocked()).thenReturn(false);

        // Act
        boolean result = distributedLock.isLocked(lockKey);

        // Assert
        assertFalse(result);
        verify(rLock, times(1)).isLocked();
    }
}