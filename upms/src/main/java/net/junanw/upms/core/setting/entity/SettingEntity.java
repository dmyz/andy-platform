package net.junanw.upms.core.setting.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.BaseEntity;

/**
 * 系统配置实体。
 *
 * <p>用于保存配置键值、作用域、分组和生效方式等系统配置元数据。
 */
@Getter
@Setter
@Table("cfg_setting")
public class SettingEntity extends BaseEntity {

    /** 配置主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 配置 Key。 */
    private String settingKey;

    /** 配置名称。 */
    private String settingName;

    /** 配置值。 */
    private String settingValue;

    /** 值类型。 */
    private String valueType;

    /** 作用域类型。 */
    private String scopeType;

    /** 作用域标识。 */
    private String scopeId;

    /** 分组编码。 */
    private String groupCode;

    /** 是否敏感。 */
    private Boolean secretFlag;

    /** 生效方式。 */
    private String effectiveMode;

    /** 配置状态。 */
    private String status;

    /** 备注。 */
    private String remark;
}
