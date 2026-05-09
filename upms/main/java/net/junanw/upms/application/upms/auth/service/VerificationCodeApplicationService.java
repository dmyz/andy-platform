package net.junanw.upms.application.upms.auth.service;

import net.junanw.upms.core.identity.verification.model.response.VerificationCodeSendResponse;

/**
 * 验证码应用门面。
 */
public interface VerificationCodeApplicationService {

    /**
     * 发送手机验证码。
     */
    VerificationCodeSendResponse sendMobileCode(String mobile, String scene);

    /**
     * 发送邮箱验证码。
     */
    VerificationCodeSendResponse sendEmailCode(String email, String scene);
}
