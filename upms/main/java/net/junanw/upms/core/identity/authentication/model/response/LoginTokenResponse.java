package net.junanw.upms.core.identity.authentication.model.response;

/**
 * 登录令牌响应。
 *
 * <p>用于承载登录成功后返回的令牌字段，并保持与前端既有契约一致。
 *
 * @param accessToken 访问令牌字符串
 * @param tokenType 令牌类型，当前固定为 {@code Bearer}
 * @param expiresIn 有效期（秒）
 */

public record LoginTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
