package net.junanw.upms.portal.profile.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 邮箱变更请求。
 *
 * <p>封装个人中心邮箱变更接口的请求参数。
 *
 * @param code 邮箱验证码
 * @param newEmail 新邮箱地址
 */

public record ProfileEmailChangeRequest(
        @NotBlank(message = "验证码不能为空")
        @Size(min = 6, max = 6, message = "验证码长度必须为6位")
        String code,
        @NotBlank(message = "新邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱长度不能超过128")
        String newEmail
) {
}
