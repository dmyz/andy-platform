package net.junanw.upms.foundation.platform.auth.query.context;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.platform.auth.query.model.CurrentUserResponse;
import net.junanw.upms.foundation.platform.auth.query.model.NavigationItem;
import net.junanw.upms.foundation.platform.auth.query.model.UserInfoView;
import net.junanw.upms.foundation.platform.auth.account.AccountNormalizer;
import net.junanw.upms.foundation.platform.auth.query.model.UserProfileSnapshot;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.system.iam.account.entity.AccountEntity;
import net.junanw.upms.system.iam.account.model.AccountType;
import net.junanw.upms.system.iam.permission.entity.PermissionEntity;
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
import net.junanw.upms.system.iam.navigation.entity.NavigationEntity;
import net.junanw.upms.system.iam.navigation.entity.NavigationPermissionEntity;
import net.junanw.upms.system.iam.navigation.mapper.NavigationPermissionMapper;
import net.junanw.upms.system.iam.navigation.mapper.NavigationMapper;
import net.junanw.upms.system.organization.entity.OrganizationMembershipEntity;
import net.junanw.upms.system.organization.entity.OrganizationUnitEntity;
import net.junanw.upms.system.organization.repository.OrganizationMembershipMapper;
import net.junanw.upms.system.organization.repository.OrganizationUnitMapper;
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

import static net.junanw.upms.system.iam.account.entity.table.AccountEntityTableDef.ACCOUNT_ENTITY;
import static net.junanw.upms.system.iam.user.entity.table.UserEntityTableDef.USER_ENTITY;
import static net.junanw.upms.system.organization.entity.table.OrganizationMembershipEntityTableDef.ORGANIZATION_MEMBERSHIP_ENTITY;
import static net.junanw.upms.system.organization.entity.table.OrganizationUnitEntityTableDef.ORGANIZATION_UNIT_ENTITY;

/**
 * 当前用户上下文查询服务实现。
 * <p>
 * 负责汇总账号、组织、角色、权限、导航等数据，
 * 构造成认证侧统一使用的用户快照与当前用户响应。
 */
@Service
public class UserContextServiceImpl implements UserContextService {

    /**
     * 账号仓储。
     */
    private final AccountMapper iamAccountMapper;
    /**
     * 用户仓储。
     */
    private final UserMapper iamUserMapper;
    /**
     * 组织成员关系 Mapper。
     */
    private final OrganizationMembershipMapper orgMembershipMapper;
    /**
     * 组织 Mapper。
     */
    private final OrganizationUnitMapper orgUnitMapper;
    /**
     * 角色绑定仓储。
     */
    private final RoleBindingMapper iamRoleBindingMapper;
    /**
     * 角色仓储。
     */
    private final RoleMapper iamRoleMapper;
    /**
     * 角色权限关系仓储。
     */
    private final RolePermissionMapper iamRolePermissionMapper;
    /**
     * 权限仓储。
     */
    private final PermissionMapper iamPermissionMapper;
    /**
     * 导航仓储。
     */
    private final NavigationMapper uiNavigationMapper;
    /**
     * 导航权限关系仓储。
     */
    private final NavigationPermissionMapper uiNavigationPermissionMapper;

