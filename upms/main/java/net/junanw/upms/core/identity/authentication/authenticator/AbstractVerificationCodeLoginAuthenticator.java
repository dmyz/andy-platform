package net.junanw.upms.core.identity.authentication.authenticator;

import net.junanw.upms.core.identity.authentication.LoginAuthentication;
import net.junanw.upms.core.identity.authentication.model.LoginPrincipal;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;
import net.junanw.upms.core.identity.verification.model.VerificationScene;
import net.junanw.upms.core.identity.verification.model.VerificationTargetType;
import net.junanw.upms.core.identity.verification.service.VerificationCodeService;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;

import java.util.Optional;

/**
 * 验证码登录认证器抽象基类。
 * <p>
 * 统一封装目标提取、归一化、验证码校验和用户档案加载逻辑。
 */
public abstract class AbstractVerificationCodeLoginAuthenticator implements LoginAuthenticator {

    /**
     * 用户上下文查询服务。
     */
    private final UserContextService userContextService;

    /**
     * 验证码服务。
     */
    private final VerificationCodeService verificationCodeService;

    protected AbstractVerificationCodeLoginAuthenticator(
            UserContextService userContextService,
            VerificationCodeService verificationCodeService
    ) {
        this.userContextService = userContextService;
        this.verificationCodeService = verificationCodeService;
    }

    @Override
    public LoginPrincipal authenticate(LoginAuthentication authentication) {
        // 1. 提取并归一化手机号或邮箱。
        String target = normalizeTarget(rawTarget(authentication));
        if (target == null) {
            throw new BusinessException(400, missingTargetMessage());
        }

        // 2. 校验登录场景验证码。
        verificationCodeService.verifyLatest(targetType(), target, VerificationScene.LOGIN, authentication.code());

        // 3. 根据目标值查找绑定用户并生成登录主体。
        UserProfileSnapshot profile = loadProfile(target)
                .orElseThrow(() -> new BusinessException(404, missingBindingMessage()));
        return new LoginPrincipal(profile.userId(), profile.username(), supports());
    }

    /**
     * 暴露给子类的用户查询服务。
     */
    protected final UserContextService userContextService() {
        return userContextService;
    }

    /**
     * 从统一认证命令中提取原始目标值。
     */
    protected abstract String rawTarget(LoginAuthentication authentication);

    /**
     * 对原始目标值做归一化处理。
     */
    protected abstract String normalizeTarget(String target);

    /**
     * 根据归一化目标值加载用户档案。
     */
    protected abstract Optional<UserProfileSnapshot> loadProfile(String normalizedTarget);

    /**
     * 返回当前验证码登录认证器对应的目标类型。
     */
    protected abstract VerificationTargetType targetType();

    /**
     * 目标值缺失时的错误提示。
     */
    protected abstract String missingTargetMessage();

    /**
     * 目标值未绑定用户时的错误提示。
     */
    protected abstract String missingBindingMessage();
}
