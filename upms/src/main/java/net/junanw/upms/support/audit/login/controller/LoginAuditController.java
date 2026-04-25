package net.junanw.upms.support.audit.login.controller;

import net.junanw.upms.foundation.shared.api.ApiResponse;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.support.audit.login.model.view.LoginAuditPageItem;
import net.junanw.upms.support.audit.login.service.LoginAuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * LoginAuditController 控制器。
 *
 * <p>负责提供 LoginAudit 相关 HTTP 接口并委托服务层处理具体业务。
 */
@RestController
@RequestMapping("/admin/login-audit")
public class LoginAuditController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LoginAuditService loginAuditService;

    public LoginAuditController(LoginAuditService loginAuditService) {
        this.loginAuditService = loginAuditService;
    }

    @GetMapping("/page")
    @SaCheckPermission("audit:login:view")
    /** 查询登录审计分页。 */
    public ApiResponse<PageResponse<LoginAuditPageItem>> page(
            String username,
            String loginType,
            String result,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            Integer pageNum,
            Integer pageSize
    ) {
        return ApiResponse.success(loginAuditService.page(
                username,
                loginType,
                result,
                startTime,
                endTime,
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        ));
    }

    @GetMapping("/export")
    @SaCheckPermission("audit:login:export")
    /** 导出登录审计 CSV。 */
    public ResponseEntity<byte[]> export(
            String username,
            String loginType,
            String result,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        List<LoginAuditPageItem> items = loginAuditService.export(username, loginType, result, startTime, endTime);
        StringBuilder builder = new StringBuilder();
        builder.append("登录账号,用户姓名,登录方式,IP,浏览器信息,登录时间,登录结果,失败原因\n");
        items.forEach(item -> builder
                .append(csv(item.username())).append(',')
                .append(csv(item.realName())).append(',')
                .append(csv(item.loginType())).append(',')
                .append(csv(item.ip())).append(',')
                .append(csv(item.browser())).append(',')
                .append(csv(item.loginTime() == null ? "" : item.loginTime().format(DATE_TIME_FORMATTER))).append(',')
                .append(csv(item.result())).append(',')
                .append(csv(item.failureReason()))
                .append('\n'));

        byte[] body = ("\uFEFF" + builder).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("login-audit.csv", StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(body);
    }

    /** 转义 CSV 文本列。 */
    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
