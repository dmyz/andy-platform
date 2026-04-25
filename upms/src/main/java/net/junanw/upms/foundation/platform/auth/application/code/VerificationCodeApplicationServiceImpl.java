package net.junanw.upms.foundation.platform.auth.application.code;

import net.junanw.upms.foundation.platform.auth.application.code.model.response.VerificationCodeSendResponse;
import net.junanw.upms.foundation.platform.auth.application.code.send.VerificationCodeSendProcessor;
import net.junanw.upms.foundation.platform.auth.application.code.send.VerificationCodeSendRequest;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import org.springframework.stereotype.Service;

/**
 * 验证码应用门面实现。
 * <p>
 * 对外暴露手机/邮箱发码入口，内部统一转成发码命令交给发送处理器执行。
 */
@Service
public class VerificationCodeApplicationServiceImpl implements VerificationCodeApplicationService {

    /**
     * 验证码默认有效期，单位秒。
     */
    private static final int VERIFICATION_CODE_EXPIRE_SECONDS = 300;

    /**
     * 发码处理器。
     */
    private final VerificationCodeSendProcessor verificationCodeSendProcessor;

    public VerificationCodeApplicationServiceImpl(VerificationCodeSendProcessor verificationCodeSendProcessor) {
        this.verificationCodeSendProcessor = verificationCodeSendProcessor;
    }

    @Override
    public VerificationCodeSendResponse sendMobileCode(String mobile, String scene) {
        return sendVerificationCode(VerificationTargetType.MOBILE, mobile, scene);
    }

    @Override
    public VerificationCodeSendResponse sendEmailCode(String email, String scene) {
        return sendVerificationCode(VerificationTargetType.EMAIL, email, scene);
    }

    /**
     * 统一构造发码命令并委托底层处理器执行。
     */
    private VerificationCodeSendResponse sendVerificationCode(VerificationTargetType targetType, String target, String scene) {
        return verificationCodeSendProcessor.send(new VerificationCodeSendRequest(
                targetType,
                target,
                scene,
                VERIFICATION_CODE_EXPIRE_SECONDS
        ));
    }
}
