package net.junanw.upms.core.identity.verification.model;

import net.junanw.upms.core.identity.account.AccountNormalizer;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;

/**
 * 验证码接收目标类型。
 */
public enum VerificationTargetType {

    /** 手机号目标。 */
    MOBILE,

    /** 邮箱目标。 */
    EMAIL;

    /**
     * 从请求值解析目标类型。
     *
     * @param targetType 原始目标类型
     * @return 解析后的目标类型
     */
    public static VerificationTargetType from(String targetType) {
        String normalized = AccountNormalizer.normalizeUpper(targetType);
        try {
            return VerificationTargetType.valueOf(normalized);
        }
        catch (RuntimeException exception) {
            throw new BusinessException(400, "不支持的目标类型");
        }
    }
}
