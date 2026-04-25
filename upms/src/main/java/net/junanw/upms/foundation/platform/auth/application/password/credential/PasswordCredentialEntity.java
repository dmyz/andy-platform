package net.junanw.upms.foundation.platform.auth.application.password.credential;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 密码凭证持久化实体。
 * <p>
 * 该实体只承载密码哈希、锁定状态、临时密码标记等认证侧私有数据，
 * 不与用户基础资料直接混放。
 */
@Getter
@Setter
@Table("auth_password_credential")
public class PasswordCredentialEntity {

    /**
     * 凭证主键。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 凭证所属用户 ID。
     */
    @Column("user_id")
    private Long userId;

    /**
     * 密码哈希值，不保存明文密码。
     */
    @Column("password_hash")
    private String passwordHash;

    /**
     * 密码哈希算法标识，例如 `BCRYPT`。
     */
    @Column("password_algo")
    private String passwordAlgo;

    /**
     * 最近一次密码变更时间。
     */
    @Column("password_changed_time")
    private LocalDateTime passwordChangedTime;

    /**
     * 是否为临时密码。
     */
    @Column("temporary_flag")
    private Boolean temporaryFlag;

    /**
     * 连续密码失败次数。
     */
    @Column("failed_count")
    private Integer failedCount;

    /**
     * 锁定截止时间；为空表示当前未锁定。
     */
    @Column("locked_until")
    private LocalDateTime lockedUntil;

    /**
     * 凭证创建时间。
     */
    @Column(value = "created_at", onInsertValue = "now()")
    private LocalDateTime createdAt;

    /**
     * 凭证最近更新时间。
     */
    @Column(value = "updated_at", onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updatedAt;
}
