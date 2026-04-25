package net.junanw.upms.foundation.platform.auth.application.session;

import net.junanw.upms.foundation.platform.auth.application.session.model.view.AuthSessionPageItem;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthRequestContext;
import net.junanw.upms.foundation.shared.api.PageResponse;

import java.time.LocalDateTime;

public interface AuthOnlineSessionService {

    PageResponse<AuthSessionPageItem> page(String username, String realName, String loginType, String ip, String status, int pageNum, int pageSize);

    void offline(String id);

    void register(Long userId, String sessionKey, String authType, AuthRequestContext requestContext, LocalDateTime loginTime, long timeoutSeconds);

    void markOfflineBySessionKey(String sessionKey, String reason);

    void offlineByUserId(Long userId, String reason);

    void touch(String sessionKey);
}
