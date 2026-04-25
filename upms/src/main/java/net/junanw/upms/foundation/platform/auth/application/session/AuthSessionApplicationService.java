package net.junanw.upms.foundation.platform.auth.application.session;

import net.junanw.upms.foundation.platform.auth.authentication.model.AuthRequestContext;
import net.junanw.upms.foundation.platform.auth.authentication.model.request.UnifiedLoginRequest;
import net.junanw.upms.foundation.platform.auth.authentication.model.response.LoginTokenResponse;
import net.junanw.upms.foundation.platform.auth.query.model.CurrentUserResponse;

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
}
