package net.junanw.upms.core.identity.navigation.service;

import cn.xbatis.core.sql.executor.chain.DeleteChain;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.permission.PermissionAccessPolicy;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.util.ServiceSupport;
import net.junanw.upms.infrastructure.shared.id.IdGenerator;
import net.junanw.upms.core.identity.permission.entity.PermissionEntity;
import net.junanw.upms.core.identity.permission.mapper.PermissionMapper;
import net.junanw.upms.core.identity.permission.service.PermissionModuleService;
import net.junanw.upms.core.identity.navigation.model.view.NavigationDetailView;
import net.junanw.upms.core.identity.navigation.model.view.NavigationTreeItem;
import net.junanw.upms.core.identity.navigation.entity.NavigationEntity;
import net.junanw.upms.core.identity.navigation.entity.NavigationPermissionEntity;
import net.junanw.upms.core.identity.navigation.mapper.NavigationPermissionMapper;
import net.junanw.upms.core.identity.navigation.mapper.NavigationMapper;
import net.junanw.upms.core.identity.role.model.view.RolePermissionItem;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * NavigationServiceImpl 服务实现。
 *
 * <p>负责承接 Navigation 相关业务编排与规则落地。
 */
@Service
@Primary
public class NavigationServiceImpl extends ServiceSupport implements NavigationService {

    private final NavigationMapper navigationMapper;
    private final NavigationPermissionMapper navigationPermissionMapper;
    private final PermissionMapper permissionMapper;
    private final PermissionModuleService permissionModuleService;

