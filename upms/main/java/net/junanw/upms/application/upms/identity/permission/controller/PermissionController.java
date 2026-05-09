package net.junanw.upms.application.upms.identity.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import net.junanw.upms.core.identity.authentication.context.LoginUserContext;
import net.junanw.upms.infrastructure.shared.api.ApiResponse;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.identity.permission.model.request.PermissionSaveRequest;
import net.junanw.upms.core.identity.permission.model.view.PermissionDetailView;
import net.junanw.upms.core.identity.permission.model.view.PermissionPageItem;
import net.junanw.upms.core.identity.permission.service.PermissionService;
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
 * 权限管理控制器。
 *
 * <p>负责提供权限定义的查询、维护以及当前登录用户权限编码查询接口。
 */
@RestController
@RequestMapping("/admin/permission")
public class PermissionController {

    private final PermissionService permissionService;
    private final LoginUserContext loginUserContext;

    public PermissionController(PermissionService permissionService, LoginUserContext loginUserContext) {
        this.permissionService = permissionService;
        this.loginUserContext = loginUserContext;
    }

    /**
     * 分页查询权限定义。
     *
     * @param name 权限名称关键字
     * @param code 权限编码关键字
     * @param type 权限类型
     * @param status 状态筛选
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/page")
    @SaCheckPermission("system:permission:view")
    public ApiResponse<PageResponse<PermissionPageItem>> page(
            String name,
            String code,
            String type,
            Integer status,
            Integer pageNum,
            Integer pageSize
    ) {
        return ApiResponse.success(permissionService.page(
                name,
                code,
                type,
                status,
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        ));
    }

    /**
     * 查询权限详情。
     *
     * @param id 权限主键
     * @return 权限详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:permission:view")
    public ApiResponse<PermissionDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(permissionService.detail(id));
    }

    /**
     * 创建权限。
     *
     * @param request 保存请求
     * @return 新建后的权限详情
     */
    @PostMapping
    @SaCheckPermission("system:permission:create")
    public ApiResponse<PermissionDetailView> create(@Valid @RequestBody PermissionSaveRequest request) {
        return ApiResponse.success(permissionService.create(request));
    }

    /**
     * 更新权限。
     *
     * @param id 权限主键
     * @param request 保存请求
     * @return 更新后的权限详情
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:permission:update")
    public ApiResponse<PermissionDetailView> update(@PathVariable String id, @Valid @RequestBody PermissionSaveRequest request) {
        return ApiResponse.success(permissionService.update(id, request));
    }

    /**
     * 删除权限。
     *
     * @param id 权限主键
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:permission:delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        permissionService.delete(id);
        return ApiResponse.success(null);
    }

    /**
     * 获取当前登录用户的权限编码列表。
     *
     * @return 权限编码列表
     */
    @GetMapping("/current-user/codes")
    public ApiResponse<List<String>> currentUserCodes() {
        return ApiResponse.success(permissionService.currentUserCodes(loginUserContext.getLoginUsername()));
    }
}
