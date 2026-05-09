package net.junanw.upms.core.identity.authentication.model;

/**
 * 认证成功后的主体信息。
 *
 * <p>认证器完成用户名/密码或验证码校验后，仅返回后续登录态建立所需的最小主体数据，
 * 由成功处理器负责把它落入 Sa-Token 会话。
 *
 * @param userId 用户主键
 * @param username 用户名
 * @param loginType 本次登录采用的登录方式
 */
public record LoginPrincipal(
        Long userId,
        String username,
        AuthGrantType loginType
) {
}
