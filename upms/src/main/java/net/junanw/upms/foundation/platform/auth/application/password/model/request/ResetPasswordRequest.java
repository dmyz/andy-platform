package net.junanw.upms.foundation.platform.auth.application.password.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求。
 *
 * @param targetType 验证目标类型，例如手机号或邮箱
 * @param target 验证目标值
 * @param code 验证码
 * @param newPassword 重置后的新密码明文
 */
public record ResetPasswordRequest(
        /**
         * 验证目标类型。
         */
        @NotBlank(message = "targetType 不能为空") String targetType,

        /**
         * 需要校验验证码的手机号或邮箱。
         */
        @NotBlank(message = "target 不能为空") String target,

        /**
         * 用户提交的验证码。
         */
        @NotBlank(message = "code 不能为空") String code,

        /**
         * 重置后的新密码。
         */
        @NotBlank(message = "newPassword 不能为空")
        @Size(min = 6, message = "newPassword 长度不能少于 6 位")
        String newPassword
) {
}
