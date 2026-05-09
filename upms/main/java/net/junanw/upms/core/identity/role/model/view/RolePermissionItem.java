package net.junanw.upms.core.identity.role.model.view;

/**
 * 角色权限项视图。
 *
 * @param code 权限编码
 * @param name 权限名称
 * @param type 权限类型
 * @param category 权限分类
 * @param moduleName 模块名称
 */
public record RolePermissionItem(
        String code,
        String name,
        String type,
        String category,
        String moduleName
) {
}
