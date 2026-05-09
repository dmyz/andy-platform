package net.junanw.upms.core.identity.verification.model.response;

/**
 * 验证码发送响应。
 *
 * <p>用于承载验证码发送接口的返回字段。
 *
 * @param scene 业务场景编码
 * @param targetType 目标类型
 * @param maskedTarget 脱敏后的目标标识
 * @param expireSeconds 有效期（秒）
 * @param devCode 开发环境调试验证码
 */

public record VerificationCodeSendResponse(
        String scene,
        String targetType,
        String maskedTarget,
        Integer expireSeconds,
        String devCode
) {
}
