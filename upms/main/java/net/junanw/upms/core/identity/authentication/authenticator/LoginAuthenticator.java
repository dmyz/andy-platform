package net.junanw.upms.core.identity.authentication.authenticator;

import net.junanw.upms.core.identity.authentication.LoginAuthentication;
import net.junanw.upms.core.identity.authentication.model.AuthGrantType;
import net.junanw.upms.core.identity.authentication.model.LoginPrincipal;

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
