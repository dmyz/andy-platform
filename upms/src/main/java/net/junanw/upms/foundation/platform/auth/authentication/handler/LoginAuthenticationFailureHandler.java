package net.junanw.upms.foundation.platform.auth.authentication.handler;

import net.junanw.upms.foundation.platform.auth.authentication.LoginAuthentication;
import net.junanw.upms.foundation.shared.exception.BusinessException;

/**
 * 登录失败处理器。
 */
public interface LoginAuthenticationFailureHandler {

    /**
     * 处理认证失败后的副作用，例如审计记录。
     */
    void onFailure(LoginAuthentication authentication, BusinessException exception);
}
