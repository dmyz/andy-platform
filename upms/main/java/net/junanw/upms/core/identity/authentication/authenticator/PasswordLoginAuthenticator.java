package net.junanw.upms.core.identity.authentication.authenticator;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.authentication.LoginAuthentication;
import net.junanw.upms.core.identity.account.AccountNormalizer;
import net.junanw.upms.core.identity.account.entity.AccountEntity;
import net.junanw.upms.core.identity.account.mapper.AccountMapper;
import net.junanw.upms.core.identity.account.model.AccountType;
import net.junanw.upms.core.identity.authentication.model.AuthGrantType;
import net.junanw.upms.core.identity.authentication.model.LoginPrincipal;
import net.junanw.upms.core.identity.password.entity.PasswordCredentialEntity;
import net.junanw.upms.core.identity.password.mapper.PasswordCredentialMapper;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.security.PasswordEncoder;
import net.junanw.upms.infrastructure.shared.security.SecurityRateLimiter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;


/**
 * 用户名密码登录认证器。
 */
@Component
public class PasswordLoginAuthenticator implements LoginAuthenticator {

    private static final String RATE_LIMIT_SCOPE = "password-login";
    private static final String LOCK_MESSAGE = "账号暂时锁定，请稍后再试";
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final long FAILURE_THRESHOLD = 5L;

    /**
     * 账号 Mapper。
     */
    private final AccountMapper accountMapper;

    /**
     * 用户 Mapper。
     */
    private final UserMapper userMapper;

    /**
     * 密码凭证 Mapper。
     */
    private final PasswordCredentialMapper passwordCredentialMapper;

    /**
     * 密码编码器。
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 安全限流器。
     */
    private final SecurityRateLimiter securityRateLimiter;

    public PasswordLoginAuthenticator(
            AccountMapper accountMapper,
            UserMapper userMapper,
            PasswordCredentialMapper passwordCredentialMapper,
            PasswordEncoder passwordEncoder,
            SecurityRateLimiter securityRateLimiter
    ) {
        this.accountMapper = accountMapper;
        this.userMapper = userMapper;
        this.passwordCredentialMapper = passwordCredentialMapper;
        this.passwordEncoder = passwordEncoder;
        this.securityRateLimiter = securityRateLimiter;
    }

    @Override
    public AuthGrantType supports() {
        return AuthGrantType.PASSWORD;
    }

    @Override
    public LoginPrincipal authenticate(LoginAuthentication authentication) {
        // 1. 归一化用户名并做空值校验。
        String normalizedUsername = AccountNormalizer.normalizeUsername(authentication.username());
        if (normalizedUsername == null || authentication.password() == null || authentication.password().isBlank()) {
            throw new BusinessException(400, "username 或 password 不能为空");
        }
        String rateLimitKey = buildRateLimitKey(normalizedUsername, resolveRemoteAddr(authentication));
        securityRateLimiter.rejectIfLocked(RATE_LIMIT_SCOPE, rateLimitKey, LOCK_MESSAGE);

        // 2. 查询启用中的用户名账号。
        AccountEntity account = QueryChain.of(accountMapper)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .eq(AccountEntity::getNormalizedIdentifier, normalizedUsername)
                .eq(AccountEntity::getStatus, "ACTIVE")
                .eq(AccountEntity::getIsLoginEnabled, true)
                .get();
        if (account == null) {
            recordLoginFailure(rateLimitKey);
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 3. 加载用户主记录并校验用户状态。
        UserEntity user = userMapper.getById(account.getUserId());
        if (user == null || user.getDeleted()) {
            recordLoginFailure(rateLimitKey);
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(401, "用户状态不可登录");
        }

        // 4. 读取密码凭证并检查锁定状态。
        PasswordCredentialEntity credential = QueryChain.of(passwordCredentialMapper)
                .eq(PasswordCredentialEntity::getUserId, user.getId())
                .get();
        if (credential == null) {
            recordLoginFailure(rateLimitKey);
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (credential.getLockedUntil() != null && credential.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(401, "账号已锁定");
        }

        // 5. 校验密码哈希是否匹配。
        if (!passwordEncoder.matches(authentication.password(), credential.getPasswordHash())) {
            recordLoginFailure(rateLimitKey);
            throw new BusinessException(401, "用户名或密码错误");
        }

        securityRateLimiter.clear(RATE_LIMIT_SCOPE, rateLimitKey);
        return new LoginPrincipal(user.getId(), account.getNormalizedIdentifier(), supports());
    }

    /**
     * 记录一次密码登录失败。
     */
    private void recordLoginFailure(String rateLimitKey) {
        securityRateLimiter.recordFailure(RATE_LIMIT_SCOPE, rateLimitKey, FAILURE_THRESHOLD, FAILURE_WINDOW, LOCK_DURATION, LOCK_MESSAGE);
    }

    /**
     * 构建用户名与来源 IP 组合限流键。
     */
    private String buildRateLimitKey(String normalizedUsername, String remoteAddr) {
        String ip = remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr.trim();
        return normalizedUsername + ":" + ip;
    }

    /**
     * 安全读取认证请求来源 IP。
     */
    private String resolveRemoteAddr(LoginAuthentication authentication) {
        return authentication.details() == null ? null : authentication.details().remoteAddr();
    }
}
