package net.junanw.upms.foundation.platform.auth.verification.model;

/**
 * 验证码业务场景。
 */
public enum VerificationScene {

    /** 登录验证码。 */
    LOGIN,

    /** 重置密码验证码。 */
    RESET_PASSWORD,

    /** 更换手机号验证码。 */
    CHANGE_MOBILE,

    /** 更换邮箱验证码。 */
    CHANGE_EMAIL
}
