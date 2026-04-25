package net.junanw.upms.system.iam.role.model.view;

/**
 * 角色关联用户项视图。
 *
 * @param id 用户主键
 * @param username 用户名
 * @param realName 真实姓名
 * @param orgName 所属组织名称
 * @param status 数值状态
 */
public record RoleRelatedUserItem(
        String id,
        String username,
        String realName,
        String orgName,
        Integer status
) {
}
