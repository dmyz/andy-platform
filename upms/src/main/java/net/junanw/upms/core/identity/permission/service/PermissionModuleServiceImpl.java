package net.junanw.upms.core.identity.permission.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.permission.PermissionAccessPolicy;
import net.junanw.upms.core.identity.permission.entity.PermissionEntity;
import net.junanw.upms.core.identity.permission.entity.PermissionModuleEntity;
import net.junanw.upms.core.identity.permission.mapper.PermissionMapper;
import net.junanw.upms.core.identity.permission.mapper.PermissionModuleMapper;
import net.junanw.upms.core.identity.permission.model.request.PermissionModuleSaveRequest;
import net.junanw.upms.core.identity.permission.model.view.PermissionModuleDetailView;
import net.junanw.upms.core.identity.permission.model.view.PermissionModuleOptionItem;
import net.junanw.upms.core.identity.permission.model.view.PermissionModulePageItem;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.util.ServiceSupport;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 权限模块目录服务实现。
 *
 * <p>负责维护权限模块目录，并为权限定义保存和授权目录展示提供模块编码校验与名称解析能力。
 */
@Service
@Primary
public class PermissionModuleServiceImpl extends ServiceSupport implements PermissionModuleService {

    private final PermissionModuleMapper permissionModuleMapper;
    private final PermissionMapper permissionMapper;

    public PermissionModuleServiceImpl(
            PermissionModuleMapper permissionModuleMapper,
            PermissionMapper permissionMapper
    ) {
        this.permissionModuleMapper = permissionModuleMapper;
        this.permissionMapper = permissionMapper;
    }

    /**
     * 分页查询权限模块目录。
     *
     * @param name 名称关键字
     * @param code 编码关键字
     * @param status 状态筛选
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionModulePageItem> page(String name, String code, Integer status, int pageNum, int pageSize) {
        QueryChain<PermissionModuleEntity> query = QueryChain.of(permissionModuleMapper)
                .eq(PermissionModuleEntity::getDeleted, false);

        String normalizedName = normalizeKeyword(name);
        String normalizedCode = normalizeKeyword(code);
        if (!normalizedName.isBlank()) {
            query.like(PermissionModuleEntity::getModuleName, normalizedName);
        }
        if (!normalizedCode.isBlank()) {
            query.like(PermissionModuleEntity::getModuleCode, normalizedCode);
        }
        if (status != null) {
            query.eq(PermissionModuleEntity::getStatus, normalizeStatus(status));
        }

        query.orderByAsc(PermissionModuleEntity::getSortOrder)
                .orderByAsc(PermissionModuleEntity::getModuleCode);

        List<PermissionModulePageItem> items = query.list().stream()
                .filter(item -> !PermissionAccessPolicy.isLoginOnlyModuleCode(item.getModuleCode()))
                .map(this::toPageItem)
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    /**
     * 查询启用中的权限模块选项。
     *
     * @return 模块选项列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<PermissionModuleOptionItem> options() {
        return QueryChain.of(permissionModuleMapper)
                .eq(PermissionModuleEntity::getDeleted, false)
                .eq(PermissionModuleEntity::getStatus, "ACTIVE")
                .orderByAsc(PermissionModuleEntity::getSortOrder)
                .orderByAsc(PermissionModuleEntity::getModuleCode)
                .list().stream()
                .filter(item -> !PermissionAccessPolicy.isLoginOnlyModuleCode(item.getModuleCode()))
                .map(item -> new PermissionModuleOptionItem(item.getModuleName(), item.getModuleCode(), item.getSortOrder()))
                .toList();
    }

    /**
     * 查询权限模块详情。
     *
     * @param id 模块主键
     * @return 模块详情
     */
    @Override
    @Transactional(readOnly = true)
    public PermissionModuleDetailView detail(String id) {
        PermissionModuleEntity entity = require(parseId(id, "权限模块不存在"));
        if (PermissionAccessPolicy.isLoginOnlyModuleCode(entity.getModuleCode())) {
            throw new BusinessException(404, "权限模块不存在");
        }
        return toDetail(entity);
    }

    /**
     * 创建权限模块。
     *
     * @param request 保存请求
     * @return 新建后的模块详情
     */
    @Override
    @Transactional
    public PermissionModuleDetailView create(PermissionModuleSaveRequest request) {
        validate(request, null, null);
        PermissionModuleEntity entity = new PermissionModuleEntity();
        fill(entity, request);
        permissionModuleMapper.save(entity);
        return toDetail(entity);
    }

