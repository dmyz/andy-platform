package net.junanw.upms.core.identity.role.service;

import cn.xbatis.core.sql.executor.chain.DeleteChain;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.permission.PermissionAccessPolicy;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.util.ServiceSupport;
import net.junanw.upms.core.identity.permission.entity.PermissionEntity;
import net.junanw.upms.core.identity.permission.service.PermissionModuleService;
import net.junanw.upms.core.identity.account.entity.AccountEntity;
import net.junanw.upms.core.identity.role.entity.RoleBindingEntity;
import net.junanw.upms.core.identity.role.entity.RoleEntity;
import net.junanw.upms.core.identity.role.entity.RolePermissionEntity;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.account.mapper.AccountMapper;
import net.junanw.upms.core.identity.permission.mapper.PermissionMapper;
import net.junanw.upms.core.identity.role.mapper.RoleBindingMapper;
import net.junanw.upms.core.identity.role.mapper.RolePermissionMapper;
import net.junanw.upms.core.identity.role.mapper.RoleMapper;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
import net.junanw.upms.core.organization.entity.OrganizationMembershipEntity;
import net.junanw.upms.core.organization.entity.OrganizationUnitEntity;
import net.junanw.upms.core.organization.mapper.OrganizationMembershipMapper;
import net.junanw.upms.core.organization.mapper.OrganizationUnitMapper;
import net.junanw.upms.core.identity.role.model.request.RolePermissionAssignRequest;
import net.junanw.upms.core.identity.role.model.request.RoleSaveRequest;
import net.junanw.upms.core.identity.role.model.request.RoleStatusUpdateRequest;
import net.junanw.upms.core.identity.role.model.view.RoleDetailView;
import net.junanw.upms.core.identity.role.model.view.RolePageItem;
import net.junanw.upms.core.identity.role.model.view.RolePermissionItem;
import net.junanw.upms.core.identity.role.model.view.RoleRelatedUserItem;
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


/**
 * RoleServiceImpl 服务实现。
 *
 * <p>负责承接 Role 相关业务编排与规则落地。
 */
