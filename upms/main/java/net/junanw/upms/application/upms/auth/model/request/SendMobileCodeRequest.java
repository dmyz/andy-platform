package net.junanw.upms.application.upms.auth.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送手机验证码请求。
 *
 * <p>封装发送手机验证码接口的请求参数。
 *
 * @param mobile 目标手机号
 * @param scene 业务场景编码
 */

public record SendMobileCodeRequest(
        @NotBlank(message = "mobile 不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "mobile 格式不正确")
        String mobile,
        String scene
) {
}
