package net.junanw.upms.core.identity.role.service;

import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.identity.role.model.request.RoleSaveRequest;
import net.junanw.upms.core.identity.role.model.view.RoleDetailView;
import net.junanw.upms.core.identity.role.model.view.RolePageItem;
import net.junanw.upms.core.identity.role.model.view.RolePermissionItem;
import net.junanw.upms.core.identity.role.model.view.RoleRelatedUserItem;

import java.util.List;

/**
 * 角色服务接口。
 *
 * <p>定义角色管理、角色授权和用户角色关系维护能力。
 */
public interface RoleService {

    PageResponse<RolePageItem> page(String name, String code, Integer status, int pageNum, int pageSize);

    RoleDetailView detail(String id);

    RoleDetailView create(RoleSaveRequest request);

    RoleDetailView update(String id, RoleSaveRequest request);

    void delete(String id);

    void updateStatus(String id, Integer status);

    List<RolePermissionItem> permissions(String id);

    void assignPermissions(String id, List<String> permissionCodes);

    List<RoleRelatedUserItem> relatedUsers(String id);

    List<RolePermissionItem> permissionCatalog();

    void replaceUserRoles(String userId, String username, String realName, String orgName, Integer status, List<String> roleCodes);

    String resolvePrimaryRoleName(List<String> roleCodes, String fallbackName);
}
