package net.junanw.upms.infrastructure.shared.security;

import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityRateLimiterTest {

    @Test
    void recordFailureShouldLockWhenThresholdReached() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RAtomicLong counter = mock(RAtomicLong.class);
        RBucket<String> bucket = mock(RBucket.class);
        when(redissonClient.getAtomicLong(anyString())).thenReturn(counter);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(counter.incrementAndGet()).thenReturn(3L);
        SecurityRateLimiter limiter = new SecurityRateLimiter(redissonClient);

        assertThrows(BusinessException.class, () -> limiter.recordFailure(
                "password-login",
                "admin:127.0.0.1",
                3,
                Duration.ofMinutes(15),
                Duration.ofMinutes(15),
                "locked"
        ));

        verify(bucket).set("1", 900L, TimeUnit.SECONDS);
        verify(counter).delete();
    }

    @Test
    void checkCooldownShouldRejectRepeatedCall() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RBucket<String> bucket = mock(RBucket.class);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        when(bucket.setIfAbsent(anyString(), any(Duration.class))).thenReturn(false);
        SecurityRateLimiter limiter = new SecurityRateLimiter(redissonClient);

        assertThrows(BusinessException.class, () -> limiter.checkCooldown(
                "verification-code-send",
                "mobile:13800138000:login",
                Duration.ofSeconds(60),
                "cooldown"
        ));
    }
}
