package net.junanw.upms.foundation.platform.auth.application.password;

import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialEntity;

/**
 * 统一密码凭证更新入口。
 */
public interface PasswordCredentialUpdater {

    /**
     * 更新用户密码及关联状态。
     *
     * @param userId 目标用户 ID
     * @param credential 已加载的密码凭证
     * @param newPassword 新密码明文
     * @param temporary 是否标记为临时密码
     */
    void updatePassword(Long userId, PasswordCredentialEntity credential, String newPassword, boolean temporary);
}
