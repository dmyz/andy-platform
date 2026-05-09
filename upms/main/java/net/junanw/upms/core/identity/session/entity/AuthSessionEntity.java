package net.junanw.upms.core.identity.session.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
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

    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;
    private String sessionKey;
    private Long userId;
    private String clientType;
    private String authType;
    private String ip;
    private String userAgent;
    private String status;
    private LocalDateTime loginTime;
    private LocalDateTime lastAccessTime;
    private LocalDateTime expireTime;
    private LocalDateTime offlineTime;
    private String offlineReason;
}
