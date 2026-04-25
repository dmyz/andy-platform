package net.junanw.upms.system.iam.navigation.service;

import net.junanw.upms.system.iam.navigation.model.view.NavigationDetailView;
import net.junanw.upms.system.iam.navigation.model.view.NavigationTreeItem;
import net.junanw.upms.system.iam.role.model.view.RolePermissionItem;

import java.util.List;

/**
 * 导航服务接口。
 *
 * <p>定义导航树读取、导航维护和导航权限分配能力。
 */
public interface NavigationService {

    /** 查询导航树。 */
    List<NavigationTreeItem> tree();

    /** 查询导航详情。 */
    NavigationDetailView detail(String id);

    /** 创建导航。 */
    NavigationDetailView create(
            String parentId,
            String name,
            String type,
            String routePath,
            String componentPath,
            String externalUrl,
            String icon,
            Integer sortOrder,
            Boolean visible,
            Integer status
    );

    /** 更新导航。 */
    NavigationDetailView update(
            String id,
            String parentId,
            String name,
            String type,
            String routePath,
            String componentPath,
            String externalUrl,
            String icon,
            Integer sortOrder,
            Boolean visible,
            Integer status
    );

    /** 删除导航。 */
    void delete(String id);

    /** 查询导航权限。 */
    List<RolePermissionItem> permissions(String id);

    /** 分配导航权限。 */
    void assignPermissions(String id, List<String> permissionCodes);
}
