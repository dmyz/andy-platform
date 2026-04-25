package net.junanw.upms.system.iam.role.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 角色绑定持久化实体。
 *
 * <p>描述某个主体与角色之间的绑定关系，例如用户与角色的授权关系。
 */
@Getter
@Setter
@Table("iam_role_binding")
public class RoleBindingEntity {

    /** 绑定主键。 */
    @Id
    private Long id;

    /** 绑定主体类型。 */
    @Column("subject_type")
    private String subjectType;

    /** 绑定主体主键。 */
    @Column("subject_id")
    private Long subjectId;

    /** 角色主键。 */
    @Column("role_id")
    private Long roleId;

    /** 授权来源类型。 */
    @Column("source_type")
    private String sourceType;

    /** 绑定状态。 */
    @Column("status")
    private String status;

    /** 生效开始时间。 */
    @Column("effective_from")
    private LocalDateTime effectiveFrom;

    /** 生效结束时间。 */
    @Column("effective_to")
    private LocalDateTime effectiveTo;

    /** 创建人 ID。 */
    @Column("creator_id")
    private Long creatorId;

    /** 创建时间。 */
    @Column(value = "created_at", onInsertValue = "now()")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @Column(value = "updated_at", onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updatedAt;
}
