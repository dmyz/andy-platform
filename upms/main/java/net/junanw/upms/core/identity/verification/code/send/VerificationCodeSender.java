package net.junanw.upms.core.identity.verification.code.send;

import net.junanw.upms.core.identity.verification.model.response.VerificationCodeSendResponse;
import net.junanw.upms.core.identity.verification.model.VerificationTargetType;

/**
 * 验证码发送器。
 */
public interface VerificationCodeSender {

    /**
     * 当前发送器支持的目标类型。
     */
    VerificationTargetType targetType();

    /**
     * 发送验证码。
     */
    VerificationCodeSendResponse send(String target, String scene, int expireSeconds);
}
