package net.junanw.upms.core.security.audit.login.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 登录审计事件实体。
 *
 * <p>用于保存登录成功、失败、登出等认证事件。
 */
@Getter
@Setter
@Table("audit_login_event")
public class LoginAuditEventEntity {

    /** 审计事件主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 关联用户主键，匿名失败场景下可为空。 */
    private Long userId;

    /** 登录时使用的账号标识。 */
    private String accountIdentifier;

    /** 认证方式，例如 PASSWORD、MOBILE_CODE。 */
    private String authType;

    /** 事件类型，例如 LOGIN_SUCCESS、LOGIN_FAIL、LOGOUT。 */
    private String eventType;

    /** 是否成功。 */
    private Boolean successFlag;

    /** 失败原因编码。 */
    private String reasonCode;

    /** 请求来源 IP。 */
    private String ip;

    /** User-Agent。 */
    private String userAgent;

    /** 会话键或 token 标识。 */
    private String sessionKey;

    /** 链路追踪标识。 */
    private String traceId;

    /** 请求标识。 */
    private String requestId;

    /** 事件发生时间。 */
    private LocalDateTime eventTime;
}
