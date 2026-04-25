package net.junanw.upms.system.dictionary.model.view;

import java.time.LocalDateTime;

/**
 * 字典类型分页项。
 *
 * @param id 字典主键
 * @param name 字典名称
 * @param code 字典编码
 * @param status 数值状态
 * @param remark 备注
 * @param updateTime 更新时间
 */
public record DictionaryTypePageItem(
        String id,
        String name,
        String code,
        Integer status,
        String remark,
        LocalDateTime updateTime
) {
}
