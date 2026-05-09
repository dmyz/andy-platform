package net.junanw.upms.core.identity.user.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.BaseEntity;

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
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 用户编码。 */
    private String userCode;

    /** 显示名称或真实姓名。 */
    private String displayName;

    /** 性别。 */
    private String gender;

    /** 工号。 */
    private String employeeNo;

    /** 头像文件主键。 */
    private Long avatarFileId;

    /** 用户类型。 */
    private String userType;

    /** 来源类型。 */
    private String sourceType;

    /** 用户状态。 */
    private String status;

    /** 是否要求下次登录重置密码。 */
    private Boolean passwordResetRequired;

    /** 最近登录时间。 */
    private LocalDateTime lastLoginTime;

    /** 最近登录 IP。 */
    private String lastLoginIp;

    /** 备注说明。 */
    private String remark;
}
