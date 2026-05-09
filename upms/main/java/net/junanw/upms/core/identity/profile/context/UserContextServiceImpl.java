package net.junanw.upms.core.identity.profile.context;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.permission.PermissionAccessPolicy;
import net.junanw.upms.core.identity.profile.model.CurrentUserResponse;
import net.junanw.upms.core.identity.profile.model.NavigationItem;
import net.junanw.upms.core.identity.profile.model.UserInfoView;
import net.junanw.upms.core.identity.account.AccountNormalizer;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.core.identity.account.entity.AccountEntity;
import net.junanw.upms.core.identity.account.model.AccountType;
import net.junanw.upms.core.identity.permission.entity.PermissionEntity;
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
import net.junanw.upms.core.identity.navigation.entity.NavigationEntity;
import net.junanw.upms.core.identity.navigation.entity.NavigationPermissionEntity;
import net.junanw.upms.core.identity.navigation.mapper.NavigationPermissionMapper;
import net.junanw.upms.core.identity.navigation.mapper.NavigationMapper;
import net.junanw.upms.core.organization.entity.OrganizationMembershipEntity;
import net.junanw.upms.core.organization.entity.OrganizationUnitEntity;
import net.junanw.upms.core.organization.mapper.OrganizationMembershipMapper;
import net.junanw.upms.core.organization.mapper.OrganizationUnitMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 当前用户上下文查询服务实现。
 * <p>
 * 负责汇总账号、组织、角色、权限、导航等数据，
 * 构造成认证侧统一使用的用户快照与当前用户响应。
 */
@Service
public class UserContextServiceImpl implements UserContextService {

    /**
     * 账号 Mapper。
     */
    private final AccountMapper accountMapper;
    /**
     * 用户 Mapper。
     */
    private final UserMapper userMapper;
    /**
     * 组织成员关系 Mapper。
     */
    private final OrganizationMembershipMapper orgMembershipMapper;
    /**
     * 组织 Mapper。
     */
    private final OrganizationUnitMapper orgUnitMapper;
    /**
     * 角色绑定 Mapper。
     */
    private final RoleBindingMapper roleBindingMapper;
    /**
     * 角色 Mapper。
     */
    private final RoleMapper roleMapper;
    /**
     * 角色权限关系 Mapper。
     */
    private final RolePermissionMapper rolePermissionMapper;
    /**
     * 权限 Mapper。
     */
    private final PermissionMapper permissionMapper;
    /**
     * 导航 Mapper。
     */
    private final NavigationMapper navigationMapper;
    /**
     * 导航权限关系 Mapper。
     */
    private final NavigationPermissionMapper navigationPermissionMapper;

    public UserContextServiceImpl(
            AccountMapper accountMapper,
            UserMapper userMapper,
            OrganizationMembershipMapper orgMembershipMapper,
            OrganizationUnitMapper orgUnitMapper,
            RoleBindingMapper roleBindingMapper,
            RoleMapper roleMapper,
            RolePermissionMapper rolePermissionMapper,
            PermissionMapper permissionMapper,
            NavigationMapper navigationMapper,
            NavigationPermissionMapper navigationPermissionMapper
    ) {
        this.accountMapper = accountMapper;
        this.userMapper = userMapper;
        this.orgMembershipMapper = orgMembershipMapper;
        this.orgUnitMapper = orgUnitMapper;
        this.roleBindingMapper = roleBindingMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.navigationMapper = navigationMapper;
        this.navigationPermissionMapper = navigationPermissionMapper;
    }

    /**
     * 组装当前用户响应：基础信息、权限和可见导航。
     */
    @Override
    public CurrentUserResponse buildCurrentUser(String username) {
        // 1. 先加载完整用户快照。
        UserProfileSnapshot snapshot = requireByUsername(username);
        return new CurrentUserResponse(
                new UserInfoView(
                        String.valueOf(snapshot.userId()),
                        snapshot.username(),
                        snapshot.displayName(),
                        snapshot.mobile(),
                        snapshot.email(),
                        snapshot.employeeNo(),
                        snapshot.gender(),
                        snapshot.avatar(),
                        snapshot.avatarFileId(),
                        snapshot.orgId(),
                        snapshot.orgName(),
                        snapshot.positionName(),
                        snapshot.remark(),
                        snapshot.userStatus(),
                        snapshot.roleCodes(),
                        Boolean.TRUE.equals(snapshot.passwordResetRequired()),
                        snapshot.lastLoginTime()
                ),
                snapshot.permissionCodes(),
                buildNavigations(snapshot.permissionCodes())
        );
    }

