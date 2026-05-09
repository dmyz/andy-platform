package net.junanw.upms.application.upms.profile.model.view;

/**
 * 验证码发送结果视图。
 *
 * <p>用于承载个人中心验证码发送接口的返回字段。
 *
 * @param scene 业务场景编码
 * @param targetType 目标类型
 * @param maskedTarget 脱敏后的目标标识
 * @param expireSeconds 有效期（秒）
 */

public record ProfileVerificationCodeSendView(
        String scene,
        String targetType,
        String maskedTarget,
        Integer expireSeconds
) {
}
