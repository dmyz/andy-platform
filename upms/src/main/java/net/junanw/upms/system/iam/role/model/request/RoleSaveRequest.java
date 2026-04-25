package net.junanw.upms.system.iam.role.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 角色保存请求。
 *
 * @param name 角色名称
 * @param code 角色编码
 * @param dataScope 数据权限范围
 * @param status 状态，1 表示启用，0 表示停用
 * @param remark 备注
 */
public record RoleSaveRequest(
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 64, message = "角色名称长度不能超过64")
        String name,
        @NotBlank(message = "角色编码不能为空")
        @Size(max = 64, message = "角色编码长度不能超过64")
        String code,
        @NotBlank(message = "数据权限不能为空")
        @Size(max = 32, message = "数据权限长度不能超过32")
        String dataScope,
        @NotNull(message = "状态不能为空")
        Integer status,
        @Size(max = 200, message = "备注长度不能超过200")
        String remark
) {
}
