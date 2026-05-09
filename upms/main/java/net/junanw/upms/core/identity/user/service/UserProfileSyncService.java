package net.junanw.upms.core.identity.user.service;

import java.util.List;
/**
 * UserProfileSyncService 服务接口。
 *
 * <p>定义 UserProfileSync 相关业务能力边界。
 */

public interface UserProfileSyncService {

    void registerProfile(
            String id,
            String username,
            String displayName,
            String mobile,
            String email,
            String employeeNo,
            String gender,
            String orgName,
            String positionName,
            String remark,
            List<String> roles,
            boolean passwordResetRequired
    );

    void updateProfile(
            String originalUsername,
            String username,
            String displayName,
            String mobile,
            String email,
            String employeeNo,
            String gender,
            String orgName,
            String positionName,
            String remark,
            List<String> roles,
            boolean passwordResetRequired
    );

    void removeProfile(String username);

    void updateRoles(String username, List<String> roleCodes);

    void markPasswordResetRequired(String username, boolean required);
}
