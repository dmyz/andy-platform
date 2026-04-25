package net.junanw.upms.foundation.platform.auth.authentication.authenticator;

import net.junanw.upms.foundation.platform.auth.account.AccountNormalizer;
import net.junanw.upms.foundation.platform.auth.authentication.LoginAuthentication;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthGrantType;
import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import net.junanw.upms.foundation.platform.auth.query.model.UserProfileSnapshot;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import net.junanw.upms.foundation.platform.auth.verification.service.VerificationCodeService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 邮箱验证码登录认证器。
 */
@Component
public class EmailCodeLoginAuthenticator extends AbstractVerificationCodeLoginAuthenticator {

    public EmailCodeLoginAuthenticator(
            UserContextService userContextService,
            VerificationCodeService verificationCodeService
    ) {
        super(userContextService, verificationCodeService);
    }

    @Override
    public AuthGrantType supports() {
        return AuthGrantType.EMAIL_CODE;
    }

    @Override
    protected String rawTarget(LoginAuthentication authentication) {
        return authentication.email();
    }

    @Override
    protected String normalizeTarget(String target) {
        return AccountNormalizer.normalizeEmail(target);
    }

    @Override
    protected Optional<UserProfileSnapshot> loadProfile(String normalizedTarget) {
        return userContextService().findByEmail(normalizedTarget);
    }

    @Override
    protected VerificationTargetType targetType() {
        return VerificationTargetType.EMAIL;
    }

    @Override
    protected String missingTargetMessage() {
        return "邮箱不能为空";
    }

    @Override
    protected String missingBindingMessage() {
        return "邮箱未绑定用户";
    }
}
