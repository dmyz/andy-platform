package net.junanw.upms.system.iam.role.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.SoftDeletableEntity;

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
    @Id
    private Long id;

    /** 角色编码。 */
    @Column("role_code")
    private String roleCode;

    /** 角色名称。 */
    @Column("role_name")
    private String roleName;

    /** 角色类型。 */
    @Column("role_type")
    private String roleType;

    /** 数据权限范围。 */
    @Column("data_scope")
    private String dataScope;

    /** 角色状态。 */
    @Column("status")
    private String status;

    /** 备注说明。 */
    @Column("remark")
    private String remark;
}
