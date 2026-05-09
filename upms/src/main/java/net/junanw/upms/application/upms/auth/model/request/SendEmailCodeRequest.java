package net.junanw.upms.application.upms.auth.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送邮箱验证码请求。
 *
 * <p>封装发送邮箱验证码接口的请求参数。
 *
 * @param email 目标邮箱地址
 * @param scene 业务场景编码
 */

public record SendEmailCodeRequest(
        @NotBlank(message = "email 不能为空")
        @Email(message = "email 格式不正确")
        String email,
        String scene
) {
}
