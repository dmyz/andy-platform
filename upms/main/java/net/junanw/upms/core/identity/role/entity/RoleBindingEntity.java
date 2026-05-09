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
 * 角色绑定持久化实体。
 *
 * <p>描述某个主体与角色之间的绑定关系，例如用户与角色的授权关系。
 */
@Getter
@Setter
@Table("iam_role_binding")
public class RoleBindingEntity {

    /** 绑定主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 绑定主体类型。 */
    private String subjectType;

    /** 绑定主体主键。 */
    private Long subjectId;

    /** 角色主键。 */
    private Long roleId;

    /** 授权来源类型。 */
    private String sourceType;

    /** 绑定状态。 */
    private String status;

    /** 生效开始时间。 */
    private LocalDateTime effectiveFrom;

    /** 生效结束时间。 */
    private LocalDateTime effectiveTo;

    /** 创建人 ID。 */
    private Long creatorId;

    /** 创建时间。 */
    @TableField(defaultValue = "{NOW}", neverUpdate = true)
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField(defaultValue = "{NOW}", updateDefaultValue = "{NOW}", updateDefaultValueFillAlways = true)
    private LocalDateTime updatedAt;
}
