package net.junanw.upms.core.identity.authentication.session;

/**
 * token-session 中使用的键名常量。
 *
 * <p>仅保留确有复用价值的会话键，避免在登录成功处理器和运行时上下文读取之间出现硬编码漂移。
 */
public final class AuthSessionKeys {

    /** 当前登录用户名。 */
    public static final String USERNAME = "username";

    /** 当前登录用户主键。 */
    public static final String USER_ID = "userId";

    /** 本次登录采用的登录方式。 */
    public static final String LOGIN_TYPE = "loginType";

    /** 本次登录客户端类型。 */
    public static final String CLIENT_TYPE = "clientType";

    /** 本次登录来源 IP。 */
    public static final String IP = "ip";

    /** 本次登录 User-Agent。 */
    public static final String USER_AGENT = "userAgent";

    /** 本次登录时间戳，单位毫秒。 */
    public static final String LOGIN_TIME = "loginTime";

    /** 最近访问时间戳，单位毫秒。 */
    public static final String LAST_ACCESS_TIME = "lastAccessTime";

    private AuthSessionKeys() {
    }
}
