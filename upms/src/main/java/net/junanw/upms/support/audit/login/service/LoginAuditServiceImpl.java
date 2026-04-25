package net.junanw.upms.support.audit.login.service;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import net.junanw.upms.foundation.shared.util.ServiceSupport;
import net.junanw.upms.support.audit.login.entity.LoginAuditEventEntity;
import net.junanw.upms.support.audit.login.model.LoginAuditEventType;
import net.junanw.upms.support.audit.login.model.view.LoginAuditPageItem;
import net.junanw.upms.support.audit.login.repository.LoginAuditEventMapper;
import net.junanw.upms.system.iam.account.entity.AccountEntity;
import net.junanw.upms.system.iam.account.model.AccountType;
import net.junanw.upms.system.iam.account.mapper.AccountMapper;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthGrantType;
import net.junanw.upms.system.iam.user.entity.UserEntity;
import net.junanw.upms.system.iam.user.mapper.UserMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * LoginAuditServiceImpl 服务实现。
 *
 * <p>负责承接 LoginAudit 相关业务编排与规则落地。
 */
@Service("loginAuditServiceImpl")
@Primary
public class LoginAuditServiceImpl extends ServiceSupport implements LoginAuditService {

    private final LoginAuditEventMapper loginAuditEventMapper;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;
    private final IdGenerator idGenerator;

    public LoginAuditServiceImpl(
            LoginAuditEventMapper loginAuditEventMapper,
            AccountMapper accountMapper,
            UserMapper userMapper,
            IdGenerator idGenerator
    ) {
        this.loginAuditEventMapper = loginAuditEventMapper;
        this.accountMapper = accountMapper;
        this.userMapper = userMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional
    /** 记录登录审计事件。 */
    public void record(LoginAuditRecord command) {
        if (command == null || isBlank(command.accountIdentifier())) {
            return;
        }

        LoginAuditEventEntity entity = new LoginAuditEventEntity();
        entity.setId(idGenerator.nextId());
        entity.setUserId(resolveUserId(command));
        entity.setAccountIdentifier(command.accountIdentifier().trim());
        entity.setAuthType(normalizeAuthType(command.authType()));
        entity.setEventType(normalizeEventType(command));
        entity.setSuccessFlag(command.success());
        entity.setReasonCode(blankToNull(command.reasonCode()));
        entity.setIp(blankToNull(command.ip()));
        entity.setUserAgent(blankToNull(command.userAgent()));
        entity.setSessionKey(blankToNull(command.sessionKey()));
        entity.setTraceId(blankToNull(command.traceId()));
        entity.setRequestId(blankToNull(command.requestId()));
        entity.setEventTime(command.eventTime() == null ? LocalDateTime.now() : command.eventTime());
        loginAuditEventMapper.insert(entity);
    }

    @Override
    @Transactional(readOnly = true)
    /** 查询登录审计分页。 */
    public PageResponse<LoginAuditPageItem> page(
            String username,
            String loginType,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize
    ) {
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);

        QueryWrapper query = buildQuery(username, loginType, result, startTime, endTime);
        query.orderBy("event_time DESC");

        List<LoginAuditEventEntity> entities = loginAuditEventMapper.selectListByQuery(query);
        List<LoginAuditPageItem> items = toPageItems(entities);
        return paginate(items, resolvedPageNum, resolvedPageSize);
    }

    @Override
    @Transactional(readOnly = true)
    /** 导出登录审计列表。 */
    public List<LoginAuditPageItem> export(
            String username,
            String loginType,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        QueryWrapper query = buildQuery(username, loginType, result, startTime, endTime);
        query.orderBy("event_time DESC");

        List<LoginAuditEventEntity> entities = loginAuditEventMapper.selectListByQuery(query);
        return toPageItems(entities);
    }

    /**
     * 构建查询条件。
     */
    private QueryWrapper buildQuery(
            String username,
            String loginType,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        QueryWrapper query = QueryWrapper.create();

        // 登录类型过滤
        if (loginType != null && !loginType.isBlank()) {
            query.and("auth_type = {0}", normalizeNullable(loginType));
        }

        // 结果过滤
        if (result != null && !result.isBlank()) {
            query.and("success_flag = {0}", "SUCCESS".equals(normalizeNullable(result)));
        }

        // 时间范围过滤
        if (startTime != null) {
            query.and("event_time >= {0}", startTime);
        }
        if (endTime != null) {
            query.and("event_time <= {0}", endTime);
        }

        // 用户名关键词过滤（包含账号标识、用户名账号、用户显示名）
        if (username != null && !username.isBlank()) {
            String normalizedKeyword = normalizeKeyword(username);
            String pattern = "%" + normalizedKeyword + "%";

            // 查找匹配的用户ID
            List<Long> matchedUserIds = findMatchedUserIds(normalizedKeyword);

            if (!matchedUserIds.isEmpty()) {
                query.and((Consumer<QueryWrapper>) wrapper -> wrapper.where("LOWER(account_identifier) LIKE {0}", pattern)
                        .or("user_id IN ({0})", matchedUserIds));
            } else {
                query.where("LOWER(account_identifier) LIKE {0}", pattern);
            }
        }

        return query;
    }

