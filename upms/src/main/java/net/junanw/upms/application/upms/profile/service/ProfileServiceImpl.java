package net.junanw.upms.application.upms.profile.service;

import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.application.upms.profile.model.request.ProfileAvatarUpdateRequest;
import net.junanw.upms.application.upms.profile.model.request.ProfileEmailChangeRequest;
import net.junanw.upms.application.upms.profile.model.request.ProfileMobileChangeRequest;
import net.junanw.upms.application.upms.profile.model.request.ProfileUpdateRequest;
import net.junanw.upms.application.upms.profile.model.view.ProfileLoginAuditItem;
import net.junanw.upms.application.upms.profile.model.view.ProfileMeView;
import net.junanw.upms.application.upms.profile.model.view.ProfileMessageItem;
import net.junanw.upms.application.upms.profile.model.view.ProfileVerificationCodeSendView;
import net.junanw.upms.core.security.audit.login.model.view.LoginAuditPageItem;
import net.junanw.upms.core.security.audit.login.service.LoginAuditService;
import net.junanw.upms.business.content.file.service.FileService;
import net.junanw.upms.business.content.announcement.service.AnnouncementService;
import net.junanw.upms.core.identity.verification.model.VerificationScene;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import net.junanw.upms.core.identity.verification.model.VerificationTargetType;
import net.junanw.upms.core.identity.account.AccountNormalizer;
import net.junanw.upms.core.identity.user.service.UserService;
import net.junanw.upms.core.identity.verification.service.VerificationCodeService;
import net.junanw.upms.core.organization.service.OrganizationService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProfileServiceImpl 服务实现。
 *
 * <p>负责承接 Profile 相关业务编排与规则落地。
 */
@Service
@Primary
public class ProfileServiceImpl implements ProfileService {

    private static final int EXPIRE_SECONDS = 300;

    private final UserContextService userContextService;
    private final UserService userService;
    private final OrganizationService organizationService;
    private final FileService fileService;
    private final VerificationCodeService verificationCodeService;
    private final LoginAuditService loginAuditService;
    private final AnnouncementService announcementService;

    public ProfileServiceImpl(
            UserContextService userContextService,
            UserService userService,
            OrganizationService organizationService,
            FileService fileService,
            VerificationCodeService verificationCodeService,
            LoginAuditService loginAuditService,
            AnnouncementService announcementService
    ) {
        this.userContextService = userContextService;
        this.userService = userService;
        this.organizationService = organizationService;
        this.fileService = fileService;
        this.verificationCodeService = verificationCodeService;
        this.loginAuditService = loginAuditService;
        this.announcementService = announcementService;
    }

    /**
     * 查询个人资料。
     */
    @Override
    @Transactional(readOnly = true)
    public ProfileMeView me(String username) {
        return toView(userContextService.requireByUsername(username));
    }

    /**
     * 更新个人资料。
     */
    @Override
    @Transactional
    public ProfileMeView updateMe(String username, ProfileUpdateRequest request) {
        UserProfileSnapshot snapshot = userContextService.requireByUsername(username);
        userService.updateProfile(snapshot.userId(), request.realName(), request.employeeNo(), normalizeGender(request.gender()), request.remark());
        organizationService.updatePrimaryPosition(snapshot.userId(), request.positionName());
        return me(username);
    }

    /**
     * 更新头像。
     */
    @Override
    @Transactional
    public ProfileMeView updateAvatar(String username, ProfileAvatarUpdateRequest request) {
        UserProfileSnapshot snapshot = userContextService.requireByUsername(username);
        Long avatarFileId = extractAvatarFileId(request.avatarUrl());
        fileService.validateActiveFile(avatarFileId);
        userService.updateAvatar(snapshot.userId(), avatarFileId);
        return me(username);
    }

    /**
     * 发送更换手机号验证码。
     */
    @Override
    @Transactional
    public ProfileVerificationCodeSendView sendMobileChangeCode(String username) {
        UserProfileSnapshot snapshot = userContextService.requireByUsername(username);
        String mobile = requireBoundTarget(AccountNormalizer.normalizeMobile(snapshot.mobile()), "当前账号未绑定手机号");
        return saveVerificationCode(VerificationTargetType.MOBILE, mobile, VerificationScene.CHANGE_MOBILE);
    }

    /**
     * 发送更换邮箱验证码。
     */
    @Override
    @Transactional
    public ProfileVerificationCodeSendView sendEmailChangeCode(String username) {
        UserProfileSnapshot snapshot = userContextService.requireByUsername(username);
        String email = requireBoundTarget(AccountNormalizer.normalizeEmail(snapshot.email()), "当前账号未绑定邮箱");
        return saveVerificationCode(VerificationTargetType.EMAIL, email, VerificationScene.CHANGE_EMAIL);
    }

    /**
     * 更换手机号。
     */
    @Override
    @Transactional
    public ProfileMeView changeMobile(String username, ProfileMobileChangeRequest request) {
        UserProfileSnapshot snapshot = userContextService.requireByUsername(username);
        validateMobileChange(snapshot.mobile(), request.newMobile());
        verifyCode(VerificationTargetType.MOBILE, AccountNormalizer.normalizeMobile(snapshot.mobile()), VerificationScene.CHANGE_MOBILE, request.code());
        userService.upsertVerifiedContact(snapshot.userId(), VerificationTargetType.MOBILE, request.newMobile(), AccountNormalizer.normalizeMobile(request.newMobile()));
        return me(username);
    }

