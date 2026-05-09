package net.junanw.upms.core.dictionary.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 字典项保存请求。
 *
 * @param name 字典项名称
 * @param value 字典项值
 * @param sortOrder 排序号
 * @param status 状态，1 表示启用，0 表示停用
 * @param remark 备注
 */
public record DictionaryItemSaveRequest(
        @NotBlank(message = "字典项名称不能为空")
        @Size(max = 64, message = "字典项名称长度不能超过64")
        String name,
        @NotBlank(message = "字典项值不能为空")
        @Size(max = 64, message = "字典项值长度不能超过64")
        String value,
        @NotNull(message = "排序号不能为空")
        @Min(value = 1, message = "排序号不能小于1")
        @Max(value = 9999, message = "排序号不能大于9999")
        Integer sortOrder,
        @NotNull(message = "状态不能为空")
        Integer status,
        @Size(max = 200, message = "备注长度不能超过200")
        String remark
) {
}
