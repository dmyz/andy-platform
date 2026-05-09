package net.junanw.upms.core.identity.authentication.authenticator;

import net.junanw.upms.core.identity.authentication.model.AuthGrantType;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 登录认证器注册表。
 */
@Component
public class LoginAuthenticatorRegistry {

    /**
     * 按登录方式索引的认证器映射。
     */
    private final Map<AuthGrantType, LoginAuthenticator> authenticators;

    public LoginAuthenticatorRegistry(List<LoginAuthenticator> authenticators) {
        EnumMap<AuthGrantType, LoginAuthenticator> authenticatorMap = new EnumMap<>(AuthGrantType.class);
        authenticators.forEach(authenticator -> authenticatorMap.put(authenticator.supports(), authenticator));
        this.authenticators = Map.copyOf(authenticatorMap);
    }

    /**
     * 获取指定登录方式的唯一认证器。
     */
    public LoginAuthenticator get(AuthGrantType grantType) {
        LoginAuthenticator authenticator = authenticators.get(grantType);
        if (authenticator == null) {
            throw new BusinessException(400, "不支持的登录方式");
        }
        return authenticator;
    }
}
