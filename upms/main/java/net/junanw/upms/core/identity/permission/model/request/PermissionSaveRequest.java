package net.junanw.upms.core.identity.permission.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 权限保存请求。
 *
 * @param name 权限名称
 * @param code 权限编码
 * @param type 权限类型
 * @param resourceType 资源类型
 * @param actionCode 动作编码
 * @param moduleCode 模块编码
 * @param status 状态，1 表示启用，0 表示停用
 * @param remark 备注
 */
public record PermissionSaveRequest(
        @NotBlank(message = "权限名称不能为空")
        @Size(max = 128, message = "权限名称长度不能超过128")
        String name,
        @NotBlank(message = "权限编码不能为空")
        @Size(max = 128, message = "权限编码长度不能超过128")
        String code,
        @NotBlank(message = "权限类型不能为空")
        @Size(max = 32, message = "权限类型长度不能超过32")
        String type,
        @NotBlank(message = "资源类型不能为空")
        @Size(max = 32, message = "资源类型长度不能超过32")
        String resourceType,
        @Size(max = 32, message = "动作编码长度不能超过32")
        String actionCode,
        @NotBlank(message = "模块编码不能为空")
        @Size(max = 64, message = "模块编码长度不能超过64")
        String moduleCode,
        @NotNull(message = "状态不能为空")
        Integer status,
        @Size(max = 200, message = "备注长度不能超过200")
        String remark
) {
}
