package net.junanw.upms.foundation.platform.auth.query.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证上下文用户快照。
 *
 * <p>聚合了认证、个人中心和权限导航等流程需要复用的用户信息。
 *
 * @param userId 用户主键
 * @param username 用户名
 * @param displayName 展示名称
 * @param mobile 绑定手机号
 * @param email 绑定邮箱
 * @param employeeNo 工号
 * @param gender 性别
 * @param avatar 头像访问地址
 * @param avatarFileId 头像文件主键
 * @param orgId 主组织主键
 * @param orgName 主组织名称
 * @param positionName 主岗位名称
 * @param remark 备注
 * @param userStatus 数值状态
 * @param passwordResetRequired 是否需要重置密码
 * @param lastLoginTime 最近登录时间
 * @param roleCodes 角色编码列表
 * @param permissionCodes 权限编码列表
 */
public record UserProfileSnapshot(
        Long userId,
        String username,
        String displayName,
        String mobile,
        String email,
        String employeeNo,
        String gender,
        String avatar,
        Long avatarFileId,
        String orgId,
        String orgName,
        String positionName,
        String remark,
        Integer userStatus,
        Boolean passwordResetRequired,
        LocalDateTime lastLoginTime,
        List<String> roleCodes,
        List<String> permissionCodes
) {
}
