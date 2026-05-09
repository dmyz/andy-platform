package net.junanw.upms.application.upms.identity.role.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import net.junanw.upms.infrastructure.shared.api.ApiResponse;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.identity.role.model.request.RolePermissionAssignRequest;
import net.junanw.upms.core.identity.role.model.request.RoleSaveRequest;
import net.junanw.upms.core.identity.role.model.request.RoleStatusUpdateRequest;
import net.junanw.upms.core.identity.role.model.view.RoleDetailView;
import net.junanw.upms.core.identity.role.model.view.RolePageItem;
import net.junanw.upms.core.identity.role.model.view.RolePermissionItem;
import net.junanw.upms.core.identity.role.model.view.RoleRelatedUserItem;
import net.junanw.upms.core.identity.role.service.RoleService;
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
 * 角色管理控制器。
 *
 * <p>负责提供角色定义、状态变更、角色权限分配以及角色关联用户查询接口。
 */
@RestController
@RequestMapping("/admin/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /** 分页查询角色列表。 */
    @GetMapping("/page")
    @SaCheckPermission("system:role:view")
    public ApiResponse<PageResponse<RolePageItem>> page(String name, String code, Integer status, Integer pageNum, Integer pageSize) {
        return ApiResponse.success(roleService.page(
                name,
                code,
                status,
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        ));
    }

    /** 查询角色详情。 */
    @GetMapping("/{id}")
    @SaCheckPermission("system:role:view")
    public ApiResponse<RoleDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(roleService.detail(id));
    }

    /** 创建角色。 */
    @PostMapping
    @SaCheckPermission("system:role:create")
    public ApiResponse<RoleDetailView> create(@Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.success(roleService.create(request));
    }

    /** 更新角色。 */
    @PutMapping("/{id}")
    @SaCheckPermission("system:role:update")
    public ApiResponse<RoleDetailView> update(@PathVariable String id, @Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.success(roleService.update(id, request));
    }

    /** 删除角色。 */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        roleService.delete(id);
        return ApiResponse.success(null);
    }

    /** 更新角色状态。 */
    @PutMapping("/{id}/status")
    @SaCheckPermission("system:role:status")
    public ApiResponse<Void> updateStatus(@PathVariable String id, @Valid @RequestBody RoleStatusUpdateRequest request) {
        roleService.updateStatus(id, request.status());
        return ApiResponse.success(null);
    }

    /** 查询角色已分配权限。 */
    @GetMapping("/{id}/permissions")
    @SaCheckPermission("system:role:permission:assign")
    public ApiResponse<List<RolePermissionItem>> permissions(@PathVariable String id) {
        return ApiResponse.success(roleService.permissions(id));
    }

    /** 分配角色权限。 */
    @PostMapping("/{id}/permissions")
    @SaCheckPermission("system:role:permission:assign")
    public ApiResponse<Void> assignPermissions(@PathVariable String id, @Valid @RequestBody RolePermissionAssignRequest request) {
        roleService.assignPermissions(id, request.permissionCodes());
        return ApiResponse.success(null);
    }

    /** 查询角色关联用户。 */
    @GetMapping("/{id}/users")
    @SaCheckPermission("system:role:user:view")
    public ApiResponse<List<RoleRelatedUserItem>> relatedUsers(@PathVariable String id) {
        return ApiResponse.success(roleService.relatedUsers(id));
    }

    /** 查询角色授权使用的权限目录。 */
    @GetMapping("/permission/catalog")
    @SaCheckPermission("system:role:permission:assign")
    public ApiResponse<List<RolePermissionItem>> permissionCatalog() {
        return ApiResponse.success(roleService.permissionCatalog());
    }
}
