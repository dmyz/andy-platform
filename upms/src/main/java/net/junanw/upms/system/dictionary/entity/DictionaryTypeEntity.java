package net.junanw.upms.system.dictionary.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.BaseEntity;

/**
 * 字典类型持久化实体。
 *
 * <p>用于映射字典类型主表，保存字典编码、名称和状态等基础信息。
 */
@Getter
@Setter
@Table("dict_type")
public class DictionaryTypeEntity extends BaseEntity {

    /** 字典主键。 */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 字典编码，要求全局唯一。 */
    @Column("dict_code")
    private String dictCode;

    /** 字典名称。 */
    @Column("dict_name")
    private String dictName;

    /** 字典状态，如 ACTIVE 或 INACTIVE。 */
    @Column("status")
    private String status;

    /** 备注说明。 */
    @Column("remark")
    private String remark;
}
