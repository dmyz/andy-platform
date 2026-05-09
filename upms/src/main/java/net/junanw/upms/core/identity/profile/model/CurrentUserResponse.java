package net.junanw.upms.core.identity.profile.model;

import java.util.List;

/**
 * 当前登录用户响应。
 *
 * @param user 当前用户基础信息
 * @param permissions 当前用户拥有的权限编码列表
 * @param navigations 当前用户可见导航树
 */
public record CurrentUserResponse(
        UserInfoView user,
        List<String> permissions,
        List<NavigationItem> navigations
) {
}
