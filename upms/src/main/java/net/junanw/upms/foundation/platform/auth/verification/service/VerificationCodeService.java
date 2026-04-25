package net.junanw.upms.foundation.platform.auth.verification.service;

import net.junanw.upms.foundation.platform.auth.verification.model.IssuedVerificationCode;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationScene;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;

/**
 * 验证码服务。
 *
 * <p>向上层提供签发与校验两类稳定能力，既支持字符串入参，也提供基于枚举的便捷重载。
 */
public interface VerificationCodeService {

    /**
     * 签发验证码。
     *
     * @param targetType 目标类型
     * @param targetValue 目标值
     * @param scene 业务场景
     * @param expireSeconds 过期秒数
     * @return 签发结果摘要
     */
    IssuedVerificationCode issue(String targetType, String targetValue, String scene, int expireSeconds);

    /**
     * 使用枚举参数签发验证码。
     *
     * @param targetType 目标类型
     * @param targetValue 目标值
     * @param scene 业务场景
     * @param expireSeconds 过期秒数
     * @return 签发结果摘要
     */
    default IssuedVerificationCode issue(VerificationTargetType targetType, String targetValue, VerificationScene scene, int expireSeconds) {
        return issue(targetType.name(), targetValue, scene.name(), expireSeconds);
    }

    /**
     * 校验最近一次验证码。
     *
     * @param targetType 目标类型
     * @param targetValue 目标值
     * @param scene 业务场景
     * @param code 用户输入验证码
     */
    void verifyLatest(String targetType, String targetValue, String scene, String code);

    /**
     * 使用枚举参数校验最近一次验证码。
     *
     * @param targetType 目标类型
     * @param targetValue 目标值
     * @param scene 业务场景
     * @param code 用户输入验证码
     */
    default void verifyLatest(VerificationTargetType targetType, String targetValue, VerificationScene scene, String code) {
        verifyLatest(targetType.name(), targetValue, scene.name(), code);
    }
}
