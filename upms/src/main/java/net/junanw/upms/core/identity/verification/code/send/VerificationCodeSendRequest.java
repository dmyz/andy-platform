package net.junanw.upms.core.identity.verification.code.send;

import net.junanw.upms.core.identity.verification.model.VerificationTargetType;

/**
 * 验证码发送请求。
 *
 * @param targetType 目标类型，例如手机号或邮箱
 * @param target 原始目标值
 * @param scene 业务场景
 * @param expireSeconds 验证码有效期秒数
 */
public record VerificationCodeSendRequest(
        VerificationTargetType targetType,
        String target,
        String scene,
        int expireSeconds
) {
}
