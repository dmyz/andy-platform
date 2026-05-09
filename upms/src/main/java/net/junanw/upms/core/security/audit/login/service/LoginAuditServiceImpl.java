package net.junanw.upms.core.security.audit.login.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.util.ServiceSupport;
import net.junanw.upms.core.security.audit.login.entity.LoginAuditEventEntity;
import net.junanw.upms.core.security.audit.login.model.LoginAuditEventType;
import net.junanw.upms.core.security.audit.login.model.view.LoginAuditPageItem;
import net.junanw.upms.core.security.audit.login.mapper.LoginAuditEventMapper;
import net.junanw.upms.core.identity.account.entity.AccountEntity;
import net.junanw.upms.core.identity.account.model.AccountType;
import net.junanw.upms.core.identity.account.mapper.AccountMapper;
import net.junanw.upms.core.identity.authentication.model.AuthGrantType;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
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

    public LoginAuditServiceImpl(
            LoginAuditEventMapper loginAuditEventMapper,
            AccountMapper accountMapper,
            UserMapper userMapper
    ) {
        this.loginAuditEventMapper = loginAuditEventMapper;
        this.accountMapper = accountMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    /** 记录登录审计事件。 */
    public void record(LoginAuditRecord command) {
        if (command == null || isBlank(command.accountIdentifier())) {
            return;
        }

        LoginAuditEventEntity entity = new LoginAuditEventEntity();
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
        loginAuditEventMapper.save(entity);
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

        // 使用 XBatis QueryChain 构建查询
        QueryChain<LoginAuditEventEntity> query = QueryChain.of(loginAuditEventMapper);

        // 登录类型过滤
        if (loginType != null && !loginType.isBlank()) {
            query.eq(LoginAuditEventEntity::getAuthType, normalizeNullable(loginType));
        }

        // 结果过滤
        if (result != null && !result.isBlank()) {
            query.eq(LoginAuditEventEntity::getSuccessFlag, "SUCCESS".equals(normalizeNullable(result)));
        }

        // 时间范围过滤
        if (startTime != null) {
            query.gte(LoginAuditEventEntity::getEventTime, startTime);
        }
        if (endTime != null) {
            query.lte(LoginAuditEventEntity::getEventTime, endTime);
        }

        query.orderByDesc(LoginAuditEventEntity::getEventTime);

        List<LoginAuditEventEntity> entities = query.list();
        List<LoginAuditPageItem> items = filterByUsername(toPageItems(entities), username);
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
        // 使用 XBatis QueryChain 构建查询
        QueryChain<LoginAuditEventEntity> query = QueryChain.of(loginAuditEventMapper);

        // 登录类型过滤
        if (loginType != null && !loginType.isBlank()) {
            query.eq(LoginAuditEventEntity::getAuthType, normalizeNullable(loginType));
        }

        // 结果过滤
        if (result != null && !result.isBlank()) {
            query.eq(LoginAuditEventEntity::getSuccessFlag, "SUCCESS".equals(normalizeNullable(result)));
        }

        // 时间范围过滤
        if (startTime != null) {
            query.gte(LoginAuditEventEntity::getEventTime, startTime);
        }
        if (endTime != null) {
            query.lte(LoginAuditEventEntity::getEventTime, endTime);
        }

        query.orderBy(false, LoginAuditEventEntity::getEventTime);

        List<LoginAuditEventEntity> entities = query.list();
        return filterByUsername(toPageItems(entities), username);
    }

    /** 按用户名、真实姓名或匿名账号标识过滤审计分页项。 */
    private List<LoginAuditPageItem> filterByUsername(List<LoginAuditPageItem> items, String username) {
        if (isBlank(username)) {
            return items;
        }
        String keyword = normalizeKeyword(username);
        return items.stream()
                .filter(item -> containsKeyword(item.username(), keyword) || containsKeyword(item.realName(), keyword))
                .toList();
    }

    /** 判断文本是否包含归一化后的关键字。 */
    private boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
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
        return userMapper.listByIds(userIds)
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
        return QueryChain.of(accountMapper)
                .in(AccountEntity::getUserId, userIds)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .list()
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
        String username = entity.getUserId() == null ? entity.getAccountIdentifier() : usernameMap.getOrDefault(entity.getUserId(), entity.getAccountIdentifier());
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

        AccountEntity account = QueryChain.of(accountMapper)
                .eq(AccountEntity::getAccountType, primaryAccountType)
                .eq(AccountEntity::getNormalizedIdentifier, normalizedIdentifier)
                .eq(AccountEntity::getStatus, "ACTIVE")
                .eq(AccountEntity::getIsLoginEnabled, true)
                .get();
        if (account != null) {
            return account.getUserId();
        }

        if (!AccountType.USERNAME.name().equals(primaryAccountType)) {
            AccountEntity usernameAccount = QueryChain.of(accountMapper)
                    .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                    .eq(AccountEntity::getNormalizedIdentifier, normalizeIdentifier(AccountType.USERNAME.name(), accountIdentifier))
                    .eq(AccountEntity::getStatus, "ACTIVE")
                    .eq(AccountEntity::getIsLoginEnabled, true)
                    .get();
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
