package net.junanw.upms.core.dictionary.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.BaseEntity;

/**
 * 字典类型持久化实体。
 *
 * <p>用于映射字典类型主表，保存字典编码、名称和状态等基础信息。
 */
@Getter
@Setter
@Table("meta_dictionary")
public class DictionaryTypeEntity extends BaseEntity {

    /** 字典主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 字典编码，要求全局唯一。 */
    private String dictCode;

    /** 字典名称。 */
    private String dictName;

    /** 字典状态，如 ACTIVE 或 INACTIVE。 */
    private String status;

    /** 备注说明。 */
    private String remark;
}
