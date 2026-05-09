package net.junanw.upms.application.upms.identity.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import net.junanw.upms.infrastructure.shared.api.ApiResponse;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.identity.user.model.request.UserRoleAssignRequest;
import net.junanw.upms.core.identity.user.model.request.UserSaveRequest;
import net.junanw.upms.core.identity.user.model.request.UserStatusUpdateRequest;
import net.junanw.upms.core.identity.user.model.response.UserImportResult;
import net.junanw.upms.core.identity.user.model.view.UserDetailView;
import net.junanw.upms.core.identity.user.model.view.UserPageItem;
import net.junanw.upms.core.identity.user.model.view.UserRoleItem;
import net.junanw.upms.core.identity.user.service.UserService;
import net.junanw.upms.infrastructure.shared.web.DateTimeRangeParser;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 用户管理控制器。
 *
 * <p>负责提供用户分页、导出、详情、创建、状态变更、密码重置、上下线和角色分配等接口。
 */
@RestController
@RequestMapping("/admin/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 分页查询用户列表。 */
    @GetMapping("/page")
    @SaCheckPermission("system:user:view")
    public ApiResponse<PageResponse<UserPageItem>> page(
            String username,
            String realName,
            String mobile,
            String orgName,
            Integer status,
            String startTime,
            String endTime,
            Integer pageNum,
            Integer pageSize
    ) {
        return ApiResponse.success(userService.page(
                username,
                realName,
                mobile,
                orgName,
                status,
                DateTimeRangeParser.parseStartTime(startTime),
                DateTimeRangeParser.parseEndTime(endTime),
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        ));
    }

    /**
     * 导出用户列表为 CSV。
     *
     * <p>导出结果会附带当前用户的角色编码列表，并使用 UTF-8 BOM 以兼容常见表格软件。
     */
    @GetMapping("/export")
    @SaCheckPermission("system:user:export")
    public ResponseEntity<byte[]> export(
            String username,
            String realName,
            String mobile,
            String orgName,
            Integer status,
            String startTime,
            String endTime
    ) {
        List<UserPageItem> items = userService.export(
                username,
                realName,
                mobile,
                orgName,
                status,
                DateTimeRangeParser.parseStartTime(startTime),
                DateTimeRangeParser.parseEndTime(endTime)
        );
        StringBuilder builder = new StringBuilder();
        builder.append("username,realName,jobNumber,mobile,email,orgName,position,status,roleCodes\n");
        items.forEach(item -> builder
                .append(csv(item.username())).append(',')
                .append(csv(item.realName())).append(',')
                .append(csv(item.jobNumber())).append(',')
                .append(csv(item.mobile())).append(',')
                .append(csv(item.email())).append(',')
                .append(csv(item.orgName())).append(',')
                .append(csv(item.position())).append(',')
                .append(csv(item.status() == null ? "" : item.status().toString())).append(',')
                .append(csv(String.join(";", userService.roles(item.id()).stream().map(UserRoleItem::code).toList())))
                .append('\n'));

        byte[] body = ("\uFEFF" + builder).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("user-export.csv", StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(body);
    }

    /** 查询用户详情。 */
    @GetMapping("/{id}")
    @SaCheckPermission("system:user:view")
    public ApiResponse<UserDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(userService.detail(id));
    }

    /** 创建用户。 */
    @PostMapping
    @SaCheckPermission("system:user:create")
    public ApiResponse<UserDetailView> create(@Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    /** 更新用户。 */
    @PutMapping("/{id}")
    @SaCheckPermission("system:user:update")
    public ApiResponse<UserDetailView> update(@PathVariable String id, @Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }

    /** 删除用户。 */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ApiResponse.success(null);
    }

    /** 更新用户状态。 */
    @PutMapping("/{id}/status")
    @SaCheckPermission("system:user:status")
    public ApiResponse<Void> updateStatus(@PathVariable String id, @Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateStatus(id, request.status());
        return ApiResponse.success(null);
    }

    /** 重置用户密码。 */
    @PostMapping("/{id}/password/reset")
    @SaCheckPermission("system:user:password:reset")
    public ApiResponse<Void> resetPassword(@PathVariable String id) {
        userService.resetPassword(id);
        return ApiResponse.success(null);
    }

    /** 强制用户下线。 */
    @PostMapping("/{id}/offline")
    @SaCheckPermission("system:user:offline")
    public ApiResponse<Void> offline(@PathVariable String id) {
        userService.offline(id);
        return ApiResponse.success(null);
    }

    /** 导入用户。 */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("system:user:import")
    public ApiResponse<UserImportResult> importUsers(MultipartFile file) {
        return ApiResponse.success(userService.importUsers(file));
    }

    /** 查询用户角色列表。 */
    @GetMapping("/{id}/roles")
    @SaCheckPermission("system:user:role:assign")
    public ApiResponse<List<UserRoleItem>> roles(@PathVariable String id) {
        return ApiResponse.success(userService.roles(id));
    }

    /** 分配用户角色。 */
    @PostMapping("/{id}/roles")
    @SaCheckPermission("system:user:role:assign")
    public ApiResponse<Void> assignRoles(@PathVariable String id, @Valid @RequestBody UserRoleAssignRequest request) {
        userService.assignRoles(id, request.roleCodes());
        return ApiResponse.success(null);
    }

    /**
     * 生成 CSV 安全文本列。
     *
     * @param value 原始值
     * @return 转义后的 CSV 列文本
     */
    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
