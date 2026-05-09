package net.junanw.upms.core.identity.navigation.model.view;

/**
 * 导航详情视图。
 *
 * <p>用于承载导航详情接口的返回字段。
 *
 * @param id 导航 ID
 * @param parentId 父级导航 ID
 * @param parentName 父级导航名称
 * @param name 导航名称
 * @param type 导航类型
 * @param routePath 路由路径
 * @param componentPath 前端组件路径
 * @param externalUrl 外链地址
 * @param icon 图标标识
 * @param sortOrder 排序号
 * @param visible 是否显示
 * @param status 状态值
 */

public record NavigationDetailView(
        String id,
        String parentId,
        String parentName,
        String name,
        String type,
        String routePath,
        String componentPath,
        String externalUrl,
        String icon,
        Integer sortOrder,
        Boolean visible,
        Integer status
) {
}
