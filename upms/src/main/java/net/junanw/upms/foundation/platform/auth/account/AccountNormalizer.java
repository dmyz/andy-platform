package net.junanw.upms.foundation.platform.auth.account;

import java.util.Locale;

/**
 * 账号标识归一化工具。
 *
 * <p>该工具为认证、验证码、资料绑定等多个子域提供统一的字符串标准化规则，
 * 避免同一账号在不同入口因大小写或空白差异造成匹配不一致。
 */
public final class AccountNormalizer {

    private AccountNormalizer() {
    }

    /**
     * 归一化用户名。
     *
     * @param username 原始用户名
     * @return 去空白并转小写后的用户名；若为空则返回 {@code null}
     */
    public static String normalizeUsername(String username) {
        return normalizeLower(username);
    }

    /**
     * 归一化手机号。
     *
     * <p>手机号只去掉前后空白与中间空白，不做国家码或格式化推断。
     *
     * @param mobile 原始手机号
     * @return 去空白后的手机号；若为空则返回 {@code null}
     */
    public static String normalizeMobile(String mobile) {
        String value = trimToNull(mobile);
        return value == null ? null : value.replaceAll("\s+", "");
    }

    /**
     * 归一化邮箱。
     *
     * @param email 原始邮箱
     * @return 去空白并转小写后的邮箱；若为空则返回 {@code null}
     */
    public static String normalizeEmail(String email) {
        return normalizeLower(email);
    }

    /**
     * 去空白并转小写。
     *
     * @param value 原始值
     * @return 处理后的结果；若为空则返回 {@code null}
     */
    public static String normalizeLower(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    /**
     * 去空白并转大写。
     *
     * @param value 原始值
     * @return 处理后的结果；若为空则返回 {@code null}
     */
    public static String normalizeUpper(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    /**
     * 去除首尾空白；空串转为 {@code null}。
     *
     * @param value 原始值
     * @return 去空白后的结果；若为空则返回 {@code null}
     */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
