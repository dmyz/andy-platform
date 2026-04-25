package net.junanw.upms.system.iam.user.model.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 用户角色分配请求。
 *
 * @param roleCodes 角色编码列表
 */
public record UserRoleAssignRequest(
        @NotNull(message = "角色不能为空")
        List<String> roleCodes
) {
}
