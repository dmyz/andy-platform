package net.junanw.upms.core.identity.profile.model;

import java.util.List;

/**
 * 当前用户可见导航节点。
 *
 * @param id 导航主键
 * @param name 导航名称
 * @param type 导航类型
 * @param routePath 路由路径
 * @param componentPath 前端组件路径
 * @param icon 图标
 * @param sortOrder 排序值
 * @param externalUrl 外链地址
 * @param children 子导航节点
 */
public record NavigationItem(
        String id,
        String name,
        String type,
        String routePath,
        String componentPath,
        String icon,
        Integer sortOrder,
        String externalUrl,
        List<NavigationItem> children
) {
}
