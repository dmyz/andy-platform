package net.junanw.upms.core.identity.permission.service;

import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.identity.permission.model.request.PermissionSaveRequest;
import net.junanw.upms.core.identity.permission.model.view.PermissionDetailView;
import net.junanw.upms.core.identity.permission.model.view.PermissionPageItem;
import net.junanw.upms.core.identity.role.model.view.RolePermissionItem;

import java.util.List;

/**
 * 权限服务接口。
 *
 * <p>定义权限定义管理与当前用户权限读取能力。
 */
public interface PermissionService {

    /**
     * 分页查询权限列表。
     */
    PageResponse<PermissionPageItem> page(String name, String code, String type, Integer status, int pageNum, int pageSize);

    /**
     * 查询权限详情。
     */
    PermissionDetailView detail(String id);

    /**
     * 创建权限。
     */
    PermissionDetailView create(PermissionSaveRequest request);

    /**
     * 更新权限。
     */
    PermissionDetailView update(String id, PermissionSaveRequest request);

    /**
     * 删除权限。
     */
    void delete(String id);

    /**
     * 查询当前用户权限编码集合。
     */
    List<String> currentUserCodes(String username);

    /**
     * 查询角色授权使用的权限目录。
     */
    List<RolePermissionItem> catalog();
}