    /**
     * 更新权限模块。
     *
     * @param id 模块主键
     * @param request 保存请求
     * @return 更新后的模块详情
     */
    @Override
    @Transactional
    public PermissionModuleDetailView update(String id, PermissionModuleSaveRequest request) {
        Long moduleId = parseId(id, "权限模块不存在");
        PermissionModuleEntity entity = require(moduleId);
        validate(request, moduleId, entity.getModuleCode());
        fill(entity, request);
        permissionModuleMapper.update(entity);
        return toDetail(entity);
    }

    /**
     * 删除权限模块。
     *
     * @param id 模块主键
     */
    @Override
    @Transactional
    public void delete(String id) {
        PermissionModuleEntity entity = require(parseId(id, "权限模块不存在"));
        long childCount = QueryChain.of(permissionModuleMapper)
                .eq(PermissionModuleEntity::getParentCode, entity.getModuleCode())
                .eq(PermissionModuleEntity::getDeleted, false)
                .count();
        if (childCount > 0) {
            throw new BusinessException(400, "存在子权限模块，不能删除");
        }
        long permissionCount = QueryChain.of(permissionMapper)
                .eq(PermissionEntity::getModuleCode, entity.getModuleCode())
                .eq(PermissionEntity::getDeleted, false)
                .count();
        if (permissionCount > 0) {
            throw new BusinessException(400, "权限模块已被权限定义引用，不能删除");
        }
        permissionModuleMapper.deleteById(entity.getId());
    }

    /**
     * 校验权限模块编码存在且启用。
     *
     * @param moduleCode 模块编码
     */
    @Override
    @Transactional(readOnly = true)
    public void validateActiveModule(String moduleCode) {
        String normalized = normalizeCode(moduleCode);
        if (PermissionAccessPolicy.isLoginOnlyModuleCode(normalized)) {
            throw new BusinessException(400, "个人中心为登录后公共能力，无需配置权限模块");
        }
        PermissionModuleEntity entity = QueryChain.of(permissionModuleMapper)
                .eq(PermissionModuleEntity::getModuleCode, normalized)
                .eq(PermissionModuleEntity::getDeleted, false)
                .get();
        if (entity == null) {
            throw new BusinessException(400, "权限模块不存在: " + normalized);
        }
        if (!"ACTIVE".equalsIgnoreCase(entity.getStatus())) {
            throw new BusinessException(400, "权限模块已停用: " + normalized);
        }
    }

    /**
     * 批量解析模块编码到模块名称的映射。
     *
     * @param moduleCodes 模块编码集合
     * @return 模块编码到模块名称的映射
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, String> nameMap(Collection<String> moduleCodes) {
        List<String> codes = moduleCodes == null ? List.of() : moduleCodes.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeCode)
                .filter(code -> !code.isBlank())
                .distinct()
                .toList();
        if (codes.isEmpty()) {
            return Map.of();
        }
        return QueryChain.of(permissionModuleMapper)
                .in(PermissionModuleEntity::getModuleCode, codes)
                .eq(PermissionModuleEntity::getDeleted, false)
                .list().stream()
                .collect(Collectors.toMap(PermissionModuleEntity::getModuleCode, PermissionModuleEntity::getModuleName, (left, right) -> left));
    }

    /**
     * 校验保存请求。
     */
    private void validate(PermissionModuleSaveRequest request, Long currentId, String currentCode) {
        String code = normalizeCode(request.code());
        if (PermissionAccessPolicy.isLoginOnlyModuleCode(code)) {
            throw new BusinessException(400, "个人中心为登录后公共能力，无需配置权限模块");
        }
        PermissionModuleEntity existing = QueryChain.of(permissionModuleMapper)
                .eq(PermissionModuleEntity::getModuleCode, code)
                .eq(PermissionModuleEntity::getDeleted, false)
                .get();
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new BusinessException(400, "权限模块编码已存在");
        }

        String parentCode = trimToNull(request.parentCode());
        if (parentCode != null) {
            parentCode = normalizeCode(parentCode);
            if (parentCode.equals(code)) {
                throw new BusinessException(400, "父模块不能是当前模块");
            }
            PermissionModuleEntity parent = requireByCode(parentCode);
            if (currentCode != null) {
                ensureParentIsNotDescendant(parent, currentCode);
            }
        }

