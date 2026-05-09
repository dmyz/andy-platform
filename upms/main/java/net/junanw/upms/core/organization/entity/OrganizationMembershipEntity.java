package net.junanw.upms.core.organization.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableField;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.BaseEntity;

import java.time.LocalDateTime;

/**
 * 组织成员关系实体。
 *
 * <p>用于描述用户与组织之间的归属关系、岗位名称和主组织标记。
 */
@Getter
@Setter
@Table("org_membership")
public class OrganizationMembershipEntity extends BaseEntity {

    /** 关系主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 用户主键。 */
    private Long userId;

    /** 组织主键。 */
    private Long orgId;

    /** 岗位名称。 */
    private String positionName;

    /** 是否为主组织。 */
    @TableField("is_primary")
    private Boolean primaryOrg;

    /** 关系状态。 */
    private String status;

    /** 加入时间。 */
    private LocalDateTime joinedAt;

    /** 离开时间。 */
    private LocalDateTime leftAt;

    /** 创建时间。 */
    @TableField(defaultValue = "{NOW}", neverUpdate = true)
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField(defaultValue = "{NOW}", updateDefaultValue = "{NOW}", updateDefaultValueFillAlways = true)
    private LocalDateTime updatedAt;
}
