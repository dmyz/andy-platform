package net.junanw.upms.core.identity.user.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 用户分页项视图。
 *
 * @param id 用户主键
 * @param username 用户名
 * @param realName 真实姓名
 * @param jobNumber 工号
 * @param mobile 手机号
 * @param email 邮箱
 * @param orgId 所属组织主键
 * @param orgName 所属组织名称
 * @param position 岗位名称
 * @param status 数值状态
 * @param lastLoginTime 最近登录时间
 * @param createTime 创建时间
 */
public record UserPageItem(
        String id,
        String username,
        String realName,
        String jobNumber,
        String mobile,
        String email,
        String orgId,
        String orgName,
        String position,
        Integer status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastLoginTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createTime
) {
}
