package net.junanw.upms.core.identity.password.entity;

import cn.xbatis.db.annotations.TableField;
import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
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
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /**
     * 凭证所属用户 ID。
     */
    private Long userId;

    /**
     * 密码哈希值，不保存明文密码。
     */
    private String passwordHash;

    /**
     * 密码哈希算法标识，例如 `BCRYPT`。
     */
    private String passwordAlgo;

    /**
     * 最近一次密码变更时间。
     */
    private LocalDateTime passwordChangedTime;

    /**
     * 是否为临时密码。
     */
    private Boolean temporaryFlag;

    /**
     * 连续密码失败次数。
     */
    private Integer failedCount;

    /**
     * 锁定截止时间；为空表示当前未锁定。
     */
    private LocalDateTime lockedUntil;

    /**
     * 凭证创建时间。
     */
    @TableField(defaultValue = "{NOW}", neverUpdate = true)
    private LocalDateTime createdAt;

    /**
     * 凭证最近更新时间。
     */
    @TableField(defaultValue = "{NOW}", updateDefaultValue = "{NOW}", updateDefaultValueFillAlways = true)
    private LocalDateTime updatedAt;
}
