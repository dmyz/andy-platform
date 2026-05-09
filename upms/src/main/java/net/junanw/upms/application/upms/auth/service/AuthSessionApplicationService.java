package net.junanw.upms.application.upms.auth.service;

import net.junanw.upms.core.identity.authentication.model.AuthRequestContext;
import net.junanw.upms.core.identity.authentication.model.request.UnifiedLoginRequest;
import net.junanw.upms.core.identity.authentication.model.response.LoginTokenResponse;
import net.junanw.upms.core.identity.profile.model.CurrentUserResponse;
import net.junanw.upms.core.identity.session.model.view.AuthSessionPageItem;
import net.junanw.upms.infrastructure.shared.api.PageResponse;

/**
 * 认证会话应用门面。
 */
public interface AuthSessionApplicationService {

    /**
     * 执行登录并返回令牌信息。
     */
    LoginTokenResponse login(UnifiedLoginRequest request, AuthRequestContext requestContext);

    /**
     * 获取当前登录用户视图。
     */
    CurrentUserResponse currentUser();

    /**
     * 执行登出。
     */
    void logout(AuthRequestContext requestContext);

    /**
     * 分页查询在线会话。
     */
    PageResponse<AuthSessionPageItem> page(String username, String realName, String loginType, String ip, String status, int pageNum, int pageSize);

    /**
     * 强制指定在线会话下线。
     */
    void offline(String id);
}
