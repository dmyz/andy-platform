package net.junanw.upms.core.identity.permission.service;

import net.junanw.upms.core.identity.permission.model.request.PermissionModuleSaveRequest;
import net.junanw.upms.core.identity.permission.model.view.PermissionModuleDetailView;
import net.junanw.upms.core.identity.permission.model.view.PermissionModuleOptionItem;
import net.junanw.upms.core.identity.permission.model.view.PermissionModulePageItem;
import net.junanw.upms.infrastructure.shared.api.PageResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 权限模块目录服务接口。
 *
 * <p>定义权限模块目录维护、选项读取和模块编码校验能力。
 */
public interface PermissionModuleService {

    /**
     * 分页查询权限模块目录。
     */
    PageResponse<PermissionModulePageItem> page(String name, String code, Integer status, int pageNum, int pageSize);

    /**
     * 查询启用中的权限模块选项。
     */
    List<PermissionModuleOptionItem> options();

    /**
     * 查询权限模块详情。
     */
    PermissionModuleDetailView detail(String id);

    /**
     * 创建权限模块。
     */
    PermissionModuleDetailView create(PermissionModuleSaveRequest request);

    /**
     * 更新权限模块。
     */
    PermissionModuleDetailView update(String id, PermissionModuleSaveRequest request);

    /**
     * 删除权限模块。
     */
    void delete(String id);

    /**
     * 校验权限模块编码存在且启用。
     */
    void validateActiveModule(String moduleCode);

    /**
     * 批量解析模块编码到模块名称的映射。
     */
    Map<String, String> nameMap(Collection<String> moduleCodes);
}
