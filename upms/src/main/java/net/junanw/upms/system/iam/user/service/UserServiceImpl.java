package net.junanw.upms.system.iam.user.service;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialEntity;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialMapper;
import net.junanw.upms.foundation.platform.auth.application.session.AuthOnlineSessionService;
import net.junanw.upms.foundation.platform.auth.account.AccountNormalizer;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.security.PasswordEncoder;
import net.junanw.upms.foundation.shared.util.ServiceSupport;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import net.junanw.upms.system.iam.account.entity.AccountEntity;
import net.junanw.upms.system.iam.account.model.AccountType;
import net.junanw.upms.system.iam.role.entity.RoleBindingEntity;
import net.junanw.upms.system.iam.role.entity.RoleEntity;
import net.junanw.upms.system.iam.user.entity.UserEntity;
import net.junanw.upms.system.iam.account.mapper.AccountMapper;
import net.junanw.upms.system.iam.role.mapper.RoleBindingMapper;
import net.junanw.upms.system.iam.role.mapper.RoleMapper;
import net.junanw.upms.system.iam.user.mapper.UserMapper;
import net.junanw.upms.system.organization.entity.OrganizationMembershipEntity;
import net.junanw.upms.system.organization.entity.OrganizationUnitEntity;
import net.junanw.upms.system.organization.repository.OrganizationMembershipMapper;
import net.junanw.upms.system.organization.repository.OrganizationUnitMapper;
import net.junanw.upms.system.iam.role.service.RoleService;
import net.junanw.upms.system.iam.user.model.request.UserRoleAssignRequest;
import net.junanw.upms.system.iam.user.model.request.UserSaveRequest;
import net.junanw.upms.system.iam.user.model.request.UserStatusUpdateRequest;
import net.junanw.upms.system.iam.user.model.response.UserImportResult;
import net.junanw.upms.system.iam.user.model.view.UserDetailView;
import net.junanw.upms.system.iam.user.model.view.UserPageItem;
import net.junanw.upms.system.iam.user.model.view.UserRoleItem;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.junanw.upms.system.iam.user.entity.table.UserEntityTableDef.USER_ENTITY;
import static net.junanw.upms.system.iam.account.entity.table.AccountEntityTableDef.ACCOUNT_ENTITY;
import static net.junanw.upms.system.organization.entity.table.OrganizationUnitEntityTableDef.ORGANIZATION_UNIT_ENTITY;
import static net.junanw.upms.system.organization.entity.table.OrganizationMembershipEntityTableDef.ORGANIZATION_MEMBERSHIP_ENTITY;
import static net.junanw.upms.foundation.platform.auth.application.password.credential.table.PasswordCredentialEntityTableDef.PASSWORD_CREDENTIAL_ENTITY;

/**
 * UserServiceImpl 服务实现。
 *
 * <p>负责承接 User 相关业务编排与规则落地。
 */
