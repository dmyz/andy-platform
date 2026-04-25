package net.junanw.upms.portal.workbench.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import net.junanw.upms.foundation.platform.auth.authentication.context.LoginUserContext;
import net.junanw.upms.foundation.shared.api.ApiResponse;
import net.junanw.upms.portal.workbench.model.view.WorkbenchAnnouncementItem;
import net.junanw.upms.portal.workbench.model.view.WorkbenchFavoriteNavigationItem;
import net.junanw.upms.portal.workbench.model.view.WorkbenchRecentOperationItem;
import net.junanw.upms.portal.workbench.model.view.WorkbenchSummaryView;
import net.junanw.upms.portal.workbench.service.WorkbenchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * WorkbenchController 控制器。
 *
 * <p>负责提供 Workbench 相关 HTTP 接口并委托服务层处理具体业务。
 */
@RestController
@RequestMapping("/admin/dashboard")
public class WorkbenchController {

    private final WorkbenchService workbenchService;
    private final LoginUserContext loginUserContext;

    public WorkbenchController(WorkbenchService workbenchService, LoginUserContext loginUserContext) {
        this.workbenchService = workbenchService;
        this.loginUserContext = loginUserContext;
    }

    @GetMapping("/summary")
    @SaCheckPermission("dashboard:view")
    /** 查询工作台汇总。 */
    public ApiResponse<WorkbenchSummaryView> summary() {
        return ApiResponse.success(workbenchService.summary(loginUserContext.getLoginUsername()));
    }

    @GetMapping("/recent-operations")
    @SaCheckPermission("dashboard:view")
    /** 查询最近操作。 */
    public ApiResponse<List<WorkbenchRecentOperationItem>> recentOperations() {
        return ApiResponse.success(workbenchService.recentOperations(loginUserContext.getLoginUsername()));
    }

    @GetMapping("/favorite-navigations")
    @SaCheckPermission("dashboard:view")
    /** 查询常用功能。 */
    public ApiResponse<List<WorkbenchFavoriteNavigationItem>> favoriteNavigations() {
        return ApiResponse.success(workbenchService.favoriteNavigations(loginUserContext.getLoginUsername()));
    }

    @GetMapping("/announcements")
    @SaCheckPermission("dashboard:view")
    /** 查询工作台公告。 */
    public ApiResponse<List<WorkbenchAnnouncementItem>> announcements() {
        return ApiResponse.success(workbenchService.announcements(loginUserContext.getLoginUsername()));
    }
}
