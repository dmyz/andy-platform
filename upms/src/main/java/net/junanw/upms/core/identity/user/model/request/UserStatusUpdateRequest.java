package net.junanw.upms.core.identity.user.model.request;

import jakarta.validation.constraints.NotNull;

/**
 * 用户状态更新请求。
 *
 * @param status 状态，1 表示启用，0 表示停用
 */
public record UserStatusUpdateRequest(
        @NotNull(message = "状态不能为空")
        Integer status
) {
}
