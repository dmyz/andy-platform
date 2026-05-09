package net.junanw.upms.core.identity.authentication;

import net.junanw.upms.core.identity.authentication.model.AuthRequestContext;
import net.junanw.upms.core.identity.authentication.model.request.UnifiedLoginRequest;
import net.junanw.upms.core.identity.authentication.model.response.LoginTokenResponse;

/**
 * 登录认证总控处理器。
 */
public interface LoginAuthenticationProcessor {

    /**
     * 执行完整登录认证流程。
     */
    LoginTokenResponse authenticate(UnifiedLoginRequest request, AuthRequestContext requestContext);
}
