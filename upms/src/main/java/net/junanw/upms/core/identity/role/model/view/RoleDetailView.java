package net.junanw.upms.core.identity.role.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 角色详情视图。
 *
 * @param id 角色主键
 * @param name 角色名称
 * @param code 角色编码
 * @param dataScope 数据权限范围
 * @param status 数值状态
 * @param remark 备注
 * @param createTime 创建时间
 */
public record RoleDetailView(
        String id,
        String name,
        String code,
        String dataScope,
        Integer status,
        String remark,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createTime
) {
}
