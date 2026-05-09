package net.junanw.upms.application.upms.profile.controller;

import jakarta.validation.Valid;
import net.junanw.upms.application.upms.profile.model.request.ProfileAvatarUpdateRequest;
import net.junanw.upms.application.upms.profile.model.request.ProfileEmailChangeRequest;
import net.junanw.upms.application.upms.profile.model.request.ProfileMobileChangeRequest;
import net.junanw.upms.application.upms.profile.model.request.ProfileUpdateRequest;
import net.junanw.upms.application.upms.profile.model.view.ProfileLoginAuditItem;
import net.junanw.upms.application.upms.profile.model.view.ProfileMeView;
import net.junanw.upms.application.upms.profile.model.view.ProfileMessageItem;
import net.junanw.upms.application.upms.profile.model.view.ProfileVerificationCodeSendView;
import net.junanw.upms.application.upms.profile.service.ProfileService;
import net.junanw.upms.core.identity.authentication.context.LoginUserContext;
import net.junanw.upms.infrastructure.shared.api.ApiResponse;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
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

    /** 查询当前登录人的个人资料。 */
    @GetMapping("/me")
    public ApiResponse<ProfileMeView> me() {
        return ApiResponse.success(profileService.me(loginUserContext.getLoginUsername()));
    }

    /** 更新个人资料。 */
    @PutMapping("/me")
    public ApiResponse<ProfileMeView> updateMe(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.success(profileService.updateMe(loginUserContext.getLoginUsername(), request));
    }

    /** 更新头像。 */
    @PostMapping("/avatar")
    public ApiResponse<ProfileMeView> updateAvatar(@Valid @RequestBody ProfileAvatarUpdateRequest request) {
        return ApiResponse.success(profileService.updateAvatar(loginUserContext.getLoginUsername(), request));
    }

    /** 发送更换手机号验证码。 */
    @PostMapping("/mobile/code/send")
    public ApiResponse<ProfileVerificationCodeSendView> sendMobileChangeCode() {
        return ApiResponse.success(profileService.sendMobileChangeCode(loginUserContext.getLoginUsername()));
    }

    /** 发送更换邮箱验证码。 */
    @PostMapping("/email/code/send")
    public ApiResponse<ProfileVerificationCodeSendView> sendEmailChangeCode() {
        return ApiResponse.success(profileService.sendEmailChangeCode(loginUserContext.getLoginUsername()));
    }

    /** 更换手机号。 */
    @PostMapping("/mobile/change")
    public ApiResponse<ProfileMeView> changeMobile(@Valid @RequestBody ProfileMobileChangeRequest request) {
        return ApiResponse.success(profileService.changeMobile(loginUserContext.getLoginUsername(), request));
    }

    /** 更换邮箱。 */
    @PostMapping("/email/change")
    public ApiResponse<ProfileMeView> changeEmail(@Valid @RequestBody ProfileEmailChangeRequest request) {
        return ApiResponse.success(profileService.changeEmail(loginUserContext.getLoginUsername(), request));
    }

    /** 查询个人登录审计分页。 */
    @GetMapping("/login-audit/page")
    public ApiResponse<PageResponse<ProfileLoginAuditItem>> loginAuditPage(Integer pageNum, Integer pageSize) {
        return ApiResponse.success(profileService.loginAuditPage(loginUserContext.getLoginUsername(), pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize));
    }

    /** 查询个人消息分页。 */
    @GetMapping("/messages/page")
    public ApiResponse<PageResponse<ProfileMessageItem>> messagePage(Integer pageNum, Integer pageSize) {
        return ApiResponse.success(profileService.messagePage(loginUserContext.getLoginUsername(), pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize));
    }
}
