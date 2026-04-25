package net.junanw.upms.foundation.platform.auth.application.session;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.platform.auth.application.session.entity.AuthSessionEntity;
import net.junanw.upms.foundation.platform.auth.application.session.mapper.AuthSessionMapper;
import net.junanw.upms.foundation.platform.auth.application.session.model.view.AuthSessionPageItem;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthRequestContext;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import net.junanw.upms.system.iam.account.entity.AccountEntity;
import net.junanw.upms.system.iam.account.model.AccountType;
import net.junanw.upms.system.iam.account.mapper.AccountMapper;
import net.junanw.upms.system.iam.user.entity.UserEntity;
import net.junanw.upms.system.iam.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.junanw.upms.foundation.platform.auth.application.session.entity.table.AuthSessionEntityTableDef.AUTH_SESSION_ENTITY;
import static net.junanw.upms.system.iam.account.entity.table.AccountEntityTableDef.ACCOUNT_ENTITY;
import static net.junanw.upms.system.iam.user.entity.table.UserEntityTableDef.USER_ENTITY;

@Service
public class AuthOnlineSessionServiceImpl implements AuthOnlineSessionService {

    private final AuthSessionMapper authSessionMapper;
    private final AccountMapper iamAccountMapper;
    private final UserMapper iamUserMapper;
    private final IdGenerator idGenerator;

