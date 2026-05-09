package net.junanw.upms.application.upms.organization.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import net.junanw.upms.infrastructure.shared.api.ApiResponse;
import net.junanw.upms.core.organization.model.request.OrganizationSaveRequest;
import net.junanw.upms.core.organization.model.request.OrganizationStatusUpdateRequest;
import net.junanw.upms.core.organization.model.view.OrganizationDetailView;
import net.junanw.upms.core.organization.model.view.OrganizationMemberItem;
import net.junanw.upms.core.organization.model.view.OrganizationTreeNode;
import net.junanw.upms.core.organization.service.OrganizationService;
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
 * 组织管理控制器。
 *
 * <p>负责提供组织树、组织维护、状态更新与组织成员查询接口。
 */
@RestController
@RequestMapping("/admin/org")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /** 查询组织树。 */
    @GetMapping("/tree")
    @SaCheckPermission("system:org:view")
    public ApiResponse<List<OrganizationTreeNode>> tree(String keyword) {
        return ApiResponse.success(organizationService.tree(keyword));
    }

    /** 查询组织详情。 */
    @GetMapping("/{id}")
    @SaCheckPermission("system:org:view")
    public ApiResponse<OrganizationDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(organizationService.detail(id));
    }

    /** 创建组织。 */
    @PostMapping
    @SaCheckPermission("system:org:create")
    public ApiResponse<OrganizationDetailView> create(@Valid @RequestBody OrganizationSaveRequest request) {
        return ApiResponse.success(organizationService.create(request));
    }

    /** 更新组织。 */
    @PutMapping("/{id}")
    @SaCheckPermission("system:org:update")
    public ApiResponse<OrganizationDetailView> update(@PathVariable String id, @Valid @RequestBody OrganizationSaveRequest request) {
        return ApiResponse.success(organizationService.update(id, request));
    }

    /** 删除组织。 */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:org:delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        organizationService.delete(id);
        return ApiResponse.success(null);
    }

    /** 更新组织状态。 */
    @PutMapping("/{id}/status")
    @SaCheckPermission("system:org:status")
    public ApiResponse<Void> updateStatus(@PathVariable String id, @Valid @RequestBody OrganizationStatusUpdateRequest request) {
        organizationService.updateStatus(id, request.status());
        return ApiResponse.success(null);
    }

    /** 查询组织成员。 */
    @GetMapping("/{id}/members")
    @SaCheckPermission("system:org:member:view")
    public ApiResponse<List<OrganizationMemberItem>> members(@PathVariable String id, String displayName, Integer status) {
        return ApiResponse.success(organizationService.members(id, displayName, status));
    }
}
