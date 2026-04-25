package net.junanw.upms.system.setting.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 配置保存请求。
 *
 * <p>封装系统配置新增与编辑接口的请求参数。
 *
 * @param settingKey 配置键
 * @param settingName 配置名称
 * @param settingValue 配置值
 * @param valueType 值类型
 * @param scopeType 作用域类型
 * @param scopeId 作用域对象标识
 * @param groupCode 分组编码
 * @param secretFlag 是否敏感
 * @param effectiveMode 生效方式
 * @param status 状态值
 * @param remark 备注
 */

public record SettingSaveRequest(
        @NotBlank(message = "配置 Key 不能为空")
        @Size(max = 128, message = "配置 Key 长度不能超过128")
        String settingKey,
        @NotBlank(message = "配置名称不能为空")
        @Size(max = 128, message = "配置名称长度不能超过128")
        String settingName,
        @NotBlank(message = "配置值不能为空")
        String settingValue,
        @NotBlank(message = "值类型不能为空")
        @Size(max = 32, message = "值类型长度不能超过32")
        String valueType,
        @NotBlank(message = "作用域不能为空")
        @Size(max = 32, message = "作用域长度不能超过32")
        String scopeType,
        @Size(max = 64, message = "作用域对象长度不能超过64")
        String scopeId,
        @NotBlank(message = "配置分组不能为空")
        @Size(max = 64, message = "配置分组长度不能超过64")
        String groupCode,
        @NotNull(message = "是否敏感不能为空")
        Boolean secretFlag,
        @NotBlank(message = "生效方式不能为空")
        @Size(max = 32, message = "生效方式长度不能超过32")
        String effectiveMode,
        @NotNull(message = "状态不能为空")
        Integer status,
        @Size(max = 200, message = "备注长度不能超过200")
        String remark
) {
}
