package net.junanw.upms.core.identity.permission.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 权限分页项视图。
 *
 * @param id 权限主键
 * @param name 权限名称
 * @param code 权限编码
 * @param type 权限类型
 * @param resourceType 资源类型
 * @param actionCode 动作编码
 * @param moduleCode 模块编码
 * @param moduleName 模块名称
 * @param status 数值状态
 * @param remark 备注
 * @param updateTime 更新时间
 */
public record PermissionPageItem(
        String id,
        String name,
        String code,
        String type,
        String resourceType,
        String actionCode,
        String moduleCode,
        String moduleName,
        Integer status,
        String remark,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updateTime
) {
}