        if (currentId != null && currentCode != null && !currentCode.equals(code)) {
            ensureCodeCanChange(currentCode);
        }
    }

    /**
     * 确认模块编码可被修改。
     */
    private void ensureCodeCanChange(String currentCode) {
        long childCount = QueryChain.of(permissionModuleMapper)
                .eq(PermissionModuleEntity::getParentCode, currentCode)
                .eq(PermissionModuleEntity::getDeleted, false)
                .count();
        if (childCount > 0) {
            throw new BusinessException(400, "存在子权限模块，不能修改模块编码");
        }
        long permissionCount = QueryChain.of(permissionMapper)
                .eq(PermissionEntity::getModuleCode, currentCode)
                .eq(PermissionEntity::getDeleted, false)
                .count();
        if (permissionCount > 0) {
            throw new BusinessException(400, "权限模块已被权限定义引用，不能修改模块编码");
        }
    }

    /**
     * 确认父模块不是当前模块的下级模块。
     */
    private void ensureParentIsNotDescendant(PermissionModuleEntity parent, String currentCode) {
        String nextParentCode = parent.getParentCode();
        List<String> visitedCodes = new java.util.ArrayList<>();
        while (nextParentCode != null && !nextParentCode.isBlank()) {
            String normalized = normalizeCode(nextParentCode);
            if (currentCode.equals(normalized)) {
                throw new BusinessException(400, "父模块不能选择当前模块的下级模块");
            }
            if (visitedCodes.contains(normalized)) {
                throw new BusinessException(400, "权限模块父级关系存在循环");
            }
            visitedCodes.add(normalized);
            PermissionModuleEntity nextParent = QueryChain.of(permissionModuleMapper)
                    .eq(PermissionModuleEntity::getModuleCode, normalized)
                    .eq(PermissionModuleEntity::getDeleted, false)
                    .get();
            nextParentCode = nextParent == null ? null : nextParent.getParentCode();
        }
    }

    /**
     * 按主键加载权限模块。
     */
    private PermissionModuleEntity require(Long id) {
        PermissionModuleEntity entity = QueryChain.of(permissionModuleMapper)
                .eq(PermissionModuleEntity::getId, id)
                .eq(PermissionModuleEntity::getDeleted, false)
                .get();
        if (entity == null) {
            throw new BusinessException(404, "权限模块不存在");
        }
        return entity;
    }

    /**
     * 按模块编码加载权限模块。
     */
    private PermissionModuleEntity requireByCode(String moduleCode) {
        PermissionModuleEntity entity = QueryChain.of(permissionModuleMapper)
                .eq(PermissionModuleEntity::getModuleCode, moduleCode)
                .eq(PermissionModuleEntity::getDeleted, false)
                .get();
        if (entity == null) {
            throw new BusinessException(400, "父权限模块不存在: " + moduleCode);
        }
        return entity;
    }

    /**
     * 填充权限模块实体。
     */
    private void fill(PermissionModuleEntity entity, PermissionModuleSaveRequest request) {
        entity.setModuleName(request.name().trim());
        entity.setModuleCode(normalizeCode(request.code()));
        entity.setParentCode(trimToNull(request.parentCode()) == null ? null : normalizeCode(request.parentCode()));
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setStatus(normalizeStatus(request.status()));
        entity.setRemark(trimToNull(request.remark()));
    }

    /**
     * 归一化模块编码。
     */
    private String normalizeCode(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? "" : normalized.toLowerCase(Locale.ROOT);
    }

    /**
     * 转换为权限模块分页项。
     */
    private PermissionModulePageItem toPageItem(PermissionModuleEntity entity) {
        return new PermissionModulePageItem(
                String.valueOf(entity.getId()),
                entity.getModuleName(),
                entity.getModuleCode(),
                entity.getParentCode(),
                entity.getSortOrder(),
                toNumericStatus(entity.getStatus()),
                entity.getRemark(),
                entity.getUpdatedAt()
        );
    }

    /**
     * 转换为权限模块详情。
     */
    private PermissionModuleDetailView toDetail(PermissionModuleEntity entity) {
        return new PermissionModuleDetailView(
                String.valueOf(entity.getId()),
                entity.getModuleName(),
                entity.getModuleCode(),
                entity.getParentCode(),
                entity.getSortOrder(),
                toNumericStatus(entity.getStatus()),
                entity.getRemark(),
                entity.getUpdatedAt()
        );
    }
}
