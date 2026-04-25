package net.junanw.upms.portal.profile.model.view;

/**
 * 个人资料视图。
 *
 * <p>用于承载个人中心“我的资料”接口的返回字段。
 *
 * @param id 用户 ID
 * @param username 用户名
 * @param realName 真实姓名
 * @param employeeNo 工号
 * @param mobile 手机号
 * @param email 邮箱地址
 * @param gender 性别
 * @param avatar 头像地址
 * @param orgName 所属组织名称
 * @param positionName 岗位名称
 * @param remark 备注
 */

public record ProfileMeView(
        String id,
        String username,
        String realName,
        String employeeNo,
        String mobile,
        String email,
        String gender,
        String avatar,
        String orgName,
        String positionName,
        String remark
) {
}
