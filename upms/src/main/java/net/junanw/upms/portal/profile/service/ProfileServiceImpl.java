package net.junanw.upms.portal.profile.service;

import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import net.junanw.upms.portal.profile.model.request.ProfileAvatarUpdateRequest;
import net.junanw.upms.portal.profile.model.request.ProfileEmailChangeRequest;
import net.junanw.upms.portal.profile.model.request.ProfileMobileChangeRequest;
import net.junanw.upms.portal.profile.model.request.ProfileUpdateRequest;
import net.junanw.upms.portal.profile.model.view.ProfileLoginAuditItem;
import net.junanw.upms.portal.profile.model.view.ProfileMeView;
import net.junanw.upms.portal.profile.model.view.ProfileMessageItem;
import net.junanw.upms.portal.profile.model.view.ProfileVerificationCodeSendView;
import net.junanw.upms.support.audit.login.model.view.LoginAuditPageItem;
import net.junanw.upms.support.audit.login.service.LoginAuditService;
import net.junanw.upms.support.file.service.FileService;
import net.junanw.upms.support.notification.announcement.service.AnnouncementService;
import net.junanw.upms.system.iam.account.entity.AccountEntity;
import net.junanw.upms.system.iam.account.model.AccountType;
import net.junanw.upms.system.iam.account.mapper.AccountMapper;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationScene;
import net.junanw.upms.foundation.platform.auth.query.model.UserProfileSnapshot;
import net.junanw.upms.foundation.platform.auth.query.context.UserContextService;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import net.junanw.upms.foundation.platform.auth.account.AccountNormalizer;
import net.junanw.upms.foundation.platform.auth.verification.service.VerificationCodeService;
import net.junanw.upms.system.iam.user.entity.UserEntity;
import net.junanw.upms.system.iam.user.mapper.UserMapper;
import net.junanw.upms.system.organization.entity.OrganizationMembershipEntity;
import net.junanw.upms.system.organization.repository.OrganizationMembershipMapper;
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
    private final UserMapper iamUserMapper;
    private final AccountMapper iamAccountMapper;
    private final OrganizationMembershipMapper orgMembershipMapper;
    private final FileService fileService;
    private final VerificationCodeService verificationCodeService;
    private final LoginAuditService loginAuditService;
    private final AnnouncementService announcementService;
    private final IdGenerator idGenerator;

    public ProfileServiceImpl(
            UserContextService userContextService,
            UserMapper iamUserMapper,
            AccountMapper iamAccountMapper,
            OrganizationMembershipMapper orgMembershipMapper,
            FileService fileService,
            VerificationCodeService verificationCodeService,
            LoginAuditService loginAuditService,
            AnnouncementService announcementService,
            IdGenerator idGenerator
    ) {
        this.userContextService = userContextService;
        this.iamUserMapper = iamUserMapper;
        this.iamAccountMapper = iamAccountMapper;
        this.orgMembershipMapper = orgMembershipMapper;
        this.fileService = fileService;
        this.verificationCodeService = verificationCodeService;
        this.loginAuditService = loginAuditService;
        this.announcementService = announcementService;
        this.idGenerator = idGenerator;
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
        UserEntity user = iamUserMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .where("id = {0}", snapshot.userId())
                        .and("deleted = false")
        );
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setDisplayName(request.realName().trim());
        user.setEmployeeNo(trimToNull(request.employeeNo()));
        user.setGender(normalizeGender(request.gender()));
        user.setRemark(trimToNull(request.remark()));
        iamUserMapper.update(user);
        OrganizationMembershipEntity membership = orgMembershipMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .where("user_id = {0}", snapshot.userId())
                        .and("is_primary_org = true")
                        .and("deleted = false")
        );
        if (membership != null) {
            membership.setPositionName(trimToNull(request.positionName()));
            orgMembershipMapper.update(membership);
        }
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
        UserEntity user = iamUserMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .where("id = {0}", snapshot.userId())
                        .and("deleted = false")
        );
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setAvatarFileId(avatarFileId);
        iamUserMapper.update(user);
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
        upsertAccount(snapshot.userId(), VerificationTargetType.MOBILE, request.newMobile(), AccountNormalizer.normalizeMobile(request.newMobile()));
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
        upsertAccount(snapshot.userId(), VerificationTargetType.EMAIL, request.newEmail(), AccountNormalizer.normalizeEmail(request.newEmail()));
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

    /** 新增或更新联系方式账号。 */
    private void upsertAccount(Long userId, VerificationTargetType targetType, String identifier, String normalizedIdentifier) {
        AccountType accountType = AccountType.fromVerificationTargetType(targetType);
        AccountEntity existing = iamAccountMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .where("account_type = {0}", accountType.name())
                        .and("normalized_identifier = {0}", normalizedIdentifier)
        );
        if (existing != null && !existing.getUserId().equals(userId)) {
            throw new BusinessException(400, targetType == VerificationTargetType.MOBILE ? "手机号已存在" : "邮箱已存在");
        }
        AccountEntity account = iamAccountMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .where("user_id = {0}", userId)
                        .and("account_type = {0}", accountType.name())
        );
        if (account == null) {
            account = new AccountEntity();
            account.setId(idGenerator.nextId());
            account.setUserId(userId);
            account.setAccountType(accountType.name());
        }
        account.setIdentifier(identifier.trim());
        account.setNormalizedIdentifier(normalizedIdentifier);
        account.setIsLoginEnabled(true);
        account.setIsPrimary(false);
        account.setVerifiedFlag(true);
        account.setStatus("ACTIVE");
        if (account.getCreatedAt() == null) {
            iamAccountMapper.insert(account);
        } else {
            iamAccountMapper.update(account);
        }
    }

    /** 转换为个人中心视图。 */
    private ProfileMeView toView(UserProfileSnapshot profile) {
        return new ProfileMeView(String.valueOf(profile.userId()), profile.username(), profile.displayName(), profile.employeeNo(), profile.mobile(), profile.email(), normalizeGender(profile.gender()), profile.avatar(), profile.orgName(), profile.positionName(), profile.remark());
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
}
