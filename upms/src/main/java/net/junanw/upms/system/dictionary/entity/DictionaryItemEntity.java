package net.junanw.upms.system.dictionary.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.BaseEntity;

/**
 * 字典项持久化实体。
 *
 * <p>用于映射具体的字典项记录，归属于某个字典类型，并保存展示名、实际值和排序号。
 */
@Getter
@Setter
@Table("dict_item")
public class DictionaryItemEntity extends BaseEntity {

    /** 字典项主键。 */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 所属字典类型主键。 */
    @Column("dict_id")
    private Long dictId;

    /** 字典项显示文本。 */
    @Column("item_text")
    private String itemText;

    /** 字典项实际值。 */
    @Column("item_value")
    private String itemValue;

    /** 排序号，越小越靠前。 */
    @Column("sort_order")
    private Integer sortOrder;

    /** 字典项状态，如 ACTIVE 或 INACTIVE。 */
    @Column("status")
    private String status;

    /** 备注说明。 */
    @Column("remark")
    private String remark;
}
