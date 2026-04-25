package net.junanw.upms.foundation.platform.auth.authentication.handler;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import net.junanw.upms.foundation.platform.auth.authentication.LoginAuthentication;
import net.junanw.upms.foundation.platform.auth.authentication.model.LoginPrincipal;
import net.junanw.upms.foundation.platform.auth.authentication.model.response.LoginTokenResponse;
import net.junanw.upms.foundation.platform.auth.authentication.session.AuthSessionKeys;
import net.junanw.upms.support.audit.login.model.LoginAuditEventType;
import net.junanw.upms.support.audit.login.service.LoginAuditRecord;
import net.junanw.upms.support.audit.login.service.LoginAuditService;
import net.junanw.upms.foundation.platform.auth.application.session.AuthOnlineSessionService;
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

    public SaTokenLoginSuccessHandler(LoginAuditService loginAuditService, AuthOnlineSessionService authOnlineSessionService) {
        this.loginAuditService = loginAuditService;
        this.authOnlineSessionService = authOnlineSessionService;
    }

    @Override
    public LoginTokenResponse onSuccess(LoginAuthentication authentication, LoginPrincipal principal) {
        LocalDateTime loginTime = LocalDateTime.now();
        // 1. 建立 Sa-Token 登录态。
        StpUtil.login(principal.userId());

        // 2. 在 token-session 中写入用户名和登录方式，供后续上下文与审计使用。
        SaSession tokenSession = StpUtil.getTokenSession();
        tokenSession.set(AuthSessionKeys.USERNAME, principal.username());
        tokenSession.set(AuthSessionKeys.LOGIN_TYPE, principal.loginType());
        String tokenValue = StpUtil.getTokenValue();

        // 3. 记录在线会话台账。
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
                tokenValue,
                authentication.details().traceId(),
                authentication.details().requestId(),
                LocalDateTime.now()
        ));

        // 5. 返回 Bearer token 响应。
        return new LoginTokenResponse(tokenValue, "Bearer", SaManager.getConfig().getTimeout());
    }
}
