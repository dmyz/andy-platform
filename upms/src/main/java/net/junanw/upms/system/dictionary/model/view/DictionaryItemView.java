package net.junanw.upms.system.dictionary.model.view;

import java.time.LocalDateTime;

/**
 * 字典项视图。
 *
 * @param id 字典项主键
 * @param dictionaryId 所属字典主键
 * @param dictionaryName 所属字典名称
 * @param dictionaryCode 所属字典编码
 * @param name 字典项名称
 * @param value 字典项值
 * @param sortOrder 排序号
 * @param status 数值状态
 * @param remark 备注
 * @param updateTime 更新时间
 */
public record DictionaryItemView(
        String id,
        String dictionaryId,
        String dictionaryName,
        String dictionaryCode,
        String name,
        String value,
        Integer sortOrder,
        Integer status,
        String remark,
        LocalDateTime updateTime
) {
}
