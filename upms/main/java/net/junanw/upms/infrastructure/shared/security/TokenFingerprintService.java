package net.junanw.upms.infrastructure.shared.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Token 指纹服务。
 *
 * <p>用于审计场景保存不可逆短指纹，避免落库明文 token。
 */
@Component
public class TokenFingerprintService {

    private static final int FINGERPRINT_LENGTH = 32;

    /**
     * 生成 token 指纹。
     *
     * @param token 原始 token
     * @return SHA-256 短指纹
     */
    public String fingerprint(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            String hash = HexFormat.of().formatHex(messageDigest.digest(token.getBytes(StandardCharsets.UTF_8)));
            return hash.substring(0, Math.min(FINGERPRINT_LENGTH, hash.length()));
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
