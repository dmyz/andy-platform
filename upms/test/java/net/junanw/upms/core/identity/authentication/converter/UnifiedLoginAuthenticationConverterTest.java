package net.junanw.upms.core.identity.authentication.converter;

import net.junanw.upms.core.identity.authentication.model.AuthRequestContext;
import net.junanw.upms.core.identity.authentication.LoginAuthentication;
import net.junanw.upms.core.identity.authentication.model.AuthGrantType;
import net.junanw.upms.core.identity.authentication.model.request.UnifiedLoginRequest;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnifiedLoginAuthenticationConverterTest {

    private final UnifiedLoginAuthenticationConverter converter = new UnifiedLoginAuthenticationConverter();
    private final AuthRequestContext requestContext = new AuthRequestContext("127.0.0.1", "JUnit", "trace-1", "req-1");

    @Test
    void shouldDefaultGrantTypeToPassword() {
        LoginAuthentication authentication = converter.convert(
                new UnifiedLoginRequest(null, "admin", "secret", null, null, null),
                requestContext
        );

        assertEquals(AuthGrantType.PASSWORD, authentication.grantType());
        assertEquals("admin", authentication.accountIdentifier());
        assertEquals(requestContext, authentication.details());
    }

    @Test
    void shouldConvertMobileCodeRequest() {
        LoginAuthentication authentication = converter.convert(
                new UnifiedLoginRequest("MOBILE_CODE", null, null, "13800138000", null, "123456"),
                requestContext
        );

        assertEquals(AuthGrantType.MOBILE_CODE, authentication.grantType());
        assertEquals("13800138000", authentication.accountIdentifier());
        assertEquals("123456", authentication.code());
    }

    @Test
    void shouldRejectUnsupportedGrantType() {
        assertThrows(BusinessException.class, () -> converter.convert(
                new UnifiedLoginRequest("LDAP", "admin", "secret", null, null, null),
                requestContext
        ));
    }
}
