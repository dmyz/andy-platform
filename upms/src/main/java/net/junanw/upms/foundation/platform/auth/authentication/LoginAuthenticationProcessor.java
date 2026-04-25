package net.junanw.upms.foundation.platform.auth.authentication;

import net.junanw.upms.foundation.platform.auth.authentication.model.AuthRequestContext;
import net.junanw.upms.foundation.platform.auth.authentication.model.request.UnifiedLoginRequest;
import net.junanw.upms.foundation.platform.auth.authentication.model.response.LoginTokenResponse;

/**
 * 登录认证总控处理器。
 */
public interface LoginAuthenticationProcessor {

    /**
     * 执行完整登录认证流程。
     */
    LoginTokenResponse authenticate(UnifiedLoginRequest request, AuthRequestContext requestContext);
}
