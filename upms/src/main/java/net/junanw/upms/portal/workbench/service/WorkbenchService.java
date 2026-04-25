package net.junanw.upms.portal.workbench.service;

import net.junanw.upms.portal.workbench.model.view.WorkbenchAnnouncementItem;
import net.junanw.upms.portal.workbench.model.view.WorkbenchFavoriteNavigationItem;
import net.junanw.upms.portal.workbench.model.view.WorkbenchRecentOperationItem;
import net.junanw.upms.portal.workbench.model.view.WorkbenchSummaryView;

import java.util.List;
/**
 * WorkbenchService 服务接口。
 *
 * <p>定义 Workbench 相关业务能力边界。
 */

/**
 * 工作台服务接口。
 *
 * <p>定义工作台首页所需的汇总、快捷入口、最近操作和公告读取能力。
 */
public interface WorkbenchService {

    /** 查询工作台汇总。 */
    WorkbenchSummaryView summary(String username);

    /** 查询最近操作。 */
    List<WorkbenchRecentOperationItem> recentOperations(String username);

    /** 查询常用功能。 */
    List<WorkbenchFavoriteNavigationItem> favoriteNavigations(String username);

    /** 查询工作台公告。 */
    List<WorkbenchAnnouncementItem> announcements(String username);
}