    /**
     * 更换邮箱。
     */
    @Override
    @Transactional
    public ProfileMeView changeEmail(String username, ProfileEmailChangeRequest request) {
        UserProfileSnapshot snapshot = userContextService.requireByUsername(username);
        validateEmailChange(snapshot.email(), request.newEmail());
        verifyCode(VerificationTargetType.EMAIL, AccountNormalizer.normalizeEmail(snapshot.email()), VerificationScene.CHANGE_EMAIL, request.code());
        userService.upsertVerifiedContact(snapshot.userId(), VerificationTargetType.EMAIL, request.newEmail(), AccountNormalizer.normalizeEmail(request.newEmail()));
        return me(username);
    }

    /**
     * 查询个人登录审计分页。
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProfileLoginAuditItem> loginAuditPage(String username, int pageNum, int pageSize) {
        PageResponse<LoginAuditPageItem> page = loginAuditService.page(username, null, null, null, null, pageNum, pageSize);
        return PageResponse.of(page.list().stream().map(item -> new ProfileLoginAuditItem(item.id(), item.loginTime(), item.loginType(), item.ip(), item.browser(), item.result())).toList(), page.total(), page.pageNum(), page.pageSize());
    }

    /**
     * 查询个人消息分页。
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProfileMessageItem> messagePage(String username, int pageNum, int pageSize) {
        var page = announcementService.myPage(username, null, null, pageNum, pageSize);
        return PageResponse.of(page.list().stream().map(item -> new ProfileMessageItem(item.id(), item.title(), item.type(), item.publishTime(), item.read(), item.content())).toList(), page.total(), page.pageNum(), page.pageSize());
    }

    /** 保存并返回验证码发送结果。 */
    private ProfileVerificationCodeSendView saveVerificationCode(VerificationTargetType targetType, String targetValue, VerificationScene scene) {
        var issued = verificationCodeService.issue(targetType, targetValue, scene, EXPIRE_SECONDS);
        return new ProfileVerificationCodeSendView(issued.scene(), issued.targetType(), issued.maskedTarget(), issued.expireSeconds());
    }

    /** 校验验证码。 */
    private void verifyCode(VerificationTargetType targetType, String targetValue, VerificationScene scene, String code) {
        verificationCodeService.verifyLatest(targetType, targetValue, scene, code);
    }

    /** 确保当前账号已绑定目标联系方式。 */
    private String requireBoundTarget(String normalizedTarget, String message) {
        if (normalizedTarget == null) {
            throw new BusinessException(400, message);
        }
        return normalizedTarget;
    }

    /** 校验手机号变更请求。 */
    private void validateMobileChange(String currentMobile, String newMobile) {
        String normalizedCurrent = AccountNormalizer.normalizeMobile(currentMobile);
        String normalizedNew = AccountNormalizer.normalizeMobile(newMobile);
        if (normalizedNew == null) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        if (normalizedNew.equals(normalizedCurrent)) {
            throw new BusinessException(400, "新手机号不能与当前手机号相同");
        }
    }

    /** 校验邮箱变更请求。 */
    private void validateEmailChange(String currentEmail, String newEmail) {
        String normalizedCurrent = AccountNormalizer.normalizeEmail(currentEmail);
        String normalizedNew = AccountNormalizer.normalizeEmail(newEmail);
        if (normalizedNew == null) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        if (normalizedNew.equals(normalizedCurrent)) {
            throw new BusinessException(400, "新邮箱不能与当前邮箱相同");
        }
    }

    /** 转换为个人中心视图。 */
    private ProfileMeView toView(UserProfileSnapshot profile) {
        return new ProfileMeView(
                String.valueOf(profile.userId()),
                profile.username(),
                profile.displayName(),
                profile.employeeNo(),
                profile.mobile(),
                profile.email(),
                normalizeGender(profile.gender()),
                buildAvatarPreviewUrl(profile.avatarFileId()),
                profile.orgName(),
                profile.positionName(),
                profile.remark()
        );
    }

    /** 归一化性别值。 */
    private String normalizeGender(String gender) {
        String normalized = trimToNull(gender);
        if (normalized == null) {
            return null;
        }
        return switch (normalized.toUpperCase()) {
            case "男", "MALE" -> "MALE";
            case "女", "FEMALE" -> "FEMALE";
            default -> normalized;
        };
    }

    /** 去空白并把空串转为 null。 */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /** 从头像地址解析文件主键。 */
    private Long extractAvatarFileId(String avatarUrl) {
        String value = trimToNull(avatarUrl);
        if (value == null) {
            throw new BusinessException(400, "头像地址不能为空");
        }
        String digits = value.replaceAll(".*?/file/([0-9]+)(/preview)?$", "$1");
        try {
            return Long.valueOf(digits.equals(value) ? value : digits);
        }
        catch (NumberFormatException exception) {
            throw new BusinessException(400, "头像地址格式不正确");
        }
    }

    /** 构建管理端文件预览地址。 */
    private String buildAvatarPreviewUrl(Long avatarFileId) {
        return avatarFileId == null ? null : "/admin/file/" + avatarFileId + "/preview";
    }
}