@Service
@Primary
public class UserServiceImpl extends ServiceSupport implements UserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final OrganizationUnitMapper orgUnitMapper;
    private final OrganizationMembershipMapper orgMembershipMapper;
    private final RoleBindingMapper iamRoleBindingMapper;
    private final RoleMapper iamRoleMapper;
    private final PasswordCredentialMapper authPasswordCredentialMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final IdGenerator idGenerator;
    private final AuthOnlineSessionService authOnlineSessionService;

    public UserServiceImpl(
            UserMapper userMapper,
            AccountMapper accountMapper,
            OrganizationUnitMapper orgUnitMapper,
            OrganizationMembershipMapper orgMembershipMapper,
            RoleBindingMapper iamRoleBindingMapper,
            RoleMapper iamRoleMapper,
            PasswordCredentialMapper authPasswordCredentialMapper,
            RoleService roleService,
            PasswordEncoder passwordEncoder,
            IdGenerator idGenerator,
            AuthOnlineSessionService authOnlineSessionService
    ) {
        this.userMapper = userMapper;
        this.accountMapper = accountMapper;
        this.orgUnitMapper = orgUnitMapper;
        this.orgMembershipMapper = orgMembershipMapper;
        this.iamRoleBindingMapper = iamRoleBindingMapper;
        this.iamRoleMapper = iamRoleMapper;
        this.authPasswordCredentialMapper = authPasswordCredentialMapper;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.idGenerator = idGenerator;
        this.authOnlineSessionService = authOnlineSessionService;
    }

    /**
     * 分页查询用户。
     *
     * <p>先按用户主表做基础筛选，再结合账号、组织上下文做二次过滤与视图组装。
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserPageItem> page(String username, String realName, String mobile, String orgName, Integer status, LocalDateTime startTime, LocalDateTime endTime, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .where(USER_ENTITY.DELETED.eq(false));

        String normalizedRealName = normalizeKeyword(realName);
        if (!normalizedRealName.isBlank()) {
            query.and(USER_ENTITY.DISPLAY_NAME.like("%" + normalizedRealName + "%"));
        }
        if (status != null) {
            query.and(USER_ENTITY.STATUS.eq(normalizeStatus(status)));
        }
        if (startTime != null) {
            query.and(USER_ENTITY.CREATED_AT.ge(startTime));
        }
        if (endTime != null) {
            query.and(USER_ENTITY.CREATED_AT.le(endTime));
        }
        query.orderBy(USER_ENTITY.CREATED_AT.desc());

        List<UserEntity> users = userMapper.selectListByQuery(query);
        UserContext context = buildUserContext(users.stream().map(UserEntity::getId).toList());
        String normalizedUsername = normalizeKeyword(username);
        String normalizedMobile = normalizeKeyword(mobile);
        String normalizedOrgName = normalizeKeyword(orgName);
        List<UserPageItem> items = users.stream()
                .map(user -> toPageItem(user, context))
                .filter(item -> normalizedUsername.isBlank() || item.username().toLowerCase().contains(normalizedUsername))
                .filter(item -> normalizedMobile.isBlank() || (item.mobile() != null && item.mobile().toLowerCase().contains(normalizedMobile)))
                .filter(item -> normalizedOrgName.isBlank() || (item.orgName() != null && item.orgName().toLowerCase().contains(normalizedOrgName)))
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    /**
     * 查询用户详情。
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetailView detail(String id) {
        Long userId = parseId(id, "用户不存在");
        UserEntity user = requireUser(userId);
        return toDetail(user, buildUserContext(List.of(userId)));
    }

    /**
     * 创建用户。
     *
     * <p>会同步创建账号、主组织关系和密码凭证。
     */
    @Override
    @Transactional
    public UserDetailView create(UserSaveRequest request) {
        validateRequest(request, null, null);
        OrganizationUnitEntity org = requireOrg(parseId(request.orgId(), "组织不存在"));
        UserEntity user = new UserEntity();
        long userId = idGenerator.nextId();
        user.setId(userId);
        user.setUserCode(request.username().trim().toUpperCase(Locale.ROOT));
        user.setDisplayName(request.realName().trim());
        user.setGender(defaultGender(request.gender()));
        user.setEmployeeNo(trimToNull(request.jobNumber()));
        user.setAvatarFileId(null);
        user.setUserType("LOCAL");
        user.setSourceType("LOCAL");
        user.setStatus(normalizeStatus(request.status()));
        user.setPasswordResetRequired(request.password() == null || request.password().isBlank());
        user.setLastLoginTime(null);
        user.setLastLoginIp(null);
        user.setRemark(trimToNull(request.remark()));
        user.setDeleted(false);
        userMapper.insert(user);
        saveOrUpdateAccounts(userId, null, request.username(), request.mobile(), request.email(), normalizeStatus(request.status()));
        savePrimaryMembership(userId, org.getId(), request.position(), normalizeStatus(request.status()));
        savePasswordCredential(userId, defaultPassword(request.password()), true);
        return toDetail(user, buildUserContext(List.of(userId)));
    }

    /**
     * 更新用户。
     *
     * <p>若用户名变化，会强制该用户下线以刷新登录态。
     */
    @Override
    @Transactional
    public UserDetailView update(String id, UserSaveRequest request) {
        Long userId = parseId(id, "用户不存在");
        UserEntity user = requireUser(userId);
        String originalUsername = usernameOf(userId);
        validateRequest(request, userId, originalUsername);
        if ("admin".equalsIgnoreCase(originalUsername) && !AccountNormalizer.normalizeUsername(request.username()).equals(AccountNormalizer.normalizeUsername(originalUsername))) {
            throw new BusinessException(400, "默认管理员用户名不允许修改");
        }
        OrganizationUnitEntity org = requireOrg(parseId(request.orgId(), "组织不存在"));
        user.setUserCode(request.username().trim().toUpperCase(Locale.ROOT));
        user.setDisplayName(request.realName().trim());
        user.setGender(defaultGender(request.gender()));
        user.setEmployeeNo(trimToNull(request.jobNumber()));
        user.setStatus(normalizeStatus(request.status()));
        user.setRemark(trimToNull(request.remark()));
        userMapper.update(user);
        saveOrUpdateAccounts(userId, originalUsername, request.username(), request.mobile(), request.email(), normalizeStatus(request.status()));
        savePrimaryMembership(userId, org.getId(), request.position(), normalizeStatus(request.status()));
        if (request.password() != null && !request.password().isBlank()) {
            savePasswordCredential(userId, request.password(), false);
        }
        if (!AccountNormalizer.normalizeUsername(originalUsername).equals(AccountNormalizer.normalizeUsername(request.username()))) {
            offlineInternal(userId);
        }
        if (request.status() == 0) {
            offlineInternal(userId);
        }
        return toDetail(user, buildUserContext(List.of(userId)));
    }

    /**
     * 删除用户。
     */
    @Override
    @Transactional
    public void delete(String id) {
        Long userId = parseId(id, "用户不存在");
        UserEntity user = requireUser(userId);
        String username = usernameOf(userId);
        if ("admin".equalsIgnoreCase(username)) {
            throw new BusinessException(400, "默认管理员不可删除");
        }
        user.setDeleted(true);
        user.setStatus("INACTIVE");
        userMapper.update(user);

        QueryWrapper accountQuery = QueryWrapper.create().where(ACCOUNT_ENTITY.USER_ID.eq(userId));
        List<AccountEntity> accounts = accountMapper.selectListByQuery(accountQuery);
        accounts.forEach(account -> {
            account.setIsLoginEnabled(false);
            account.setStatus("INACTIVE");
            accountMapper.update(account);
        });

        QueryWrapper membershipQuery = QueryWrapper.create()
                .where(ORGANIZATION_MEMBERSHIP_ENTITY.USER_ID.eq(userId))
                .and(ORGANIZATION_MEMBERSHIP_ENTITY.DELETED.eq(false));
        List<OrganizationMembershipEntity> memberships = orgMembershipMapper.selectListByQuery(membershipQuery);
        memberships.forEach(membership -> {
            membership.setStatus("INACTIVE");
            membership.setLeftAt(LocalDateTime.now());
            orgMembershipMapper.update(membership);
        });
        roleService.replaceUserRoles(String.valueOf(userId), username, user.getDisplayName(), null, 0, List.of());
        offlineInternal(userId);
    }

    /**
     * 更新用户状态。
     */
    @Override
    @Transactional
    public void updateStatus(String id, Integer status) {
        Long userId = parseId(id, "用户不存在");
        UserEntity user = requireUser(userId);
        String username = usernameOf(userId);
        if ("admin".equalsIgnoreCase(username) && status != null && status == 0) {
            throw new BusinessException(400, "默认管理员不可禁用");
        }
        user.setStatus(normalizeStatus(status));
        userMapper.update(user);

        QueryWrapper accountQuery = QueryWrapper.create().where(ACCOUNT_ENTITY.USER_ID.eq(userId));
        List<AccountEntity> accounts = accountMapper.selectListByQuery(accountQuery);
        accounts.forEach(account -> {
            account.setStatus(normalizeStatus(status));
            account.setIsLoginEnabled(status != null && status == 1);
            accountMapper.update(account);
        });

        savePrimaryMembership(userId, primaryMembership(userId).getOrgId(), primaryMembership(userId).getPositionName(), normalizeStatus(status));
        if (status != null && status == 0) {
            offlineInternal(userId);
        }
    }

    /**
     * 重置用户密码。
     */
    @Override
    @Transactional
    public void resetPassword(String id) {
        Long userId = parseId(id, "用户不存在");
        UserEntity user = requireUser(userId);
        user.setPasswordResetRequired(true);
        userMapper.update(user);
        savePasswordCredential(userId, DEFAULT_PASSWORD, true);
        offlineInternal(userId);
    }

    /**
     * 强制用户下线。
     */
    @Override
    @Transactional
    public void offline(String id) {
        Long userId = parseId(id, "用户不存在");
        requireUser(userId);
        offlineInternal(userId);
    }

    /**
     * 查询用户角色列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserRoleItem> roles(String id) {
        Long userId = parseId(id, "用户不存在");
        requireUser(userId);
        Map<String, String> roleCatalog = iamRoleMapper.selectAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .collect(Collectors.toMap(RoleEntity::getRoleCode, RoleEntity::getRoleName, (left, right) -> left));
        return roleCodes(userId).stream()
                .map(code -> new UserRoleItem(code, roleCatalog.getOrDefault(code, code)))
                .toList();
    }

    /**
     * 分配用户角色。
     */
    @Override
    @Transactional
    public void assignRoles(String id, List<String> roleCodes) {
        Long userId = parseId(id, "用户不存在");
        UserEntity user = requireUser(userId);
        String username = usernameOf(userId);
        List<String> requested = roleCodes == null ? List.of() : roleCodes.stream().filter(Objects::nonNull).map(String::trim).filter(code -> !code.isEmpty()).distinct().toList();
        if (requested.isEmpty()) {
            throw new BusinessException(400, "至少分配一个角色");
        }
        Map<String, RoleEntity> roleCatalog = iamRoleMapper.selectAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toMap(RoleEntity::getRoleCode, Function.identity(), (left, right) -> left));
        requested.forEach(code -> {
            if (!roleCatalog.containsKey(code)) {
                throw new BusinessException(400, "角色不存在: " + code);
            }
        });
        if ("admin".equalsIgnoreCase(username) && requested.stream().noneMatch(code -> "admin".equalsIgnoreCase(code))) {
            throw new BusinessException(400, "默认管理员必须保留超级管理员角色");
        }
        OrganizationMembershipEntity membership = primaryMembership(userId);
        OrganizationUnitEntity org = requireOrg(membership.getOrgId());
        roleService.replaceUserRoles(String.valueOf(userId), username, user.getDisplayName(), org.getOrgName(), toNumericStatus(user.getStatus()), requested);
    }

    /**
     * 导出用户列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserPageItem> export(String username, String realName, String mobile, String orgName, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        return page(username, realName, mobile, orgName, status, startTime, endTime, 1, Integer.MAX_VALUE).list();
    }

    /**
     * 导入用户。
     *
     * <p>按 CSV 头部映射字段，逐行执行创建或更新。
     */
    @Override
    @Transactional
    public UserImportResult importUsers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "导入文件不能为空");
        }
        int importedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new BusinessException(400, "导入文件内容为空");
            }
            Map<String, Integer> headerIndexes = resolveHeaderIndexes(parseCsvLine(headerLine));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> values = parseCsvLine(line);
                String username = readCsvValue(values, headerIndexes, "username");
                if (username == null || username.isBlank()) {
                    skippedCount++;
                    continue;
                }
                QueryWrapper accountQuery = QueryWrapper.create()
                        .where(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.USERNAME.name()))
                        .and(ACCOUNT_ENTITY.NORMALIZED_IDENTIFIER.eq(AccountNormalizer.normalizeUsername(username)));
                AccountEntity existingAccount = accountMapper.selectOneByQuery(accountQuery);
                String orgId = resolveImportedOrgId(values, headerIndexes);
                UserSaveRequest request = new UserSaveRequest(
                        requiredImportedValue(values, headerIndexes, "username", "用户名不能为空"),
                        requiredImportedValue(values, headerIndexes, "realname", "真实姓名不能为空"),
                        readCsvValue(values, headerIndexes, "jobnumber"),
                        requiredImportedValue(values, headerIndexes, "mobile", "手机号不能为空"),
                        readCsvValue(values, headerIndexes, "email"),
                        readCsvValue(values, headerIndexes, "gender"),
                        orgId,
                        readCsvValue(values, headerIndexes, "position"),
                        readCsvValue(values, headerIndexes, "password"),
                        parseImportedStatus(readCsvValue(values, headerIndexes, "status")),
                        readCsvValue(values, headerIndexes, "remark")
                );
                if (existingAccount == null) {
                    UserDetailView detail = create(request);
                    List<String> importedRoles = parseImportedRoleCodes(readCsvValue(values, headerIndexes, "rolecodes"));
                    if (!importedRoles.isEmpty()) {
                        assignRoles(detail.id(), importedRoles);
                    }
                    importedCount++;
                }
                else {
                    update(String.valueOf(existingAccount.getUserId()), request);
                    List<String> importedRoles = parseImportedRoleCodes(readCsvValue(values, headerIndexes, "rolecodes"));
                    if (!importedRoles.isEmpty()) {
                        assignRoles(String.valueOf(existingAccount.getUserId()), importedRoles);
                    }
                    updatedCount++;
                }
            }
        }
        catch (IOException exception) {
            throw new BusinessException(400, "导入文件读取失败");
        }
        return new UserImportResult(importedCount, updatedCount, skippedCount);
    }

    /**
     * 校验用户保存请求。
     */
    private void validateRequest(UserSaveRequest request, Long currentUserId, String originalUsername) {
        if (request.password() != null && request.password().isBlank()) {
            throw new BusinessException(400, "密码不能为空字符串");
        }
        String normalizedUsername = AccountNormalizer.normalizeUsername(request.username());
        String normalizedMobile = AccountNormalizer.normalizeMobile(request.mobile());
        String normalizedEmail = AccountNormalizer.normalizeEmail(request.email());

        QueryWrapper usernameQuery = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.USERNAME.name()))
                .and(ACCOUNT_ENTITY.NORMALIZED_IDENTIFIER.eq(normalizedUsername));
        AccountEntity existingUsername = accountMapper.selectOneByQuery(usernameQuery);
        if (existingUsername != null && (currentUserId == null || !existingUsername.getUserId().equals(currentUserId))) {
            throw new BusinessException(400, "用户名已存在");
        }

        QueryWrapper mobileQuery = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.MOBILE.name()))
                .and(ACCOUNT_ENTITY.NORMALIZED_IDENTIFIER.eq(normalizedMobile));
        AccountEntity existingMobile = accountMapper.selectOneByQuery(mobileQuery);
        if (existingMobile != null && (currentUserId == null || !existingMobile.getUserId().equals(currentUserId))) {
            throw new BusinessException(400, "手机号已存在");
        }

        if (normalizedEmail != null) {
            QueryWrapper emailQuery = QueryWrapper.create()
                    .where(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.EMAIL.name()))
                    .and(ACCOUNT_ENTITY.NORMALIZED_IDENTIFIER.eq(normalizedEmail));
            AccountEntity existingEmail = accountMapper.selectOneByQuery(emailQuery);
            if (existingEmail != null && (currentUserId == null || !existingEmail.getUserId().equals(currentUserId))) {
                throw new BusinessException(400, "邮箱已存在");
            }
        }

        String jobNumber = trimToNull(request.jobNumber());
        if (jobNumber != null) {
            QueryWrapper query = QueryWrapper.create()
                    .where(USER_ENTITY.EMPLOYEE_NO.eq(jobNumber))
                    .and(USER_ENTITY.DELETED.eq(false));
            UserEntity existingUser = userMapper.selectOneByQuery(query);
            if (existingUser != null && (currentUserId == null || !existingUser.getId().equals(currentUserId))) {
                throw new BusinessException(400, "工号已存在");
            }
        }
        requireOrg(parseId(request.orgId(), "组织不存在"));
    }

    /**
     * 加载用户实体，不存在则抛异常。
     */
    private UserEntity requireUser(Long userId) {
        QueryWrapper query = QueryWrapper.create()
                .where(USER_ENTITY.ID.eq(userId))
                .and(USER_ENTITY.DELETED.eq(false));
        UserEntity user = userMapper.selectOneByQuery(query);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    /**
     * 加载组织实体，不存在则抛异常。
     */
    private OrganizationUnitEntity requireOrg(Long orgId) {
        QueryWrapper query = QueryWrapper.create()
                .where(ORGANIZATION_UNIT_ENTITY.ID.eq(orgId))
                .and(ORGANIZATION_UNIT_ENTITY.DELETED.eq(false));
        OrganizationUnitEntity org = orgUnitMapper.selectOneByQuery(query);
        if (org == null) {
            throw new BusinessException(404, "组织不存在");
        }
        return org;
    }

    /**
     * 获取用户主组织关系。
     */
    private OrganizationMembershipEntity primaryMembership(Long userId) {
        QueryWrapper query = QueryWrapper.create()
                .where(ORGANIZATION_MEMBERSHIP_ENTITY.USER_ID.eq(userId))
                .and(ORGANIZATION_MEMBERSHIP_ENTITY.PRIMARY_ORG.eq(true))
                .and(ORGANIZATION_MEMBERSHIP_ENTITY.DELETED.eq(false));
        OrganizationMembershipEntity membership = orgMembershipMapper.selectOneByQuery(query);
        if (membership == null) {
            throw new BusinessException(400, "用户缺少主组织关系");
        }
        return membership;
    }

    /**
     * 保存或更新用户账号标识。
     */
    private void saveOrUpdateAccounts(Long userId, String originalUsername, String username, String mobile, String email, String status) {
        upsertAccount(userId, AccountType.USERNAME, username, AccountNormalizer.normalizeUsername(username), true, true, status);
        upsertAccount(userId, AccountType.MOBILE, mobile, AccountNormalizer.normalizeMobile(mobile), false, true, status);
        if (trimToNull(email) == null) {
            QueryWrapper emailQuery = QueryWrapper.create()
                    .where(ACCOUNT_ENTITY.USER_ID.eq(userId))
                    .and(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.EMAIL.name()));
            AccountEntity emailAccount = accountMapper.selectOneByQuery(emailQuery);
            if (emailAccount != null) {
                accountMapper.deleteById(emailAccount.getId());
            }
        } else {
            upsertAccount(userId, AccountType.EMAIL, email, AccountNormalizer.normalizeEmail(email), false, true, status);
        }
    }

    /**
     * 新增或更新单个账号记录。
     */
    private void upsertAccount(Long userId, AccountType accountType, String identifier, String normalizedIdentifier, boolean primary, boolean verified, String status) {
        QueryWrapper query = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.USER_ID.eq(userId))
                .and(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(accountType.name()));
        AccountEntity account = accountMapper.selectOneByQuery(query);
        if (account == null) {
            account = new AccountEntity();
            account.setId(idGenerator.nextId());
            account.setUserId(userId);
            account.setAccountType(accountType.name());
        }
        account.setIdentifier(identifier == null ? null : identifier.trim());
        account.setNormalizedIdentifier(normalizedIdentifier);
        account.setIsLoginEnabled("ACTIVE".equalsIgnoreCase(status));
        account.setIsPrimary(primary);
        account.setVerifiedFlag(verified);
        account.setStatus(status);
        if (account.getCreatedAt() == null) {
            accountMapper.insert(account);
        } else {
            accountMapper.update(account);
        }
    }

    /**
     * 保存或更新用户主组织关系。
     */
    private void savePrimaryMembership(Long userId, Long orgId, String positionName, String status) {
        QueryWrapper query = QueryWrapper.create()
                .where(ORGANIZATION_MEMBERSHIP_ENTITY.USER_ID.eq(userId))
                .and(ORGANIZATION_MEMBERSHIP_ENTITY.PRIMARY_ORG.eq(true))
                .and(ORGANIZATION_MEMBERSHIP_ENTITY.DELETED.eq(false));
        OrganizationMembershipEntity membership = orgMembershipMapper.selectOneByQuery(query);
        if (membership == null) {
            membership = new OrganizationMembershipEntity();
            membership.setId(idGenerator.nextId());
            membership.setUserId(userId);
            membership.setJoinedAt(LocalDateTime.now());
            membership.setPrimaryOrg(true);
        }
        membership.setOrgId(orgId);
        membership.setPositionName(trimToNull(positionName));
        membership.setStatus(status);
        membership.setLeftAt("ACTIVE".equalsIgnoreCase(status) ? null : LocalDateTime.now());
        if (membership.getCreatedAt() == null) {
            orgMembershipMapper.insert(membership);
        } else {
            orgMembershipMapper.update(membership);
        }
    }

    /**
     * 保存密码凭证并同步用户重置标记。
     */
    private void savePasswordCredential(Long userId, String password, boolean passwordResetRequired) {
        QueryWrapper query = QueryWrapper.create()
                .where(PASSWORD_CREDENTIAL_ENTITY.USER_ID.eq(userId));
        PasswordCredentialEntity credential = authPasswordCredentialMapper.selectOneByQuery(query);
        if (credential == null) {
            credential = new PasswordCredentialEntity();
            credential.setId(idGenerator.nextId());
            credential.setUserId(userId);
        }
        credential.setPasswordHash(passwordEncoder.encode(password));
        credential.setPasswordAlgo("BCRYPT");
        credential.setPasswordChangedTime(LocalDateTime.now());
        credential.setTemporaryFlag(passwordResetRequired);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        if (credential.getCreatedAt() == null) {
            authPasswordCredentialMapper.insert(credential);
        } else {
            authPasswordCredentialMapper.update(credential);
        }
        UserEntity user = requireUser(userId);
        user.setPasswordResetRequired(passwordResetRequired);
        userMapper.update(user);
    }

    /**
     * 解析默认密码。
     */
    private String defaultPassword(String requestPassword) {
        String password = trimToNull(requestPassword);
        return password == null ? DEFAULT_PASSWORD : password;
    }

    /**
     * 解析默认性别。
     */
    private String defaultGender(String gender) {
        String normalized = trimToNull(gender);
        return normalized == null ? "UNKNOWN" : normalized;
    }

    /**
     * 执行用户下线。
     */
    private void offlineInternal(Long userId) {
        authOnlineSessionService.offlineByUserId(userId, "USER_OFFLINE");
    }

    /**
     * 获取用户用户名。
     */
    private String usernameOf(Long userId) {
        QueryWrapper query = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.USER_ID.eq(userId))
                .and(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.USERNAME.name()));
        AccountEntity account = accountMapper.selectOneByQuery(query);
        if (account == null) {
            throw new BusinessException(404, "登录账号不存在");
        }
        return account.getIdentifier();
    }

    /**
     * 查询用户角色编码列表。
     */
    private List<String> roleCodes(Long userId) {
        Map<Long, RoleEntity> roleMap = iamRoleMapper.selectAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .collect(Collectors.toMap(RoleEntity::getId, Function.identity(), (left, right) -> left));
        return iamRoleBindingMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("subject_type = {0}", "USER")
                        .and("subject_id = {0}", userId)
        ).stream()
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .map(RoleBindingEntity::getRoleId)
                .map(roleMap::get)
                .filter(Objects::nonNull)
                .map(RoleEntity::getRoleCode)
                .distinct()
                .toList();
    }

    /**
     * 批量构建用户上下文聚合。
     */
    private UserContext buildUserContext(Collection<Long> userIds) {
        List<Long> ids = userIds.stream().distinct().toList();
        Map<Long, List<AccountEntity>> accountMap;
        if (ids.isEmpty()) {
            accountMap = Map.of();
        } else {
            QueryWrapper accountQuery = QueryWrapper.create().where(ACCOUNT_ENTITY.USER_ID.in(ids));
            List<AccountEntity> accounts = accountMapper.selectListByQuery(accountQuery);
            accountMap = accounts.stream().collect(Collectors.groupingBy(AccountEntity::getUserId));
        }

        QueryWrapper membershipQuery = QueryWrapper.create()
                .where(ORGANIZATION_MEMBERSHIP_ENTITY.USER_ID.in(ids))
                .and(ORGANIZATION_MEMBERSHIP_ENTITY.DELETED.eq(false));
        Map<Long, OrganizationMembershipEntity> membershipMap = orgMembershipMapper.selectListByQuery(membershipQuery).stream()
                .filter(item -> Boolean.TRUE.equals(item.getPrimaryOrg()))
                .collect(Collectors.toMap(OrganizationMembershipEntity::getUserId, Function.identity(), (left, right) -> left));

        List<Long> orgIds = membershipMap.values().stream().map(OrganizationMembershipEntity::getOrgId).distinct().toList();
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
        return new UserContext(accountMap, membershipMap, orgMap);
    }

    /**
     * 转换为用户分页项视图。
     */
    private UserPageItem toPageItem(UserEntity user, UserContext context) {
        Map<String, String> accounts = context.accountMap().getOrDefault(user.getId(), List.of()).stream().collect(Collectors.toMap(AccountEntity::getAccountType, AccountEntity::getIdentifier, (left, right) -> left));
        OrganizationMembershipEntity membership = context.membershipMap().get(user.getId());
        OrganizationUnitEntity org = membership == null ? null : context.orgMap().get(membership.getOrgId());
        return new UserPageItem(
                String.valueOf(user.getId()),
                accounts.getOrDefault(AccountType.USERNAME.name(), ""),
                user.getDisplayName(),
                user.getEmployeeNo(),
                accounts.get(AccountType.MOBILE.name()),
                accounts.get(AccountType.EMAIL.name()),
                org == null ? null : String.valueOf(org.getId()),
                org == null ? null : org.getOrgName(),
                membership == null ? null : membership.getPositionName(),
                toNumericStatus(user.getStatus()),
                user.getLastLoginTime(),
                user.getCreatedAt()
        );
    }

    /**
     * 转换为用户详情视图。
     */
    private UserDetailView toDetail(UserEntity user, UserContext context) {
        Map<String, String> accounts = context.accountMap().getOrDefault(user.getId(), List.of()).stream().collect(Collectors.toMap(AccountEntity::getAccountType, AccountEntity::getIdentifier, (left, right) -> left));
        OrganizationMembershipEntity membership = context.membershipMap().get(user.getId());
        OrganizationUnitEntity org = membership == null ? null : context.orgMap().get(membership.getOrgId());
        return new UserDetailView(
                String.valueOf(user.getId()),
                accounts.getOrDefault(AccountType.USERNAME.name(), ""),
                user.getDisplayName(),
                user.getEmployeeNo(),
                accounts.get(AccountType.MOBILE.name()),
                accounts.get(AccountType.EMAIL.name()),
                user.getGender(),
                org == null ? null : String.valueOf(org.getId()),
                org == null ? null : org.getOrgName(),
                membership == null ? null : membership.getPositionName(),
                toNumericStatus(user.getStatus()),
                user.getRemark(),
                user.getPasswordResetRequired(),
                user.getLastLoginTime(),
                user.getCreatedAt()
        );
    }

    /**
     * 解析导入行中的组织主键。
     */
    private String resolveImportedOrgId(List<String> values, Map<String, Integer> headerIndexes) {
        String orgId = readCsvValue(values, headerIndexes, "orgid");
        if (orgId != null) {
            requireOrg(parseId(orgId, "组织不存在"));
            return orgId;
        }
        String orgName = readCsvValue(values, headerIndexes, "orgname");
        if (orgName == null) {
            throw new BusinessException(400, "所属组织不能为空");
        }
        QueryWrapper query = QueryWrapper.create()
                .where(ORGANIZATION_UNIT_ENTITY.DELETED.eq(false))
                .orderBy(ORGANIZATION_UNIT_ENTITY.SORT_ORDER, true)
                .orderBy(ORGANIZATION_UNIT_ENTITY.UPDATED_AT, false);
        return orgUnitMapper.selectListByQuery(query).stream()
                .filter(item -> orgName.equals(item.getOrgName()))
                .findFirst()
                .map(item -> String.valueOf(item.getId()))
                .orElseThrow(() -> new BusinessException(400, "所属组织不存在: " + orgName));
    }

    /**
     * 解析 CSV 表头索引映射。
     */
    private Map<String, Integer> resolveHeaderIndexes(List<String> headerColumns) {
        Map<String, Integer> headerIndexes = new LinkedHashMap<>();
        for (int index = 0; index < headerColumns.size(); index++) {
            headerIndexes.put(normalizeHeader(headerColumns.get(index)), index);
        }
        return headerIndexes;
    }

    /**
     * 归一化导入表头。
     */
    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        String normalized = header.replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "用户名" -> "username";
            case "真实姓名" -> "realname";
            case "工号" -> "jobnumber";
            case "手机号" -> "mobile";
            case "邮箱" -> "email";
            case "性别" -> "gender";
            case "所属组织" -> "orgname";
            case "所属组织id" -> "orgid";
            case "岗位" -> "position";
            case "状态" -> "status";
            case "角色编码", "角色码" -> "rolecodes";
            case "备注" -> "remark";
            case "初始密码" -> "password";
            default -> normalized.replaceAll("[^a-z0-9]", "");
        };
    }

    /**
     * 读取 CSV 指定列值。
     */
    private String readCsvValue(List<String> values, Map<String, Integer> headerIndexes, String key) {
        Integer index = headerIndexes.get(key.toLowerCase(Locale.ROOT));
        if (index == null || index < 0 || index >= values.size()) {
            return null;
        }
        String value = values.get(index);
        return trimToNull(value);
    }

    /**
     * 读取必填导入值。
     */
    private String requiredImportedValue(List<String> values, Map<String, Integer> headerIndexes, String key, String message) {
        String value = readCsvValue(values, headerIndexes, key);
        if (value == null) {
            throw new BusinessException(400, message);
        }
        return value;
    }

    /**
     * 解析导入状态值。
     */
    private Integer parseImportedStatus(String value) {
        if (value == null) {
            return 1;
        }
        if ("0".equals(value) || value.contains("禁用")) {
            return 0;
        }
        return 1;
    }

    /**
     * 解析导入角色编码列表。
     */
    private List<String> parseImportedRoleCodes(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(";|,"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .toList();
    }

    /**
     * 解析单行 CSV。
     */
    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char currentChar = line.charAt(index);
            if (currentChar == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (currentChar == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(currentChar);
            }
        }
        values.add(current.toString());
        return values;
    }

    /**
     * 用户上下文聚合。
     *
     * <p>集中承载用户详情视图组装时所需的账号、组织与成员关系数据。
     */
    private record UserContext(
            Map<Long, List<AccountEntity>> accountMap,
            Map<Long, OrganizationMembershipEntity> membershipMap,
            Map<Long, OrganizationUnitEntity> orgMap
    ) {
    }
}
