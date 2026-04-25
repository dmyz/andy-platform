package net.junanw.upms.foundation.platform.auth.application.code.send;

import net.junanw.upms.foundation.platform.auth.application.code.model.response.VerificationCodeSendResponse;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;

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
