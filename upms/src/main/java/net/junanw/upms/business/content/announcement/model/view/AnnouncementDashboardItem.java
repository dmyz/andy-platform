package net.junanw.upms.business.content.announcement.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 公告看板项视图。
 *
 * <p>用于承载工作台等应用场景中的公告列表返回字段。
 *
 * @param id 公告 ID
 * @param title 公告标题
 * @param publishTime 发布时间
 * @param top 是否置顶
 * @param type 公告类型
 */
public record AnnouncementDashboardItem(
        String id,
        String title,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime publishTime,
        Boolean top,
        String type
) {
}