    /**
     * 根据关键词查找匹配的用户ID列表。
     */
    private List<Long> findMatchedUserIds(String normalizedKeyword) {
        String pattern = "%" + normalizedKeyword + "%";

        // 从账号表查找匹配的用户ID（用户名类型）
        QueryWrapper accountQuery = QueryWrapper.create()
                .where("account_type = {0}", AccountType.USERNAME.name())
                .and("LOWER(identifier) LIKE {0}", pattern);
        List<Long> accountUserIds = accountMapper.selectListByQuery(accountQuery).stream()
                .map(AccountEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 从用户表查找匹配的用户ID（显示名）
        QueryWrapper userQuery = QueryWrapper.create()
                .where("deleted = false")
                .and("LOWER(display_name) LIKE {0}", pattern);
        List<Long> displayNameUserIds = userMapper.selectListByQuery(userQuery).stream()
                .map(UserEntity::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 合并两个列表
        return java.util.stream.Stream.concat(accountUserIds.stream(), displayNameUserIds.stream())
                .distinct()
                .toList();
    }

    private List<LoginAuditPageItem> toPageItems(List<LoginAuditEventEntity> entities) {
        Map<Long, UserEntity> userMap = resolveUserMap(entities);
        Map<Long, String> usernameMap = resolveUsernameMap(entities);
        return entities.stream()
                .map(entity -> toPageItem(entity, userMap, usernameMap))
                .toList();
    }

    private Map<Long, UserEntity> resolveUserMap(List<LoginAuditEventEntity> entities) {
        List<Long> userIds = entities.stream()
                .map(LoginAuditEventEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectListByIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
    }

    private Map<Long, String> resolveUsernameMap(List<LoginAuditEventEntity> entities) {
        List<Long> userIds = entities.stream()
                .map(LoginAuditEventEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper query = QueryWrapper.create()
                .where("user_id IN ({0})", userIds)
                .and("account_type = {0}", AccountType.USERNAME.name());
        return accountMapper.selectListByQuery(query)
                .stream()
                .collect(Collectors.toMap(AccountEntity::getUserId, AccountEntity::getIdentifier, (left, right) -> left));
    }

    private int normalizePageNum(int pageNum) {
        return Math.max(pageNum, 1);
    }

    private int normalizePageSize(int pageSize) {
        return Math.max(pageSize, 1);
    }

    /** 转换为登录审计分页项。 */
    private LoginAuditPageItem toPageItem(
            LoginAuditEventEntity entity,
            Map<Long, UserEntity> userMap,
            Map<Long, String> usernameMap
    ) {
        String username = usernameMap.getOrDefault(entity.getUserId(), entity.getAccountIdentifier());
        UserEntity user = entity.getUserId() == null ? null : userMap.get(entity.getUserId());
        String realName = user == null ? username : user.getDisplayName();
        return new LoginAuditPageItem(
                String.valueOf(entity.getId()),
                username,
                realName,
                entity.getAuthType(),
                entity.getIp(),
                entity.getUserAgent(),
                entity.getEventTime(),
                Boolean.TRUE.equals(entity.getSuccessFlag()) ? "SUCCESS" : "FAIL",
                entity.getReasonCode()
        );
    }

    /** 解析登录审计对应用户主键。 */
    private Long resolveUserId(LoginAuditRecord command) {
        String accountIdentifier = command.accountIdentifier().trim();
        String primaryAccountType = resolveAccountType(command.authType());
        String normalizedIdentifier = normalizeIdentifier(primaryAccountType, accountIdentifier);

        QueryWrapper query = QueryWrapper.create()
                .where("account_type = {0}", primaryAccountType)
                .and("normalized_identifier = {0}", normalizedIdentifier)
                .and("status = {0}", "ACTIVE")
                .and("is_login_enabled = true");

        AccountEntity account = accountMapper.selectOneByQuery(query);
        if (account != null) {
            return account.getUserId();
        }

        if (!AccountType.USERNAME.name().equals(primaryAccountType)) {
            QueryWrapper usernameQuery = QueryWrapper.create()
                    .where("account_type = {0}", AccountType.USERNAME.name())
                    .and("normalized_identifier = {0}", normalizeIdentifier(AccountType.USERNAME.name(), accountIdentifier))
                    .and("status = {0}", "ACTIVE")
                    .and("is_login_enabled = true");

            AccountEntity usernameAccount = accountMapper.selectOneByQuery(usernameQuery);
            if (usernameAccount != null) {
                return usernameAccount.getUserId();
            }
        }
        return null;
    }

    /** 根据认证类型解析账号类型。 */
    private String resolveAccountType(String authType) {
        AuthGrantType grantType = AuthGrantType.fromSessionValue(authType);
        return switch (grantType) {
            case MOBILE_CODE -> AccountType.MOBILE.name();
            case EMAIL_CODE -> AccountType.EMAIL.name();
            case PASSWORD -> AccountType.USERNAME.name();
        };
    }

    /** 归一化账号标识。 */
    private String normalizeIdentifier(String accountType, String value) {
        String normalized = value == null ? "" : value.trim();
        if (AccountType.EMAIL.name().equals(accountType) || AccountType.USERNAME.name().equals(accountType)) {
            return normalized.toLowerCase(Locale.ROOT);
        }
        return normalized;
    }

    /** 归一化认证方式。 */
    private String normalizeAuthType(String authType) {
        return AuthGrantType.fromSessionValue(authType).name();
    }

    /** 归一化登录事件类型。 */
    private String normalizeEventType(LoginAuditRecord command) {
        LoginAuditEventType eventType = LoginAuditEventType.fromNullable(command.eventType());
        return (eventType == null ? LoginAuditEventType.defaultFor(command.success()) : eventType).name();
    }

    /** 归一化关键字查询条件。 */
    protected String normalizeKeyword(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    /** 归一化可空枚举值。 */
    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized.toUpperCase(Locale.ROOT);
    }

    /** 去空白并把空串转为 null。 */
    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /** 判断字符串是否为空白。 */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
