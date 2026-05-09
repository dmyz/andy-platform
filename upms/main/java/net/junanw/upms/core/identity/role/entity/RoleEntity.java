package net.junanw.upms.core.identity.role.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.SoftDeletableEntity;

/**
 * 角色持久化实体。
 *
 * <p>用于保存角色定义、数据权限范围和状态等基础信息。
 */
@Getter
@Setter
@Table("iam_role")
public class RoleEntity extends SoftDeletableEntity {

    /** 角色主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 角色编码。 */
    private String roleCode;

    /** 角色名称。 */
    private String roleName;

    /** 角色类型。 */
    private String roleType;

    /** 数据权限范围。 */
    private String dataScope;

    /** 角色状态。 */
    private String status;

    /** 备注说明。 */
    private String remark;
}
