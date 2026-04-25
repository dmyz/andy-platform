package net.junanw.upms.system.iam.account.model;

import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;

/**
 * 账号类型枚举。
 *
 * <p>用于区分用户名、手机号、邮箱等不同账号标识类型。
 */
public enum AccountType {

    /** 用户名账号。 */
    USERNAME,

    /** 手机号账号。 */
    MOBILE,

    /** 邮箱账号。 */
    EMAIL;

    /**
     * 由验证码目标类型映射为账号类型。
     *
     * @param targetType 验证码目标类型
     * @return 对应账号类型
     */
    public static AccountType fromVerificationTargetType(VerificationTargetType targetType) {
        return switch (targetType) {
            case MOBILE -> MOBILE;
            case EMAIL -> EMAIL;
        };
    }
}
