package net.junanw.upms.foundation.platform.auth.application.code.send;

import net.junanw.upms.foundation.platform.auth.application.code.model.response.VerificationCodeSendResponse;
import org.springframework.stereotype.Service;

/**
 * 验证码发送处理器实现。
 *
 * <p>负责根据目标类型路由到具体发送器，统一串联验证码发送前的目标校验、归一化和签发流程。
 */
@Service
public class VerificationCodeSendProcessorImpl implements VerificationCodeSendProcessor {

    /** 发码器注册表。 */
    private final VerificationCodeSenderRegistry verificationCodeSenderRegistry;

    public VerificationCodeSendProcessorImpl(VerificationCodeSenderRegistry verificationCodeSenderRegistry) {
        this.verificationCodeSenderRegistry = verificationCodeSenderRegistry;
    }

    /**
     * 执行验证码发送流程。
     *
     * @param request 内部发码请求
     * @return 发送结果
     */
    @Override
    public VerificationCodeSendResponse send(VerificationCodeSendRequest request) {
        VerificationCodeSender sender = verificationCodeSenderRegistry.get(request.targetType());
        return sender.send(request.target(), request.scene(), request.expireSeconds());
    }
}
