package net.junanw.upms.core.identity.session.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

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
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime loginTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastAccessTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime expireTime,
        Boolean current
) {
}
