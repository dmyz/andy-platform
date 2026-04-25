package net.junanw.upms.foundation.platform.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import net.junanw.upms.foundation.platform.auth.application.code.VerificationCodeApplicationService;
import net.junanw.upms.foundation.platform.auth.application.code.model.request.SendEmailCodeRequest;
import net.junanw.upms.foundation.platform.auth.application.code.model.request.SendMobileCodeRequest;
import net.junanw.upms.foundation.platform.auth.application.code.model.response.VerificationCodeSendResponse;
import net.junanw.upms.foundation.platform.auth.application.password.PasswordApplicationService;
import net.junanw.upms.foundation.platform.auth.application.password.model.request.ChangePasswordRequest;
import net.junanw.upms.foundation.platform.auth.application.password.model.request.ResetPasswordRequest;
import net.junanw.upms.foundation.platform.auth.application.session.AuthSessionApplicationService;
import net.junanw.upms.foundation.platform.auth.authentication.model.AuthRequestContext;
import net.junanw.upms.foundation.platform.auth.authentication.model.request.UnifiedLoginRequest;
import net.junanw.upms.foundation.platform.auth.authentication.model.response.LoginTokenResponse;
import net.junanw.upms.foundation.platform.auth.query.model.CurrentUserResponse;
import net.junanw.upms.foundation.shared.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证相关 HTTP 控制器。
 * <p>
 * 负责承接登录、登出、当前用户、验证码发送以及密码相关接口请求，
 * 自身只做 HTTP 适配，不承载认证和密码核心业务逻辑。
 */
@RestController
@RequestMapping("/admin/auth")
public class AuthController {

    /**
     * 会话门面。
     */
    private final AuthSessionApplicationService authSessionApplicationService;

    /**
     * 验证码门面。
     */
    private final VerificationCodeApplicationService verificationCodeApplicationService;

    /**
     * 密码门面。
     */
    private final PasswordApplicationService passwordApplicationService;

    public AuthController(
            AuthSessionApplicationService authSessionApplicationService,
            VerificationCodeApplicationService verificationCodeApplicationService,
            PasswordApplicationService passwordApplicationService
    ) {
        this.authSessionApplicationService = authSessionApplicationService;
        this.verificationCodeApplicationService = verificationCodeApplicationService;
        this.passwordApplicationService = passwordApplicationService;
    }

    /**
     * 登录接口。
     */
    @PostMapping("/login")
    public ApiResponse<LoginTokenResponse> login(@RequestBody UnifiedLoginRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.success(authSessionApplicationService.login(request, buildRequestContext(httpServletRequest)));
    }

    /**
     * 发送手机验证码。
     */
    @PostMapping("/code/mobile/send")
    public ApiResponse<VerificationCodeSendResponse> sendMobileCode(@Valid @RequestBody SendMobileCodeRequest request) {
        return ApiResponse.success(verificationCodeApplicationService.sendMobileCode(request.mobile(), request.scene()));
    }

    /**
     * 发送邮箱验证码。
     */
    @PostMapping("/code/email/send")
    public ApiResponse<VerificationCodeSendResponse> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest request) {
        return ApiResponse.success(verificationCodeApplicationService.sendEmailCode(request.email(), request.scene()));
    }

    /**
     * 获取当前登录用户。
     */
    @GetMapping("/current-user")
    public ApiResponse<CurrentUserResponse> currentUser() {
        return ApiResponse.success(authSessionApplicationService.currentUser());
    }

    /**
     * 登出接口。
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authSessionApplicationService.logout(buildRequestContext(request));
        return ApiResponse.success(null);
    }

    /**
     * 当前登录用户修改密码。
     */
    @PostMapping("/password/change")
    @SaCheckPermission("profile:password:update")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        passwordApplicationService.changePassword(request.oldPassword(), request.newPassword());
        return ApiResponse.success(null);
    }

    /**
     * 基于验证码重置密码。
     */
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordApplicationService.resetPassword(request);
        return ApiResponse.success(null);
    }

    /**
     * 占位验证码接口。
     */
    @GetMapping("/captcha")
    public ApiResponse<Map<String, String>> captcha() {
        return ApiResponse.success(Map.of("captchaId", "db-captcha", "captchaUrl", "https://via.placeholder.com/120x40?text=ABCD"));
    }

    /**
     * 从 HTTP 请求中提取认证请求上下文。
     */
    private AuthRequestContext buildRequestContext(HttpServletRequest request) {
        return new AuthRequestContext(
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("X-Trace-Id"),
                request.getHeader("X-Request-Id")
        );
    }
}
