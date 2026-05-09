package net.junanw.upms.core.dictionary.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.BaseEntity;

/**
 * 字典项持久化实体。
 *
 * <p>用于映射具体的字典项记录，归属于某个字典类型，并保存展示名、实际值和排序号。
 */
@Getter
@Setter
@Table("meta_dictionary_item")
public class DictionaryItemEntity extends BaseEntity {

    /** 字典项主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 所属字典类型主键。 */
    private Long dictId;

    /** 字典项显示文本。 */
    private String itemText;

    /** 字典项实际值。 */
    private String itemValue;

    /** 排序号，越小越靠前。 */
    private Integer sortOrder;

    /** 字典项状态，如 ACTIVE 或 INACTIVE。 */
    private String status;

    /** 备注说明。 */
    private String remark;
}
