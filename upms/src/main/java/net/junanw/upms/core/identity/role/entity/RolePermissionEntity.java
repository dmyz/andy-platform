package net.junanw.upms.core.identity.role.entity;

import cn.xbatis.db.annotations.TableField;
import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
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
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 角色主键。 */
    private Long roleId;

    /** 权限主键。 */
    private Long permissionId;

    /** 创建人 ID。 */
    private Long creatorId;

    /** 创建时间。 */
    @TableField(defaultValue = "{NOW}", neverUpdate = true)
    private LocalDateTime createdAt;
}
