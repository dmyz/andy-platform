package net.junanw.upms.system.iam.navigation.model.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 导航权限分配请求。
 *
 * <p>封装导航权限分配接口的请求参数。
 *
 * @param permissionCodes 权限编码列表
 */

public record NavigationPermissionAssignRequest(
        @NotNull(message = "权限列表不能为空")
        List<String> permissionCodes
) {
}
