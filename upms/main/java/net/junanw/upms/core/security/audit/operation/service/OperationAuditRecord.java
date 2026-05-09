package net.junanw.upms.core.security.audit.operation.service;

import java.time.LocalDateTime;
/**
 * OperationAuditRecord 记录对象。
 */

public record OperationAuditRecord(
        String actorUsername,
        String moduleCode,
        String actionCode,
        String targetType,
        Long targetId,
        String requestMethod,
        String requestUri,
        String requestSummary,
        String responseSummary,
        String resultStatus,
        Long durationMs,
        String ip,
        String traceId,
        String requestId,
        LocalDateTime eventTime
) {
}
