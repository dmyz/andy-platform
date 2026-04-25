package net.junanw.upms.system.iam.role.service;

import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.util.ServiceSupport;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import net.junanw.upms.system.iam.permission.entity.PermissionEntity;
import net.junanw.upms.system.iam.account.entity.AccountEntity;
import net.junanw.upms.system.iam.role.entity.RoleBindingEntity;
import net.junanw.upms.system.iam.role.entity.RoleEntity;
import net.junanw.upms.system.iam.role.entity.RolePermissionEntity;
import net.junanw.upms.system.iam.user.entity.UserEntity;
import net.junanw.upms.system.iam.account.mapper.AccountMapper;
import net.junanw.upms.system.iam.permission.mapper.PermissionMapper;
import net.junanw.upms.system.iam.role.mapper.RoleBindingMapper;
import net.junanw.upms.system.iam.role.mapper.RolePermissionMapper;
import net.junanw.upms.system.iam.role.mapper.RoleMapper;
import net.junanw.upms.system.iam.user.mapper.UserMapper;
import net.junanw.upms.system.organization.entity.OrganizationMembershipEntity;
import net.junanw.upms.system.organization.entity.OrganizationUnitEntity;
import net.junanw.upms.system.organization.repository.OrganizationMembershipMapper;
import net.junanw.upms.system.organization.repository.OrganizationUnitMapper;
import net.junanw.upms.system.iam.role.model.request.RolePermissionAssignRequest;
import net.junanw.upms.system.iam.role.model.request.RoleSaveRequest;
import net.junanw.upms.system.iam.role.model.request.RoleStatusUpdateRequest;
import net.junanw.upms.system.iam.role.model.view.RoleDetailView;
import net.junanw.upms.system.iam.role.model.view.RolePageItem;
import net.junanw.upms.system.iam.role.model.view.RolePermissionItem;
import net.junanw.upms.system.iam.role.model.view.RoleRelatedUserItem;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.junanw.upms.system.organization.entity.table.OrganizationMembershipEntityTableDef.ORGANIZATION_MEMBERSHIP_ENTITY;
import static net.junanw.upms.system.organization.entity.table.OrganizationUnitEntityTableDef.ORGANIZATION_UNIT_ENTITY;

/**
 * RoleServiceImpl 服务实现。
 *
 * <p>负责承接 Role 相关业务编排与规则落地。
 */
@Service
@Primary
public class RoleServiceImpl extends ServiceSupport implements RoleService {

    private final RoleMapper iamRoleMapper;
    private final RoleBindingMapper iamRoleBindingMapper;
    private final RolePermissionMapper iamRolePermissionMapper;
    private final PermissionMapper iamPermissionMapper;
    private final UserMapper iamUserMapper;
    private final AccountMapper iamAccountMapper;
    private final OrganizationMembershipMapper orgMembershipMapper;
    private final OrganizationUnitMapper orgUnitMapper;
    private final IdGenerator idGenerator;

    public RoleServiceImpl(
            RoleMapper iamRoleMapper,
            RoleBindingMapper iamRoleBindingMapper,
            RolePermissionMapper iamRolePermissionMapper,
            PermissionMapper iamPermissionMapper,
            UserMapper iamUserMapper,
            AccountMapper iamAccountMapper,
            OrganizationMembershipMapper orgMembershipMapper,
            OrganizationUnitMapper orgUnitMapper,
            IdGenerator idGenerator
    ) {
        this.iamRoleMapper = iamRoleMapper;
        this.iamRoleBindingMapper = iamRoleBindingMapper;
        this.iamRolePermissionMapper = iamRolePermissionMapper;
        this.iamPermissionMapper = iamPermissionMapper;
        this.iamUserMapper = iamUserMapper;
        this.iamAccountMapper = iamAccountMapper;
        this.orgMembershipMapper = orgMembershipMapper;
        this.orgUnitMapper = orgUnitMapper;
        this.idGenerator = idGenerator;
    }

