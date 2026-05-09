package net.junanw.upms.core.organization.model.view;

/**
 * 组织成员项视图。
 *
 * <p>用于承载组织成员列表的返回字段。
 *
 * @param id 用户 ID
 * @param username 用户名
 * @param displayName 显示名称
 * @param mobile 手机号
 * @param email 邮箱地址
 * @param positionName 岗位名称
 * @param status 状态值
 */

public record OrganizationMemberItem(
        String id,
        String username,
        String displayName,
        String mobile,
        String email,
        String positionName,
        Integer status
) {
}
