package net.junanw.upms.application.upms.identity.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import net.junanw.upms.core.identity.permission.model.request.PermissionModuleSaveRequest;
import net.junanw.upms.core.identity.permission.model.view.PermissionModuleDetailView;
import net.junanw.upms.core.identity.permission.model.view.PermissionModuleOptionItem;
import net.junanw.upms.core.identity.permission.model.view.PermissionModulePageItem;
import net.junanw.upms.core.identity.permission.service.PermissionModuleService;
import net.junanw.upms.infrastructure.shared.api.ApiResponse;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
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
 * 权限模块管理控制器。
 *
 * <p>负责提供权限模块目录的查询、维护和前端下拉选项接口。
 */
@RestController
@RequestMapping("/admin/permission-module")
public class PermissionModuleController {

    private final PermissionModuleService permissionModuleService;

    public PermissionModuleController(PermissionModuleService permissionModuleService) {
        this.permissionModuleService = permissionModuleService;
    }

    /**
     * 分页查询权限模块目录。
     */
    @GetMapping("/page")
    @SaCheckPermission("system:permission-module:view")
    public ApiResponse<PageResponse<PermissionModulePageItem>> page(String name, String code, Integer status, Integer pageNum, Integer pageSize) {
        return ApiResponse.success(permissionModuleService.page(
                name,
                code,
                status,
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        ));
    }

    /**
     * 查询启用中的权限模块选项。
     */
    @GetMapping("/options")
    @SaCheckPermission("system:permission-module:view")
    public ApiResponse<List<PermissionModuleOptionItem>> options() {
        return ApiResponse.success(permissionModuleService.options());
    }

    /**
     * 查询权限模块详情。
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:permission-module:view")
    public ApiResponse<PermissionModuleDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(permissionModuleService.detail(id));
    }

    /**
     * 创建权限模块。
     */
    @PostMapping
    @SaCheckPermission("system:permission-module:create")
    public ApiResponse<PermissionModuleDetailView> create(@Valid @RequestBody PermissionModuleSaveRequest request) {
        return ApiResponse.success(permissionModuleService.create(request));
    }

    /**
     * 更新权限模块。
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:permission-module:update")
    public ApiResponse<PermissionModuleDetailView> update(@PathVariable String id, @Valid @RequestBody PermissionModuleSaveRequest request) {
        return ApiResponse.success(permissionModuleService.update(id, request));
    }

    /**
     * 删除权限模块。
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:permission-module:delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        permissionModuleService.delete(id);
        return ApiResponse.success(null);
    }
}
