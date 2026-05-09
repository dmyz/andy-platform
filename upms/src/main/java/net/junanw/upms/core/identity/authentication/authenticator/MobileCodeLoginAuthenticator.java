package net.junanw.upms.core.identity.authentication.authenticator;

import net.junanw.upms.core.identity.account.AccountNormalizer;
import net.junanw.upms.core.identity.authentication.LoginAuthentication;
import net.junanw.upms.core.identity.authentication.model.AuthGrantType;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;
import net.junanw.upms.core.identity.verification.model.VerificationTargetType;
import net.junanw.upms.core.identity.verification.service.VerificationCodeService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 手机验证码登录认证器。
 */
@Component
public class MobileCodeLoginAuthenticator extends AbstractVerificationCodeLoginAuthenticator {

    public MobileCodeLoginAuthenticator(
            UserContextService userContextService,
            VerificationCodeService verificationCodeService
    ) {
        super(userContextService, verificationCodeService);
    }

    @Override
    public AuthGrantType supports() {
        return AuthGrantType.MOBILE_CODE;
    }

    @Override
    protected String rawTarget(LoginAuthentication authentication) {
        return authentication.mobile();
    }

    @Override
    protected String normalizeTarget(String target) {
        return AccountNormalizer.normalizeMobile(target);
    }

    @Override
    protected Optional<UserProfileSnapshot> loadProfile(String normalizedTarget) {
        return userContextService().findByMobile(normalizedTarget);
    }

    @Override
    protected VerificationTargetType targetType() {
        return VerificationTargetType.MOBILE;
    }

    @Override
    protected String missingTargetMessage() {
        return "手机号不能为空";
    }

    @Override
    protected String missingBindingMessage() {
        return "手机号未绑定用户";
    }
}
