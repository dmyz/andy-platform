package net.junanw.upms.business.content.announcement.service;

import net.junanw.upms.business.content.announcement.model.view.AnnouncementDetailView;
import net.junanw.upms.business.content.announcement.model.view.AnnouncementDashboardItem;
import net.junanw.upms.business.content.announcement.model.view.AnnouncementMyItem;
import net.junanw.upms.business.content.announcement.model.view.AnnouncementPageItem;
import net.junanw.upms.business.content.announcement.model.request.AnnouncementSaveRequest;
import net.junanw.upms.infrastructure.shared.api.PageResponse;

import java.util.List;
/**
 * AnnouncementService 服务接口。
 *
 * <p>定义 Announcement 相关业务能力边界。
 */

/**
 * 公告服务接口。
 *
 * <p>定义公告管理、个人公告读取和工作台公告读取能力。
 */
public interface AnnouncementService {

    /** 查询公告分页。 */
    PageResponse<AnnouncementPageItem> page(String title, String type, String status, int pageNum, int pageSize);

    /** 查询公告详情。 */
    AnnouncementDetailView detail(String id);

    /** 创建公告。 */
    AnnouncementDetailView create(String operatorName, AnnouncementSaveRequest request);

    /** 更新公告。 */
    AnnouncementDetailView update(String id, AnnouncementSaveRequest request);

    /** 删除公告。 */
    void delete(String id);

    /** 发布公告。 */
    AnnouncementDetailView publish(String id);

    /** 撤回公告。 */
    AnnouncementDetailView revoke(String id);

    /** 查询当前用户公告分页。 */
    PageResponse<AnnouncementMyItem> myPage(String username, String title, Boolean read, int pageNum, int pageSize);

    /** 标记公告已读。 */
    void markRead(String username, String id);

    /** 查询工作台公告。 */
    List<AnnouncementDashboardItem> dashboardAnnouncements(String username, int limit);
}
