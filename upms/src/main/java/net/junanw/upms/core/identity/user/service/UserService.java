package net.junanw.upms.core.identity.user.service;

import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.identity.verification.model.VerificationTargetType;
import net.junanw.upms.core.identity.user.model.request.UserSaveRequest;
import net.junanw.upms.core.identity.user.model.response.UserImportResult;
import net.junanw.upms.core.identity.user.model.view.UserDetailView;
import net.junanw.upms.core.identity.user.model.view.UserPageItem;
import net.junanw.upms.core.identity.user.model.view.UserRoleItem;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务接口。
 *
 * <p>定义用户管理、角色分配、密码重置、导入导出与用户上下线能力。
 */
public interface UserService {

    PageResponse<UserPageItem> page(
            String username,
            String realName,
            String mobile,
            String orgName,
            Integer status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize
    );

    UserDetailView detail(String id);

    UserDetailView create(UserSaveRequest request);

    UserDetailView update(String id, UserSaveRequest request);

    void delete(String id);

    void updateStatus(String id, Integer status);

    void resetPassword(String id);

    void offline(String id);

    List<UserRoleItem> roles(String id);

    void assignRoles(String id, List<String> roleCodes);

    List<UserPageItem> export(String username, String realName, String mobile, String orgName, Integer status, LocalDateTime startTime, LocalDateTime endTime);

    UserImportResult importUsers(MultipartFile file);

    /** 更新用户个人资料字段。 */
    void updateProfile(Long userId, String displayName, String employeeNo, String gender, String remark);

    /** 更新用户头像文件。 */
    void updateAvatar(Long userId, Long avatarFileId);

    /** 新增或更新已验证联系方式账号。 */
    void upsertVerifiedContact(Long userId, VerificationTargetType targetType, String identifier, String normalizedIdentifier);
}