    public UserContextServiceImpl(
            AccountMapper iamAccountMapper,
            UserMapper iamUserMapper,
            OrganizationMembershipMapper orgMembershipMapper,
            OrganizationUnitMapper orgUnitMapper,
            RoleBindingMapper iamRoleBindingMapper,
            RoleMapper iamRoleMapper,
            RolePermissionMapper iamRolePermissionMapper,
            PermissionMapper iamPermissionMapper,
            NavigationMapper uiNavigationMapper,
            NavigationPermissionMapper uiNavigationPermissionMapper
    ) {
        this.iamAccountMapper = iamAccountMapper;
        this.iamUserMapper = iamUserMapper;
        this.orgMembershipMapper = orgMembershipMapper;
        this.orgUnitMapper = orgUnitMapper;
        this.iamRoleBindingMapper = iamRoleBindingMapper;
        this.iamRoleMapper = iamRoleMapper;
        this.iamRolePermissionMapper = iamRolePermissionMapper;
        this.iamPermissionMapper = iamPermissionMapper;
        this.uiNavigationMapper = uiNavigationMapper;
        this.uiNavigationPermissionMapper = uiNavigationPermissionMapper;
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
                        snapshot.avatar(),
                        snapshot.roleCodes(),
                        Boolean.TRUE.equals(snapshot.passwordResetRequired())
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
        QueryWrapper query = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.USERNAME.name()))
                .and(ACCOUNT_ENTITY.NORMALIZED_IDENTIFIER.eq(normalized));
        AccountEntity account = iamAccountMapper.selectOneByQuery(query);
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
        QueryWrapper query = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.MOBILE.name()))
                .and(ACCOUNT_ENTITY.NORMALIZED_IDENTIFIER.eq(normalized));
        AccountEntity account = iamAccountMapper.selectOneByQuery(query);
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
        QueryWrapper query = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.EMAIL.name()))
                .and(ACCOUNT_ENTITY.NORMALIZED_IDENTIFIER.eq(normalized));
        AccountEntity account = iamAccountMapper.selectOneByQuery(query);
        return account == null ? Optional.empty() : Optional.of(buildSnapshot(account.getUserId(), resolveUsername(account.getUserId())));
    }

    /**
     * 聚合账号、组织、角色、权限等信息，构造成统一用户快照。
     */
    private UserProfileSnapshot buildSnapshot(Long userId, String fallbackUsername) {
        // 1. 读取用户主记录。
        UserEntity user = Optional.ofNullable(iamUserMapper.selectOneById(userId))
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        // 2. 读取账号信息并按类型建立索引。
        List<AccountEntity> accounts = iamAccountMapper.selectListByQuery(
                QueryWrapper.create().where(ACCOUNT_ENTITY.USER_ID.eq(userId)));
        Map<String, AccountEntity> accountMap = accounts.stream()
                .collect(Collectors.toMap(AccountEntity::getAccountType, item -> item, (left, right) -> left));
        // 3. 读取主组织与岗位信息。
        QueryWrapper membershipQuery = QueryWrapper.create()
                .where(ORGANIZATION_MEMBERSHIP_ENTITY.USER_ID.eq(userId))
                .and(ORGANIZATION_MEMBERSHIP_ENTITY.PRIMARY_ORG.eq(true))
                .and(ORGANIZATION_MEMBERSHIP_ENTITY.STATUS.eq("ACTIVE"))
                .and(ORGANIZATION_MEMBERSHIP_ENTITY.DELETED.eq(false));
        OrganizationMembershipEntity membership = orgMembershipMapper.selectOneByQuery(membershipQuery);

        OrganizationUnitEntity org = null;
        if (membership != null) {
            QueryWrapper orgQuery = QueryWrapper.create()
                    .where(ORGANIZATION_UNIT_ENTITY.ID.eq(membership.getOrgId()))
                    .and(ORGANIZATION_UNIT_ENTITY.DELETED.eq(false));
            org = orgUnitMapper.selectOneByQuery(orgQuery);
        }
        // 4. 读取有效角色绑定，并推导角色编码列表。
        List<RoleBindingEntity> roleBindings = iamRoleBindingMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("subject_type = {0}", "USER")
                        .and("subject_id = {0}", userId)
        ).stream()
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .toList();
        List<Long> roleIds = roleBindings.stream().map(RoleBindingEntity::getRoleId).distinct().toList();
        Map<Long, RoleEntity> roleMap = roleIds.isEmpty() ? Map.of() : iamRoleMapper.selectListByQuery(
                QueryWrapper.create().where("id IN ({0})", roleIds)
        ).stream()
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
                : iamRolePermissionMapper.selectListByQuery(
                        QueryWrapper.create().where("role_id IN ({0})", activeRoleIds)
                );
        List<Long> permissionIds = rolePermissions.stream().map(RolePermissionEntity::getPermissionId).distinct().toList();
        Map<Long, PermissionEntity> permissionMap = permissionIds.isEmpty()
                ? Map.of()
                : iamPermissionMapper.selectListByQuery(
                        QueryWrapper.create().where("id IN ({0})", permissionIds)
                ).stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toMap(PermissionEntity::getId, item -> item, (left, right) -> left));
        List<String> permissionCodes = rolePermissions.stream()
                .map(item -> permissionMap.get(item.getPermissionId()))
                .filter(Objects::nonNull)
                .map(PermissionEntity::getPermissionCode)
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
                resolveAvatar(user.getAvatarFileId()),
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
        List<NavigationEntity> entities = uiNavigationMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("deleted = false")
                        .orderBy("sort_order", true)
                        .orderBy("updated_at", true)
        ).stream()
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
        List<NavigationPermissionEntity> bindings = uiNavigationPermissionMapper.selectListByQuery(
                QueryWrapper.create().where("navigation_id IN ({0})", navigationIds)
        );
        List<Long> permissionIds = bindings.stream().map(NavigationPermissionEntity::getPermissionId).distinct().toList();
        Map<Long, String> permissionCodeMap = permissionIds.isEmpty() ? Map.of() : iamPermissionMapper.selectListByQuery(
                QueryWrapper.create().where("id IN ({0})", permissionIds)
        ).stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toMap(PermissionEntity::getId, PermissionEntity::getPermissionCode, (left, right) -> left));
        Map<Long, List<String>> result = new HashMap<>();
        for (NavigationPermissionEntity binding : bindings) {
            String permissionCode = permissionCodeMap.get(binding.getPermissionId());
            if (permissionCode != null) {
                result.computeIfAbsent(binding.getNavigationId(), ignored -> new ArrayList<>()).add(permissionCode);
            }
        }
        return result;
    }

    /**
     * 解析用户名账号；缺失时回退为用户 ID 字符串。
     */
    private String resolveUsername(Long userId) {
        QueryWrapper query = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.USER_ID.eq(userId))
                .and(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.USERNAME.name()));
        AccountEntity account = iamAccountMapper.selectOneByQuery(query);
        return account != null ? account.getIdentifier() : String.valueOf(userId);
    }

    /**
     * 解析头像预览地址。
     */
    private String resolveAvatar(Long avatarFileId) {
        return avatarFileId == null ? null : "/admin/file/" + avatarFileId + "/preview";
    }

    /**
     * 将枚举式状态转换为前端使用的数值状态。
     */
    private Integer toNumericStatus(String status) {
        return "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;
    }
}