    /**
     * 分页查询角色列表。
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<RolePageItem> page(String name, String code, Integer status, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .where("deleted = false");

        String normalizedName = normalizeKeyword(name);
        String normalizedCode = normalizeKeyword(code);
        if (!normalizedName.isBlank()) {
            query.and("LOWER(role_name) LIKE {0}", "%" + normalizedName + "%");
        }
        if (!normalizedCode.isBlank()) {
            query.and("LOWER(role_code) LIKE {0}", "%" + normalizedCode + "%");
        }
        if (status != null) {
            query.and("status = {0}", normalizeStatus(status));
        }
        query.orderBy("created_at", false);

        Map<Long, Integer> permissionCountMap = iamRolePermissionMapper.selectAll().stream()
                .collect(Collectors.groupingBy(RolePermissionEntity::getRoleId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        List<RolePageItem> items = iamRoleMapper.selectListByQuery(query).stream()
                .map(item -> new RolePageItem(String.valueOf(item.getId()), item.getRoleName(), item.getRoleCode(), item.getDataScope(), permissionCountMap.getOrDefault(item.getId(), 0), toNumericStatus(item.getStatus()), item.getRemark(), item.getCreatedAt()))
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    /**
     * 查询角色详情。
     */
    @Override
    @Transactional(readOnly = true)
    public RoleDetailView detail(String id) {
        return toDetail(require(parseId(id, "角色不存在")));
    }

    /**
     * 创建角色。
     */
    @Override
    @Transactional
    public RoleDetailView create(RoleSaveRequest request) {
        validate(request, null);
        RoleEntity entity = new RoleEntity();
        entity.setId(idGenerator.nextId());
        entity.setDeleted(false);
        fill(entity, request);
        iamRoleMapper.insert(entity);
        return toDetail(entity);
    }

    /**
     * 更新角色。
     */
    @Override
    @Transactional
    public RoleDetailView update(String id, RoleSaveRequest request) {
        Long roleId = parseId(id, "角色不存在");
        RoleEntity entity = require(roleId);
        validate(request, roleId);
        fill(entity, request);
        iamRoleMapper.update(entity);
        return toDetail(entity);
    }

    /**
     * 删除角色。
     *
     * <p>删除前会校验角色是否仍被用户绑定。
     */
    @Override
    @Transactional
    public void delete(String id) {
        Long roleId = parseId(id, "角色不存在");
        RoleEntity entity = require(roleId);
        List<RoleBindingEntity> bindings = iamRoleBindingMapper.selectListByQuery(
                QueryWrapper.create().where("role_id = {0}", roleId)
        );
        if (!bindings.isEmpty()) {
            throw new BusinessException(400, "角色已绑定用户，不能删除");
        }
        iamRolePermissionMapper.deleteByQuery(
                QueryWrapper.create().where("role_id = {0}", roleId)
        );
        entity.setDeleted(true);
        iamRoleMapper.update(entity);
    }

    /**
     * 更新角色状态。
     */
    @Override
    @Transactional
    public void updateStatus(String id, Integer status) {
        RoleEntity entity = require(parseId(id, "角色不存在"));
        entity.setStatus(normalizeStatus(status));
        iamRoleMapper.update(entity);
    }

