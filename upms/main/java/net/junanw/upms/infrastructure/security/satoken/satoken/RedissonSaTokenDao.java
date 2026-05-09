package net.junanw.upms.infrastructure.security.satoken.satoken;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson 的 Sa-Token DAO 实现。
 *
 * <p>该实现负责把 Sa-Token 的字符串值、对象值和会话对象统一映射到 Redis，
 * 是当前登录态持久化的核心基础设施。
 */
public class RedissonSaTokenDao implements SaTokenDao {

    private final RedissonClient redissonClient;

    public RedissonSaTokenDao(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 读取字符串值。
     *
     * @param key Redis 键
     * @return 键对应的字符串值
     */
    @Override
    public String get(String key) {
        return getObject(key, String.class);
    }

    /**
     * 写入字符串值并设置过期时间。
     *
     * @param key Redis 键
     * @param value 字符串值
     * @param timeout 过期秒数
     */
    @Override
    public void set(String key, String value, long timeout) {
        setValue(key, value, timeout);
    }

    /**
     * 更新字符串值并保留原 TTL。
     *
     * @param key Redis 键
     * @param value 新值
     */
    @Override
    public void update(String key, String value) {
        getBucket(key).setAndKeepTTL(value);
    }

    /**
     * 删除指定键。
     *
     * @param key Redis 键
     */
    @Override
    public void delete(String key) {
        redissonClient.getKeys().delete(key);
    }

    /**
     * 获取剩余有效期。
     *
     * @param key Redis 键
     * @return Sa-Token 约定的超时值
     */
    @Override
    public long getTimeout(String key) {
        return toSaTimeout(redissonClient.getKeys().remainTimeToLive(key));
    }

    /**
     * 更新键的过期时间。
     *
     * @param key Redis 键
     * @param timeout 新过期秒数
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        updateExpire(key, timeout);
    }

    /**
     * 读取对象值。
     *
     * @param key Redis 键
     * @return 键对应的对象值
     */
    @Override
    public Object getObject(String key) {
        return getBucket(key).get();
    }

    /**
     * 按类型读取对象值。
     *
     * @param key Redis 键
     * @param classType 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象；不存在时返回 {@code null}
     */
    @Override
    public <T> T getObject(String key, Class<T> classType) {
        Object value = getObject(key);
        if (value == null) {
            return null;
        }
        return classType.cast(value);
    }

    /**
     * 写入对象值。
     *
     * @param key Redis 键
     * @param object 对象值
     * @param timeout 过期秒数
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        setValue(key, object, timeout);
    }

    /**
     * 更新对象值并保留原 TTL。
     *
     * @param key Redis 键
     * @param object 新对象值
     */
    @Override
    public void updateObject(String key, Object object) {
        getBucket(key).setAndKeepTTL(object);
    }

    /**
     * 删除对象值。
     *
     * @param key Redis 键
     */
    @Override
    public void deleteObject(String key) {
        delete(key);
    }

    /**
     * 获取对象值剩余有效期。
     *
     * @param key Redis 键
     * @return Sa-Token 约定的超时值
     */
    @Override
    public long getObjectTimeout(String key) {
        return getTimeout(key);
    }

    /**
     * 更新对象值过期时间。
     *
     * @param key Redis 键
     * @param timeout 新过期秒数
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        updateTimeout(key, timeout);
    }

    /**
     * 读取会话对象。
     *
     * @param sessionId 会话 ID
     * @return SaSession
     */
    @Override
    public SaSession getSession(String sessionId) {
        return getObject(sessionId, SaSession.class);
    }

    /**
     * 写入会话对象。
     *
     * @param session 会话对象
     * @param timeout 过期秒数
     */
    @Override
    public void setSession(SaSession session, long timeout) {
        setObject(session.getId(), session, timeout);
    }

    /**
     * 更新会话对象并保留原 TTL。
     *
     * @param session 会话对象
     */
    @Override
    public void updateSession(SaSession session) {
        updateObject(session.getId(), session);
    }

    /**
     * 删除会话对象。
     *
     * @param sessionId 会话 ID
     */
    @Override
    public void deleteSession(String sessionId) {
        deleteObject(sessionId);
    }

    /**
     * 获取会话剩余有效期。
     *
     * @param sessionId 会话 ID
     * @return Sa-Token 约定的超时值
     */
    @Override
    public long getSessionTimeout(String sessionId) {
        return getObjectTimeout(sessionId);
    }

    /**
     * 更新会话剩余有效期。
     *
     * @param sessionId 会话 ID
     * @param timeout 新过期秒数
     */
    @Override
    public void updateSessionTimeout(String sessionId, long timeout) {
        updateObjectTimeout(sessionId, timeout);
    }

    /**
     * 按前缀和关键字搜索键。
     *
     * @param prefix 键前缀
     * @param keyword 关键字
     * @param start 起始下标
     * @param size 返回条数
     * @param sortType 是否正序
     * @return 命中的键列表
     */
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        String pattern = prefix + "*";
        if (keyword != null && !keyword.isBlank()) {
            pattern = prefix + "*" + keyword + "*";
        }
        List<String> keys = new ArrayList<>();
        redissonClient.getKeys().getKeysByPattern(pattern).forEach(keys::add);
        keys.sort(sortType ? Comparator.naturalOrder() : Comparator.reverseOrder());
        int fromIndex = Math.max(start, 0);
        if (fromIndex >= keys.size()) {
            return List.of();
        }
        int toIndex = size < 0 ? keys.size() : Math.min(fromIndex + size, keys.size());
        return keys.subList(fromIndex, toIndex);
    }

    /**
     * 获取通用 bucket 访问器。
     *
     * @param key Redis 键
     * @return bucket 对象
     */
    private RBucket<Object> getBucket(String key) {
        return redissonClient.getBucket(key);
    }

    /**
     * 统一写值逻辑。
     *
     * <p>兼容 Sa-Token 的“永不过期”和“立即删除”语义，避免不同写入方法重复处理 TTL 分支。
     *
     * @param key Redis 键
     * @param value 待写入值
     * @param timeout 过期秒数
     */
    private void setValue(String key, Object value, long timeout) {
        if (timeout == NEVER_EXPIRE) {
            getBucket(key).set(value);
            return;
        }
        if (timeout <= 0) {
            delete(key);
            return;
        }
        getBucket(key).set(value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 统一过期时间更新逻辑。
     *
     * @param key Redis 键
     * @param timeout 新过期秒数
     */
    private void updateExpire(String key, long timeout) {
        if (timeout == NEVER_EXPIRE) {
            getBucket(key).clearExpire();
            return;
        }
        if (timeout <= 0) {
            delete(key);
            return;
        }
        getBucket(key).expire(Duration.ofSeconds(timeout));
    }

    /**
     * 将 Redis TTL 结果转换为 Sa-Token 语义。
     *
     * @param ttlMillis Redis 返回的毫秒 TTL
     * @return Sa-Token 约定的超时值
     */
    private long toSaTimeout(long ttlMillis) {
        if (ttlMillis == -1) {
            return NEVER_EXPIRE;
        }
        if (ttlMillis == -2) {
            return NOT_VALUE_EXPIRE;
        }
        if (ttlMillis <= 0) {
            return NOT_VALUE_EXPIRE;
        }
        return Math.max(1L, ttlMillis / 1000);
    }
}
