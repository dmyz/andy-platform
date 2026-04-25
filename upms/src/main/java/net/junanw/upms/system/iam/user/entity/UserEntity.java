package net.junanw.upms.system.iam.user.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.BaseEntity;

import java.time.LocalDateTime;

/**
 * 用户持久化实体。
 *
 * <p>用于保存用户主体资料、状态、登录信息与密码重置标记，是用户管理的核心主表。
 */
@Getter
@Setter
@Table("iam_user")
public class UserEntity extends BaseEntity {

    /** 用户主键。 */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户编码。 */
    @Column("user_code")
    private String userCode;

    /** 显示名称或真实姓名。 */
    @Column("display_name")
    private String displayName;

    /** 性别。 */
    @Column("gender")
    private String gender;

    /** 工号。 */
    @Column("employee_no")
    private String employeeNo;

    /** 头像文件主键。 */
    @Column("avatar_file_id")
    private Long avatarFileId;

    /** 用户类型。 */
    @Column("user_type")
    private String userType;

    /** 来源类型。 */
    @Column("source_type")
    private String sourceType;

    /** 用户状态。 */
    @Column("status")
    private String status;

    /** 是否要求下次登录重置密码。 */
    @Column("password_reset_required")
    private Boolean passwordResetRequired;

    /** 最近登录时间。 */
    @Column("last_login_time")
    private LocalDateTime lastLoginTime;

    /** 最近登录 IP。 */
    @Column("last_login_ip")
    private String lastLoginIp;

    /** 备注说明。 */
    @Column("remark")
    private String remark;
}
