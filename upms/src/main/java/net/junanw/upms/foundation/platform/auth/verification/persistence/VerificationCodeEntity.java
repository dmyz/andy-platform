package net.junanw.upms.foundation.platform.auth.verification.persistence;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 验证码持久化实体。
 *
 * <p>用于记录验证码签发、使用和过期状态，是验证码校验链路的唯一持久化来源。
 */
@Getter
@Setter
@Table("auth_verification_code")
public class VerificationCodeEntity {

    /** 主键。 */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 目标类型，例如 MOBILE 或 EMAIL。 */
    @Column("target_type")
    private String targetType;

    /** 归一化后的目标值，如手机号或邮箱。 */
    @Column("target_value")
    private String targetValue;

    /** 验证码所属业务场景。 */
    @Column("scene")
    private String scene;

    /** 实际验证码内容。 */
    @Column("verification_code")
    private String verificationCode;

    /** 过期时间。 */
    @Column("expire_time")
    private LocalDateTime expireTime;

    /** 是否已被消费。 */
    @Column("used_flag")
    private Boolean usedFlag;

    /** 实际消费时间。 */
    @Column("used_time")
    private LocalDateTime usedTime;

    /** 创建时间。 */
    @Column(value = "created_at", onInsertValue = "now()")
    private LocalDateTime createdAt;
}
