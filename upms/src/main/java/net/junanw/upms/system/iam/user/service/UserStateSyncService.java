package net.junanw.upms.system.iam.user.service;

import java.time.LocalDateTime;
/**
 * UserStateSyncService 服务接口。
 *
 * <p>定义 UserStateSync 相关业务能力边界。
 */

public interface UserStateSyncService {

    void markPasswordResetRequired(String username, boolean required);

    void recordLoginSuccess(String username, LocalDateTime loginTime);
}
