package net.junanw.upms.application.upms.auth.service;

import net.junanw.upms.core.identity.password.model.request.ResetPasswordRequest;

/**
 * 密码应用门面。
 */
public interface PasswordApplicationService {

    /**
     * 当前登录用户修改自己的密码。
     *
     * @param oldPassword 原密码明文
     * @param newPassword 新密码明文
     */
    void changePassword(String oldPassword, String newPassword);

    /**
     * 基于验证码重置密码。
     *
     * @param request 重置密码请求
     */
    void resetPassword(ResetPasswordRequest request);
}
