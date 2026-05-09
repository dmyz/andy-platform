package net.junanw.upms.application.upms.profile.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 头像更新请求。
 *
 * <p>封装个人中心头像更新接口的请求参数。
 *
 * @param avatarUrl 头像图片地址
 */

public record ProfileAvatarUpdateRequest(
        @NotBlank(message = "头像地址不能为空")
        @Size(max = 500, message = "头像地址长度不能超过500")
        String avatarUrl
) {
}
