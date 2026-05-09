package net.junanw.upms.core.identity.authentication.handler;

import net.junanw.upms.core.identity.authentication.LoginAuthentication;
import net.junanw.upms.core.identity.authentication.model.LoginPrincipal;
import net.junanw.upms.core.identity.authentication.model.response.LoginTokenResponse;

/**
 * 登录成功处理器。
 */
public interface LoginAuthenticationSuccessHandler {

    /**
     * 处理认证成功后的登录态落地与响应构造。
     */
    LoginTokenResponse onSuccess(LoginAuthentication authentication, LoginPrincipal principal);
}
