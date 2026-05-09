package net.junanw.upms.core.dictionary.model.view;

/**
 * 字典选项项。
 *
 * @param label 显示文本
 * @param value 实际值
 * @param sortOrder 排序号
 */
public record DictionaryOptionItem(
        String label,
        String value,
        Integer sortOrder
) {
}
