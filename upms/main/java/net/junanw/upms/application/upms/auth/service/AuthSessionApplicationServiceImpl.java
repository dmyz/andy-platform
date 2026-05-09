package net.junanw.upms.application.upms.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import net.junanw.upms.core.identity.authentication.LoginAuthenticationProcessor;
import net.junanw.upms.core.identity.authentication.context.LoginUserContext;
import net.junanw.upms.core.identity.authentication.model.AuthGrantType;
import net.junanw.upms.core.identity.authentication.model.AuthRequestContext;
import net.junanw.upms.core.identity.authentication.model.request.UnifiedLoginRequest;
import net.junanw.upms.core.identity.authentication.model.response.LoginTokenResponse;
import net.junanw.upms.core.identity.authentication.session.AuthSessionKeys;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import net.junanw.upms.core.identity.profile.model.CurrentUserResponse;
import net.junanw.upms.core.identity.profile.model.UserInfoView;
import net.junanw.upms.core.identity.session.AuthOnlineSessionService;
import net.junanw.upms.core.identity.session.model.view.AuthSessionPageItem;
import net.junanw.upms.core.security.audit.login.model.LoginAuditEventType;
import net.junanw.upms.core.security.audit.login.service.LoginAuditRecord;
import net.junanw.upms.core.security.audit.login.service.LoginAuditService;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
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
        return withAvatarPreview(userContextService.buildCurrentUser(loginUserContext.getLoginUsername()));
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
        // 2. 清理当前 token 对应的登录态，在线会话以 Sa-Token 状态为准。
        StpUtil.logout();
    }

    @Override
    public PageResponse<AuthSessionPageItem> page(String username, String realName, String loginType, String ip, String status, int pageNum, int pageSize) {
        // 管理端在线会话列表由领域会话服务提供，应用层只暴露聚合入口。
        return authOnlineSessionService.page(username, realName, loginType, ip, status, pageNum, pageSize);
    }

    @Override
    public void offline(String id) {
        // 强制下线逻辑保留在领域会话服务中，应用层负责管理端权限语义和入口收敛。
        authOnlineSessionService.offline(id);
    }

    /**
     * 从 token-session 解析当前登录方式。
     */
    private AuthGrantType resolveCurrentLoginType() {
        return AuthGrantType.fromSessionValue(StpUtil.getTokenSession().get(AuthSessionKeys.LOGIN_TYPE));
    }

    /**
     * 为管理端当前用户视图补齐头像预览地址。
     */
    private CurrentUserResponse withAvatarPreview(CurrentUserResponse response) {
        UserInfoView user = response.user();
        return new CurrentUserResponse(
                new UserInfoView(
                        user.id(),
                        user.username(),
                        user.displayName(),
                        user.mobile(),
                        user.email(),
                        user.employeeNo(),
                        user.gender(),
                        buildAvatarPreviewUrl(user.avatarFileId()),
                        user.avatarFileId(),
                        user.orgId(),
                        user.orgName(),
                        user.positionName(),
                        user.remark(),
                        user.status(),
                        user.roles(),
                        user.passwordResetRequired(),
                        user.lastLoginTime()
                ),
                response.permissions(),
                response.navigations()
        );
    }

    /**
     * 构建管理端文件预览地址。
     */
    private String buildAvatarPreviewUrl(Long avatarFileId) {
        return avatarFileId == null ? null : "/admin/file/" + avatarFileId + "/preview";
    }
}
