package net.junanw.upms.application.upms.profile.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 手机号变更请求。
 *
 * <p>封装个人中心手机号变更接口的请求参数。
 *
 * @param code 手机验证码
 * @param newMobile 新手机号
 */

public record ProfileMobileChangeRequest(
        @NotBlank(message = "验证码不能为空")
        @Size(min = 6, max = 6, message = "验证码长度必须为6位")
        String code,
        @NotBlank(message = "新手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String newMobile
) {
}
