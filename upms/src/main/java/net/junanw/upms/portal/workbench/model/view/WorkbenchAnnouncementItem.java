package net.junanw.upms.portal.workbench.model.view;

import java.time.LocalDateTime;

/**
 * 工作台公告项视图。
 *
 * <p>用于承载工作台公告列表的返回字段。
 *
 * @param id 公告 ID
 * @param title 公告标题
 * @param publishTime 发布时间
 * @param top 是否置顶
 * @param type 公告类型
 */

public record WorkbenchAnnouncementItem(
        String id,
        String title,
        LocalDateTime publishTime,
        Boolean top,
        String type
) {
}
