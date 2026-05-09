package net.junanw.upms.core.identity.verification.code.target;

import net.junanw.upms.core.identity.account.AccountNormalizer;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;
import net.junanw.upms.core.identity.verification.model.VerificationTargetType;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 邮箱目标用户画像解析器。
 *
 * <p>负责根据邮箱目标查询并返回绑定用户快照，供重置密码等流程复用。
 */
@Component
public class EmailVerificationTargetProfileHandler extends AbstractVerificationTargetProfileHandler {

    public EmailVerificationTargetProfileHandler(UserContextService userContextService) {
        super(userContextService);
    }

    /**
     * 返回当前解析器支持的目标类型。
     *
     * @return 邮箱目标类型
     */
    @Override
    public VerificationTargetType targetType() {
        return VerificationTargetType.EMAIL;
    }

    /**
     * 归一化邮箱目标。
     *
     * @param target 原始邮箱
     * @return 归一化后的邮箱
     */
    @Override
    protected String normalizeTarget(String target) {
        return AccountNormalizer.normalizeEmail(target);
    }

    /**
     * 根据邮箱查找用户快照。
     *
     * @param normalizedTarget 归一化后的邮箱
     * @return 用户快照
     */
    @Override
    protected Optional<UserProfileSnapshot> findProfile(String normalizedTarget) {
        return userContextService().findByEmail(normalizedTarget);
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

    /**
     * 返回邮箱未绑定用户时的错误提示。
     *
     * @return 错误提示文本
     */
    @Override
    protected String missingBindingMessage() {
        return "邮箱未绑定用户";
    }
}
