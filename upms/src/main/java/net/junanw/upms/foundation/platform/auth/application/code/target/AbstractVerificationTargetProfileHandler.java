package net.junanw.upms.foundation.platform.auth.application.code.target;

import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import net.junanw.upms.foundation.platform.auth.query.model.UserProfileSnapshot;
import net.junanw.upms.foundation.shared.exception.BusinessException;

import java.util.Optional;

/**
 * 验证目标档案处理器抽象基类。
 * <p>
 * 统一封装目标归一化、空值校验和按目标加载用户档案。
 */
public abstract class AbstractVerificationTargetProfileHandler implements VerificationTargetProfileHandler {

    /**
     * 用户上下文查询服务。
     */
    private final UserContextService userContextService;

    protected AbstractVerificationTargetProfileHandler(UserContextService userContextService) {
        this.userContextService = userContextService;
    }

    @Override
    public String normalizeRequiredTarget(String target) {
        // 1. 对原始手机号/邮箱做归一化处理。
        String normalizedTarget = normalizeTarget(target);
        if (normalizedTarget == null) {
            throw new BusinessException(400, missingTargetMessage());
        }
        return normalizedTarget;
    }

    @Override
    public UserProfileSnapshot requireProfile(String normalizedTarget) {
        // 2. 根据归一化目标值加载绑定用户，找不到则直接失败。
        return findProfile(normalizedTarget)
                .orElseThrow(() -> new BusinessException(404, missingBindingMessage()));
    }

    /**
     * 暴露给子类的用户查询服务。
     */
    protected final UserContextService userContextService() {
        return userContextService;
    }

    protected abstract String normalizeTarget(String target);

    protected abstract Optional<UserProfileSnapshot> findProfile(String normalizedTarget);

    protected abstract String missingTargetMessage();

    protected abstract String missingBindingMessage();
}