    /**
     * 查询角色权限列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionItem> permissions(String id) {
        Long roleId = parseId(id, "角色不存在");
        require(roleId);
        Map<Long, PermissionEntity> permissionMap = iamPermissionMapper.selectAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .collect(Collectors.toMap(PermissionEntity::getId, Function.identity(), (left, right) -> left));
        return iamRolePermissionMapper.selectListByQuery(
                QueryWrapper.create().where("role_id = {0}", roleId)
        ).stream()
                .map(item -> permissionMap.get(item.getPermissionId()))
                .filter(Objects::nonNull)
                .map(permission -> new RolePermissionItem(permission.getPermissionCode(), permission.getPermissionName(), permission.getPermissionType(), permission.getModuleCode()))
                .toList();
    }

    /**
     * 分配角色权限。
     */
    @Override
    @Transactional
    public void assignPermissions(String id, List<String> permissionCodes) {
        Long roleId = parseId(id, "角色不存在");
        require(roleId);
        List<String> requested = permissionCodes == null ? List.of() : permissionCodes.stream().filter(Objects::nonNull).map(String::trim).filter(code -> !code.isEmpty()).distinct().toList();
        Map<String, PermissionEntity> permissionMap = iamPermissionMapper.selectAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .collect(Collectors.toMap(PermissionEntity::getPermissionCode, Function.identity(), (left, right) -> left));
        requested.forEach(code -> {
            if (!permissionMap.containsKey(code)) {
                throw new BusinessException(400, "权限不存在: " + code);
            }
        });
        iamRolePermissionMapper.deleteByQuery(
                QueryWrapper.create().where("role_id = {0}", roleId)
        );
        List<RolePermissionEntity> entities = new ArrayList<>();
        for (String code : requested) {
            RolePermissionEntity entity = new RolePermissionEntity();
            entity.setId(idGenerator.nextId());
            entity.setRoleId(roleId);
            entity.setPermissionId(permissionMap.get(code).getId());
            entities.add(entity);
        }
        iamRolePermissionMapper.insertBatch(entities);
    }

    /**
     * 查询角色关联用户。
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoleRelatedUserItem> relatedUsers(String id) {
        Long roleId = parseId(id, "角色不存在");
        require(roleId);
        List<RoleBindingEntity> bindings = iamRoleBindingMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("role_id = {0}", roleId)
                        .and("subject_type = {0}", "USER")
                        .and("status = {0}", "ACTIVE")
        );
        List<Long> userIds = bindings.stream().map(RoleBindingEntity::getSubjectId).distinct().toList();
        Map<Long, UserEntity> userMap = iamUserMapper.selectListByQuery(
                QueryWrapper.create().where("id IN ({0})", userIds).and("deleted = false")
        ).stream().collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));
        Map<Long, String> usernameMap = iamAccountMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("user_id IN ({0})", userIds)
                        .and("account_type = {0}", "USERNAME")
        ).stream().collect(Collectors.toMap(AccountEntity::getUserId, AccountEntity::getIdentifier, (left, right) -> left));
        Map<Long, String> orgNameMap = resolvePrimaryOrgNameMap(userIds);
        return userIds.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(user -> new RoleRelatedUserItem(String.valueOf(user.getId()), usernameMap.getOrDefault(user.getId(), ""), user.getDisplayName(), orgNameMap.get(user.getId()), toNumericStatus(user.getStatus())))
                .toList();
    }

    /**
     * 查询权限目录。
     */
    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionItem> permissionCatalog() {
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

    /**
     * 用新角色集合覆盖用户已有角色。
     */
    @Override
    @Transactional
    public void replaceUserRoles(String userId, String username, String realName, String orgName, Integer status, List<String> roleCodes) {
        Long parsedUserId = parseId(userId, "用户不存在");
        List<String> requested = roleCodes == null ? List.of() : roleCodes.stream().filter(Objects::nonNull).map(String::trim).filter(code -> !code.isEmpty()).distinct().toList();
        Map<String, RoleEntity> roleMap = iamRoleMapper.selectAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .collect(Collectors.toMap(RoleEntity::getRoleCode, Function.identity(), (left, right) -> left));
        requested.forEach(code -> {
            if (!roleMap.containsKey(code)) {
                throw new BusinessException(400, "角色不存在: " + code);
            }
        });
        iamRoleBindingMapper.deleteByQuery(
                QueryWrapper.create()
                        .where("subject_type = {0}", "USER")
                        .and("subject_id = {0}", parsedUserId)
        );
        List<RoleBindingEntity> entities = new ArrayList<>();
        for (String code : requested) {
            RoleBindingEntity entity = new RoleBindingEntity();
            entity.setId(idGenerator.nextId());
            entity.setSubjectType("USER");
            entity.setSubjectId(parsedUserId);
            entity.setRoleId(roleMap.get(code).getId());
            entity.setSourceType("MANUAL");
            entity.setStatus("ACTIVE");
            entity.setEffectiveFrom(LocalDateTime.now());
            entity.setEffectiveTo(null);
            entities.add(entity);
        }
        iamRoleBindingMapper.insertBatch(entities);
    }

    /**
     * 解析用户主角色名称。
     */
    @Override
    @Transactional(readOnly = true)
    public String resolvePrimaryRoleName(List<String> roleCodes, String fallbackName) {
        List<String> requested = roleCodes == null ? List.of() : roleCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .distinct()
                .toList();
        if (requested.isEmpty()) {
            return fallbackName;
        }
        return iamRoleMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("role_code IN ({0})", requested)
                        .and("deleted = false")
        ).stream()
                .sorted(java.util.Comparator.comparing(RoleEntity::getRoleName, java.util.Comparator.nullsLast(String::compareTo)))
                .map(RoleEntity::getRoleName)
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElse(requested.get(0));
    }

