package net.junanw.upms.foundation.platform.auth.application.password;

import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;

/**
 * 重置密码命令。
 *
 * @param targetType 验证目标类型
 * @param target 原始目标值
 * @param code 验证码
 * @param newPassword 新密码明文
 */
public record ResetPasswordCommand(
        VerificationTargetType targetType,
        String target,
        String code,
        String newPassword
) {
}
