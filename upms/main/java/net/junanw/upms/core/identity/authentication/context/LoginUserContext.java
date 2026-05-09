package net.junanw.upms.core.identity.authentication.context;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import net.junanw.upms.core.identity.authentication.session.AuthSessionKeys;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户读取入口。
 *
 * <p>该组件屏蔽 Sa-Token session 访问细节，供审计、业务查询和当前用户接口统一获取登录人信息。
 */
@Component
public class LoginUserContext {

    private final UserContextService userContextService;

    public LoginUserContext(UserContextService userContextService) {
        this.userContextService = userContextService;
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前登录用户主键
     */
    public Long getLoginUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录用户名。
     *
     * <p>优先使用 token-session 中在登录成功时写入的用户名，避免每次都回源查询；
     * 若 session 中不存在，则回退到用户快照查询。
     *
     * @return 当前登录用户名
     */
    public String getLoginUsername() {
        SaSession tokenSession = StpUtil.getTokenSession();
        Object username = tokenSession.get(AuthSessionKeys.USERNAME);
        if (username instanceof String value && !value.isBlank()) {
            return value;
        }
        return getLoginProfile().username();
    }

    /**
     * 获取当前登录用户快照。
     *
     * @return 当前登录用户的聚合视图
     */
    public UserProfileSnapshot getLoginProfile() {
        return userContextService.requireByUserId(getLoginUserId());
    }

    /**
     * 获取当前登录用户名；未登录时返回 {@code null}。
     *
     * <p>该方法主要用于审计等横切逻辑，避免在匿名请求上触发鉴权异常。
     *
     * @return 当前用户名；未登录或获取失败时返回 {@code null}
     */
    public String getLoginUsernameDefaultNull() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId == null) {
                return null;
            }
            return getLoginUsername();
        }
        catch (RuntimeException exception) {
            return null;
        }
    }
}
