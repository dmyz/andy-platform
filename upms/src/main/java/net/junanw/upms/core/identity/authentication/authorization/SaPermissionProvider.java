package net.junanw.upms.core.identity.authentication.authorization;

import cn.dev33.satoken.stp.StpInterface;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限与角色提供器。
 *
 * <p>该组件把平台已有的用户上下文查询能力适配到 Sa-Token 的授权扩展点上，
 * 让注解鉴权仍基于统一的用户快照数据工作。
 */
@Component
public class SaPermissionProvider implements StpInterface {

    private final UserContextService userContextService;

    public SaPermissionProvider(UserContextService userContextService) {
        this.userContextService = userContextService;
    }

    /**
     * 获取当前登录用户拥有的权限编码集合。
     *
     * @param loginId Sa-Token 登录标识
     * @param loginType Sa-Token 登录类型，当前未区分使用
     * @return 权限编码列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return resolveProfile(loginId).permissionCodes();
    }

    /**
     * 获取当前登录用户拥有的角色编码集合。
     *
     * @param loginId Sa-Token 登录标识
     * @param loginType Sa-Token 登录类型，当前未区分使用
     * @return 角色编码列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return resolveProfile(loginId).roleCodes();
    }

    /**
     * 根据登录标识回源解析用户快照。
     *
     * @param loginId Sa-Token 登录标识
     * @return 用户快照
     */
    private UserProfileSnapshot resolveProfile(Object loginId) {
        return userContextService.requireByUserId(Long.valueOf(String.valueOf(loginId)));
    }
}
