package net.junanw.upms.portal.profile.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import net.junanw.upms.foundation.platform.auth.authentication.context.LoginUserContext;
import net.junanw.upms.foundation.shared.api.ApiResponse;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.portal.profile.model.request.ProfileAvatarUpdateRequest;
import net.junanw.upms.portal.profile.model.request.ProfileEmailChangeRequest;
import net.junanw.upms.portal.profile.model.request.ProfileMobileChangeRequest;
import net.junanw.upms.portal.profile.model.request.ProfileUpdateRequest;
import net.junanw.upms.portal.profile.model.view.ProfileLoginAuditItem;
import net.junanw.upms.portal.profile.model.view.ProfileMeView;
import net.junanw.upms.portal.profile.model.view.ProfileMessageItem;
import net.junanw.upms.portal.profile.model.view.ProfileVerificationCodeSendView;
import net.junanw.upms.portal.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProfileController 控制器。
 *
 * <p>负责提供 Profile 相关 HTTP 接口并委托服务层处理具体业务。
 */
@RestController
@RequestMapping("/admin/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final LoginUserContext loginUserContext;

    public ProfileController(ProfileService profileService, LoginUserContext loginUserContext) {
        this.profileService = profileService;
        this.loginUserContext = loginUserContext;
    }

    @GetMapping("/me")
    @SaCheckPermission("profile:view")
    /** 查询当前登录人的个人资料。 */
    public ApiResponse<ProfileMeView> me() {
        return ApiResponse.success(profileService.me(loginUserContext.getLoginUsername()));
    }

    @PutMapping("/me")
    @SaCheckPermission("profile:update")
    /** 更新个人资料。 */
    public ApiResponse<ProfileMeView> updateMe(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.success(profileService.updateMe(loginUserContext.getLoginUsername(), request));
    }

    @PostMapping("/avatar")
    @SaCheckPermission("profile:avatar:update")
    /** 更新头像。 */
    public ApiResponse<ProfileMeView> updateAvatar(@Valid @RequestBody ProfileAvatarUpdateRequest request) {
        return ApiResponse.success(profileService.updateAvatar(loginUserContext.getLoginUsername(), request));
    }

    @PostMapping("/mobile/code/send")
    @SaCheckPermission("profile:mobile:update")
    /** 发送更换手机号验证码。 */
    public ApiResponse<ProfileVerificationCodeSendView> sendMobileChangeCode() {
        return ApiResponse.success(profileService.sendMobileChangeCode(loginUserContext.getLoginUsername()));
    }

    @PostMapping("/email/code/send")
    @SaCheckPermission("profile:email:update")
    /** 发送更换邮箱验证码。 */
    public ApiResponse<ProfileVerificationCodeSendView> sendEmailChangeCode() {
        return ApiResponse.success(profileService.sendEmailChangeCode(loginUserContext.getLoginUsername()));
    }

    @PostMapping("/mobile/change")
    @SaCheckPermission("profile:mobile:update")
    /** 更换手机号。 */
    public ApiResponse<ProfileMeView> changeMobile(@Valid @RequestBody ProfileMobileChangeRequest request) {
        return ApiResponse.success(profileService.changeMobile(loginUserContext.getLoginUsername(), request));
    }

    @PostMapping("/email/change")
    @SaCheckPermission("profile:email:update")
    /** 更换邮箱。 */
    public ApiResponse<ProfileMeView> changeEmail(@Valid @RequestBody ProfileEmailChangeRequest request) {
        return ApiResponse.success(profileService.changeEmail(loginUserContext.getLoginUsername(), request));
    }

    @GetMapping("/login-audit/page")
    @SaCheckPermission("profile:view")
    /** 查询个人登录审计分页。 */
    public ApiResponse<PageResponse<ProfileLoginAuditItem>> loginAuditPage(Integer pageNum, Integer pageSize) {
        return ApiResponse.success(profileService.loginAuditPage(loginUserContext.getLoginUsername(), pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize));
    }

    @GetMapping("/messages/page")
    @SaCheckPermission("profile:view")
    /** 查询个人消息分页。 */
    public ApiResponse<PageResponse<ProfileMessageItem>> messagePage(Integer pageNum, Integer pageSize) {
        return ApiResponse.success(profileService.messagePage(loginUserContext.getLoginUsername(), pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize));
    }
}
