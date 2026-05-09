package net.junanw.upms.core.identity.account.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableField;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.AuditableEntity;

import java.time.LocalDateTime;

/**
 * 账号持久化实体。
 *
 * <p>用于映射用户账号标识，如用户名、手机号、邮箱等，是认证与绑定关系的重要持久化基础。
 */
@Getter
@Setter
@Table("iam_account")
public class AccountEntity extends AuditableEntity {

    /** 账号主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 账号所属用户主键。 */
    private Long userId;

    /** 账号类型，例如 USERNAME、MOBILE、EMAIL。 */
    private String accountType;

    /** 原始账号标识。 */
    private String identifier;

    /** 归一化后的账号标识，用于检索和去重。 */
    private String normalizedIdentifier;

    /** 是否允许用于登录。 */
    @TableField("is_login_enabled")
    private Boolean isLoginEnabled;

    /** 是否为该类型的主账号。 */
    @TableField("is_primary")
    private Boolean isPrimary;

    /** 是否已完成验证。 */
    private Boolean verifiedFlag;

    /** 账号状态。 */
    private String status;

    /** 最近一次使用时间。 */
    private LocalDateTime lastUsedTime;
}
