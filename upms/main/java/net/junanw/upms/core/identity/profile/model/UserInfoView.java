package net.junanw.upms.core.identity.profile.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前用户基础信息视图。
 *
 * @param id 用户主键
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
 * @param status 数值状态
 * @param roles 角色编码列表
 * @param passwordResetRequired 是否需要重置密码
 * @param lastLoginTime 最近登录时间
 */
public record UserInfoView(
        String id,
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
        Integer status,
        List<String> roles,
        boolean passwordResetRequired,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastLoginTime
) {
}
