package net.junanw.upms.system.iam.permission.service;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.util.ServiceSupport;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import net.junanw.upms.system.iam.permission.model.view.PermissionDetailView;
import net.junanw.upms.system.iam.permission.model.view.PermissionPageItem;
import net.junanw.upms.system.iam.permission.model.request.PermissionSaveRequest;
import net.junanw.upms.system.iam.permission.entity.PermissionEntity;
import net.junanw.upms.system.iam.permission.mapper.PermissionMapper;
import net.junanw.upms.system.iam.role.model.view.RolePermissionItem;
import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PermissionServiceImpl 服务实现。
 *
 * <p>负责承接 Permission 相关业务编排与规则落地。
 */
@Service
@Primary
public class PermissionServiceImpl extends ServiceSupport implements PermissionService {

    private final PermissionMapper iamPermissionMapper;
    private final UserContextService userContextService;
    private final IdGenerator idGenerator;

    public PermissionServiceImpl(
            PermissionMapper iamPermissionMapper,
            UserContextService userContextService,
            IdGenerator idGenerator
    ) {
        this.iamPermissionMapper = iamPermissionMapper;
        this.userContextService = userContextService;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 分页查询权限列表。
     */
    public PageResponse<PermissionPageItem> page(String name, String code, String type, Integer status, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .where("deleted = false");

        String normalizedName = normalizeKeyword(name);
        String normalizedCode = normalizeKeyword(code);
        String normalizedType = trimToNull(type);
        if (!normalizedName.isBlank()) {
            query.and("LOWER(permission_name) LIKE {0}", "%" + normalizedName + "%");
        }
        if (!normalizedCode.isBlank()) {
            query.and("LOWER(permission_code) LIKE {0}", "%" + normalizedCode + "%");
        }
        if (normalizedType != null) {
            query.and("permission_type = {0}", normalizedType);
        }
        if (status != null) {
            query.and("status = {0}", normalizeStatus(status));
        }
        query.orderBy("module_code", true)
                .orderBy("resource_type", true)
                .orderBy("permission_code", true);

        List<PermissionPageItem> items = iamPermissionMapper.selectListByQuery(query).stream()
                .map(this::toPageItem)
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 查询权限详情。
     */
    public PermissionDetailView detail(String id) {
        return toDetail(require(parseId(id, "权限不存在")));
    }

    @Override
    @Transactional
    /**
     * 创建权限。
     */
    public PermissionDetailView create(PermissionSaveRequest request) {
        validate(request, null);
        PermissionEntity entity = new PermissionEntity();
        entity.setId(idGenerator.nextId());
        fill(entity, request);
        entity.setDeleted(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        iamPermissionMapper.insert(entity);
        return toDetail(entity);
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
        iamPermissionMapper.update(entity);
        return toDetail(entity);
    }

    @Override
    @Transactional
    /**
     * 删除权限。
     */
    public void delete(String id) {
        PermissionEntity entity = require(parseId(id, "权限不存在"));
        entity.setDeleted(true);
        entity.setUpdatedAt(LocalDateTime.now());
        iamPermissionMapper.update(entity);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 构建角色授权所用权限目录。
     */
    public List<RolePermissionItem> catalog() {
        return iamPermissionMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("deleted = false")
                        .and("status = {0}", "ACTIVE")
                        .orderBy("module_code", true)
                        .orderBy("permission_code", true)
        ).stream()
                .map(item -> new RolePermissionItem(item.getPermissionCode(), item.getPermissionName(), item.getPermissionType(), item.getModuleCode()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 查询当前用户权限编码。
     */
    public List<String> currentUserCodes(String username) {
        return userContextService.requireByUsername(username).permissionCodes();
    }

    /**
     * 校验权限编码唯一性。
     */
    private void validate(PermissionSaveRequest request, Long currentId) {
        PermissionEntity existing = iamPermissionMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("permission_code = {0}", request.code().trim())
                        .and("deleted = false")
        );
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
        entity.setModuleCode(request.moduleCode().trim());
        entity.setStatus(normalizeStatus(request.status()));
        entity.setRemark(trimToNull(request.remark()));
    }

    /**
     * 加载权限实体，不存在则抛异常。
     */
    private PermissionEntity require(Long id) {
        PermissionEntity entity = iamPermissionMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("id = {0}", id)
                        .and("deleted = false")
        );
        if (entity == null) {
            throw new BusinessException(404, "权限不存在");
        }
        return entity;
    }

    /**
     * 转换为权限分页项视图。
     */
    private PermissionPageItem toPageItem(PermissionEntity entity) {
        return new PermissionPageItem(
                String.valueOf(entity.getId()),
                entity.getPermissionName(),
                entity.getPermissionCode(),
                entity.getPermissionType(),
                entity.getResourceType(),
                entity.getActionCode(),
                entity.getModuleCode(),
                toNumericStatus(entity.getStatus()),
                entity.getRemark(),
                entity.getUpdatedAt()
        );
    }

    /**
     * 转换为权限详情视图。
     */
    private PermissionDetailView toDetail(PermissionEntity entity) {
        return new PermissionDetailView(
                String.valueOf(entity.getId()),
                entity.getPermissionName(),
                entity.getPermissionCode(),
                entity.getPermissionType(),
                entity.getResourceType(),
                entity.getActionCode(),
                entity.getModuleCode(),
                toNumericStatus(entity.getStatus()),
                entity.getRemark(),
                entity.getUpdatedAt()
        );
    }
}
