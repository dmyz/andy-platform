package net.junanw.upms.business.content.announcement.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 公告保存请求。
 *
 * <p>封装公告新增与编辑接口的请求参数。
 *
 * @param title 公告标题
 * @param content 公告内容
 * @param type 公告类型
 * @param targetType 投放目标类型
 * @param targetValue 投放目标值
 * @param top 是否置顶
 */

public record AnnouncementSaveRequest(
        @NotBlank(message = "title 不能为空") String title,
        @NotBlank(message = "content 不能为空") String content,
        @NotBlank(message = "type 不能为空") String type,
        @NotBlank(message = "targetType 不能为空") String targetType,
        String targetValue,
        @NotNull(message = "top 不能为空") Boolean top
) {
}
