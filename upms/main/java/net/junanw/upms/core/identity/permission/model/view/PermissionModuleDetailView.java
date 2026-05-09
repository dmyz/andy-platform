package net.junanw.upms.core.identity.permission.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 权限模块详情视图。
 *
 * @param id 模块主键
 * @param name 模块名称
 * @param code 模块编码
 * @param parentCode 父模块编码
 * @param sortOrder 排序号
 * @param status 数值状态
 * @param remark 备注
 * @param updateTime 更新时间
 */
public record PermissionModuleDetailView(
        String id,
        String name,
        String code,
        String parentCode,
        Integer sortOrder,
        Integer status,
        String remark,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updateTime
) {
}
