package net.junanw.upms.infrastructure.shared.security;

import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Redis 安全限流器。
 *
 * <p>基于现有 Redisson 计数器和 TTL 实现认证相关失败计数、发送冷却和临时锁定。
 */
@Component
public class SecurityRateLimiter {

    private static final String PREFIX = "upms:security:";

    private final RedissonClient redissonClient;

    public SecurityRateLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 若锁定键存在则拒绝请求。
     *
     * @param scope 场景
     * @param key 业务键
     * @param message 错误消息
     */
    public void rejectIfLocked(String scope, String key, String message) {
        RBucket<String> bucket = redissonClient.getBucket(lockKey(scope, key));
        if (Boolean.TRUE.equals(bucket.isExists())) {
            throw new BusinessException(429, message);
        }
    }

    /**
     * 注册一次失败，超过阈值后加锁。
     *
     * @param scope 场景
     * @param key 业务键
     * @param threshold 阈值
     * @param window 计数窗口
     * @param lockDuration 锁定时长
     * @param message 错误消息
     */
    public void recordFailure(String scope, String key, long threshold, Duration window, Duration lockDuration, String message) {
        String counterKey = counterKey(scope, key);
        RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
        long count = counter.incrementAndGet();
        if (count == 1) {
            counter.expire(window);
        }
        if (count >= threshold) {
            redissonClient.getBucket(lockKey(scope, key)).set("1", lockDuration.toSeconds(), TimeUnit.SECONDS);
            counter.delete();
            throw new BusinessException(429, message);
        }
    }

    /**
     * 检查窗口内调用次数是否超过阈值。
     *
     * @param scope 场景
     * @param key 业务键
     * @param threshold 阈值
     * @param window 计数窗口
     * @param message 错误消息
     */
    public void checkLimit(String scope, String key, long threshold, Duration window, String message) {
        String counterKey = counterKey(scope, key);
        RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
        long count = counter.incrementAndGet();
        if (count == 1) {
            counter.expire(window);
        }
        if (count > threshold) {
            throw new BusinessException(429, message);
        }
    }

    /**
     * 清理失败状态。
     *
     * @param scope 场景
     * @param key 业务键
     */
    public void clear(String scope, String key) {
        redissonClient.getAtomicLong(counterKey(scope, key)).delete();
        redissonClient.getBucket(lockKey(scope, key)).delete();
    }

    /**
     * 检查冷却窗口，窗口内重复调用会被拒绝。
     *
     * @param scope 场景
     * @param key 业务键
     * @param cooldown 冷却时间
     * @param message 错误消息
     */
    public void checkCooldown(String scope, String key, Duration cooldown, String message) {
        RBucket<String> bucket = redissonClient.getBucket(cooldownKey(scope, key));
        if (!bucket.setIfAbsent("1", cooldown)) {
            throw new BusinessException(429, message);
        }
    }

    private String counterKey(String scope, String key) {
        return PREFIX + normalize(scope) + ":fail:" + normalize(key);
    }

    private String lockKey(String scope, String key) {
        return PREFIX + normalize(scope) + ":lock:" + normalize(key);
    }

    private String cooldownKey(String scope, String key) {
        return PREFIX + normalize(scope) + ":cooldown:" + normalize(key);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "blank";
        }
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.:@-]", "_");
    }
}
