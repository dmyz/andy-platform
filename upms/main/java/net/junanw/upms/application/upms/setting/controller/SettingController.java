package net.junanw.upms.application.upms.setting.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import net.junanw.upms.infrastructure.shared.api.ApiResponse;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.setting.model.request.SettingSaveRequest;
import net.junanw.upms.core.setting.model.view.SettingDetailView;
import net.junanw.upms.core.setting.model.view.SettingPageItem;
import net.junanw.upms.core.setting.service.SettingService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统配置控制器。
 *
 * <p>负责提供配置项分页、详情、增删改和按 Key 查询接口。
 */
@RestController
@RequestMapping("/admin/setting")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    /** 分页查询配置项。 */
    @GetMapping("/page")
    @SaCheckPermission("system:setting:view")
    public ApiResponse<PageResponse<SettingPageItem>> page(
            String settingName,
            String settingKey,
            String groupCode,
            Integer status,
            Integer pageNum,
            Integer pageSize
    ) {
        return ApiResponse.success(settingService.page(
                settingName,
                settingKey,
                groupCode,
                status,
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        ));
    }

    /** 查询配置详情。 */
    @GetMapping("/{id}")
    @SaCheckPermission("system:setting:view")
    public ApiResponse<SettingDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(settingService.detail(id));
    }

    /** 创建配置。 */
    @PostMapping
    @SaCheckPermission("system:setting:create")
    public ApiResponse<SettingDetailView> create(@Valid @RequestBody SettingSaveRequest request) {
        return ApiResponse.success(settingService.create(request));
    }

    /** 更新配置。 */
    @PutMapping("/{id}")
    @SaCheckPermission("system:setting:update")
    public ApiResponse<SettingDetailView> update(@PathVariable String id, @Valid @RequestBody SettingSaveRequest request) {
        return ApiResponse.success(settingService.update(id, request));
    }

    /** 删除配置。 */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:setting:delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        settingService.delete(id);
        return ApiResponse.success(null);
    }

    /** 按 Key 查询配置。 */
    @GetMapping("/key/{settingKey}")
    public ApiResponse<SettingDetailView> getByKey(@PathVariable String settingKey) {
        return ApiResponse.success(settingService.getByKey(settingKey));
    }
}
