package net.junanw.upms.core.identity.permission.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.SoftDeletableEntity;

/**
 * 权限模块目录持久化实体。
 *
 * <p>用于维护权限定义的模块分组编码、展示名称、层级和启停状态。
 */
@Getter
@Setter
@Table("iam_permission_module")
public class PermissionModuleEntity extends SoftDeletableEntity {

    /** 权限模块主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 模块编码，供权限定义的 moduleCode 引用。 */
    private String moduleCode;

    /** 模块名称，用于前端展示。 */
    private String moduleName;

    /** 父模块编码。 */
    private String parentCode;

    /** 排序号。 */
    private Integer sortOrder;

    /** 模块状态。 */
    private String status;

    /** 备注说明。 */
    private String remark;
}
