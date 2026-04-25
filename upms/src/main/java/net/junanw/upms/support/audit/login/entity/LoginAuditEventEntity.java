package net.junanw.upms.support.audit.login.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
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
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 关联用户主键，匿名失败场景下可为空。 */
    @Column("user_id")
    private Long userId;

    /** 登录时使用的账号标识。 */
    @Column("account_identifier")
    private String accountIdentifier;

    /** 认证方式，例如 PASSWORD、MOBILE_CODE。 */
    @Column("auth_type")
    private String authType;

    /** 事件类型，例如 LOGIN_SUCCESS、LOGIN_FAIL、LOGOUT。 */
    @Column("event_type")
    private String eventType;

    /** 是否成功。 */
    @Column("success_flag")
    private Boolean successFlag;

    /** 失败原因编码。 */
    @Column("reason_code")
    private String reasonCode;

    /** 请求来源 IP。 */
    @Column("ip")
    private String ip;

    /** User-Agent。 */
    @Column("user_agent")
    private String userAgent;

    /** 会话键或 token 标识。 */
    @Column("session_key")
    private String sessionKey;

    /** 链路追踪标识。 */
    @Column("trace_id")
    private String traceId;

    /** 请求标识。 */
    @Column("request_id")
    private String requestId;

    /** 事件发生时间。 */
    @Column("event_time")
    private LocalDateTime eventTime;
}
