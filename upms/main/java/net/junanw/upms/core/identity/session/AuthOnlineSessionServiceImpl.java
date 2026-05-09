package net.junanw.upms.core.identity.session;

import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.core.identity.account.entity.AccountEntity;
import net.junanw.upms.core.identity.account.model.AccountType;
import net.junanw.upms.core.identity.account.mapper.AccountMapper;
import net.junanw.upms.core.identity.authentication.model.AuthRequestContext;
import net.junanw.upms.core.identity.authentication.session.AuthSessionKeys;
import net.junanw.upms.core.identity.session.model.view.AuthSessionPageItem;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
import net.junanw.upms.infrastructure.shared.security.TokenFingerprintService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class AuthOnlineSessionServiceImpl implements AuthOnlineSessionService {

    private static final String ONLINE_STATUS = "ONLINE";
    private static final String CLIENT_TYPE_WEB = "WEB";
    private static final int SEARCH_ALL_SIZE = -1;

    private final AccountMapper accountMapper;
    private final UserMapper userMapper;
    private final TokenFingerprintService tokenFingerprintService;

    public AuthOnlineSessionServiceImpl(
            AccountMapper accountMapper,
            UserMapper userMapper,
            TokenFingerprintService tokenFingerprintService
    ) {
        this.accountMapper = accountMapper;
        this.userMapper = userMapper;
        this.tokenFingerprintService = tokenFingerprintService;
    }

    @Override
    public PageResponse<AuthSessionPageItem> page(String username, String realName, String loginType, String ip, String status, int pageNum, int pageSize) {
        String normalizedIp = normalizeKeyword(ip);
        String normalizedStatus = normalizeStatus(status);
        String normalizedLoginType = normalizeNullable(loginType);
        if (normalizedStatus != null && !ONLINE_STATUS.equals(normalizedStatus)) {
            return PageResponse.of(List.of(), 0, Math.max(pageNum, 1), Math.max(pageSize, 1));
        }

        List<SessionSnapshot> sessions = loadOnlineSessions();
        SessionContext context = buildContext(sessions);
        String normalizedUsername = normalizeKeyword(username);
        String normalizedRealName = normalizeKeyword(realName);
        List<AuthSessionPageItem> items = sessions.stream()
                .map(session -> toPageItem(session, context))
                .filter(item -> normalizedIp.isBlank() || normalizeKeyword(item.ip()).contains(normalizedIp))
                .filter(item -> normalizedLoginType == null || normalizedLoginType.equals(normalizeNullable(item.loginType())))
                .filter(item -> normalizedUsername.isBlank() || item.username().toLowerCase(Locale.ROOT).contains(normalizedUsername))
                .filter(item -> normalizedRealName.isBlank() || item.realName().toLowerCase(Locale.ROOT).contains(normalizedRealName))
                .sorted(Comparator.comparing(AuthSessionPageItem::loginTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    @Override
    public void offline(String id) {
        String tokenValue = resolveTokenById(id);
        StpUtil.logoutByTokenValue(tokenValue);
    }

    @Override
    public void register(Long userId, String sessionKey, String authType, AuthRequestContext requestContext, LocalDateTime loginTime, long timeoutSeconds) {
        if (userId == null || isBlank(sessionKey)) {
            return;
        }
        LocalDateTime resolvedLoginTime = loginTime == null ? LocalDateTime.now() : loginTime;
        SaSession tokenSession = StpUtil.getStpLogic().getTokenSessionByToken(sessionKey, true);
        tokenSession.set(AuthSessionKeys.USER_ID, userId);
        tokenSession.set(AuthSessionKeys.USERNAME, currentUsername(userId));
        tokenSession.set(AuthSessionKeys.LOGIN_TYPE, normalizeNullable(authType) == null ? "PASSWORD" : normalizeNullable(authType));
        tokenSession.set(AuthSessionKeys.CLIENT_TYPE, CLIENT_TYPE_WEB);
        setIfPresent(tokenSession, AuthSessionKeys.IP, blankToNull(requestContext == null ? null : requestContext.remoteAddr()));
        setIfPresent(tokenSession, AuthSessionKeys.USER_AGENT, blankToNull(requestContext == null ? null : requestContext.userAgent()));
        tokenSession.set(AuthSessionKeys.LOGIN_TIME, toEpochMillis(resolvedLoginTime));
        tokenSession.set(AuthSessionKeys.LAST_ACCESS_TIME, toEpochMillis(resolvedLoginTime));
    }

    @Override
    public void offlineByUserId(Long userId, String reason) {
        if (userId == null) {
            return;
        }
        StpUtil.logout(userId);
    }

    @Override
    public void touch(String sessionKey) {
        if (isBlank(sessionKey)) {
            return;
        }
        SaSession tokenSession = StpUtil.getStpLogic().getTokenSessionByToken(sessionKey, false);
        if (tokenSession != null) {
            tokenSession.set(AuthSessionKeys.LAST_ACCESS_TIME, toEpochMillis(LocalDateTime.now()));
        }
    }

    /**
     * 从 Sa-Token 存储中加载当前仍有效的在线 token 快照。
     */
    private List<SessionSnapshot> loadOnlineSessions() {
        String tokenKeyPrefix = StpUtil.getStpLogic().splicingKeyTokenValue("");
        return StpUtil.searchTokenValue("", 0, SEARCH_ALL_SIZE, false).stream()
                .map(value -> stripPrefix(value, tokenKeyPrefix))
                .filter(token -> !isBlank(token))
                .filter(token -> StpUtil.getLoginIdByToken(token) != null)
                .map(this::toSnapshot)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 将 Sa-Token token 和 token-session 转换为在线会话快照。
     */
    private SessionSnapshot toSnapshot(String tokenValue) {
        SaSession tokenSession = StpUtil.getStpLogic().getTokenSessionByToken(tokenValue, false);
        Object loginId = StpUtil.getLoginIdByToken(tokenValue);
        Long userId = toLong(firstNonNull(tokenSession == null ? null : tokenSession.get(AuthSessionKeys.USER_ID), loginId));
        if (userId == null) {
            return null;
        }
        LocalDateTime loginTime = resolveLoginTime(tokenSession);
        LocalDateTime lastAccessTime = resolveLastAccessTime(tokenValue, tokenSession, loginTime);
        return new SessionSnapshot(
                tokenValue,
                tokenFingerprintService.fingerprint(tokenValue),
                userId,
                tokenSession == null ? null : asString(tokenSession.get(AuthSessionKeys.USERNAME)),
                tokenSession == null ? null : asString(tokenSession.get(AuthSessionKeys.LOGIN_TYPE)),
                tokenSession == null ? null : asString(tokenSession.get(AuthSessionKeys.CLIENT_TYPE)),
                tokenSession == null ? null : asString(tokenSession.get(AuthSessionKeys.IP)),
                tokenSession == null ? null : asString(tokenSession.get(AuthSessionKeys.USER_AGENT)),
                loginTime,
                lastAccessTime,
                resolveExpireTime(tokenValue, loginTime)
        );
    }

    /**
     * 根据前端传入的在线会话标识定位 Sa-Token token。
     */
    private String resolveTokenById(String id) {
        if (isBlank(id)) {
            throw new BusinessException(400, "在线会话标识不能为空");
        }
        String normalizedId = id.trim();
        return loadOnlineSessions().stream()
                .filter(session -> normalizedId.equals(session.id()) || normalizedId.equals(session.tokenValue()))
                .map(SessionSnapshot::tokenValue)
                .findFirst()
                .orElseThrow(() -> new BusinessException(404, "在线会话不存在"));
    }

    /**
     * 批量加载用户和用户名上下文，避免在线会话列表逐条回源。
     */
    private SessionContext buildContext(List<SessionSnapshot> sessions) {
        List<Long> userIds = sessions.stream().map(SessionSnapshot::userId).filter(Objects::nonNull).distinct().toList();

        Map<Long, UserEntity> userMap = userIds.isEmpty() ? Map.of() : QueryChain.of(userMapper)
                .in(UserEntity::getId, userIds)
                .list().stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));

        Map<Long, String> usernameMap = userIds.isEmpty() ? Map.of() : QueryChain.of(accountMapper)
                .in(AccountEntity::getUserId, userIds)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .list().stream()
                .collect(Collectors.toMap(AccountEntity::getUserId, AccountEntity::getIdentifier, (left, right) -> left));

        return new SessionContext(userMap, usernameMap);
    }

    /**
     * 组装在线会话分页项。
     */
    private AuthSessionPageItem toPageItem(SessionSnapshot session, SessionContext context) {
        UserEntity user = context.userMap().get(session.userId());
        String username = firstText(session.username(), context.usernameMap().get(session.userId()), String.valueOf(session.userId()));
        String realName = user == null || isBlank(user.getDisplayName()) ? username : user.getDisplayName();
        return new AuthSessionPageItem(
                session.id(),
                username,
                realName,
                firstText(session.loginType(), "PASSWORD"),
                firstText(session.clientType(), CLIENT_TYPE_WEB),
                session.ip(),
                session.userAgent(),
                ONLINE_STATUS,
                session.loginTime(),
                session.lastAccessTime(),
                session.expireTime(),
                Objects.equals(session.tokenValue(), currentToken())
        );
    }

    /**
     * 对已完成过滤的会话列表执行内存分页。
     */
    private <T> PageResponse<T> paginate(List<T> items, int pageNum, int pageSize) {
        int resolvedPageNum = Math.max(pageNum, 1);
        int resolvedPageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, items.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, items.size());
        return PageResponse.of(items.subList(fromIndex, toIndex), items.size(), resolvedPageNum, resolvedPageSize);
    }

    private LocalDateTime resolveLoginTime(SaSession tokenSession) {
        if (tokenSession == null) {
            return LocalDateTime.now();
        }
        LocalDateTime loginTime = toLocalDateTime(tokenSession.get(AuthSessionKeys.LOGIN_TIME));
        return loginTime == null ? fromEpochMillis(tokenSession.getCreateTime()) : loginTime;
    }

    private LocalDateTime resolveLastAccessTime(String tokenValue, SaSession tokenSession, LocalDateTime loginTime) {
        LocalDateTime lastAccessTime = tokenSession == null ? null : toLocalDateTime(tokenSession.get(AuthSessionKeys.LAST_ACCESS_TIME));
        if (lastAccessTime != null) {
            return lastAccessTime;
        }
        long lastActiveTime = StpUtil.getStpLogic().getTokenLastActiveTime(tokenValue);
        if (lastActiveTime >= 0) {
            return fromEpochMillis(lastActiveTime);
        }
        return loginTime;
    }

    private LocalDateTime resolveExpireTime(String tokenValue, LocalDateTime loginTime) {
        long timeoutSeconds = StpUtil.getTokenTimeout(tokenValue);
        if (timeoutSeconds == -1) {
            return loginTime.plusYears(100);
        }
        if (timeoutSeconds < 0) {
            return null;
        }
        return LocalDateTime.now().plusSeconds(timeoutSeconds);
    }

    private String currentUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        AccountEntity account = QueryChain.of(accountMapper)
                .eq(AccountEntity::getUserId, userId)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .get();
        return account == null ? String.valueOf(userId) : account.getIdentifier();
    }

    /**
     * 非空时写入 token-session，避免内存 DAO 对 null 值抛出异常。
     */
    private void setIfPresent(SaSession tokenSession, String key, Object value) {
        if (value != null) {
            tokenSession.set(key, value);
        }
    }

    private String normalizeStatus(String status) {
        String normalized = normalizeNullable(status);
        if (normalized == null) {
            return null;
        }
        return switch (normalized) {
            case "1", "ONLINE" -> "ONLINE";
            case "0", "OFFLINE" -> "OFFLINE";
            default -> normalized;
        };
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeKeyword(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private long toEpochMillis(LocalDateTime value) {
        return value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof Number number) {
            return fromEpochMillis(number.longValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return fromEpochMillis(Long.parseLong(text));
            }
            catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private LocalDateTime fromEpochMillis(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text);
            }
            catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Object firstNonNull(Object first, Object second) {
        return first == null ? second : first;
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String stripPrefix(String value, String prefix) {
        if (value != null && prefix != null && value.startsWith(prefix)) {
            return value.substring(prefix.length());
        }
        return value;
    }

    private String currentToken() {
        try {
            return StpUtil.getTokenValue();
        }
        catch (SaTokenContextException exception) {
            return null;
        }
    }

    private record SessionContext(Map<Long, UserEntity> userMap, Map<Long, String> usernameMap) {
    }

    private record SessionSnapshot(
            String tokenValue,
            String id,
            Long userId,
            String username,
            String loginType,
            String clientType,
            String ip,
            String userAgent,
            LocalDateTime loginTime,
            LocalDateTime lastAccessTime,
            LocalDateTime expireTime
    ) {
    }
}
