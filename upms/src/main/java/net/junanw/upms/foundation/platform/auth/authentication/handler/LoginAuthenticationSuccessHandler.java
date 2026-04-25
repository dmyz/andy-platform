package net.junanw.upms.foundation.platform.auth.authentication.handler;

import net.junanw.upms.foundation.platform.auth.authentication.LoginAuthentication;
import net.junanw.upms.foundation.platform.auth.authentication.model.LoginPrincipal;
import net.junanw.upms.foundation.platform.auth.authentication.model.response.LoginTokenResponse;

/**
 * 登录成功处理器。
 */
public interface LoginAuthenticationSuccessHandler {

    /**
     * 处理认证成功后的登录态落地与响应构造。
     */
    LoginTokenResponse onSuccess(LoginAuthentication authentication, LoginPrincipal principal);
}
