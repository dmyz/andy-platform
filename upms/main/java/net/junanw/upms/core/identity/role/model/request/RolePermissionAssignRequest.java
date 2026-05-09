package net.junanw.upms.core.identity.role.model.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 角色权限分配请求。
 *
 * @param permissionCodes 权限编码列表
 */
public record RolePermissionAssignRequest(
        @NotNull(message = "权限列表不能为空")
        List<String> permissionCodes
) {
}
