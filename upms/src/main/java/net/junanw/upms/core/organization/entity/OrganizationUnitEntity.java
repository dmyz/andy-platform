package net.junanw.upms.core.organization.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.BaseEntity;

/**
 * 组织单元实体。
 *
 * <p>用于保存组织树节点的编码、名称、层级、负责人和状态信息。
 */
@Getter
@Setter
@Table("org_unit")
public class OrganizationUnitEntity extends BaseEntity {

    /** 组织主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 上级组织主键。 */
    private Long parentId;

    /** 组织编码。 */
    private String orgCode;

    /** 组织名称。 */
    private String orgName;

    /** 组织全称。 */
    private String orgFullName;

    /** 负责人用户主键。 */
    private Long leaderUserId;

    /** 层级号。 */
    private Integer levelNo;

    /** 排序号。 */
    private Integer sortOrder;

    /** 组织状态。 */
    private String status;

    /** 备注。 */
    private String remark;
}
