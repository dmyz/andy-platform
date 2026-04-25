package net.junanw.upms.system.organization.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.BaseEntity;

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
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 上级组织主键。 */
    @Column("parent_id")
    private Long parentId;

    /** 组织编码。 */
    @Column("org_code")
    private String orgCode;

    /** 组织名称。 */
    @Column("org_name")
    private String orgName;

    /** 组织全称。 */
    @Column("org_full_name")
    private String orgFullName;

    /** 负责人用户主键。 */
    @Column("leader_user_id")
    private Long leaderUserId;

    /** 层级号。 */
    @Column("level_no")
    private Integer levelNo;

    /** 排序号。 */
    @Column("sort_order")
    private Integer sortOrder;

    /** 组织状态。 */
    @Column("status")
    private String status;

    /** 备注。 */
    @Column("remark")
    private String remark;
}
