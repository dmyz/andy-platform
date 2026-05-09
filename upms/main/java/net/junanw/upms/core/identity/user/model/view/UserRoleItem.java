package net.junanw.upms.core.identity.user.model.view;

/**
 * 用户角色项视图。
 *
 * @param code 角色编码
 * @param name 角色名称
 */
public record UserRoleItem(
        String code,
        String name
) {
}