    public NavigationServiceImpl(
            NavigationMapper navigationMapper,
            NavigationPermissionMapper navigationPermissionMapper,
            PermissionMapper permissionMapper,
            PermissionModuleService permissionModuleService
    ) {
        this.navigationMapper = navigationMapper;
        this.navigationPermissionMapper = navigationPermissionMapper;
        this.permissionMapper = permissionMapper;
        this.permissionModuleService = permissionModuleService;
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 构建导航树。
     *
     * <p>按父子关系组装导航层级，并按排序号与主键稳定排序。
     */
    public List<NavigationTreeItem> tree() {
        List<NavigationEntity> entities = QueryChain.of(navigationMapper)
                .eq(NavigationEntity::getDeleted, false)
                .orderByAsc(NavigationEntity::getSortOrder)
                .orderByAsc(NavigationEntity::getUpdatedAt)
                .list();
        Map<Long, List<NavigationEntity>> childrenMap = new HashMap<>();
        entities.forEach(item -> childrenMap.computeIfAbsent(item.getParentId(), ignored -> new ArrayList<>()).add(item));
        childrenMap.values().forEach(list -> list.sort(Comparator.comparing(NavigationEntity::getSortOrder).thenComparing(NavigationEntity::getId)));
        return childrenMap.getOrDefault(null, List.of()).stream().map(item -> toTree(item, childrenMap)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 查询导航详情。
     */
    public NavigationDetailView detail(String id) {
        return toDetail(require(parseId(id, "导航不存在")));
    }

    @Override
    @Transactional
    /**
     * 创建导航。
     *
     * <p>创建前会校验导航类型与父子层级规则，再生成导航编码并落库。
     */
    public NavigationDetailView create(String parentId, String name, String type, String routePath, String componentPath, String externalUrl, String icon, Integer sortOrder, Boolean visible, Integer status) {
        NavigationEntity parent = parentId == null || parentId.isBlank() ? null : require(parseId(parentId, "上级导航不存在"));
        validatePayload(parent, type, routePath, componentPath, externalUrl, sortOrder, visible, status);
        NavigationEntity entity = new NavigationEntity();
        entity.setDeleted(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        fill(entity, parent, name, type, routePath, componentPath, externalUrl, icon, sortOrder, visible, status);
        navigationMapper.save(entity);
        return toDetail(entity);
    }

    @Override
    @Transactional
    /**
     * 更新导航。
     *
     * <p>更新时会阻止把自己设为父节点或挂到自己的后代节点下。
     */
    public NavigationDetailView update(String id, String parentId, String name, String type, String routePath, String componentPath, String externalUrl, String icon, Integer sortOrder, Boolean visible, Integer status) {
        Long navigationId = parseId(id, "导航不存在");
        NavigationEntity entity = require(navigationId);
        NavigationEntity parent = parentId == null || parentId.isBlank() ? null : require(parseId(parentId, "上级导航不存在"));
        validatePayload(parent, type, routePath, componentPath, externalUrl, sortOrder, visible, status);
        if (parent != null && parent.getId().equals(entity.getId())) {
            throw new BusinessException(400, "上级导航不能选择自己");
        }
        if (parent != null) {
            ensureNotDescendant(parent.getId(), entity.getId());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        fill(entity, parent, name, type, routePath, componentPath, externalUrl, icon, sortOrder, visible, status);
        navigationMapper.update(entity);
        return toDetail(entity);
    }

    @Override
    @Transactional
    /**
     * 删除导航。
     *
     * <p>仅允许删除没有子导航的节点，并同步清理导航权限关联。
     */
    public void delete(String id) {
        Long navigationId = parseId(id, "导航不存在");
        NavigationEntity entity = require(navigationId);
        long childCount = QueryChain.of(navigationMapper)
                .eq(NavigationEntity::getParentId, navigationId)
                .eq(NavigationEntity::getDeleted, false)
                .count();
        if (childCount > 0) {
            throw new BusinessException(400, "存在子导航，不能删除");
        }
        navigationMapper.deleteById(entity.getId());
        DeleteChain.of(navigationPermissionMapper)
                .eq(NavigationPermissionEntity::getNavigationId, navigationId)
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 查询导航权限列表。
     */
    public List<RolePermissionItem> permissions(String id) {
        Long navigationId = parseId(id, "导航不存在");
        require(navigationId);
        Map<Long, PermissionEntity> permissionMap = permissionMapper.listAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .collect(Collectors.toMap(PermissionEntity::getId, item -> item, (left, right) -> left));
        List<PermissionEntity> permissions = QueryChain.of(navigationPermissionMapper)
                .eq(NavigationPermissionEntity::getNavigationId, navigationId)
                .list().stream()
                .map(binding -> permissionMap.get(binding.getPermissionId()))
                .filter(Objects::nonNull)
                .filter(item -> !PermissionAccessPolicy.isLoginOnlyPermissionCode(item.getPermissionCode()))
                .toList();
        Map<String, String> moduleNameMap = permissionModuleService.nameMap(
                permissions.stream().map(PermissionEntity::getModuleCode).toList()
        );
        return permissions.stream()
                .map(item -> new RolePermissionItem(
                        item.getPermissionCode(),
                        item.getPermissionName(),
                        item.getPermissionType(),
                        item.getModuleCode(),
                        moduleNameMap.getOrDefault(item.getModuleCode(), item.getModuleCode())
                ))
                .toList();
    }

    @Override
    @Transactional
    /**
     * 分配导航权限。
     *
     * <p>会以传入权限编码集合为准，先清旧再建新。
     */
    public void assignPermissions(String id, List<String> permissionCodes) {
        Long navigationId = parseId(id, "导航不存在");
        require(navigationId);
        List<String> requested = permissionCodes == null ? List.of() : permissionCodes.stream().filter(Objects::nonNull).map(String::trim).filter(code -> !code.isEmpty()).distinct().toList();
        Map<String, PermissionEntity> permissionMap = permissionMapper.listAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .collect(Collectors.toMap(PermissionEntity::getPermissionCode, item -> item, (left, right) -> left));
        requested.forEach(code -> {
            if (PermissionAccessPolicy.isLoginOnlyPermissionCode(code)) {
                throw new BusinessException(400, "登录后公共权限无需分配: " + code);
            }
            if (!permissionMap.containsKey(code)) {
                throw new BusinessException(400, "权限不存在: " + code);
            }
        });
        DeleteChain.of(navigationPermissionMapper)
                .eq(NavigationPermissionEntity::getNavigationId, navigationId)
                .execute();
        List<NavigationPermissionEntity> bindings = new ArrayList<>();
        for (String code : requested) {
            NavigationPermissionEntity entity = new NavigationPermissionEntity();
            entity.setNavigationId(navigationId);
            entity.setPermissionId(permissionMap.get(code).getId());
            entity.setCreatedAt(LocalDateTime.now());
            bindings.add(entity);
        }
        navigationPermissionMapper.save(bindings);
    }

    /**
     * 填充导航实体字段。
     */
    private void fill(NavigationEntity entity, NavigationEntity parent, String name, String type, String routePath, String componentPath, String externalUrl, String icon, Integer sortOrder, Boolean visible, Integer status) {
        String normalizedType = type == null ? null : type.trim().toUpperCase();
        entity.setParentId(parent == null ? null : parent.getId());
        entity.setNavName(name == null ? null : name.trim());
        entity.setNavCode(buildNavCode(parent, routePath, name));
        entity.setNavType(normalizedType);
        entity.setRoutePath(resolveStoredRoutePath(normalizedType, routePath));
        entity.setComponentPath(resolveStoredComponentPath(normalizedType, componentPath));
        entity.setExternalUrl(trimToNull(externalUrl));
        entity.setIcon(trimToNull(icon));
        entity.setVisibleFlag(Boolean.TRUE.equals(visible));
        entity.setSortOrder(sortOrder);
        entity.setStatus(normalizeStatus(status));
    }

    /**
     * 生成导航编码。
     */
    private String buildNavCode(NavigationEntity parent, String routePath, String name) {
        String source = trimToNull(routePath);
        if (source == null) {
            source = trimToNull(name);
        }
        if (source == null) {
            return "nav_" + IdGenerator.nextId();
        }
        String normalized = source.replace('/', '_').replace('-', '_').replaceAll("_+", "_").replaceAll("[^A-Za-z0-9_]", "");
        if (normalized.startsWith("_")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank()) {
            normalized = "nav";
        }
        return normalized.toLowerCase();
    }

    /**
     * 校验导航请求参数。
     */
    private void validatePayload(NavigationEntity parent, String type, String routePath, String componentPath, String externalUrl, Integer sortOrder, Boolean visible, Integer status) {
        String normalizedType = type == null ? "" : type.trim().toUpperCase();
        if (!List.of("GROUP", "PAGE", "LINK").contains(normalizedType)) {
            throw new BusinessException(400, "导航类型不支持");
        }
        if (sortOrder == null || sortOrder < 1) {
            throw new BusinessException(400, "排序号不能为空");
        }
        if (visible == null) {
            throw new BusinessException(400, "是否显示不能为空");
        }
        if (!Objects.equals(status, 1) && !Objects.equals(status, 0)) {
            throw new BusinessException(400, "状态只支持 0 或 1");
        }
        if (parent != null && "LINK".equalsIgnoreCase(parent.getNavType())) {
            throw new BusinessException(400, "外链节点下不能新增子节点");
        }
        if ("GROUP".equals(normalizedType)) {
            return;
        }
        if (trimToNull(routePath) == null) {
            throw new BusinessException(400, "路由路径不能为空");
        }
        if ("PAGE".equals(normalizedType) && trimToNull(componentPath) == null) {
            throw new BusinessException(400, "组件路径不能为空");
        }
        if ("LINK".equals(normalizedType) && trimToNull(externalUrl) == null) {
            throw new BusinessException(400, "外链地址不能为空");
        }
    }

    private String resolveStoredRoutePath(String type, String routePath) {
        String normalizedRoutePath = trimToNull(routePath);
        return normalizedRoutePath == null ? "" : normalizedRoutePath;
    }

    private String resolveStoredComponentPath(String type, String componentPath) {
        String normalizedComponentPath = trimToNull(componentPath);
        if (normalizedComponentPath != null) {
            return normalizedComponentPath;
        }
        return "PAGE".equals(type) ? "" : "";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * 校验父节点不是当前节点后代。
     */
    private void ensureNotDescendant(Long parentId, Long id) {
        Long current = parentId;
        while (current != null) {
            if (current.equals(id)) {
                throw new BusinessException(400, "上级导航不能选择当前节点的下级节点");
            }
            NavigationEntity entity = QueryChain.of(navigationMapper)
                    .eq(NavigationEntity::getId, current)
                    .eq(NavigationEntity::getDeleted, false)
                    .get();
            current = entity == null ? null : entity.getParentId();
        }
    }

    /**
     * 加载导航实体，不存在则抛异常。
     */
    private NavigationEntity require(Long id) {
        NavigationEntity entity = QueryChain.of(navigationMapper)
                .eq(NavigationEntity::getId, id)
                .eq(NavigationEntity::getDeleted, false)
                .get();
        if (entity == null) {
            throw new BusinessException(404, "导航不存在");
        }
        return entity;
    }

    /**
     * 转换为导航树节点。
     */
    private NavigationTreeItem toTree(NavigationEntity entity, Map<Long, List<NavigationEntity>> childrenMap) {
        return new NavigationTreeItem(
                String.valueOf(entity.getId()),
                entity.getParentId() == null ? null : String.valueOf(entity.getParentId()),
                entity.getNavName(),
                entity.getNavType(),
                blankToNull(entity.getRoutePath()),
                blankToNull(entity.getComponentPath()),
                blankToNull(entity.getExternalUrl()),
                entity.getIcon(),
                entity.getSortOrder(),
                entity.getVisibleFlag(),
                toNumericStatus(entity.getStatus()),
                childrenMap.getOrDefault(entity.getId(), List.of()).stream().map(child -> toTree(child, childrenMap)).toList()
        );
    }

    /**
     * 转换为导航详情视图。
     */
    private NavigationDetailView toDetail(NavigationEntity entity) {
        NavigationEntity parent = entity.getParentId() == null ? null : QueryChain.of(navigationMapper)
                .eq(NavigationEntity::getId, entity.getParentId())
                .eq(NavigationEntity::getDeleted, false)
                .get();
        return new NavigationDetailView(
                String.valueOf(entity.getId()),
                entity.getParentId() == null ? null : String.valueOf(entity.getParentId()),
                parent == null ? null : parent.getNavName(),
                entity.getNavName(),
                entity.getNavType(),
                blankToNull(entity.getRoutePath()),
                blankToNull(entity.getComponentPath()),
                blankToNull(entity.getExternalUrl()),
                entity.getIcon(),
                entity.getSortOrder(),
                entity.getVisibleFlag(),
                toNumericStatus(entity.getStatus())
        );
    }
}
