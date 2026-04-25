package net.junanw.upms.system.organization.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.BaseEntity;

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
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户主键。 */
    @Column("user_id")
    private Long userId;

    /** 组织主键。 */
    @Column("org_id")
    private Long orgId;

    /** 岗位名称。 */
    @Column("position_name")
    private String positionName;

    /** 是否为主组织。 */
    @Column("is_primary")
    private Boolean primaryOrg;

    /** 关系状态。 */
    @Column("status")
    private String status;

    /** 加入时间。 */
    @Column("joined_at")
    private LocalDateTime joinedAt;

    /** 离开时间。 */
    @Column("left_at")
    private LocalDateTime leftAt;

    /** 创建时间。 */
    @Column(value = "created_at", onInsertValue = "now()")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @Column(value = "updated_at", onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updatedAt;
}
