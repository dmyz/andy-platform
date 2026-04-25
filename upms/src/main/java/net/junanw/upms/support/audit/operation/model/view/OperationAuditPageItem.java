package net.junanw.upms.support.audit.operation.model.view;

import java.time.LocalDateTime;

/**
 * 操作审计分页项视图。
 *
 * <p>用于承载操作审计分页列表的返回字段。
 *
 * @param id 审计记录 ID
 * @param operatorName 操作人姓名
 * @param moduleName 所属模块名称
 * @param actionType 操作类型
 * @param requestUri 请求路径
 * @param durationMs 执行耗时（毫秒）
 * @param operationTime 操作时间
 * @param result 执行结果
 */

public record OperationAuditPageItem(
        String id,
        String operatorName,
        String moduleName,
        String actionType,
        String requestUri,
        Long durationMs,
        LocalDateTime operationTime,
        String result
) {
}
