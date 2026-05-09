package net.junanw.upms.infrastructure.test.support;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 测试环境 Sa-Token 存储配置。
 *
 * <p>集成测试使用内存 DAO，避免测试上下文依赖外部 Redis 服务。
 */
@TestConfiguration
@Profile("test")
public class TestSaTokenDaoConfig {

    /**
     * 注册内存版 Sa-Token DAO。
     *
     * @return 基于本地内存的 Sa-Token DAO
     */
    @Bean
    @Primary
    public SaTokenDao testSaTokenDao() {
        return new SaTokenDaoDefaultImpl();
    }

    /**
     * 注册内存版 RedissonClient。
     *
     * @return 支持安全限流所需最小 API 的 Redisson mock
     */
    @Bean
    @Primary
    public RedissonClient testRedissonClient() {
        Map<String, BucketState> buckets = new ConcurrentHashMap<>();
        Map<String, CounterState> counters = new ConcurrentHashMap<>();
        RedissonClient redissonClient = mock(RedissonClient.class);
        when(redissonClient.<String>getBucket(anyString())).thenAnswer(invocation -> bucketMock(
                buckets.computeIfAbsent(invocation.getArgument(0), ignored -> new BucketState())
        ));
        when(redissonClient.getAtomicLong(anyString())).thenAnswer(invocation -> counterMock(
                counters.computeIfAbsent(invocation.getArgument(0), ignored -> new CounterState())
        ));
        return redissonClient;
    }

    /**
     * 构建内存 Bucket mock。
     */
    private RBucket<String> bucketMock(BucketState state) {
        RBucket<String> bucket = mock(RBucket.class);
        when(bucket.isExists()).thenAnswer(invocation -> state.exists());
        when(bucket.setIfAbsent(anyString(), any(Duration.class))).thenAnswer(invocation -> state.setIfAbsent(
                invocation.getArgument(0),
                invocation.getArgument(1)
        ));
        doAnswer(invocation -> {
            state.set(invocation.getArgument(0), Duration.ofSeconds(((TimeUnit) invocation.getArgument(2)).toSeconds(invocation.getArgument(1))));
            return null;
        }).when(bucket).set(anyString(), anyLong(), any(TimeUnit.class));
        when(bucket.delete()).thenAnswer(invocation -> state.delete());
        return bucket;
    }

    /**
     * 构建内存 AtomicLong mock。
     */
    private RAtomicLong counterMock(CounterState state) {
        RAtomicLong counter = mock(RAtomicLong.class);
        when(counter.incrementAndGet()).thenAnswer(invocation -> state.incrementAndGet());
        when(counter.expire(any(Duration.class))).thenAnswer(invocation -> {
            state.expire(invocation.getArgument(0));
            return true;
        });
        when(counter.delete()).thenAnswer(invocation -> state.delete());
        return counter;
    }

    private static final class BucketState {
        private String value;
        private long expiresAtMillis;

        private boolean exists() {
            expireIfNeeded();
            return value != null;
        }

        private boolean setIfAbsent(String newValue, Duration ttl) {
            if (exists()) {
                return false;
            }
            set(newValue, ttl);
            return true;
        }

        private void set(String newValue, Duration ttl) {
            value = newValue;
            expiresAtMillis = ttl == null || ttl.isZero() || ttl.isNegative()
                    ? 0L
                    : System.currentTimeMillis() + ttl.toMillis();
        }

        private boolean delete() {
            value = null;
            expiresAtMillis = 0L;
            return true;
        }

        private void expireIfNeeded() {
            if (expiresAtMillis > 0L && System.currentTimeMillis() >= expiresAtMillis) {
                delete();
            }
        }
    }

    private static final class CounterState {
        private long value;
        private long expiresAtMillis;

        private long incrementAndGet() {
            expireIfNeeded();
            value++;
            return value;
        }

        private void expire(Duration ttl) {
            expiresAtMillis = ttl == null || ttl.isZero() || ttl.isNegative()
                    ? 0L
                    : System.currentTimeMillis() + ttl.toMillis();
        }

        private boolean delete() {
            value = 0L;
            expiresAtMillis = 0L;
            return true;
        }

        private void expireIfNeeded() {
            if (expiresAtMillis > 0L && System.currentTimeMillis() >= expiresAtMillis) {
                delete();
            }
        }
    }
}
