package net.junanw.upms.system.iam.role.model.view;

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
        LocalDateTime createTime
) {
}
