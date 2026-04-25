package net.junanw.upms.system.setting.service;

import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.system.setting.model.request.SettingSaveRequest;
import net.junanw.upms.system.setting.model.view.SettingDetailView;
import net.junanw.upms.system.setting.model.view.SettingPageItem;

/**
 * 系统配置服务接口。
 *
 * <p>定义配置项管理与按 Key 读取能力。
 */
public interface SettingService {

    /** 分页查询配置项。 */
    PageResponse<SettingPageItem> page(String settingName, String settingKey, String groupCode, Integer status, int pageNum, int pageSize);

    /** 查询配置详情。 */
    SettingDetailView detail(String id);

    /** 创建配置。 */
    SettingDetailView create(SettingSaveRequest request);

    /** 更新配置。 */
    SettingDetailView update(String id, SettingSaveRequest request);

    /** 删除配置。 */
    void delete(String id);

    /** 按 Key 查询配置。 */
    SettingDetailView getByKey(String settingKey);
}
