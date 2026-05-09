package net.junanw.upms.application.upms.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import net.junanw.upms.application.upms.auth.service.AuthSessionApplicationService;
import net.junanw.upms.core.identity.session.model.view.AuthSessionPageItem;
import net.junanw.upms.infrastructure.shared.api.ApiResponse;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端在线会话控制器。
 */
@RestController
@RequestMapping("/admin/auth/session")
public class AuthSessionController {

    private final AuthSessionApplicationService authSessionApplicationService;

    public AuthSessionController(AuthSessionApplicationService authSessionApplicationService) {
        this.authSessionApplicationService = authSessionApplicationService;
    }

    /**
     * 分页查询在线会话。
     */
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
        return ApiResponse.success(authSessionApplicationService.page(
                username,
                realName,
                loginType,
                ip,
                status,
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        ));
    }

    /**
     * 强制指定在线会话下线。
     */
    @PostMapping("/{id}/offline")
    @SaCheckPermission("auth:session:offline")
    public ApiResponse<Void> offline(@PathVariable("id") String id) {
        authSessionApplicationService.offline(id);
        return ApiResponse.success(null);
    }

    /**
     * 兼容旧版下线请求格式。
     */
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
        authSessionApplicationService.offline(resolvedId);
        return ApiResponse.success(null);
    }
}
