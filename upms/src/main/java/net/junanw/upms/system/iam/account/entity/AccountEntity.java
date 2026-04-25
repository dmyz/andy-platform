package net.junanw.upms.system.iam.account.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.AuditableEntity;

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
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 账号所属用户主键。 */
    @Column("user_id")
    private Long userId;

    /** 账号类型，例如 USERNAME、MOBILE、EMAIL。 */
    @Column("account_type")
    private String accountType;

    /** 原始账号标识。 */
    @Column("identifier")
    private String identifier;

    /** 归一化后的账号标识，用于检索和去重。 */
    @Column("normalized_identifier")
    private String normalizedIdentifier;

    /** 是否允许用于登录。 */
    @Column("is_login_enabled")
    private Boolean isLoginEnabled;

    /** 是否为该类型的主账号。 */
    @Column("is_primary")
    private Boolean isPrimary;

    /** 是否已完成验证。 */
    @Column("verified_flag")
    private Boolean verifiedFlag;

    /** 账号状态。 */
    @Column("status")
    private String status;

    /** 最近一次使用时间。 */
    @Column("last_used_time")
    private LocalDateTime lastUsedTime;
}
