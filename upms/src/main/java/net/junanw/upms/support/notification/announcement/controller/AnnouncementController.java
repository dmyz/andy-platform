package net.junanw.upms.support.notification.announcement.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import net.junanw.upms.foundation.platform.auth.authentication.context.LoginUserContext;
import net.junanw.upms.foundation.shared.api.ApiResponse;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.support.notification.announcement.model.request.AnnouncementSaveRequest;
import net.junanw.upms.support.notification.announcement.model.view.AnnouncementDetailView;
import net.junanw.upms.support.notification.announcement.model.view.AnnouncementMyItem;
import net.junanw.upms.support.notification.announcement.model.view.AnnouncementPageItem;
import net.junanw.upms.support.notification.announcement.service.AnnouncementService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AnnouncementController 控制器。
 *
 * <p>负责提供 Announcement 相关 HTTP 接口并委托服务层处理具体业务。
 */
@RestController
@RequestMapping("/admin/announcement")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final LoginUserContext loginUserContext;

    public AnnouncementController(AnnouncementService announcementService, LoginUserContext loginUserContext) {
        this.announcementService = announcementService;
        this.loginUserContext = loginUserContext;
    }

    @GetMapping("/page")
    @SaCheckPermission("announcement:manage:view")
    /** 查询公告分页。 */
    public ApiResponse<PageResponse<AnnouncementPageItem>> page(String title, String type, String status, Integer pageNum, Integer pageSize) {
        return ApiResponse.success(announcementService.page(title, type, status, pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("announcement:manage:view")
    /** 查询公告详情。 */
    public ApiResponse<AnnouncementDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(announcementService.detail(id));
    }

    @PostMapping
    @SaCheckPermission("announcement:manage:create")
    /** 创建公告。 */
    public ApiResponse<AnnouncementDetailView> create(@Valid @RequestBody AnnouncementSaveRequest request) {
        return ApiResponse.success(announcementService.create(loginUserContext.getLoginUsername(), request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("announcement:manage:update")
    /** 更新公告。 */
    public ApiResponse<AnnouncementDetailView> update(@PathVariable String id, @Valid @RequestBody AnnouncementSaveRequest request) {
        return ApiResponse.success(announcementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("announcement:manage:delete")
    /** 删除公告。 */
    public ApiResponse<Void> delete(@PathVariable String id) {
        announcementService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/publish")
    @SaCheckPermission("announcement:manage:publish")
    /** 发布公告。 */
    public ApiResponse<AnnouncementDetailView> publish(@PathVariable String id) {
        return ApiResponse.success(announcementService.publish(id));
    }

    @PostMapping("/{id}/revoke")
    @SaCheckPermission("announcement:manage:revoke")
    /** 撤回公告。 */
    public ApiResponse<AnnouncementDetailView> revoke(@PathVariable String id) {
        return ApiResponse.success(announcementService.revoke(id));
    }

    @PostMapping("/{id}/read")
    @SaCheckPermission("announcement:inbox:read")
    /** 标记公告已读。 */
    public ApiResponse<Void> read(@PathVariable String id) {
        announcementService.markRead(loginUserContext.getLoginUsername(), id);
        return ApiResponse.success(null);
    }

    @GetMapping("/my/page")
    @SaCheckPermission("announcement:inbox:view")
    /** 查询当前用户公告分页。 */
    public ApiResponse<PageResponse<AnnouncementMyItem>> myPage(
            String title,
            @RequestParam(required = false) Boolean read,
            Integer pageNum,
            Integer pageSize
    ) {
        return ApiResponse.success(announcementService.myPage(loginUserContext.getLoginUsername(), title, read, pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize));
    }
}
