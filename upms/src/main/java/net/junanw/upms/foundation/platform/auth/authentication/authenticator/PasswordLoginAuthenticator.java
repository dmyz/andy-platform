package net.junanw.upms.foundation.platform.auth.authentication.authenticator;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.platform.auth.account.AccountNormalizer;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialEntity;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialMapper;
import net.junanw.upms.foundation.platform.auth.authentication.LoginAuthentication;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthGrantType;
import net.junanw.upms.foundation.platform.auth.authentication.model.LoginPrincipal;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.security.PasswordEncoder;
import net.junanw.upms.system.iam.account.entity.AccountEntity;
import net.junanw.upms.system.iam.account.model.AccountType;
import net.junanw.upms.system.iam.account.mapper.AccountMapper;
import net.junanw.upms.system.iam.user.entity.UserEntity;
import net.junanw.upms.system.iam.user.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static net.junanw.upms.foundation.platform.auth.application.password.credential.table.PasswordCredentialEntityTableDef.PASSWORD_CREDENTIAL_ENTITY;
import static net.junanw.upms.system.iam.account.entity.table.AccountEntityTableDef.ACCOUNT_ENTITY;
import static net.junanw.upms.system.iam.user.entity.table.UserEntityTableDef.USER_ENTITY;

/**
 * 用户名密码登录认证器。
 */
@Component
public class PasswordLoginAuthenticator implements LoginAuthenticator {

    /**
     * 账号 Mapper。
     */
    private final AccountMapper iamAccountMapper;

    /**
     * 用户 Mapper。
     */
    private final UserMapper iamUserMapper;

    /**
     * 密码凭证 Mapper。
     */
    private final PasswordCredentialMapper authPasswordCredentialMapper;

    /**
     * 密码编码器。
     */
    private final PasswordEncoder passwordEncoder;

    public PasswordLoginAuthenticator(
            AccountMapper iamAccountMapper,
            UserMapper iamUserMapper,
            PasswordCredentialMapper authPasswordCredentialMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.iamAccountMapper = iamAccountMapper;
        this.iamUserMapper = iamUserMapper;
        this.authPasswordCredentialMapper = authPasswordCredentialMapper;
        this.passwordEncoder = passwordEncoder;
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

        // 2. 查询启用中的用户名账号。
        QueryWrapper accountQuery = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.USERNAME.name()))
                .and(ACCOUNT_ENTITY.NORMALIZED_IDENTIFIER.eq(normalizedUsername))
                .and(ACCOUNT_ENTITY.STATUS.eq("ACTIVE"))
                .and(ACCOUNT_ENTITY.IS_LOGIN_ENABLED.eq(true));
        AccountEntity account = iamAccountMapper.selectOneByQuery(accountQuery);
        if (account == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 3. 加载用户主记录并校验用户状态。
        UserEntity user = iamUserMapper.selectOneById(account.getUserId());
        if (user == null || user.getDeleted()) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(401, "用户状态不可登录");
        }

        // 4. 读取密码凭证并检查锁定状态。
        QueryWrapper credentialQuery = QueryWrapper.create()
                .where(PASSWORD_CREDENTIAL_ENTITY.USER_ID.eq(user.getId()));
        PasswordCredentialEntity credential = authPasswordCredentialMapper.selectOneByQuery(credentialQuery);
        if (credential == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (credential.getLockedUntil() != null && credential.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(401, "账号已锁定");
        }

        // 5. 校验密码哈希是否匹配。
        if (!passwordEncoder.matches(authentication.password(), credential.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        return new LoginPrincipal(user.getId(), account.getNormalizedIdentifier(), supports());
    }
}
