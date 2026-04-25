package net.junanw.upms.foundation.platform.auth.authentication.converter;

import net.junanw.upms.foundation.platform.auth.authentication.LoginAuthentication;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthRequestContext;
import net.junanw.upms.foundation.platform.auth.authentication.model.request.UnifiedLoginRequest;

/**
 * 认证请求转换器。
 */
public interface AuthenticationConverter {

    /**
     * 将 HTTP 请求模型转换为统一内部认证命令。
     */
    LoginAuthentication convert(UnifiedLoginRequest request, AuthRequestContext requestContext);
}
