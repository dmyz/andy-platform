package net.junanw.upms.core.identity.password;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.password.entity.PasswordCredentialEntity;
import net.junanw.upms.core.identity.password.mapper.PasswordCredentialMapper;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.security.PasswordEncoder;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


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
        UserEntity user = QueryChain.of(userMapper)
                .eq(UserEntity::getId, userId)
                .eq(UserEntity::getDeleted, false)
                .get();
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPasswordResetRequired(false);
        userMapper.update(user);
    }
}
