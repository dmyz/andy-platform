package net.junanw.upms.core.security.audit.operation.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
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
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 操作人用户主键。 */
    private Long actorUserId;

    /** 模块编码。 */
    private String moduleCode;

    /** 动作编码。 */
    private String actionCode;

    /** 目标类型。 */
    private String targetType;

    /** 目标主键。 */
    private Long targetId;

    /** 请求方法。 */
    private String requestMethod;

    /** 请求地址。 */
    private String requestUri;

    /** 请求摘要。 */
    private String requestSummary;

    /** 响应摘要。 */
    private String responseSummary;

    /** 结果状态，例如 SUCCESS 或 FAIL。 */
    private String resultStatus;

    /** 耗时毫秒数。 */
    private Integer durationMs;

    /** 请求来源 IP。 */
    private String ip;

    /** 链路追踪标识。 */
    private String traceId;

    /** 请求标识。 */
    private String requestId;

    /** 事件发生时间。 */
    private LocalDateTime eventTime;
}
