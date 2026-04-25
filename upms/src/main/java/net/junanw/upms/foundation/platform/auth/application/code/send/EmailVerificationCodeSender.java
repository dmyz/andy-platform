package net.junanw.upms.foundation.platform.auth.application.code.send;

import net.junanw.upms.foundation.platform.auth.account.AccountNormalizer;
import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import net.junanw.upms.foundation.platform.auth.verification.service.VerificationCodeService;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * 邮箱验证码发送器。
 *
 * <p>负责处理邮箱目标的归一化、绑定校验与验证码发送前置校验逻辑。
 */
@Component
public class EmailVerificationCodeSender extends AbstractVerificationCodeSender {

    public EmailVerificationCodeSender(
            UserContextService userContextService,
            VerificationCodeService verificationCodeService
    ) {
        super(userContextService, verificationCodeService);
    }

    /**
     * 返回当前发送器支持的目标类型。
     *
     * @return 邮箱目标类型
     */
    @Override
    public VerificationTargetType targetType() {
        return VerificationTargetType.EMAIL;
    }

    /**
     * 归一化邮箱。
     *
     * @param target 原始邮箱
     * @return 归一化后的邮箱
     */
    @Override
    protected String normalizeTarget(String target) {
        return AccountNormalizer.normalizeEmail(target);
    }

    /**
     * 校验邮箱已绑定用户。
     *
     * @param normalizedTarget 归一化后的邮箱
     */
    @Override
    protected void ensureBound(String normalizedTarget) {
        userContextService().findByEmail(normalizedTarget)
                .orElseThrow(() -> new BusinessException(404, "邮箱未绑定用户"));
    }

    /**
     * 返回邮箱缺失时的错误提示。
     *
     * @return 错误提示文本
     */
    @Override
    protected String missingTargetMessage() {
        return "邮箱不能为空";
    }
}
