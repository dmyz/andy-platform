package net.junanw.upms.core.identity.verification.code.send;

import net.junanw.upms.core.identity.verification.model.response.VerificationCodeSendResponse;

/**
 * 验证码发送处理器。
 *
 * <p>定义验证码发送链路的统一入口，由应用服务构造内部请求后调用。
 */
public interface VerificationCodeSendProcessor {

    /**
     * 执行验证码发送流程。
     *
     * @param request 内部发码请求
     * @return 发送结果
     */
    VerificationCodeSendResponse send(VerificationCodeSendRequest request);
}
