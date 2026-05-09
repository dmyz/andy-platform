package net.junanw.upms.core.security.audit.login.model;

import java.util.Locale;

/**
 * 登录审计事件类型。
 */
public enum LoginAuditEventType {

    /** 登录成功。 */
    LOGIN_SUCCESS,

    /** 登录失败。 */
    LOGIN_FAIL,

    /** 登出。 */
    LOGOUT;

    /**
     * 从可空文本解析事件类型。
     *
     * @param eventType 原始事件类型文本
     * @return 事件类型；无法识别时返回 {@code null}
     */
    public static LoginAuditEventType fromNullable(String eventType) {
        if (eventType == null) {
            return null;
        }
        String normalized = eventType.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return LoginAuditEventType.valueOf(normalized.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * 根据是否成功推导默认事件类型。
     *
     * @param success 是否成功
     * @return 默认事件类型
     */
    public static LoginAuditEventType defaultFor(boolean success) {
        return success ? LOGIN_SUCCESS : LOGIN_FAIL;
    }
}
