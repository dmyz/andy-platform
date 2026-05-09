package net.junanw.upms.core.organization.model.request;

import jakarta.validation.constraints.NotNull;

/**
 * 组织状态更新请求。
 *
 * <p>封装组织状态切换接口的请求参数。
 *
 * @param status 状态值
 */

public record OrganizationStatusUpdateRequest(
        @NotNull(message = "状态不能为空")
        Integer status
) {
}
