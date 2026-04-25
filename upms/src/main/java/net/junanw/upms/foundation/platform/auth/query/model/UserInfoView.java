package net.junanw.upms.foundation.platform.auth.query.model;

import java.util.List;

/**
 * 当前用户基础信息视图。
 *
 * @param id 用户主键
 * @param username 用户名
 * @param displayName 展示名称
 * @param avatar 头像访问地址
 * @param roles 角色编码列表
 * @param passwordResetRequired 是否需要重置密码
 */
public record UserInfoView(
        String id,
        String username,
        String displayName,
        String avatar,
        List<String> roles,
        boolean passwordResetRequired
) {
}
