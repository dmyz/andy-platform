package net.junanw.upms.foundation.platform.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import net.junanw.upms.foundation.platform.auth.application.session.AuthOnlineSessionService;
import net.junanw.upms.foundation.platform.auth.application.session.model.view.AuthSessionPageItem;
import net.junanw.upms.foundation.shared.api.ApiResponse;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/auth/session")
public class AuthSessionController {

    private final AuthOnlineSessionService authOnlineSessionService;

    public AuthSessionController(AuthOnlineSessionService authOnlineSessionService) {
        this.authOnlineSessionService = authOnlineSessionService;
    }

    @GetMapping("/page")
    @SaCheckPermission("auth:session:view")
    public ApiResponse<PageResponse<AuthSessionPageItem>> page(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "realName", required = false) String realName,
            @RequestParam(name = "loginType", required = false) String loginType,
            @RequestParam(name = "ip", required = false) String ip,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "pageNum", required = false) Integer pageNum,
            @RequestParam(name = "pageSize", required = false) Integer pageSize
    ) {
        return ApiResponse.success(authOnlineSessionService.page(
                username,
                realName,
                loginType,
                ip,
                status,
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        ));
    }

    @PostMapping("/{id}/offline")
    @SaCheckPermission("auth:session:offline")
    public ApiResponse<Void> offline(@PathVariable("id") String id) {
        authOnlineSessionService.offline(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/offline")
    @SaCheckPermission("auth:session:offline")
    public ApiResponse<Void> offlineCompat(
            @RequestParam(name = "id", required = false) String id,
            @RequestBody(required = false) Map<String, String> requestBody
    ) {
        String resolvedId = id;
        if ((resolvedId == null || resolvedId.isBlank()) && requestBody != null) {
            resolvedId = requestBody.get("id");
        }
        if (resolvedId == null || resolvedId.isBlank()) {
            throw new BusinessException(400, "在线会话标识不能为空");
        }
        authOnlineSessionService.offline(resolvedId);
        return ApiResponse.success(null);
    }
}
