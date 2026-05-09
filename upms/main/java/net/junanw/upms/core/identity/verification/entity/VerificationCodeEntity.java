package net.junanw.upms.core.identity.verification.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableField;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import db.sql.api.DbType;
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
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 目标类型，例如 MOBILE 或 EMAIL。 */
    private String targetType;

    /** 归一化后的目标值，如手机号或邮箱。 */
    private String targetValue;

    /** 验证码所属业务场景。 */
    private String scene;

    /** 实际验证码内容。 */
    private String verificationCode;

    /** 过期时间。 */
    private LocalDateTime expireTime;

    /** 是否已被消费。 */
    private Boolean usedFlag;

    /** 实际消费时间。 */
    private LocalDateTime usedTime;

    /** 创建时间。 */
    @TableField(defaultValue = "{NOW}", neverUpdate = true)
    private LocalDateTime createdAt;
}
