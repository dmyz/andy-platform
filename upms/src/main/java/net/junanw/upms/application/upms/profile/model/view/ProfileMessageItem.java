package net.junanw.upms.application.upms.profile.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 个人消息项视图。
 *
 * <p>用于承载个人中心消息列表的返回字段。
 *
 * @param id 消息 ID
 * @param title 消息标题
 * @param type 消息类型
 * @param publishTime 发布时间
 * @param read 是否已读
 * @param content 消息内容
 */

public record ProfileMessageItem(
        String id,
        String title,
        String type,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime publishTime,
        Boolean read,
        String content
) {
}
