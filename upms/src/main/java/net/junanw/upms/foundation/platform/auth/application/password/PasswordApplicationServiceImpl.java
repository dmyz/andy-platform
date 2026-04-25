package net.junanw.upms.foundation.platform.auth.application.password;

import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import net.junanw.upms.foundation.platform.auth.application.password.model.request.ResetPasswordRequest;
import net.junanw.upms.foundation.platform.auth.authentication.context.LoginUserContext;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import org.springframework.stereotype.Service;

/**
 * 密码相关应用门面实现。
 * <p>
 * 负责承接控制器请求，并把改密、重置密码分别委托给对应处理器。
 */
@Service
public class PasswordApplicationServiceImpl implements PasswordApplicationService {

    /**
     * 当前登录人上下文。
     */
    private final LoginUserContext loginUserContext;

    /**
     * 改密处理器。
     */
    private final ChangePasswordProcessor changePasswordProcessor;

    /**
     * 重置密码处理器。
     */
    private final ResetPasswordProcessor resetPasswordProcessor;

    public PasswordApplicationServiceImpl(
            LoginUserContext loginUserContext,
            ChangePasswordProcessor changePasswordProcessor,
            ResetPasswordProcessor resetPasswordProcessor
    ) {
        this.loginUserContext = loginUserContext;
        this.changePasswordProcessor = changePasswordProcessor;
        this.resetPasswordProcessor = resetPasswordProcessor;
    }

    /**
     * 当前登录人修改自己的密码。
     */
    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // 请求级快速校验：避免把明显非法请求继续下沉到处理器层。
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException(400, "新密码不能与原密码相同");
        }

        // 构造完整内部命令，并绑定当前登录用户名。
        changePasswordProcessor.change(new ChangePasswordCommand(
                loginUserContext.getLoginUsername(),
                oldPassword,
                newPassword
        ));
    }

    /**
     * 通过验证码校验后重置目标账号密码。
     */
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // 先把 HTTP 请求转换成内部命令，再交给重置密码处理器执行。
        resetPasswordProcessor.reset(new ResetPasswordCommand(
                VerificationTargetType.from(request.targetType()),
                request.target(),
                request.code(),
                request.newPassword()
        ));
    }
}
