package net.junanw.upms.system.iam.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.SoftDeletableEntity;

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
    @Id
    private Long id;

    /** 权限编码，供系统内部唯一识别。 */
    @Column("permission_code")
    private String permissionCode;

    /** 权限名称。 */
    @Column("permission_name")
    private String permissionName;

    /** 权限类型，例如菜单权限、按钮权限。 */
    @Column("permission_type")
    private String permissionType;

    /** 资源类型，例如 API、VIEW。 */
    @Column("resource_type")
    private String resourceType;

    /** 行为编码，例如 VIEW、CREATE、UPDATE。 */
    @Column("action_code")
    private String actionCode;

    /** 所属模块编码。 */
    @Column("module_code")
    private String moduleCode;

    /** 权限状态。 */
    @Column("status")
    private String status;

    /** 备注说明。 */
    @Column("remark")
    private String remark;
}
