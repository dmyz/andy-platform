package net.junanw.upms.infrastructure.security.satoken;

import cn.dev33.satoken.dao.SaTokenDao;
import net.junanw.upms.infrastructure.security.satoken.satoken.RedissonSaTokenDao;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Sa-Token 基础设施装配。
 *
 * <p>当前平台使用 Redisson 作为 Sa-Token 的底层存储实现，相关 bean 统一在此注册。
 */
@Configuration
@Profile("!test")
public class SaTokenInfrastructureConfig {

    /**
     * 注册 Sa-Token DAO 实现。
     *
     * @param redissonClient Redisson 客户端
     * @return 基于 Redisson 的 Sa-Token DAO
     */
    @Bean
    @Primary
    public SaTokenDao saTokenDao(RedissonClient redissonClient) {
        return new RedissonSaTokenDao(redissonClient);
    }
}
