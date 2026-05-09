package net.junanw.upms.core.identity.password.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 改密请求。
 *
 * @param oldPassword 当前密码明文
 * @param newPassword 新密码明文
 */
public record ChangePasswordRequest(
        /**
         * 当前登录用户输入的原密码。
         */
        @NotBlank(message = "oldPassword 不能为空") String oldPassword,

        /**
         * 期望修改后的新密码。
         */
        @NotBlank(message = "newPassword 不能为空")
        @Size(min = 6, message = "newPassword 长度不能少于 6 位")
        String newPassword
) {
}
