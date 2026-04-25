package net.junanw.upms.portal.workbench.model.view;

/**
 * 工作台常用导航项视图。
 *
 * <p>用于承载工作台常用入口列表的返回字段。
 *
 * @param label 导航显示名称
 * @param path 导航路径
 * @param icon 图标标识
 * @param color 主题色
 */

public record WorkbenchFavoriteNavigationItem(
        String label,
        String path,
        String icon,
        String color
) {
}
