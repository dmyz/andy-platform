package net.junanw.upms.support.audit.operation.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 操作审计事件实体。
 *
 * <p>用于保存管理端写操作请求的模块、动作、请求摘要和执行结果等信息。
 */
@Getter
@Setter
@Table("audit_operation_event")
public class OperationAuditEventEntity {

    /** 审计事件主键。 */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 操作人用户主键。 */
    @Column("actor_user_id")
    private Long actorUserId;

    /** 模块编码。 */
    @Column("module_code")
    private String moduleCode;

    /** 动作编码。 */
    @Column("action_code")
    private String actionCode;

    /** 目标类型。 */
    @Column("target_type")
    private String targetType;

    /** 目标主键。 */
    @Column("target_id")
    private Long targetId;

    /** 请求方法。 */
    @Column("request_method")
    private String requestMethod;

    /** 请求地址。 */
    @Column("request_uri")
    private String requestUri;

    /** 请求摘要。 */
    @Column("request_summary")
    private String requestSummary;

    /** 响应摘要。 */
    @Column("response_summary")
    private String responseSummary;

    /** 结果状态，例如 SUCCESS 或 FAIL。 */
    @Column("result_status")
    private String resultStatus;

    /** 耗时毫秒数。 */
    @Column("duration_ms")
    private Integer durationMs;

    /** 请求来源 IP。 */
    @Column("ip")
    private String ip;

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
