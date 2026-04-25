package net.junanw.upms.support.audit.operation.service;

import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.support.audit.operation.model.view.OperationAuditDetailView;
import net.junanw.upms.support.audit.operation.model.view.OperationAuditPageItem;

import java.time.LocalDateTime;
import java.util.List;
/**
 * OperationAuditService 服务接口。
 *
 * <p>定义 OperationAudit 相关业务能力边界。
 */

/**
 * 操作审计服务接口。
 *
 * <p>定义操作事件记录、分页查询、详情查看与导出能力。
 */
public interface OperationAuditService {

    /** 记录操作审计事件。 */
    default void record(OperationAuditRecord command) {
    }

    /** 查询操作审计分页。 */
    PageResponse<OperationAuditPageItem> page(
            String operatorName,
            String moduleName,
            String actionType,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize
    );

    /** 查询操作审计详情。 */
    OperationAuditDetailView detail(String id);

    /** 导出操作审计列表。 */
    List<OperationAuditPageItem> export(
            String operatorName,
            String moduleName,
            String actionType,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
