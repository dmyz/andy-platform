package net.junanw.upms.core.identity.authentication;

import net.junanw.upms.core.identity.authentication.model.AuthRequestContext;
import net.junanw.upms.core.identity.authentication.authenticator.LoginAuthenticator;
import net.junanw.upms.core.identity.authentication.authenticator.LoginAuthenticatorRegistry;
import net.junanw.upms.core.identity.authentication.converter.AuthenticationConverter;
import net.junanw.upms.core.identity.authentication.handler.LoginAuthenticationFailureHandler;
import net.junanw.upms.core.identity.authentication.handler.LoginAuthenticationSuccessHandler;
import net.junanw.upms.core.identity.authentication.model.AuthGrantType;
import net.junanw.upms.core.identity.authentication.model.LoginPrincipal;
import net.junanw.upms.core.identity.authentication.model.request.UnifiedLoginRequest;
import net.junanw.upms.core.identity.authentication.model.response.LoginTokenResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAuthenticationProcessorTest {

    @Test
    void shouldInvokeSuccessHandlerOnSuccessfulAuthentication() {
        LoginAuthentication authentication = new LoginAuthentication(
                AuthGrantType.PASSWORD,
                "admin",
                "secret",
                null,
                null,
                null,
                new AuthRequestContext("127.0.0.1", "JUnit", "trace", "request")
        );
        AuthenticationConverter converter = (request, requestContext) -> authentication;
        LoginAuthenticator authenticator = new LoginAuthenticator() {
            @Override
            public AuthGrantType supports() {
                return AuthGrantType.PASSWORD;
            }

            @Override
            public LoginPrincipal authenticate(LoginAuthentication input) {
                return new LoginPrincipal(1L, "admin", AuthGrantType.PASSWORD);
            }
        };
        LoginAuthenticatorRegistry registry = new LoginAuthenticatorRegistry(List.of(authenticator));
        AtomicBoolean successCalled = new AtomicBoolean(false);
        LoginAuthenticationSuccessHandler successHandler = (input, principal) -> {
            successCalled.set(true);
            return new LoginTokenResponse("token", "Bearer", 3600);
        };
        LoginAuthenticationFailureHandler failureHandler = (input, exception) -> {
            throw new AssertionError("failureHandler should not be called");
        };
        LoginAuthenticationProcessorImpl processor = new LoginAuthenticationProcessorImpl(converter, registry, successHandler, failureHandler);

        LoginTokenResponse response = processor.authenticate(new UnifiedLoginRequest("PASSWORD", "admin", "secret", null, null, null), authentication.details());

        assertTrue(successCalled.get());
        assertEquals("token", response.accessToken());
    }

    @Test
    void shouldInvokeFailureHandlerAndRethrow() {
        LoginAuthentication authentication = new LoginAuthentication(
                AuthGrantType.PASSWORD,
                "admin",
                "bad",
                null,
                null,
                null,
                new AuthRequestContext("127.0.0.1", "JUnit", "trace", "request")
        );
        AuthenticationConverter converter = (request, requestContext) -> authentication;
        LoginAuthenticator authenticator = new LoginAuthenticator() {
            @Override
            public AuthGrantType supports() {
                return AuthGrantType.PASSWORD;
            }

            @Override
            public LoginPrincipal authenticate(LoginAuthentication input) {
                throw new BusinessException(401, "用户名或密码错误");
            }
        };
        LoginAuthenticatorRegistry registry = new LoginAuthenticatorRegistry(List.of(authenticator));
        AtomicBoolean failureCalled = new AtomicBoolean(false);
        LoginAuthenticationSuccessHandler successHandler = (input, principal) -> new LoginTokenResponse("token", "Bearer", 3600);
        LoginAuthenticationFailureHandler failureHandler = (input, exception) -> failureCalled.set(true);
        LoginAuthenticationProcessorImpl processor = new LoginAuthenticationProcessorImpl(converter, registry, successHandler, failureHandler);

        assertThrows(BusinessException.class, () -> processor.authenticate(
                new UnifiedLoginRequest("PASSWORD", "admin", "bad", null, null, null),
                authentication.details()
        ));

        assertTrue(failureCalled.get());
    }
}
