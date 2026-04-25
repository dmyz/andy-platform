package net.junanw.upms.foundation.platform.auth.application.session.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 认证会话实体。
 */
@Getter
@Setter
@Table("auth_session")
public class AuthSessionEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column("session_key")
    private String sessionKey;

    @Column("user_id")
    private Long userId;

    @Column("client_type")
    private String clientType;

    @Column("auth_type")
    private String authType;

    @Column("ip")
    private String ip;

    @Column("user_agent")
    private String userAgent;

    @Column("status")
    private String status;

    @Column("login_time")
    private LocalDateTime loginTime;

    @Column("last_access_time")
    private LocalDateTime lastAccessTime;

    @Column("expire_time")
    private LocalDateTime expireTime;

    @Column("offline_time")
    private LocalDateTime offlineTime;

    @Column("offline_reason")
    private String offlineReason;
}
