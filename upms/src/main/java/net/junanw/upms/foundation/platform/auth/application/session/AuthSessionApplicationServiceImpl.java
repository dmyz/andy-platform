package net.junanw.upms.foundation.platform.auth.application.session;

import cn.dev33.satoken.stp.StpUtil;
import net.junanw.upms.foundation.platform.auth.authentication.LoginAuthenticationProcessor;
import net.junanw.upms.foundation.platform.auth.authentication.context.LoginUserContext;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthGrantType;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthRequestContext;
import net.junanw.upms.foundation.platform.auth.authentication.model.request.UnifiedLoginRequest;
import net.junanw.upms.foundation.platform.auth.authentication.model.response.LoginTokenResponse;
import net.junanw.upms.foundation.platform.auth.authentication.session.AuthSessionKeys;
import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import net.junanw.upms.foundation.platform.auth.query.model.CurrentUserResponse;
import net.junanw.upms.support.audit.login.model.LoginAuditEventType;
import net.junanw.upms.support.audit.login.service.LoginAuditRecord;
import net.junanw.upms.support.audit.login.service.LoginAuditService;
import net.junanw.upms.foundation.platform.auth.application.session.AuthOnlineSessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证会话应用门面实现。
 * <p>
 * 负责串联登录、当前用户读取和登出审计。
 */
@Service
public class AuthSessionApplicationServiceImpl implements AuthSessionApplicationService {

    /**
     * 当前用户查询服务。
     */
    private final UserContextService userContextService;

    /**
     * 登录审计服务。
     */
    private final LoginAuditService loginAuditService;

    /**
     * 当前登录人上下文。
     */
    private final LoginUserContext loginUserContext;

    /**
     * 登录认证总控处理器。
     */
    private final LoginAuthenticationProcessor loginAuthenticationProcessor;
    private final AuthOnlineSessionService authOnlineSessionService;

    public AuthSessionApplicationServiceImpl(
            UserContextService userContextService,
            LoginAuditService loginAuditService,
            LoginUserContext loginUserContext,
            LoginAuthenticationProcessor loginAuthenticationProcessor,
            AuthOnlineSessionService authOnlineSessionService
    ) {
        this.userContextService = userContextService;
        this.loginAuditService = loginAuditService;
        this.loginUserContext = loginUserContext;
        this.loginAuthenticationProcessor = loginAuthenticationProcessor;
        this.authOnlineSessionService = authOnlineSessionService;
    }

    @Override
    public LoginTokenResponse login(UnifiedLoginRequest request, AuthRequestContext requestContext) {
        // 登录实际由认证处理器完成，这里只保留门面委托职责。
        return loginAuthenticationProcessor.authenticate(request, requestContext);
    }

    @Override
    public CurrentUserResponse currentUser() {
        // 使用当前登录用户名构建完整的当前用户视图。
        return userContextService.buildCurrentUser(loginUserContext.getLoginUsername());
    }

    @Override
    public void logout(AuthRequestContext requestContext) {
        // 1. 在清理 Sa-Token 登录态前先记录登出审计。
        loginAuditService.record(new LoginAuditRecord(
                loginUserContext.getLoginUsername(),
                resolveCurrentLoginType().name(),
                LoginAuditEventType.LOGOUT.name(),
                true,
                LoginAuditEventType.LOGOUT.name(),
                requestContext.remoteAddr(),
                requestContext.userAgent(),
                StpUtil.getTokenValue(),
                requestContext.traceId(),
                requestContext.requestId(),
                LocalDateTime.now()
        ));
        // 2. 同步当前在线会话台账。
        authOnlineSessionService.markOfflineBySessionKey(StpUtil.getTokenValue(), "LOGOUT");
        // 3. 清理当前 token 对应的登录态。
        StpUtil.logout();
    }

    /**
     * 从 token-session 解析当前登录方式。
     */
    private AuthGrantType resolveCurrentLoginType() {
        return AuthGrantType.fromSessionValue(StpUtil.getTokenSession().get(AuthSessionKeys.LOGIN_TYPE));
    }
}
