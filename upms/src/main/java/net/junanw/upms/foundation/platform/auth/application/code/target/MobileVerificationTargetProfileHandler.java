package net.junanw.upms.foundation.platform.auth.application.code.target;

import net.junanw.upms.foundation.platform.auth.account.AccountNormalizer;
import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import net.junanw.upms.foundation.platform.auth.query.model.UserProfileSnapshot;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 手机目标用户画像解析器。
 *
 * <p>负责根据手机号目标查询并返回绑定用户快照，供重置密码等流程复用。
 */
@Component
public class MobileVerificationTargetProfileHandler extends AbstractVerificationTargetProfileHandler {

    public MobileVerificationTargetProfileHandler(UserContextService userContextService) {
        super(userContextService);
    }

    /**
     * 返回当前解析器支持的目标类型。
     *
     * @return 手机目标类型
     */
    @Override
    public VerificationTargetType targetType() {
        return VerificationTargetType.MOBILE;
    }

    /**
     * 归一化手机号目标。
     *
     * @param target 原始手机号
     * @return 归一化后的手机号
     */
    @Override
    protected String normalizeTarget(String target) {
        return AccountNormalizer.normalizeMobile(target);
    }

    /**
     * 根据手机号查找用户快照。
     *
     * @param normalizedTarget 归一化后的手机号
     * @return 用户快照
     */
    @Override
    protected Optional<UserProfileSnapshot> findProfile(String normalizedTarget) {
        return userContextService().findByMobile(normalizedTarget);
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

    /**
     * 返回手机号未绑定用户时的错误提示。
     *
     * @return 错误提示文本
     */
    @Override
    protected String missingBindingMessage() {
        return "手机号未绑定用户";
    }
}