    /**
     * 校验角色编码唯一性。
     */
    private void validate(RoleSaveRequest request, Long currentId) {
        RoleEntity existing = iamRoleMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("role_code = {0}", request.code().trim())
                        .and("deleted = false")
        );
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new BusinessException(400, "角色编码已存在");
        }
    }

    /**
     * 加载角色实体，不存在则抛异常。
     */
    private RoleEntity require(Long id) {
        RoleEntity entity = iamRoleMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("id = {0}", id)
                        .and("deleted = false")
        );
        if (entity == null) {
            throw new BusinessException(404, "角色不存在");
        }
        return entity;
    }

    /**
     * 填充角色实体字段。
     */
    private void fill(RoleEntity entity, RoleSaveRequest request) {
        entity.setRoleName(request.name().trim());
        entity.setRoleCode(request.code().trim());
        entity.setRoleType("PLATFORM");
        entity.setDataScope(request.dataScope().trim());
        entity.setStatus(normalizeStatus(request.status()));
        entity.setRemark(trimToNull(request.remark()));
    }

    /**
     * 转换为角色详情视图。
     */
    private RoleDetailView toDetail(RoleEntity entity) {
        return new RoleDetailView(String.valueOf(entity.getId()), entity.getRoleName(), entity.getRoleCode(), entity.getDataScope(), toNumericStatus(entity.getStatus()), entity.getRemark(), entity.getCreatedAt());
    }

    /**
     * 批量解析用户主组织名称。
     */
    private Map<Long, String> resolvePrimaryOrgNameMap(List<Long> userIds) {
        QueryWrapper membershipQuery = QueryWrapper.create()
                .where(ORGANIZATION_MEMBERSHIP_ENTITY.USER_ID.in(userIds))
                .and(ORGANIZATION_MEMBERSHIP_ENTITY.DELETED.eq(false));
        List<OrganizationMembershipEntity> memberships = orgMembershipMapper.selectListByQuery(membershipQuery).stream()
                .filter(item -> Boolean.TRUE.equals(item.getPrimaryOrg()))
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .toList();
        Map<Long, OrganizationMembershipEntity> membershipMap = memberships.stream().collect(Collectors.toMap(OrganizationMembershipEntity::getUserId, Function.identity(), (left, right) -> left));
        List<Long> orgIds = memberships.stream().map(OrganizationMembershipEntity::getOrgId).distinct().toList();

        Map<Long, OrganizationUnitEntity> orgMap;
        if (orgIds.isEmpty()) {
            orgMap = Map.of();
        } else {
            QueryWrapper orgQuery = QueryWrapper.create()
                    .where(ORGANIZATION_UNIT_ENTITY.ID.in(orgIds))
                    .and(ORGANIZATION_UNIT_ENTITY.DELETED.eq(false));
            orgMap = orgUnitMapper.selectListByQuery(orgQuery).stream()
                    .collect(Collectors.toMap(OrganizationUnitEntity::getId, Function.identity(), (left, right) -> left));
        }

        Map<Long, String> result = new HashMap<>();
        membershipMap.forEach((userId, membership) -> {
            OrganizationUnitEntity org = orgMap.get(membership.getOrgId());
            result.put(userId, org == null ? null : org.getOrgName());
        });
        return result;
    }
}
