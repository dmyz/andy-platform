package net.junanw.upms.core.identity.permission.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.permission.PermissionAccessPolicy;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.util.ServiceSupport;
import net.junanw.upms.core.identity.permission.model.view.PermissionDetailView;
import net.junanw.upms.core.identity.permission.model.view.PermissionPageItem;
import net.junanw.upms.core.identity.permission.model.request.PermissionSaveRequest;
import net.junanw.upms.core.identity.permission.entity.PermissionEntity;
import net.junanw.upms.core.identity.permission.mapper.PermissionMapper;
import net.junanw.upms.core.identity.role.model.view.RolePermissionItem;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PermissionServiceImpl 服务实现。
 *
 * <p>负责承接 Permission 相关业务编排与规则落地。
 */
@Service
@Primary
public class PermissionServiceImpl extends ServiceSupport implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final UserContextService userContextService;
    private final PermissionModuleService permissionModuleService;

    public PermissionServiceImpl(
            PermissionMapper permissionMapper,
            UserContextService userContextService,
            PermissionModuleService permissionModuleService
    ) {
        this.permissionMapper = permissionMapper;
        this.userContextService = userContextService;
        this.permissionModuleService = permissionModuleService;
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 分页查询权限列表。
     */
    public PageResponse<PermissionPageItem> page(String name, String code, String type, Integer status, int pageNum, int pageSize) {
        QueryChain<PermissionEntity> query = QueryChain.of(permissionMapper)
                .eq(PermissionEntity::getDeleted, false);

        String normalizedName = normalizeKeyword(name);
        String normalizedCode = normalizeKeyword(code);
        String normalizedType = trimToNull(type);
        if (!normalizedName.isBlank()) {
            query.like(PermissionEntity::getPermissionName, normalizedName);
        }
        if (!normalizedCode.isBlank()) {
            query.like(PermissionEntity::getPermissionCode, normalizedCode);
        }
        if (normalizedType != null) {
            query.eq(PermissionEntity::getPermissionType, normalizedType);
        }
        if (status != null) {
            query.eq(PermissionEntity::getStatus, normalizeStatus(status));
        }
        query.orderByAsc(PermissionEntity::getModuleCode)
                .orderByAsc(PermissionEntity::getResourceType)
                .orderByAsc(PermissionEntity::getPermissionCode);

        List<PermissionEntity> entities = query.list().stream()
                .filter(item -> !PermissionAccessPolicy.isLoginOnlyPermissionCode(item.getPermissionCode()))
                .toList();
        Map<String, String> moduleNameMap = permissionModuleService.nameMap(
                entities.stream().map(PermissionEntity::getModuleCode).toList()
        );
        List<PermissionPageItem> items = entities.stream()
                .map(entity -> toPageItem(entity, moduleNameMap.getOrDefault(entity.getModuleCode(), entity.getModuleCode())))
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 查询权限详情。
     */
    public PermissionDetailView detail(String id) {
        PermissionEntity entity = require(parseId(id, "权限不存在"));
        if (PermissionAccessPolicy.isLoginOnlyPermissionCode(entity.getPermissionCode())) {
            throw new BusinessException(404, "权限不存在");
        }
        String moduleName = permissionModuleService.nameMap(List.of(entity.getModuleCode()))
                .getOrDefault(entity.getModuleCode(), entity.getModuleCode());
        return toDetail(entity, moduleName);
    }

    @Override
    @Transactional
    /**
     * 创建权限。
     */
    public PermissionDetailView create(PermissionSaveRequest request) {
        validate(request, null);
        PermissionEntity entity = new PermissionEntity();
        fill(entity, request);
        entity.setDeleted(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        permissionMapper.save(entity);
        String moduleName = permissionModuleService.nameMap(List.of(entity.getModuleCode()))
                .getOrDefault(entity.getModuleCode(), entity.getModuleCode());
        return toDetail(entity, moduleName);
    }

    @Override
    @Transactional
    /**
     * 更新权限。
     */
    public PermissionDetailView update(String id, PermissionSaveRequest request) {
        Long permissionId = parseId(id, "权限不存在");
        PermissionEntity entity = require(permissionId);
        validate(request, permissionId);
        fill(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        permissionMapper.update(entity);
        String moduleName = permissionModuleService.nameMap(List.of(entity.getModuleCode()))
                .getOrDefault(entity.getModuleCode(), entity.getModuleCode());
        return toDetail(entity, moduleName);
    }

    @Override
    @Transactional
    /**
     * 删除权限。
     */
    public void delete(String id) {
        PermissionEntity entity = require(parseId(id, "权限不存在"));
        permissionMapper.deleteById(entity.getId());
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 构建角色授权所用权限目录。
     */
    public List<RolePermissionItem> catalog() {
        List<PermissionEntity> entities = QueryChain.of(permissionMapper)
                .eq(PermissionEntity::getDeleted, false)
                .eq(PermissionEntity::getStatus, "ACTIVE")
                .orderByAsc(PermissionEntity::getModuleCode)
                .orderByAsc(PermissionEntity::getPermissionCode)
                .list().stream()
                .filter(item -> !PermissionAccessPolicy.isLoginOnlyPermissionCode(item.getPermissionCode()))
                .toList();
        Map<String, String> moduleNameMap = permissionModuleService.nameMap(
                entities.stream().map(PermissionEntity::getModuleCode).toList()
        );
        return entities.stream()
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
    @Transactional(readOnly = true)
    /**
     * 查询当前用户权限编码。
     */
    public List<String> currentUserCodes(String username) {
        return userContextService.requireByUsername(username).permissionCodes().stream()
                .filter(code -> !PermissionAccessPolicy.isLoginOnlyPermissionCode(code))
                .toList();
    }

    /**
     * 校验权限编码唯一性。
     */
    private void validate(PermissionSaveRequest request, Long currentId) {
        permissionModuleService.validateActiveModule(request.moduleCode());
        PermissionEntity existing = QueryChain.of(permissionMapper)
                .eq(PermissionEntity::getPermissionCode, request.code().trim())
                .eq(PermissionEntity::getDeleted, false)
                .get();
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new BusinessException(400, "权限编码已存在");
        }
    }

    /**
     * 填充权限实体字段。
     */
    private void fill(PermissionEntity entity, PermissionSaveRequest request) {
        entity.setPermissionName(request.name().trim());
        entity.setPermissionCode(request.code().trim());
        entity.setPermissionType(request.type().trim());
        entity.setResourceType(request.resourceType().trim());
        entity.setActionCode(trimToNull(request.actionCode()));
        entity.setModuleCode(normalizeModuleCode(request.moduleCode()));
        entity.setStatus(normalizeStatus(request.status()));
        entity.setRemark(trimToNull(request.remark()));
    }

    /**
     * 归一化权限模块编码。
     */
    private String normalizeModuleCode(String moduleCode) {
        String normalized = trimToNull(moduleCode);
        return normalized == null ? "" : normalized.toLowerCase(Locale.ROOT);
    }

    /**
     * 加载权限实体，不存在则抛异常。
     */
    private PermissionEntity require(Long id) {
        PermissionEntity entity = QueryChain.of(permissionMapper)
                .eq(PermissionEntity::getId, id)
                .eq(PermissionEntity::getDeleted, false)
                .get();
        if (entity == null) {
            throw new BusinessException(404, "权限不存在");
        }
        return entity;
    }

    /**
     * 转换为权限分页项视图。
     */
    private PermissionPageItem toPageItem(PermissionEntity entity, String moduleName) {
        return new PermissionPageItem(
                String.valueOf(entity.getId()),
                entity.getPermissionName(),
                entity.getPermissionCode(),
                entity.getPermissionType(),
                entity.getResourceType(),
                entity.getActionCode(),
                entity.getModuleCode(),
                moduleName,
                toNumericStatus(entity.getStatus()),
                entity.getRemark(),
                entity.getUpdatedAt()
        );
    }

    /**
     * 转换为权限详情视图。
     */
    private PermissionDetailView toDetail(PermissionEntity entity, String moduleName) {
        return new PermissionDetailView(
                String.valueOf(entity.getId()),
                entity.getPermissionName(),
                entity.getPermissionCode(),
                entity.getPermissionType(),
                entity.getResourceType(),
                entity.getActionCode(),
                entity.getModuleCode(),
                moduleName,
                toNumericStatus(entity.getStatus()),
                entity.getRemark(),
                entity.getUpdatedAt()
        );
    }
}
