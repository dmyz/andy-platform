package net.junanw.upms.core.identity.permission.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.SoftDeletableEntity;

/**
 * 权限持久化实体。
 *
 * <p>用于映射系统权限定义，是角色授权、导航鉴权和接口权限控制的基础数据来源。
 */
@Getter
@Setter
@Table("iam_permission")
public class PermissionEntity extends SoftDeletableEntity {

    /** 权限主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 权限编码，供系统内部唯一识别。 */
    private String permissionCode;

    /** 权限名称。 */
    private String permissionName;

    /** 权限类型，例如菜单权限、按钮权限。 */
    private String permissionType;

    /** 资源类型，例如 API、VIEW。 */
    private String resourceType;

    /** 行为编码，例如 VIEW、CREATE、UPDATE。 */
    private String actionCode;

    /** 所属模块编码。 */
    private String moduleCode;

    /** 权限状态。 */
    private String status;

    /** 备注说明。 */
    private String remark;
}
