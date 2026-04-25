package net.junanw.upms.foundation.platform.auth.authentication.session;

/**
 * token-session 中使用的键名常量。
 *
 * <p>仅保留确有复用价值的会话键，避免在登录成功处理器和运行时上下文读取之间出现硬编码漂移。
 */
public final class AuthSessionKeys {

    /** 当前登录用户名。 */
    public static final String USERNAME = "username";

    /** 本次登录采用的登录方式。 */
    public static final String LOGIN_TYPE = "loginType";

    private AuthSessionKeys() {
    }
}
