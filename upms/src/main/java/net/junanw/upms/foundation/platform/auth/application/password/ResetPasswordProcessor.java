package net.junanw.upms.foundation.platform.auth.application.password;

/**
 * 重置密码处理器。
 */
public interface ResetPasswordProcessor {

    /**
     * 执行基于验证码的重置密码流程。
     *
     * @param command 重置密码命令
     */
    void reset(ResetPasswordCommand command);
}
