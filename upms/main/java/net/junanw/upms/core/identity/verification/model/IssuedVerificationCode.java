package net.junanw.upms.core.identity.verification.model;

/**
 * 验证码签发结果。
 *
 * <p>用于向上游应用服务返回“本次验证码已成功签发”的摘要信息，
 * 不暴露真实验证码本身。
 *
 * @param targetType 目标类型，如手机或邮箱
 * @param scene 验证码场景
 * @param maskedTarget 脱敏后的接收目标
 * @param expireSeconds 过期秒数
 */
public record IssuedVerificationCode(
        String targetType,
        String scene,
        String maskedTarget,
        int expireSeconds
) {
}
