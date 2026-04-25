package net.junanw.upms.support.notification.announcement.model.view;

import java.time.LocalDateTime;

/**
 * 我的公告项视图。
 *
 * <p>用于承载当前用户公告列表的返回字段。
 *
 * @param id 公告 ID
 * @param title 公告标题
 * @param type 公告类型
 * @param publishTime 发布时间
 * @param read 是否已读
 * @param top 是否置顶
 * @param content 公告内容
 */

public record AnnouncementMyItem(
        String id,
        String title,
        String type,
        LocalDateTime publishTime,
        Boolean read,
        Boolean top,
        String content
) {
}