    public AuthOnlineSessionServiceImpl(
            AuthSessionMapper authSessionMapper,
            AccountMapper iamAccountMapper,
            UserMapper iamUserMapper,
            IdGenerator idGenerator
    ) {
        this.authSessionMapper = authSessionMapper;
        this.iamAccountMapper = iamAccountMapper;
        this.iamUserMapper = iamUserMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuthSessionPageItem> page(String username, String realName, String loginType, String ip, String status, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create();

        String normalizedIp = normalizeKeyword(ip);
        String normalizedStatus = normalizeStatus(status);
        String normalizedLoginType = normalizeNullable(loginType);

        if (!normalizedIp.isBlank()) {
            query.and(AUTH_SESSION_ENTITY.IP.like("%" + normalizedIp + "%"));
        }
        if (normalizedStatus != null) {
            query.and(AUTH_SESSION_ENTITY.STATUS.eq(normalizedStatus));
        } else {
            query.and(AUTH_SESSION_ENTITY.STATUS.eq("ONLINE"));
        }
        if (normalizedLoginType != null) {
            query.and(AUTH_SESSION_ENTITY.AUTH_TYPE.eq(normalizedLoginType));
        }

        query.orderBy(AUTH_SESSION_ENTITY.LOGIN_TIME.desc());

        List<AuthSessionEntity> sessions = authSessionMapper.selectListByQuery(query);
        SessionContext context = buildContext(sessions);
        String normalizedUsername = normalizeKeyword(username);
        String normalizedRealName = normalizeKeyword(realName);
        String currentToken = StpUtil.getTokenValue();
        List<AuthSessionPageItem> items = sessions.stream()
                .map(entity -> toPageItem(entity, context, currentToken))
                .filter(item -> normalizedUsername.isBlank() || item.username().toLowerCase(Locale.ROOT).contains(normalizedUsername))
                .filter(item -> normalizedRealName.isBlank() || item.realName().toLowerCase(Locale.ROOT).contains(normalizedRealName))
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    @Override
    @Transactional
    public void offline(String id) {
        AuthSessionEntity entity = requireSession(id);
        if ("ONLINE".equalsIgnoreCase(entity.getStatus())) {
            StpUtil.logoutByTokenValue(entity.getSessionKey());
        }
        markOffline(entity, "ADMIN_OFFLINE");
    }

    @Override
    @Transactional
    public void register(Long userId, String sessionKey, String authType, AuthRequestContext requestContext, LocalDateTime loginTime, long timeoutSeconds) {
        if (userId == null || isBlank(sessionKey)) {
            return;
        }
        LocalDateTime resolvedLoginTime = loginTime == null ? LocalDateTime.now() : loginTime;

        QueryWrapper query = QueryWrapper.create()
                .where(AUTH_SESSION_ENTITY.SESSION_KEY.eq(sessionKey));
        AuthSessionEntity entity = authSessionMapper.selectOneByQuery(query);
        if (entity == null) {
            entity = new AuthSessionEntity();
            entity.setId(idGenerator.nextId());
        }

        entity.setSessionKey(sessionKey);
        entity.setUserId(userId);
        entity.setClientType("WEB");
        entity.setAuthType(normalizeNullable(authType) == null ? "PASSWORD" : normalizeNullable(authType));
        entity.setIp(blankToNull(requestContext == null ? null : requestContext.remoteAddr()));
        entity.setUserAgent(blankToNull(requestContext == null ? null : requestContext.userAgent()));
        entity.setStatus("ONLINE");
        entity.setLoginTime(resolvedLoginTime);
        entity.setLastAccessTime(resolvedLoginTime);
        entity.setExpireTime(resolveExpireTime(resolvedLoginTime, timeoutSeconds));
        entity.setOfflineTime(null);
        entity.setOfflineReason(null);
        authSessionMapper.insertOrUpdate(entity);
    }

    @Override
    @Transactional
    public void markOfflineBySessionKey(String sessionKey, String reason) {
        if (isBlank(sessionKey)) {
            return;
        }
        QueryWrapper query = QueryWrapper.create()
                .where(AUTH_SESSION_ENTITY.SESSION_KEY.eq(sessionKey));
        AuthSessionEntity entity = authSessionMapper.selectOneByQuery(query);
        if (entity != null) {
            markOffline(entity, reason);
        }
    }

    @Override
    @Transactional
    public void offlineByUserId(Long userId, String reason) {
        if (userId == null) {
            return;
        }
        QueryWrapper query = QueryWrapper.create()
                .where(AUTH_SESSION_ENTITY.USER_ID.eq(userId))
                .and(AUTH_SESSION_ENTITY.STATUS.eq("ONLINE"));
        List<AuthSessionEntity> sessions = authSessionMapper.selectListByQuery(query);
        for (AuthSessionEntity session : sessions) {
            StpUtil.logoutByTokenValue(session.getSessionKey());
            markOffline(session, reason);
        }
    }

    @Override
    @Transactional
    public void touch(String sessionKey) {
        if (isBlank(sessionKey)) {
            return;
        }
        QueryWrapper query = QueryWrapper.create()
                .where(AUTH_SESSION_ENTITY.SESSION_KEY.eq(sessionKey));
        AuthSessionEntity entity = authSessionMapper.selectOneByQuery(query);
        if (entity != null && "ONLINE".equalsIgnoreCase(entity.getStatus())) {
            entity.setLastAccessTime(LocalDateTime.now());
            authSessionMapper.update(entity);
        }
    }

    private AuthSessionEntity requireSession(String id) {
        try {
            AuthSessionEntity entity = authSessionMapper.selectOneById(Long.parseLong(id));
            if (entity == null) {
                throw new BusinessException(404, "在线会话不存在");
            }
            return entity;
        }
        catch (NumberFormatException exception) {
            throw new BusinessException(400, "在线会话标识非法");
        }
    }

    private void markOffline(AuthSessionEntity entity, String reason) {
        entity.setStatus("OFFLINE");
        entity.setOfflineTime(LocalDateTime.now());
        entity.setOfflineReason(blankToNull(reason));
        entity.setLastAccessTime(entity.getLastAccessTime() == null ? entity.getLoginTime() : entity.getLastAccessTime());
        authSessionMapper.update(entity);
    }

    private SessionContext buildContext(List<AuthSessionEntity> sessions) {
        List<Long> userIds = sessions.stream().map(AuthSessionEntity::getUserId).filter(Objects::nonNull).distinct().toList();

        QueryWrapper userQuery = QueryWrapper.create()
                .where(USER_ENTITY.ID.in(userIds));
        Map<Long, UserEntity> userMap = userIds.isEmpty() ? Map.of() : iamUserMapper.selectListByQuery(userQuery).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));

        QueryWrapper accountQuery = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.USER_ID.in(userIds))
                .and(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.USERNAME.name()));
        Map<Long, String> usernameMap = userIds.isEmpty() ? Map.of() : iamAccountMapper.selectListByQuery(accountQuery).stream()
                .collect(Collectors.toMap(AccountEntity::getUserId, AccountEntity::getIdentifier, (left, right) -> left));

        return new SessionContext(userMap, usernameMap);
    }

    private AuthSessionPageItem toPageItem(AuthSessionEntity entity, SessionContext context, String currentToken) {
        UserEntity user = context.userMap().get(entity.getUserId());
        String username = context.usernameMap().getOrDefault(entity.getUserId(), String.valueOf(entity.getUserId()));
        String realName = user == null || isBlank(user.getDisplayName()) ? username : user.getDisplayName();
        return new AuthSessionPageItem(
                String.valueOf(entity.getId()),
                username,
                realName,
                entity.getAuthType(),
                entity.getClientType(),
                entity.getIp(),
                entity.getUserAgent(),
                entity.getStatus(),
                entity.getLoginTime(),
                entity.getLastAccessTime(),
                entity.getExpireTime(),
                Objects.equals(entity.getSessionKey(), currentToken)
        );
    }

    private <T> PageResponse<T> paginate(List<T> items, int pageNum, int pageSize) {
        int resolvedPageNum = Math.max(pageNum, 1);
        int resolvedPageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, items.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, items.size());
        return PageResponse.of(items.subList(fromIndex, toIndex), items.size(), resolvedPageNum, resolvedPageSize);
    }

    private LocalDateTime resolveExpireTime(LocalDateTime loginTime, long timeoutSeconds) {
        if (timeoutSeconds < 0) {
            return loginTime.plusYears(100);
        }
        return loginTime.plusSeconds(timeoutSeconds);
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

    private record SessionContext(Map<Long, UserEntity> userMap, Map<Long, String> usernameMap) {
    }
}
