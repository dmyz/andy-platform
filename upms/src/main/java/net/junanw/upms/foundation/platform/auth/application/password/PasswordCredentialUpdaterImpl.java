package net.junanw.upms.foundation.platform.auth.application.password;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialEntity;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialMapper;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.security.PasswordEncoder;
import net.junanw.upms.system.iam.user.entity.UserEntity;
import net.junanw.upms.system.iam.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static net.junanw.upms.system.iam.user.entity.table.UserEntityTableDef.USER_ENTITY;

/**
 * 统一密码落库更新器实现。
 * <p>
 * 改密、重置密码、初始化密码等场景都应通过该实现落库，
 * 以确保密码哈希、锁定状态和用户的重置标记保持一致。
 */
@Service
public class PasswordCredentialUpdaterImpl implements PasswordCredentialUpdater {

    /**
     * 密码凭证持久化 Mapper。
     */
    private final PasswordCredentialMapper passwordCredentialMapper;

    /**
     * 用户 Mapper，用于同步维护用户密码状态标记。
     */
    private final UserMapper userMapper;

    /**
     * 密码编码器。
     */
    private final PasswordEncoder passwordEncoder;

    public PasswordCredentialUpdaterImpl(
            PasswordCredentialMapper passwordCredentialMapper,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.passwordCredentialMapper = passwordCredentialMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 更新密码凭证并同步清理用户的"需要重置密码"标记。
     */
    @Override
    public void updatePassword(Long userId, PasswordCredentialEntity credential, String newPassword, boolean temporary) {
        // 1. 刷新密码凭证本身：重新计算哈希、重置失败次数、解除锁定。
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        credential.setPasswordAlgo("BCRYPT");
        credential.setPasswordChangedTime(LocalDateTime.now());
        credential.setTemporaryFlag(temporary);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        passwordCredentialMapper.update(credential);

        // 2. 密码已成功更新后，清理用户层面的"需要重置密码"标记。
        QueryWrapper userQuery = QueryWrapper.create()
                .where(USER_ENTITY.ID.eq(userId))
                .and(USER_ENTITY.DELETED.eq(false));
        UserEntity user = userMapper.selectOneByQuery(userQuery);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPasswordResetRequired(false);
        userMapper.update(user);
    }
}
