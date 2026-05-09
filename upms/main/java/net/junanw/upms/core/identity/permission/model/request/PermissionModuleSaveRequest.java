package net.junanw.upms.core.identity.permission.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 权限模块保存请求。
 *
 * @param name 模块名称
 * @param code 模块编码
 * @param parentCode 父模块编码
 * @param sortOrder 排序号
 * @param status 状态，1 表示启用，0 表示停用
 * @param remark 备注
 */
public record PermissionModuleSaveRequest(
        @NotBlank(message = "模块名称不能为空")
        @Size(max = 128, message = "模块名称长度不能超过128")
        String name,
        @NotBlank(message = "模块编码不能为空")
        @Size(max = 64, message = "模块编码长度不能超过64")
        String code,
        @Size(max = 64, message = "父模块编码长度不能超过64")
        String parentCode,
        Integer sortOrder,
        @NotNull(message = "状态不能为空")
        Integer status,
        @Size(max = 500, message = "备注长度不能超过500")
        String remark
) {
}
