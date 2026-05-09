package net.junanw.upms.core.setting.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 配置分页项视图。
 *
 * <p>用于承载系统配置分页列表的返回字段。
 *
 * @param id 配置 ID
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
 * @param updateTime 更新时间
 */

public record SettingPageItem(
        String id,
        String settingKey,
        String settingName,
        String settingValue,
        String valueType,
        String scopeType,
        String scopeId,
        String groupCode,
        Boolean secretFlag,
        String effectiveMode,
        Integer status,
        String remark,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updateTime
) {
}
