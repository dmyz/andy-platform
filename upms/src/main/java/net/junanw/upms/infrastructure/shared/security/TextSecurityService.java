package net.junanw.upms.infrastructure.shared.security;

import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * 文本安全服务。
 *
 * <p>统一处理用户输入中的控制字符、长度限制和 HTML 特殊字符转义，避免业务层重复实现。
 */
@Component
public class TextSecurityService {

    /** 默认文本最大长度。 */
    public static final int DEFAULT_MAX_LENGTH = 500;

    /** 公告标题最大长度。 */
    public static final int ANNOUNCEMENT_TITLE_MAX_LENGTH = 200;

    /** 公告内容最大长度。 */
    public static final int ANNOUNCEMENT_CONTENT_MAX_LENGTH = 10_000;

    /** 配置值最大长度。 */
    public static final int SETTING_VALUE_MAX_LENGTH = 20_000;

    /**
     * 清理普通文本输入。
     *
     * @param value 原始值
     * @param fieldName 字段名称
     * @param required 是否必填
     * @param maxLength 最大长度
     * @return 清理后的文本
     */
    public String sanitizePlainText(String value, String fieldName, boolean required, int maxLength) {
        String cleaned = stripUnsafeControlChars(value == null ? null : value.trim());
        if (cleaned == null || cleaned.isBlank()) {
            if (required) {
                throw new BusinessException(400, fieldName + "不能为空");
            }
            return null;
        }
        ensureLength(cleaned, fieldName, maxLength);
        return escapeHtml(cleaned);
    }

    /**
     * 对历史文本做输出兜底转义。
     *
     * @param value 原始值
     * @return 转义后的安全文本
     */
    public String sanitizeForOutput(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = stripUnsafeControlChars(value);
        return containsHtmlSensitiveChar(cleaned) ? escapeHtml(cleaned) : cleaned;
    }

    /**
     * 检查文本长度。
     *
     * @param value 文本
     * @param fieldName 字段名称
     * @param maxLength 最大长度
     */
    public void ensureLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new BusinessException(400, fieldName + "长度不能超过" + maxLength);
        }
    }

    /**
     * 移除不安全控制字符，保留换行、回车和制表符。
     *
     * @param value 原始值
     * @return 清理后的值
     */
    public String stripUnsafeControlChars(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '\n' || current == '\r' || current == '\t' || !Character.isISOControl(current)) {
                builder.append(current);
            }
        }
        return builder.toString();
    }

    /**
     * 转义 HTML 特殊字符。
     *
     * @param value 原始文本
     * @return 转义后的文本
     */
    public String escapeHtml(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '&' -> builder.append("&amp;");
                case '<' -> builder.append("&lt;");
                case '>' -> builder.append("&gt;");
                case '"' -> builder.append("&quot;");
                case '\'' -> builder.append("&#x27;");
                case '/' -> builder.append("&#x2F;");
                default -> builder.append(current);
            }
        }
        return builder.toString();
    }

    private boolean containsHtmlSensitiveChar(String value) {
        return value.indexOf('<') >= 0
                || value.indexOf('>') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\'') >= 0
                || value.indexOf('/') >= 0;
    }
}
