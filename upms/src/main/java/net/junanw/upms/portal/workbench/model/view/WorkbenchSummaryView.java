package net.junanw.upms.portal.workbench.model.view;

import java.time.LocalDateTime;

/**
 * 工作台摘要视图。
 *
 * <p>用于承载工作台首页摘要信息的返回字段。
 *
 * @param username 用户名
 * @param displayName 显示名称
 * @param orgName 所属组织名称
 * @param roleName 当前角色名称
 * @param loginTime 登录时间
 */

public record WorkbenchSummaryView(
        String username,
        String displayName,
        String orgName,
        String roleName,
        LocalDateTime loginTime
) {
}
