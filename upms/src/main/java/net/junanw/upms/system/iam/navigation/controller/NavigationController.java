package net.junanw.upms.system.iam.navigation.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import net.junanw.upms.foundation.shared.api.ApiResponse;
import net.junanw.upms.system.iam.navigation.model.request.NavigationPermissionAssignRequest;
import net.junanw.upms.system.iam.navigation.model.request.NavigationSaveRequest;
import net.junanw.upms.system.iam.navigation.model.view.NavigationDetailView;
import net.junanw.upms.system.iam.navigation.model.view.NavigationTreeItem;
import net.junanw.upms.system.iam.navigation.service.NavigationService;
import net.junanw.upms.system.iam.role.model.view.RolePermissionItem;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 导航管理控制器。
 *
 * <p>负责提供导航树、导航维护和导航权限分配接口。
 */
@RestController
@RequestMapping("/admin/navigation")
public class NavigationController {

    private final NavigationService navigationService;

    public NavigationController(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    /** 查询导航树。 */
    @GetMapping("/tree")
    @SaCheckPermission("system:navigation:view")
    public ApiResponse<List<NavigationTreeItem>> tree() {
        return ApiResponse.success(navigationService.tree());
    }

    /** 查询导航详情。 */
    @GetMapping("/{id}")
    @SaCheckPermission("system:navigation:view")
    public ApiResponse<NavigationDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(navigationService.detail(id));
    }

    /** 创建导航。 */
    @PostMapping
    @SaCheckPermission("system:navigation:create")
    public ApiResponse<NavigationDetailView> create(@Valid @RequestBody NavigationSaveRequest request) {
        return ApiResponse.success(navigationService.create(
                request.parentId(),
                request.name(),
                request.type(),
                request.routePath(),
                request.componentPath(),
                request.externalUrl(),
                request.icon(),
                request.sortOrder(),
                request.visible(),
                request.status()
        ));
    }

    /** 更新导航。 */
    @PutMapping("/{id}")
    @SaCheckPermission("system:navigation:update")
    public ApiResponse<NavigationDetailView> update(@PathVariable String id, @Valid @RequestBody NavigationSaveRequest request) {
        return ApiResponse.success(navigationService.update(
                id,
                request.parentId(),
                request.name(),
                request.type(),
                request.routePath(),
                request.componentPath(),
                request.externalUrl(),
                request.icon(),
                request.sortOrder(),
                request.visible(),
                request.status()
        ));
    }

    /** 删除导航。 */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:navigation:delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        navigationService.delete(id);
        return ApiResponse.success(null);
    }

    /** 查询导航已分配权限。 */
    @GetMapping("/{id}/permissions")
    @SaCheckPermission("system:navigation:access:assign")
    public ApiResponse<List<RolePermissionItem>> permissions(@PathVariable String id) {
        return ApiResponse.success(navigationService.permissions(id));
    }

    /** 分配导航权限。 */
    @PostMapping("/{id}/permissions")
    @SaCheckPermission("system:navigation:access:assign")
    public ApiResponse<Void> assignPermissions(@PathVariable String id, @Valid @RequestBody NavigationPermissionAssignRequest request) {
        navigationService.assignPermissions(id, request.permissionCodes());
        return ApiResponse.success(null);
    }
}
