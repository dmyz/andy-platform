package net.junanw.upms.core.identity.password;

/**
 * 改密处理器。
 */
public interface ChangePasswordProcessor {

    /**
     * 执行当前用户改密流程。
     *
     * @param command 改密命令
     */
    void change(ChangePasswordCommand command);
}
