package net.junanw.upms.system.iam.user.model.view;

import java.time.LocalDateTime;

/**
 * 用户详情视图。
 *
 * @param id 用户主键
 * @param username 用户名
 * @param realName 真实姓名
 * @param jobNumber 工号
 * @param mobile 手机号
 * @param email 邮箱
 * @param gender 性别
 * @param orgId 所属组织主键
 * @param orgName 所属组织名称
 * @param position 岗位名称
 * @param status 数值状态
 * @param remark 备注
 * @param passwordResetRequired 是否要求重置密码
 * @param lastLoginTime 最近登录时间
 * @param createTime 创建时间
 */
public record UserDetailView(
        String id,
        String username,
        String realName,
        String jobNumber,
        String mobile,
        String email,
        String gender,
        String orgId,
        String orgName,
        String position,
        Integer status,
        String remark,
        Boolean passwordResetRequired,
        LocalDateTime lastLoginTime,
        LocalDateTime createTime
) {
}
