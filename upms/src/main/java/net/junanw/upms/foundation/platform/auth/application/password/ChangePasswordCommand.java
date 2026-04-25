package net.junanw.upms.foundation.platform.auth.application.password;

/**
 * 改密命令。
 *
 * @param username 当前登录用户名
 * @param oldPassword 原密码明文
 * @param newPassword 新密码明文
 */
public record ChangePasswordCommand(
        String username,
        String oldPassword,
        String newPassword
) {
}
