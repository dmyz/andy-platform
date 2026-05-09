package net.junanw.upms.core.identity.session;

import net.junanw.upms.core.identity.session.model.view.AuthSessionPageItem;
import net.junanw.upms.core.identity.authentication.model.AuthRequestContext;
import net.junanw.upms.infrastructure.shared.api.PageResponse;

import java.time.LocalDateTime;

public interface AuthOnlineSessionService {

    /**
     * 基于 Sa-Token 会话分页查询当前在线 token。
     */
    PageResponse<AuthSessionPageItem> page(String username, String realName, String loginType, String ip, String status, int pageNum, int pageSize);

    /**
     * 强制指定在线 token 下线。
     */
    void offline(String id);

    /**
     * 在 Sa-Token token-session 中登记在线会话扩展信息。
     */
    void register(Long userId, String sessionKey, String authType, AuthRequestContext requestContext, LocalDateTime loginTime, long timeoutSeconds);

    /**
     * 强制指定用户的所有在线 token 下线。
     */
    void offlineByUserId(Long userId, String reason);

    /**
     * 刷新指定 token 的最近访问时间。
     */
    void touch(String sessionKey);
}
