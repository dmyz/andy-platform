package net.junanw.upms.foundation.platform.auth.authentication.model;

/**
 * 认证请求上下文。
 *
 * <p>该对象只承载来自 HTTP 层的环境信息，供登录成功/失败审计和会话落地时使用，
 * 不参与凭证校验逻辑。
 *
 * @param remoteAddr 请求来源 IP
 * @param userAgent 请求头中的 User-Agent
 * @param traceId 链路追踪标识
 * @param requestId 请求唯一标识
 */
public record AuthRequestContext(
        String remoteAddr,
        String userAgent,
        String traceId,
        String requestId
) {
}
