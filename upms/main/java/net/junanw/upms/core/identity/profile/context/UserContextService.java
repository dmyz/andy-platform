package net.junanw.upms.core.identity.profile.context;

import net.junanw.upms.core.identity.profile.model.CurrentUserResponse;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;

import java.util.Optional;

/**
 * 当前用户上下文查询服务。
 * <p>
 * 对外提供按用户名、用户 ID、手机号、邮箱查询认证侧用户快照的能力，
 * 并负责构造当前用户响应视图。
 */
public interface UserContextService {

    /**
     * 构造当前登录用户响应。
     *
     * @param username 当前登录用户名
     * @return 当前用户视图
     */
    CurrentUserResponse buildCurrentUser(String username);

    /**
     * 按用户名加载用户快照，不存在时抛出异常。
     */
    UserProfileSnapshot requireByUsername(String username);

    /**
     * 按用户 ID 加载用户快照，不存在时抛出异常。
     */
    UserProfileSnapshot requireByUserId(Long userId);

    /**
     * 按手机号查找绑定用户快照。
     */
    Optional<UserProfileSnapshot> findByMobile(String mobile);

    /**
     * 按邮箱查找绑定用户快照。
     */
    Optional<UserProfileSnapshot> findByEmail(String email);
}
