package net.junanw.upms.foundation.platform.auth.application.code.send;

import net.junanw.upms.foundation.platform.auth.application.code.model.response.VerificationCodeSendResponse;
import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import net.junanw.upms.foundation.platform.auth.verification.service.VerificationCodeService;
import net.junanw.upms.foundation.shared.exception.BusinessException;

/**
 * 验证码发送器抽象基类。
 * <p>
 * 统一封装目标归一化、绑定校验和验证码下发，
 * 子类只需要声明各自的校验细节。
 */
public abstract class AbstractVerificationCodeSender implements VerificationCodeSender {

    /**
     * 用户上下文查询服务。
     */
    private final UserContextService userContextService;

    /**
     * 验证码服务。
     */
    private final VerificationCodeService verificationCodeService;

    protected AbstractVerificationCodeSender(
            UserContextService userContextService,
            VerificationCodeService verificationCodeService
    ) {
        this.userContextService = userContextService;
        this.verificationCodeService = verificationCodeService;
    }

    @Override
    public VerificationCodeSendResponse send(String target, String scene, int expireSeconds) {
        // 1. 归一化原始目标值，避免格式差异影响后续查询。
        String normalizedTarget = normalizeTarget(target);
        if (normalizedTarget == null) {
            throw new BusinessException(400, missingTargetMessage());
        }
        // 2. 确认该目标已经绑定用户。
        ensureBound(normalizedTarget);
        // 3. 调用验证码服务签发新验证码。
        var issued = verificationCodeService.issue(targetType().name(), normalizedTarget, scene, expireSeconds);
        return new VerificationCodeSendResponse(issued.scene(), issued.targetType(), issued.maskedTarget(), issued.expireSeconds(), null);
    }

    /**
     * 暴露给子类的用户上下文查询服务。
     */
    protected final UserContextService userContextService() {
        return userContextService;
    }

    /**
     * 对原始目标值做归一化处理。
     */
    protected abstract String normalizeTarget(String target);

    /**
     * 校验目标值是否已绑定到用户。
     */
    protected abstract void ensureBound(String normalizedTarget);

    /**
     * 目标值缺失时的错误提示。
     */
    protected abstract String missingTargetMessage();
}
