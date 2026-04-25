package net.junanw.upms.system.iam.role.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 角色权限关联持久化实体。
 *
 * <p>用于描述角色与权限之间的多对多授权关系。
 */
@Getter
@Setter
@Table("iam_role_permission")
public class RolePermissionEntity {

    /** 关联主键。 */
    @Id
    private Long id;

    /** 角色主键。 */
    @Column("role_id")
    private Long roleId;

    /** 权限主键。 */
    @Column("permission_id")
    private Long permissionId;

    /** 创建人 ID。 */
    @Column("creator_id")
    private Long creatorId;

    /** 创建时间。 */
    @Column("created_at")
    private LocalDateTime createdAt;
}
