package net.junanw.upms.infrastructure.shared.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TokenFingerprintServiceTest {

    private final TokenFingerprintService tokenFingerprintService = new TokenFingerprintService();

    @Test
    void fingerprintShouldReturnSha256Prefix() {
        assertEquals("2bb80d537b1da3e38bd30361aa855686", tokenFingerprintService.fingerprint("secret"));
    }

    @Test
    void fingerprintShouldIgnoreBlankToken() {
        assertNull(tokenFingerprintService.fingerprint(" "));
    }
}
