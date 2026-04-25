package net.junanw.upms.foundation.platform.auth.application.password;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialEntity;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialMapper;
import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import net.junanw.upms.foundation.platform.auth.query.model.UserProfileSnapshot;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.security.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.junanw.upms.foundation.platform.auth.application.password.credential.table.PasswordCredentialEntityTableDef.PASSWORD_CREDENTIAL_ENTITY;

/**
 * 改密处理器实现。
 * <p>
 * 负责校验当前用户、原密码以及密码凭证状态，
 * 最终通过共享更新器完成密码写入。
 */
@Service
public class ChangePasswordProcessorImpl implements ChangePasswordProcessor {

    /**
     * 用户上下文查询服务。
     */
    private final UserContextService userContextService;

    /**
     * 密码凭证 Mapper。
     */
    private final PasswordCredentialMapper passwordCredentialMapper;

    /**
     * 密码编码器，用于比对原密码。
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 统一密码更新入口。
     */
    private final PasswordCredentialUpdater passwordCredentialUpdater;

    public ChangePasswordProcessorImpl(
            UserContextService userContextService,
            PasswordCredentialMapper passwordCredentialMapper,
            PasswordEncoder passwordEncoder,
            PasswordCredentialUpdater passwordCredentialUpdater
    ) {
        this.userContextService = userContextService;
        this.passwordCredentialMapper = passwordCredentialMapper;
        this.passwordEncoder = passwordEncoder;
        this.passwordCredentialUpdater = passwordCredentialUpdater;
    }

    /**
     * 执行当前登录人的改密流程。
     */
    @Override
    @Transactional
    public void change(ChangePasswordCommand command) {
        // 1. 根据当前登录用户名加载完整用户快照。
        UserProfileSnapshot snapshot = userContextService.requireByUsername(command.username());

        // 2. 读取该用户的密码凭证；没有凭证视为异常数据状态。
        QueryWrapper query = QueryWrapper.create()
                .where(PASSWORD_CREDENTIAL_ENTITY.USER_ID.eq(snapshot.userId()));
        PasswordCredentialEntity credential = passwordCredentialMapper.selectOneByQuery(query);
        if (credential == null) {
            throw new BusinessException(404, "密码凭证不存在");
        }

        // 3. 校验原密码是否匹配，防止越权改密。
        if (!passwordEncoder.matches(command.oldPassword(), credential.getPasswordHash())) {
            throw new BusinessException(400, "原密码不正确");
        }

        // 4. 通过统一更新器完成密码写入和状态清理。
        passwordCredentialUpdater.updatePassword(snapshot.userId(), credential, command.newPassword(), false);
    }
}
