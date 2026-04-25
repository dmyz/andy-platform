package net.junanw.upms.system.dictionary.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 字典类型保存请求。
 *
 * @param name 字典名称
 * @param code 字典编码
 * @param status 状态，1 表示启用，0 表示停用
 * @param remark 备注
 */
public record DictionaryTypeSaveRequest(
        @NotBlank(message = "字典名称不能为空")
        @Size(max = 64, message = "字典名称长度不能超过64")
        String name,
        @NotBlank(message = "字典编码不能为空")
        @Size(max = 64, message = "字典编码长度不能超过64")
        String code,
        @NotNull(message = "状态不能为空")
        Integer status,
        @Size(max = 200, message = "备注长度不能超过200")
        String remark
) {
}
