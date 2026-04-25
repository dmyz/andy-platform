package net.junanw.upms.portal.workbench.model.view;

import java.time.LocalDateTime;

/**
 * 工作台最近操作项视图。
 *
 * <p>用于承载工作台最近操作列表的返回字段。
 *
 * @param id 操作记录 ID
 * @param operationTime 操作时间
 * @param module 模块名称
 * @param action 操作动作
 * @param result 操作结果
 */

public record WorkbenchRecentOperationItem(
        String id,
        LocalDateTime operationTime,
        String module,
        String action,
        String result
) {
}
