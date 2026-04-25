package net.junanw.upms.portal.workbench.service;

import net.junanw.upms.portal.workbench.model.view.WorkbenchAnnouncementItem;
import net.junanw.upms.portal.workbench.model.view.WorkbenchFavoriteNavigationItem;
import net.junanw.upms.portal.workbench.model.view.WorkbenchRecentOperationItem;
import net.junanw.upms.portal.workbench.model.view.WorkbenchSummaryView;
import net.junanw.upms.support.audit.operation.service.OperationAuditService;
import net.junanw.upms.support.notification.announcement.service.AnnouncementService;
import net.junanw.upms.foundation.platform.auth.query.model.NavigationItem;
import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import net.junanw.upms.system.iam.role.service.RoleService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * WorkbenchServiceImpl 服务实现。
 *
 * <p>负责承接 Workbench 相关业务编排与规则落地。
 */
@Service
@Primary
public class WorkbenchServiceImpl implements WorkbenchService {

    private final UserContextService userContextService;
    private final OperationAuditService operationAuditService;
    private final AnnouncementService announcementService;
    private final RoleService roleService;

    public WorkbenchServiceImpl(
            UserContextService userContextService,
            OperationAuditService operationAuditService,
            AnnouncementService announcementService,
            RoleService roleService
    ) {
        this.userContextService = userContextService;
        this.operationAuditService = operationAuditService;
        this.announcementService = announcementService;
        this.roleService = roleService;
    }

    /**
     * 查询工作台汇总。
     */
    @Override
    public WorkbenchSummaryView summary(String username) {
        var profile = userContextService.requireByUsername(username);
        String roleName = profile.roleCodes().isEmpty()
                ? profile.positionName()
                : roleService.resolvePrimaryRoleName(profile.roleCodes(), profile.roleCodes().get(0));
        return new WorkbenchSummaryView(profile.username(), profile.displayName(), profile.orgName(), roleName, profile.lastLoginTime());
    }

    /**
     * 查询最近操作。
     */
    @Override
    public List<WorkbenchRecentOperationItem> recentOperations(String username) {
        return operationAuditService.page(username, null, null, null, null, null, 1, 4).list().stream()
                .map(item -> new WorkbenchRecentOperationItem(item.id(), item.operationTime(), item.moduleName(), item.actionType(), item.result()))
                .toList();
    }

    /**
     * 查询常用功能。
     */
    @Override
    public List<WorkbenchFavoriteNavigationItem> favoriteNavigations(String username) {
        List<NavigationItem> visible = userContextService.buildCurrentUser(username).navigations();
        List<NavigationItem> pages = new ArrayList<>();
        flattenPages(visible, pages);
        return pages.stream()
                .sorted(java.util.Comparator.comparing(NavigationItem::sortOrder))
                .limit(6)
                .map(item -> new WorkbenchFavoriteNavigationItem(item.name(), item.routePath(), item.icon(), resolveColor(item.routePath())))
                .toList();
    }

    /**
     * 查询工作台公告。
     */
    @Override
    public List<WorkbenchAnnouncementItem> announcements(String username) {
        return announcementService.dashboardAnnouncements(username, 3);
    }

    /**
     * 平铺导航树中的页面节点。
     */
    private void flattenPages(List<NavigationItem> items, List<NavigationItem> pages) {
        for (NavigationItem item : items) {
            if ("PAGE".equalsIgnoreCase(item.type())) {
                pages.add(item);
            }
            flattenPages(item.children(), pages);
        }
    }

    /**
     * 根据路径分配展示颜色。
     */
    private String resolveColor(String path) {
        String normalized = path == null ? "" : path.toLowerCase(Locale.ROOT);
        if (normalized.contains("user")) return "#0052d9";
        if (normalized.contains("role")) return "#00a870";
        if (normalized.contains("announcement")) return "#ed7b2f";
        if (normalized.contains("file")) return "#e34d59";
        if (normalized.contains("setting")) return "#7b67ee";
        if (normalized.contains("audit")) return "#0594fa";
        return "#0052d9";
    }
}
