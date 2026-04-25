package net.junanw.upms.foundation.platform.auth.application.code.send;

import net.junanw.upms.foundation.platform.auth.account.AccountNormalizer;
import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import net.junanw.upms.foundation.platform.auth.verification.service.VerificationCodeService;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * 手机验证码发送器。
 *
 * <p>负责处理手机号目标的归一化、绑定校验与验证码发送前置校验逻辑。
 */
@Component
public class MobileVerificationCodeSender extends AbstractVerificationCodeSender {

    public MobileVerificationCodeSender(
            UserContextService userContextService,
            VerificationCodeService verificationCodeService
    ) {
        super(userContextService, verificationCodeService);
    }

    /**
     * 返回当前发送器支持的目标类型。
     *
     * @return 手机目标类型
     */
    @Override
    public VerificationTargetType targetType() {
        return VerificationTargetType.MOBILE;
    }

    /**
     * 归一化手机号。
     *
     * @param target 原始手机号
     * @return 归一化后的手机号
     */
    @Override
    protected String normalizeTarget(String target) {
        return AccountNormalizer.normalizeMobile(target);
    }

    /**
     * 校验手机号已绑定用户。
     *
     * @param normalizedTarget 归一化后的手机号
     */
    @Override
    protected void ensureBound(String normalizedTarget) {
        userContextService().findByMobile(normalizedTarget)
                .orElseThrow(() -> new BusinessException(404, "手机号未绑定用户"));
    }

    /**
     * 返回手机号缺失时的错误提示。
     *
     * @return 错误提示文本
     */
    @Override
    protected String missingTargetMessage() {
        return "手机号不能为空";
    }
}
