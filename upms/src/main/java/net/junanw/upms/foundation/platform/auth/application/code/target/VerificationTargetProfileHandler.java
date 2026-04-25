package net.junanw.upms.foundation.platform.auth.application.code.target;

import net.junanw.upms.foundation.platform.auth.query.model.UserProfileSnapshot;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;

/**
 * 验证目标档案处理器。
 */
public interface VerificationTargetProfileHandler {

    /**
     * 当前处理器支持的目标类型。
     */
    VerificationTargetType targetType();

    /**
     * 归一化并校验原始目标值。
     */
    String normalizeRequiredTarget(String target);

    /**
     * 根据归一化后的目标值加载绑定用户档案。
     */
    UserProfileSnapshot requireProfile(String normalizedTarget);
}
