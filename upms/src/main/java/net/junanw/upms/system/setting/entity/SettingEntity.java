package net.junanw.upms.system.setting.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.BaseEntity;

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
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 配置 Key。 */
    @Column("setting_key")
    private String settingKey;

    /** 配置名称。 */
    @Column("setting_name")
    private String settingName;

    /** 配置值。 */
    @Column("setting_value")
    private String settingValue;

    /** 值类型。 */
    @Column("value_type")
    private String valueType;

    /** 作用域类型。 */
    @Column("scope_type")
    private String scopeType;

    /** 作用域标识。 */
    @Column("scope_id")
    private String scopeId;

    /** 分组编码。 */
    @Column("group_code")
    private String groupCode;

    /** 是否敏感。 */
    @Column("secret_flag")
    private Boolean secretFlag;

    /** 生效方式。 */
    @Column("effective_mode")
    private String effectiveMode;

    /** 配置状态。 */
    @Column("status")
    private String status;

    /** 备注。 */
    @Column("remark")
    private String remark;
}
