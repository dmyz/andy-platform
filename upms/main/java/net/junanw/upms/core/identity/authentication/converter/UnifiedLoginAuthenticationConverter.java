package net.junanw.upms.core.identity.authentication.converter;

import net.junanw.upms.core.identity.authentication.LoginAuthentication;
import net.junanw.upms.core.identity.authentication.model.AuthGrantType;
import net.junanw.upms.core.identity.authentication.model.AuthRequestContext;
import net.junanw.upms.core.identity.authentication.model.request.UnifiedLoginRequest;
import org.springframework.stereotype.Component;

/**
 * 统一登录请求转换器。
 */
@Component
public class UnifiedLoginAuthenticationConverter implements AuthenticationConverter {

    /**
     * 将前端统一登录请求转换成内部认证命令。
     */
    @Override
    public LoginAuthentication convert(UnifiedLoginRequest request, AuthRequestContext requestContext) {
        // 1. 解析登录方式，默认回退为密码登录。
        AuthGrantType grantType = AuthGrantType.from(request.grantType());

        // 2. 把不同登录方式的原始字段统一装入一个认证命令对象。
        return new LoginAuthentication(
                grantType,
                request.username(),
                request.password(),
                request.mobile(),
                request.email(),
                request.code(),
                requestContext
        );
    }
}
