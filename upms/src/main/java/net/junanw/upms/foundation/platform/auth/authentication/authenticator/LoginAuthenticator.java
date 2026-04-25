package net.junanw.upms.foundation.platform.auth.authentication.authenticator;

import net.junanw.upms.foundation.platform.auth.authentication.LoginAuthentication;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthGrantType;
import net.junanw.upms.foundation.platform.auth.authentication.model.LoginPrincipal;

/**
 * 登录认证器。
 */
public interface LoginAuthenticator {

    /**
     * 当前认证器支持的登录方式。
     */
    AuthGrantType supports();

    /**
     * 执行凭证校验并返回登录主体。
     */
    LoginPrincipal authenticate(LoginAuthentication authentication);
}
