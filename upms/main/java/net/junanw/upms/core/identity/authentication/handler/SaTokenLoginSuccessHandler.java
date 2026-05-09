package net.junanw.upms.core.identity.authentication.handler;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import net.junanw.upms.core.identity.authentication.LoginAuthentication;
import net.junanw.upms.core.identity.authentication.model.LoginPrincipal;
import net.junanw.upms.core.identity.authentication.model.response.LoginTokenResponse;
import net.junanw.upms.core.security.audit.login.model.LoginAuditEventType;
import net.junanw.upms.core.security.audit.login.service.LoginAuditRecord;
import net.junanw.upms.core.security.audit.login.service.LoginAuditService;
import net.junanw.upms.core.identity.session.AuthOnlineSessionService;
import net.junanw.upms.infrastructure.shared.security.TokenFingerprintService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 基于 Sa-Token 的登录成功处理器。
 */
@Component
public class SaTokenLoginSuccessHandler implements LoginAuthenticationSuccessHandler {

    /**
     * 登录审计服务。
     */
    private final LoginAuditService loginAuditService;
    private final AuthOnlineSessionService authOnlineSessionService;
    private final TokenFingerprintService tokenFingerprintService;

    public SaTokenLoginSuccessHandler(
            LoginAuditService loginAuditService,
            AuthOnlineSessionService authOnlineSessionService,
            TokenFingerprintService tokenFingerprintService
    ) {
        this.loginAuditService = loginAuditService;
        this.authOnlineSessionService = authOnlineSessionService;
        this.tokenFingerprintService = tokenFingerprintService;
    }

    @Override
    public LoginTokenResponse onSuccess(LoginAuthentication authentication, LoginPrincipal principal) {
        LocalDateTime loginTime = LocalDateTime.now();
        // 1. 建立 Sa-Token 登录态。
        StpUtil.login(principal.userId());

        // 2. 取得本次登录生成的 token。
        String tokenValue = StpUtil.getTokenValue();

        // 3. 在 Sa-Token token-session 中登记在线会话信息。
        authOnlineSessionService.register(principal.userId(), tokenValue, principal.loginType().name(), authentication.details(), loginTime, SaManager.getConfig().getTimeout());

        // 4. 记录登录成功审计。
        loginAuditService.record(new LoginAuditRecord(
                authentication.accountIdentifier(),
                principal.loginType().name(),
                LoginAuditEventType.LOGIN_SUCCESS.name(),
                true,
                null,
                authentication.details().remoteAddr(),
                authentication.details().userAgent(),
                tokenFingerprintService.fingerprint(tokenValue),
                authentication.details().traceId(),
                authentication.details().requestId(),
                LocalDateTime.now()
        ));

        // 5. 返回 Bearer token 响应。
        return new LoginTokenResponse(tokenValue, "Bearer", SaManager.getConfig().getTimeout());
    }
}
