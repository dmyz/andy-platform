package net.junanw.upms.foundation.platform.auth.application.session.model.view;

import java.time.LocalDateTime;

public record AuthSessionPageItem(
        String id,
        String username,
        String realName,
        String loginType,
        String clientType,
        String ip,
        String userAgent,
        String status,
        LocalDateTime loginTime,
        LocalDateTime lastAccessTime,
        LocalDateTime expireTime,
        Boolean current
) {
}
