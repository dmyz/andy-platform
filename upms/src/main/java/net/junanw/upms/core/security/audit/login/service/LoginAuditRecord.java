package net.junanw.upms.core.security.audit.login.service;

import java.time.LocalDateTime;

/**
 * 登录审计记录命令。
 *
 * @param accountIdentifier 账号标识
 * @param authType 认证方式
 * @param eventType 事件类型
 * @param success 是否成功
 * @param reasonCode 失败原因编码
 * @param ip 请求来源 IP
 * @param userAgent 请求 User-Agent
 * @param sessionKey 会话标识
 * @param traceId 链路追踪标识
 * @param requestId 请求标识
 * @param eventTime 事件发生时间
 */
public record LoginAuditRecord(
        String accountIdentifier,
        String authType,
        String eventType,
        boolean success,
        String reasonCode,
        String ip,
        String userAgent,
        String sessionKey,
        String traceId,
        String requestId,
        LocalDateTime eventTime
) {
}
