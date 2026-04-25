package net.junanw.upms.support.audit.operation.model.view;

import java.time.LocalDateTime;

/**
 * 操作审计详情视图。
 *
 * <p>用于承载操作审计详情接口的返回字段。
 *
 * @param id 审计记录 ID
 * @param operatorName 操作人姓名
 * @param moduleName 所属模块名称
 * @param actionType 操作类型
 * @param requestMethod 请求方法
 * @param requestUri 请求路径
 * @param requestParams 请求参数摘要
 * @param durationMs 执行耗时（毫秒）
 * @param responseCode 响应状态码
 * @param result 执行结果
 * @param errorMessage 错误信息
 * @param operationTime 操作时间
 */

public record OperationAuditDetailView(
        String id,
        String operatorName,
        String moduleName,
        String actionType,
        String requestMethod,
        String requestUri,
        String requestParams,
        Long durationMs,
        Integer responseCode,
        String result,
        String errorMessage,
        LocalDateTime operationTime
) {
}
