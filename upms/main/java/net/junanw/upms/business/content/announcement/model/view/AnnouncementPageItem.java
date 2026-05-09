package net.junanw.upms.business.content.announcement.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 公告分页项视图。
 *
 * <p>用于承载公告分页列表的返回字段。
 *
 * @param id 公告 ID
 * @param title 公告标题
 * @param type 公告类型
 * @param status 公告状态
 * @param top 是否置顶
 * @param targetType 投放目标类型
 * @param targetValue 投放目标值
 * @param creatorName 创建人姓名
 * @param publishTime 发布时间
 * @param updateTime 更新时间
 */

public record AnnouncementPageItem(
        String id,
        String title,
        String type,
        String status,
        Boolean top,
        String targetType,
        String targetValue,
        String creatorName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime publishTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updateTime
) {
}
