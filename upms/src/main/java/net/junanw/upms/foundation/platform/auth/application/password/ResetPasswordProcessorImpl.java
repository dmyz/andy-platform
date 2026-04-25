package net.junanw.upms.foundation.platform.auth.application.password;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.platform.auth.application.code.target.VerificationTargetProfileHandler;
import net.junanw.upms.foundation.platform.auth.application.code.target.VerificationTargetProfileHandlerRegistry;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialEntity;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialMapper;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationScene;
import net.junanw.upms.foundation.platform.auth.query.model.UserProfileSnapshot;
import net.junanw.upms.foundation.platform.auth.verification.service.VerificationCodeService;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.junanw.upms.foundation.platform.auth.application.password.credential.table.PasswordCredentialEntityTableDef.PASSWORD_CREDENTIAL_ENTITY;

/**
 * 重置密码处理器实现。
 * <p>
 * 负责基于手机号或邮箱验证码校验目标用户身份，
 * 再通过统一密码更新器完成密码重置。
 */
@Service
public class ResetPasswordProcessorImpl implements ResetPasswordProcessor {

    /**
     * 验证目标到用户档案的解析注册表。
     */
    private final VerificationTargetProfileHandlerRegistry verificationTargetProfileHandlerRegistry;

    /**
     * 验证码服务。
     */
    private final VerificationCodeService verificationCodeService;

    /**
     * 密码凭证 Mapper。
     */
    private final PasswordCredentialMapper passwordCredentialMapper;

    /**
     * 统一密码更新入口。
     */
    private final PasswordCredentialUpdater passwordCredentialUpdater;

    public ResetPasswordProcessorImpl(
            VerificationTargetProfileHandlerRegistry verificationTargetProfileHandlerRegistry,
            VerificationCodeService verificationCodeService,
            PasswordCredentialMapper passwordCredentialMapper,
            PasswordCredentialUpdater passwordCredentialUpdater
    ) {
        this.verificationTargetProfileHandlerRegistry = verificationTargetProfileHandlerRegistry;
        this.verificationCodeService = verificationCodeService;
        this.passwordCredentialMapper = passwordCredentialMapper;
        this.passwordCredentialUpdater = passwordCredentialUpdater;
    }

    /**
     * 执行重置密码流程。
     */
    @Override
    @Transactional
    public void reset(ResetPasswordCommand command) {
        // 1. 根据目标类型选择手机号/邮箱处理器，并完成目标归一化。
        VerificationTargetProfileHandler targetHandler = verificationTargetProfileHandlerRegistry.get(command.targetType());
        String normalizedTarget = targetHandler.normalizeRequiredTarget(command.target());

        // 2. 校验该目标最新验证码是否正确且未过期。
        verificationCodeService.verifyLatest(command.targetType(), normalizedTarget, VerificationScene.RESET_PASSWORD, command.code());

        // 3. 根据目标值定位到绑定的用户档案。
        UserProfileSnapshot snapshot = targetHandler.requireProfile(normalizedTarget);

        // 4. 读取密码凭证，若不存在则说明账号数据不完整。
        QueryWrapper query = QueryWrapper.create()
                .where(PASSWORD_CREDENTIAL_ENTITY.USER_ID.eq(snapshot.userId()));
        PasswordCredentialEntity credential = passwordCredentialMapper.selectOneByQuery(query);
        if (credential == null) {
            throw new BusinessException(404, "密码凭证不存在");
        }

        // 5. 通过统一更新器完成密码重置和状态清理。
        passwordCredentialUpdater.updatePassword(snapshot.userId(), credential, command.newPassword(), false);
    }
}
