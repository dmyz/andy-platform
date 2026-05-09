package net.junanw.upms.core.identity.permission.model.view;

/**
 * 权限模块选项项。
 *
 * @param label 展示文本
 * @param value 模块编码
 * @param sortOrder 排序号
 */
public record PermissionModuleOptionItem(
        String label,
        String value,
        Integer sortOrder
) {
}
