package net.junanw.upms.core.identity.authentication.model;

import net.junanw.upms.core.identity.account.AccountNormalizer;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;

/**
 * 支持的登录方式枚举。
 *
 * <p>当前认证链路只保留一个统一的 {@code LoginAuthentication} 命令对象，
 * 具体由该枚举决定后续路由到哪一个认证器执行凭证校验。
 */
public enum AuthGrantType {

    /** 用户名密码登录。 */
    PASSWORD,

    /** 手机验证码登录。 */
    MOBILE_CODE,

    /** 邮箱验证码登录。 */
    EMAIL_CODE;

    /**
     * 从外部请求值解析登录方式。
     *
     * <p>空值默认回退为密码登录；非法值直接抛业务异常，保证控制层收到明确错误。
     *
     * @param grantType 请求中的登录方式
     * @return 解析后的登录方式
     */
    public static AuthGrantType from(String grantType) {
        String normalized = AccountNormalizer.normalizeUpper(grantType);
        if (normalized == null) {
            return PASSWORD;
        }
        AuthGrantType parsed = parse(normalized);
        if (parsed != null) {
            return parsed;
        }
        throw new BusinessException(400, "不支持的登录方式");
    }

    /**
     * 从 token-session 中保存的值恢复登录方式。
     *
     * <p>会话中允许直接存枚举，也兼容字符串值；异常或未知值统一兜底为密码登录，
     * 以避免旧会话或历史数据造成运行时崩溃。
     *
     * @param value session 中保存的原始值
     * @return 恢复后的登录方式
     */
    public static AuthGrantType fromSessionValue(Object value) {
        if (value instanceof AuthGrantType authGrantType) {
            return authGrantType;
        }
        String normalized = AccountNormalizer.normalizeUpper(value == null ? null : String.valueOf(value));
        if (normalized == null) {
            return PASSWORD;
        }
        AuthGrantType parsed = parse(normalized);
        return parsed == null ? PASSWORD : parsed;
    }

    /**
     * 安全解析枚举值。
     *
     * @param grantType 规范化后的登录方式文本
     * @return 对应枚举；无法识别时返回 {@code null}
     */
    private static AuthGrantType parse(String grantType) {
        try {
            return AuthGrantType.valueOf(grantType);
        }
        catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
