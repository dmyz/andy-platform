package net.junanw.upms.core.identity.permission;

/**
 * 权限访问策略。
 *
 * <p>集中声明登录后公共功能，避免个人中心这类能力继续进入角色授权或导航授权流程。
 */
public final class PermissionAccessPolicy {

    private static final String PROFILE_MODULE_CODE = "profile";
    private static final String PROFILE_PERMISSION_PREFIX = "profile:";

    private PermissionAccessPolicy() {
    }

    /**
     * 判断权限编码是否属于登录后公共能力。
     *
     * @param permissionCode 权限编码
     * @return 属于公共能力时返回 true
     */
    public static boolean isLoginOnlyPermissionCode(String permissionCode) {
        String normalized = normalize(permissionCode);
        return normalized != null && normalized.startsWith(PROFILE_PERMISSION_PREFIX);
    }

    /**
     * 判断模块编码是否属于登录后公共能力。
     *
     * @param moduleCode 模块编码
     * @return 属于公共能力时返回 true
     */
    public static boolean isLoginOnlyModuleCode(String moduleCode) {
        return PROFILE_MODULE_CODE.equals(normalize(moduleCode));
    }

    /**
     * 判断导航编码是否属于登录后公共入口。
     *
     * @param navigationCode 导航编码
     * @return 属于公共入口时返回 true
     */
    public static boolean isLoginOnlyNavigationCode(String navigationCode) {
        return PROFILE_MODULE_CODE.equals(normalize(navigationCode));
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