    /**
     * 按用户名查询用户快照。
     */
    @Override
    public UserProfileSnapshot requireByUsername(String username) {
        // 用户名查询前先做归一化。
        String normalized = AccountNormalizer.normalizeUsername(username);
        AccountEntity account = QueryChain.of(accountMapper)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .eq(AccountEntity::getNormalizedIdentifier, normalized)
                .get();
        if (account == null) {
            throw new BusinessException(401, "当前登录用户不存在");
        }
        return buildSnapshot(account.getUserId(), account.getIdentifier());
    }

    /**
     * 按用户 ID 查询用户快照。
     */
    @Override
    public UserProfileSnapshot requireByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "当前登录用户不存在");
        }
        return buildSnapshot(userId, resolveUsername(userId));
    }


    /**
     * 按手机号查找绑定用户。
     */
    @Override
    public Optional<UserProfileSnapshot> findByMobile(String mobile) {
        String normalized = AccountNormalizer.normalizeMobile(mobile);
        if (normalized == null) {
            return Optional.empty();
        }
        AccountEntity account = QueryChain.of(accountMapper)
                .eq(AccountEntity::getAccountType, AccountType.MOBILE.name())
                .eq(AccountEntity::getNormalizedIdentifier, normalized)
                .get();
        return account == null ? Optional.empty() : Optional.of(buildSnapshot(account.getUserId(), resolveUsername(account.getUserId())));
    }

    /**
     * 按邮箱查找绑定用户。
     */
    @Override
    public Optional<UserProfileSnapshot> findByEmail(String email) {
        String normalized = AccountNormalizer.normalizeEmail(email);
        if (normalized == null) {
            return Optional.empty();
        }
        AccountEntity account = QueryChain.of(accountMapper)
                .eq(AccountEntity::getAccountType, AccountType.EMAIL.name())
                .eq(AccountEntity::getNormalizedIdentifier, normalized)
                .get();
        return account == null ? Optional.empty() : Optional.of(buildSnapshot(account.getUserId(), resolveUsername(account.getUserId())));
    }

    /**
     * 聚合账号、组织、角色、权限等信息，构造成统一用户快照。
     */
    private UserProfileSnapshot buildSnapshot(Long userId, String fallbackUsername) {
        // 1. 读取用户主记录。
        UserEntity user = Optional.ofNullable(userMapper.getById(userId))
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        // 2. 读取账号信息并按类型建立索引。
        List<AccountEntity> accounts = QueryChain.of(accountMapper)
                .eq(AccountEntity::getUserId, userId)
                .list();
        Map<String, AccountEntity> accountMap = accounts.stream()
                .collect(Collectors.toMap(AccountEntity::getAccountType, item -> item, (left, right) -> left));
        // 3. 读取主组织与岗位信息。
        OrganizationMembershipEntity membership = QueryChain.of(orgMembershipMapper)
                .eq(OrganizationMembershipEntity::getUserId, userId)
                .eq(OrganizationMembershipEntity::getPrimaryOrg, true)
                .eq(OrganizationMembershipEntity::getStatus, "ACTIVE")
                .eq(OrganizationMembershipEntity::getDeleted, false)
                .get();

        OrganizationUnitEntity org = null;
        if (membership != null) {
            org = QueryChain.of(orgUnitMapper)
                    .eq(OrganizationUnitEntity::getId, membership.getOrgId())
                    .eq(OrganizationUnitEntity::getDeleted, false)
                    .get();
        }
        // 4. 读取有效角色绑定，并推导角色编码列表。
        List<RoleBindingEntity> roleBindings = QueryChain.of(roleBindingMapper)
                .eq(RoleBindingEntity::getSubjectType, "USER")
                .eq(RoleBindingEntity::getSubjectId, userId)
                .list().stream()
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .toList();
        List<Long> roleIds = roleBindings.stream().map(RoleBindingEntity::getRoleId).distinct().toList();
        Map<Long, RoleEntity> roleMap = roleIds.isEmpty() ? Map.of() : QueryChain.of(roleMapper)
                .in(RoleEntity::getId, roleIds)
                .list().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toMap(RoleEntity::getId, item -> item, (left, right) -> left));
        List<String> roleCodes = roleBindings.stream()
                .map(item -> roleMap.get(item.getRoleId()))
                .filter(Objects::nonNull)
                .map(RoleEntity::getRoleCode)
                .distinct()
                .sorted()
                .toList();
        List<Long> activeRoleIds = roleBindings.stream()
                .map(RoleBindingEntity::getRoleId)
                .filter(roleMap::containsKey)
                .distinct()
                .toList();
        // 5. 基于有效角色解析权限编码列表。
        List<RolePermissionEntity> rolePermissions = activeRoleIds.isEmpty()
                ? List.of()
                : QueryChain.of(rolePermissionMapper)
                        .in(RolePermissionEntity::getRoleId, activeRoleIds)
                        .list();
        List<Long> permissionIds = rolePermissions.stream().map(RolePermissionEntity::getPermissionId).distinct().toList();
        Map<Long, PermissionEntity> permissionMap = permissionIds.isEmpty()
                ? Map.of()
                : QueryChain.of(permissionMapper)
                        .in(PermissionEntity::getId, permissionIds)
                        .list().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toMap(PermissionEntity::getId, item -> item, (left, right) -> left));
        List<String> permissionCodes = rolePermissions.stream()
                .map(item -> permissionMap.get(item.getPermissionId()))
                .filter(Objects::nonNull)
                .map(PermissionEntity::getPermissionCode)
                .filter(code -> !PermissionAccessPolicy.isLoginOnlyPermissionCode(code))
                .distinct()
                .sorted()
                .toList();
        // 6. 合并账号、组织、角色、权限等信息为统一快照。
        String username = Optional.ofNullable(accountMap.get(AccountType.USERNAME.name())).map(AccountEntity::getIdentifier).orElse(fallbackUsername);
        String mobile = Optional.ofNullable(accountMap.get(AccountType.MOBILE.name())).map(AccountEntity::getIdentifier).orElse(null);
        String email = Optional.ofNullable(accountMap.get(AccountType.EMAIL.name())).map(AccountEntity::getIdentifier).orElse(null);
        return new UserProfileSnapshot(
                userId,
                username,
                user.getDisplayName(),
                mobile,
                email,
                user.getEmployeeNo(),
                user.getGender(),
                null,
                user.getAvatarFileId(),
                org == null ? null : String.valueOf(org.getId()),
                org == null ? null : org.getOrgName(),
                membership == null ? null : membership.getPositionName(),
                user.getRemark(),
                toNumericStatus(user.getStatus()),
                user.getPasswordResetRequired(),
                user.getLastLoginTime(),
                roleCodes,
                permissionCodes
        );
    }

    /**
     * 根据权限编码构建当前用户可见导航树。
     */
    private List<NavigationItem> buildNavigations(List<String> permissionCodes) {
        // 1. 先把权限转成集合，提高后续判断效率。
        Set<String> permissionSet = Set.copyOf(permissionCodes);
        List<NavigationEntity> entities = QueryChain.of(navigationMapper)
                .eq(NavigationEntity::getDeleted, false)
                .orderBy(true, NavigationEntity::getSortOrder)
                .orderBy(true, NavigationEntity::getUpdatedAt)
                .list().stream()
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .filter(item -> Boolean.TRUE.equals(item.getVisibleFlag()))
                .toList();
        List<Long> navigationIds = entities.stream().map(NavigationEntity::getId).toList();
        // 2. 读取导航与权限的关系映射。
        Map<Long, List<String>> navPermissionMap = buildNavigationPermissionMap(navigationIds);
        // 3. 构建父子导航关系，并按排序规则整理。
        Map<Long, List<NavigationEntity>> childrenMap = new HashMap<>();
        for (NavigationEntity entity : entities) {
            childrenMap.computeIfAbsent(entity.getParentId(), ignored -> new ArrayList<>()).add(entity);
        }
        childrenMap.values().forEach(list -> list.sort(Comparator.comparing(NavigationEntity::getSortOrder).thenComparing(NavigationEntity::getId)));
        // 4. 从根节点开始递归构建可见导航树。
        List<NavigationItem> result = new ArrayList<>();
        for (NavigationEntity root : childrenMap.getOrDefault(null, List.of())) {
            NavigationItem item = buildNavigationItem(root, childrenMap, navPermissionMap, permissionSet);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 递归构建单个导航节点及其子树。
     */
    private NavigationItem buildNavigationItem(
            NavigationEntity entity,
            Map<Long, List<NavigationEntity>> childrenMap,
            Map<Long, List<String>> navPermissionMap,
            Set<String> permissionSet
    ) {
        // 先递归构建所有可见子节点。
        List<NavigationItem> children = childrenMap.getOrDefault(entity.getId(), List.of()).stream()
                .map(child -> buildNavigationItem(child, childrenMap, navPermissionMap, permissionSet))
                .filter(Objects::nonNull)
                .toList();
        boolean permitted = isNavigationPermitted(entity, navPermissionMap.getOrDefault(entity.getId(), List.of()), permissionSet);
        if ("GROUP".equalsIgnoreCase(entity.getNavType())) {
            if (!permitted || children.isEmpty()) {
                return null;
            }
            return toNavigationItem(entity, children);
        }
        if (!permitted) {
            return null;
        }
        return toNavigationItem(entity, children);
    }

    /**
     * 判断当前导航在给定权限集合下是否可见。
     */
    private boolean isNavigationPermitted(NavigationEntity entity, List<String> requiredPermissions, Set<String> permissionSet) {
        if (PermissionAccessPolicy.isLoginOnlyNavigationCode(entity.getNavCode())) {
            return true;
        }
        if (requiredPermissions.isEmpty()) {
            return true;
        }
        return requiredPermissions.stream().anyMatch(permissionSet::contains);
    }

    /**
     * 将导航实体转换为前端消费的导航节点视图。
     */
    private NavigationItem toNavigationItem(NavigationEntity entity, List<NavigationItem> children) {
        return new NavigationItem(
                String.valueOf(entity.getId()),
                entity.getNavName(),
                entity.getNavType(),
                entity.getRoutePath(),
                entity.getComponentPath(),
                entity.getIcon(),
                entity.getSortOrder(),
                entity.getExternalUrl(),
                children
        );
    }

    /**
     * 构建导航 ID 到权限编码列表的映射。
     */
    private Map<Long, List<String>> buildNavigationPermissionMap(Collection<Long> navigationIds) {
        if (navigationIds.isEmpty()) {
            return Map.of();
        }
        List<NavigationPermissionEntity> bindings = QueryChain.of(navigationPermissionMapper)
                .in(NavigationPermissionEntity::getNavigationId, navigationIds)
                .list();
        List<Long> permissionIds = bindings.stream().map(NavigationPermissionEntity::getPermissionId).distinct().toList();
        Map<Long, String> permissionCodeMap = permissionIds.isEmpty() ? Map.of() : QueryChain.of(permissionMapper)
                .in(PermissionEntity::getId, permissionIds)
                .list().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toMap(PermissionEntity::getId, PermissionEntity::getPermissionCode, (left, right) -> left));
        Map<Long, List<String>> result = new HashMap<>();
        for (NavigationPermissionEntity binding : bindings) {
            String permissionCode = permissionCodeMap.get(binding.getPermissionId());
            if (permissionCode != null && !PermissionAccessPolicy.isLoginOnlyPermissionCode(permissionCode)) {
                result.computeIfAbsent(binding.getNavigationId(), ignored -> new ArrayList<>()).add(permissionCode);
            }
        }
        return result;
    }

    /**
     * 解析用户名账号；缺失时回退为用户 ID 字符串。
     */
    private String resolveUsername(Long userId) {
        AccountEntity account = QueryChain.of(accountMapper)
                .eq(AccountEntity::getUserId, userId)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .get();
        return account != null ? account.getIdentifier() : String.valueOf(userId);
    }

    /**
     * 将枚举式状态转换为前端使用的数值状态。
     */
    private Integer toNumericStatus(String status) {
        return "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;
    }
}