@Service
@Primary
public class RoleServiceImpl extends ServiceSupport implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleBindingMapper roleBindingMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final OrganizationMembershipMapper orgMembershipMapper;
    private final OrganizationUnitMapper orgUnitMapper;
    private final PermissionModuleService permissionModuleService;

    public RoleServiceImpl(
            RoleMapper roleMapper,
            RoleBindingMapper roleBindingMapper,
            RolePermissionMapper rolePermissionMapper,
            PermissionMapper permissionMapper,
            UserMapper userMapper,
            AccountMapper accountMapper,
            OrganizationMembershipMapper orgMembershipMapper,
            OrganizationUnitMapper orgUnitMapper,
            PermissionModuleService permissionModuleService
    ) {
        this.roleMapper = roleMapper;
        this.roleBindingMapper = roleBindingMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.userMapper = userMapper;
        this.accountMapper = accountMapper;
        this.orgMembershipMapper = orgMembershipMapper;
        this.orgUnitMapper = orgUnitMapper;
        this.permissionModuleService = permissionModuleService;
    }

    /**
     * 分页查询角色列表。
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<RolePageItem> page(String name, String code, Integer status, int pageNum, int pageSize) {
        QueryChain<RoleEntity> query = QueryChain.of(roleMapper)
                .eq(RoleEntity::getDeleted, false);

        String normalizedName = normalizeKeyword(name);
        String normalizedCode = normalizeKeyword(code);
        if (!normalizedName.isBlank()) {
            query.like(RoleEntity::getRoleName, normalizedName);
        }
        if (!normalizedCode.isBlank()) {
            query.like(RoleEntity::getRoleCode, normalizedCode);
        }
        if (status != null) {
            query.eq(RoleEntity::getStatus, normalizeStatus(status));
        }
        query.orderByDesc(RoleEntity::getCreatedAt);

        Map<Long, Integer> permissionCountMap = rolePermissionMapper.listAll().stream()
                .collect(Collectors.groupingBy(RolePermissionEntity::getRoleId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        List<RolePageItem> items = query.list().stream()
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
        entity.setDeleted(false);
        fill(entity, request);
        roleMapper.save(entity);
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
        roleMapper.update(entity);
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
        List<RoleBindingEntity> bindings = QueryChain.of(roleBindingMapper)
                .eq(RoleBindingEntity::getRoleId, roleId)
                .list();
        if (!bindings.isEmpty()) {
            throw new BusinessException(400, "角色已绑定用户，不能删除");
        }
        DeleteChain.of(rolePermissionMapper)
                .eq(RolePermissionEntity::getRoleId, roleId)
                .execute();
        roleMapper.deleteById(entity.getId());
    }

    /**
     * 更新角色状态。
     */
    @Override
    @Transactional
    public void updateStatus(String id, Integer status) {
        RoleEntity entity = require(parseId(id, "角色不存在"));
        entity.setStatus(normalizeStatus(status));
        roleMapper.update(entity);
    }

    /**
     * 查询角色权限列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionItem> permissions(String id) {
        Long roleId = parseId(id, "角色不存在");
        require(roleId);
        Map<Long, PermissionEntity> permissionMap = permissionMapper.listAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .collect(Collectors.toMap(PermissionEntity::getId, Function.identity(), (left, right) -> left));
        List<PermissionEntity> permissions = QueryChain.of(rolePermissionMapper)
                .eq(RolePermissionEntity::getRoleId, roleId)
                .list().stream()
                .map(item -> permissionMap.get(item.getPermissionId()))
                .filter(Objects::nonNull)
                .filter(item -> !PermissionAccessPolicy.isLoginOnlyPermissionCode(item.getPermissionCode()))
                .toList();
        Map<String, String> moduleNameMap = permissionModuleService.nameMap(
                permissions.stream().map(PermissionEntity::getModuleCode).toList()
        );
        return permissions.stream()
                .map(permission -> new RolePermissionItem(
                        permission.getPermissionCode(),
                        permission.getPermissionName(),
                        permission.getPermissionType(),
                        permission.getModuleCode(),
                        moduleNameMap.getOrDefault(permission.getModuleCode(), permission.getModuleCode())
                ))
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
        Map<String, PermissionEntity> permissionMap = permissionMapper.listAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .collect(Collectors.toMap(PermissionEntity::getPermissionCode, Function.identity(), (left, right) -> left));
        requested.forEach(code -> {
            if (PermissionAccessPolicy.isLoginOnlyPermissionCode(code)) {
                throw new BusinessException(400, "登录后公共权限无需分配: " + code);
            }
            if (!permissionMap.containsKey(code)) {
                throw new BusinessException(400, "权限不存在: " + code);
            }
        });
        DeleteChain.of(rolePermissionMapper)
                .eq(RolePermissionEntity::getRoleId, roleId)
                .execute();
        List<RolePermissionEntity> entities = new ArrayList<>();
        for (String code : requested) {
            RolePermissionEntity entity = new RolePermissionEntity();
            entity.setRoleId(roleId);
            entity.setPermissionId(permissionMap.get(code).getId());
            entities.add(entity);
        }
        rolePermissionMapper.save(entities);
    }

    /**
     * 查询角色关联用户。
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoleRelatedUserItem> relatedUsers(String id) {
        Long roleId = parseId(id, "角色不存在");
        require(roleId);
        List<RoleBindingEntity> bindings = QueryChain.of(roleBindingMapper)
                .eq(RoleBindingEntity::getRoleId, roleId)
                .eq(RoleBindingEntity::getSubjectType, "USER")
                .eq(RoleBindingEntity::getStatus, "ACTIVE")
                .list();
        List<Long> userIds = bindings.stream().map(RoleBindingEntity::getSubjectId).distinct().toList();
        if (userIds.isEmpty()) {
            return List.of();
        }
        Map<Long, UserEntity> userMap = QueryChain.of(userMapper)
                .in(UserEntity::getId, userIds)
                .eq(UserEntity::getDeleted, false)
                .list().stream().collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));
        Map<Long, String> usernameMap = QueryChain.of(accountMapper)
                .in(AccountEntity::getUserId, userIds)
                .eq(AccountEntity::getAccountType, "USERNAME")
                .list().stream().collect(Collectors.toMap(AccountEntity::getUserId, AccountEntity::getIdentifier, (left, right) -> left));
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
        List<PermissionEntity> permissions = QueryChain.of(permissionMapper)
                .eq(PermissionEntity::getDeleted, false)
                .eq(PermissionEntity::getStatus, "ACTIVE")
                .orderByAsc(PermissionEntity::getModuleCode)
                .orderByAsc(PermissionEntity::getPermissionCode)
                .list();
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

    /**
     * 用新角色集合覆盖用户已有角色。
     */
    @Override
    @Transactional
    public void replaceUserRoles(String userId, String username, String realName, String orgName, Integer status, List<String> roleCodes) {
        Long parsedUserId = parseId(userId, "用户不存在");
        List<String> requested = roleCodes == null ? List.of() : roleCodes.stream().filter(Objects::nonNull).map(String::trim).filter(code -> !code.isEmpty()).distinct().toList();
        Map<String, RoleEntity> roleMap = roleMapper.listAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .collect(Collectors.toMap(RoleEntity::getRoleCode, Function.identity(), (left, right) -> left));
        requested.forEach(code -> {
            if (!roleMap.containsKey(code)) {
                throw new BusinessException(400, "角色不存在: " + code);
            }
        });
        DeleteChain.of(roleBindingMapper)
                .eq(RoleBindingEntity::getSubjectType, "USER")
                .eq(RoleBindingEntity::getSubjectId, parsedUserId)
                .execute();
        List<RoleBindingEntity> entities = new ArrayList<>();
        for (String code : requested) {
            RoleBindingEntity entity = new RoleBindingEntity();
            entity.setSubjectType("USER");
            entity.setSubjectId(parsedUserId);
            entity.setRoleId(roleMap.get(code).getId());
            entity.setSourceType("MANUAL");
            entity.setStatus("ACTIVE");
            entity.setEffectiveFrom(LocalDateTime.now());
            entity.setEffectiveTo(null);
            entities.add(entity);
        }
        roleBindingMapper.save(entities);
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
        return QueryChain.of(roleMapper)
                .in(RoleEntity::getRoleCode, requested)
                .eq(RoleEntity::getDeleted, false)
                .list().stream()
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
        RoleEntity existing = QueryChain.of(roleMapper)
                .eq(RoleEntity::getRoleCode, request.code().trim())
                .eq(RoleEntity::getDeleted, false)
                .get();
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new BusinessException(400, "角色编码已存在");
        }
    }

    /**
     * 加载角色实体，不存在则抛异常。
     */
    private RoleEntity require(Long id) {
        RoleEntity entity = QueryChain.of(roleMapper)
                .eq(RoleEntity::getId, id)
                .eq(RoleEntity::getDeleted, false)
                .get();
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
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<OrganizationMembershipEntity> memberships = QueryChain.of(orgMembershipMapper)
                .in(OrganizationMembershipEntity::getUserId, userIds)
                .eq(OrganizationMembershipEntity::getDeleted, false)
                .list().stream()
                .filter(item -> Boolean.TRUE.equals(item.getPrimaryOrg()))
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .toList();
        Map<Long, OrganizationMembershipEntity> membershipMap = memberships.stream().collect(Collectors.toMap(OrganizationMembershipEntity::getUserId, Function.identity(), (left, right) -> left));
        List<Long> orgIds = memberships.stream().map(OrganizationMembershipEntity::getOrgId).distinct().toList();

        Map<Long, OrganizationUnitEntity> orgMap;
        if (orgIds.isEmpty()) {
            orgMap = Map.of();
        } else {
            orgMap = QueryChain.of(orgUnitMapper)
                    .in(OrganizationUnitEntity::getId, orgIds)
                    .eq(OrganizationUnitEntity::getDeleted, false)
                    .list().stream()
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
