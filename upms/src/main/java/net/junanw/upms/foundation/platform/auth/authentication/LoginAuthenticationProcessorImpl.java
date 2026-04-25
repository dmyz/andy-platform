package net.junanw.upms.foundation.platform.auth.authentication;

import net.junanw.upms.foundation.platform.auth.authentication.authenticator.LoginAuthenticator;
import net.junanw.upms.foundation.platform.auth.authentication.authenticator.LoginAuthenticatorRegistry;
import net.junanw.upms.foundation.platform.auth.authentication.converter.AuthenticationConverter;
import net.junanw.upms.foundation.platform.auth.authentication.handler.LoginAuthenticationFailureHandler;
import net.junanw.upms.foundation.platform.auth.authentication.handler.LoginAuthenticationSuccessHandler;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthRequestContext;
import net.junanw.upms.foundation.platform.auth.authentication.model.LoginPrincipal;
import net.junanw.upms.foundation.platform.auth.authentication.model.request.UnifiedLoginRequest;
import net.junanw.upms.foundation.platform.auth.authentication.model.response.LoginTokenResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import org.springframework.stereotype.Service;

/**
 * 登录认证总控处理器实现。
 * <p>
 * 统一串联请求转换、认证器路由、成功处理和失败处理。
 */
@Service
public class LoginAuthenticationProcessorImpl implements LoginAuthenticationProcessor {

    /**
     * 请求到认证命令的转换器。
     */
    private final AuthenticationConverter authenticationConverter;

    /**
     * 认证器注册表。
     */
    private final LoginAuthenticatorRegistry loginAuthenticatorRegistry;

    /**
     * 认证成功处理器。
     */
    private final LoginAuthenticationSuccessHandler successHandler;

    /**
     * 认证失败处理器。
     */
    private final LoginAuthenticationFailureHandler failureHandler;

    public LoginAuthenticationProcessorImpl(
            AuthenticationConverter authenticationConverter,
            LoginAuthenticatorRegistry loginAuthenticatorRegistry,
            LoginAuthenticationSuccessHandler successHandler,
            LoginAuthenticationFailureHandler failureHandler
    ) {
        this.authenticationConverter = authenticationConverter;
        this.loginAuthenticatorRegistry = loginAuthenticatorRegistry;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Override
    public LoginTokenResponse authenticate(UnifiedLoginRequest request, AuthRequestContext requestContext) {
        // 1. 将 HTTP 请求转换为统一内部认证命令。
        LoginAuthentication authentication = authenticationConverter.convert(request, requestContext);
        try {
            // 2. 根据 grantType 选择唯一认证器。
            LoginAuthenticator authenticator = loginAuthenticatorRegistry.get(authentication.grantType());
            // 3. 执行凭证校验，产出登录主体。
            LoginPrincipal principal = authenticator.authenticate(authentication);
            // 4. 成功后由成功处理器落登录态、写审计并返回 token。
            return successHandler.onSuccess(authentication, principal);
        }
        catch (BusinessException exception) {
            // 5. 失败时统一交给失败处理器记录审计，再把异常继续抛出。
            failureHandler.onFailure(authentication, exception);
            throw exception;
        } catch (Exception exception) {
            BusinessException businessException = new BusinessException(exception.getMessage());
            businessException.setStackTrace(exception.getStackTrace());
            // 5. 失败时统一交给失败处理器记录审计，再把异常继续抛出。
            failureHandler.onFailure(authentication, businessException);
            throw businessException;
        }
    }
}
