package net.junanw.upms.core.security.audit.operation.service;

import java.util.Locale;
import java.util.Map;

/**
 * 操作审计类型目录。
 *
 * <p>集中维护模块编码到展示名称的映射规则，供操作审计记录展示时统一使用。
 */
final class OperationAuditTypeCatalog {

    /** 模块编码与模块名称映射。 */
    private static final Map<String, String> MODULE_NAMES = Map.ofEntries(
            Map.entry("auth", "认证管理"),
            Map.entry("org", "组织管理"),
            Map.entry("user", "用户管理"),
            Map.entry("role", "角色管理"),
            Map.entry("navigation", "导航管理"),
            Map.entry("permission", "权限定义"),
            Map.entry("dictionary", "字典管理"),
            Map.entry("setting", "系统配置"),
            Map.entry("announcement", "公告管理"),
            Map.entry("file", "文件管理"),
            Map.entry("profile", "个人中心")
    );

    private OperationAuditTypeCatalog() {
    }

    /**
     * 根据模块编码解析模块名称。
     *
     * @param moduleCode 模块编码
     * @return 展示名称
     */
    static String moduleName(String moduleCode) {
        String normalized = normalizeCode(moduleCode);
        return MODULE_NAMES.getOrDefault(normalized, normalized.toUpperCase(Locale.ROOT));
    }

    /**
     * 归一化模块编码。
     *
     * @param value 原始模块编码
     * @return 规范化后的模块编码
     */
    static String normalizeCode(String value) {
        if (value == null) {
            return "unknown";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? "unknown" : normalized;
    }
}
