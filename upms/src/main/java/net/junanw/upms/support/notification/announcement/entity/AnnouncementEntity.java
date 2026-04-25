package net.junanw.upms.support.notification.announcement.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.BaseEntity;

import java.time.LocalDateTime;

/**
 * 公告实体。
 *
 * <p>用于保存公告内容、投放状态、发布时间和优先级等核心信息。
 */
@Getter
@Setter
@Table("msg_announcement")
public class AnnouncementEntity extends BaseEntity {

    /** 公告主键。 */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 消息编码。 */
    @Column("message_code")
    private String messageCode;

    /** 标题。 */
    @Column("title")
    private String title;

    /** 正文内容。 */
    @Column("content")
    private String content;

    /** 内容格式。 */
    @Column("content_format")
    private String contentFormat;

    /** 分类编码。 */
    @Column("category_code")
    private String categoryCode;

    /** 渠道类型。 */
    @Column("channel_type")
    private String channelType;

    /** 发布状态。 */
    @Column("publish_status")
    private String publishStatus;

    /** 发布时间。 */
    @Column("publish_time")
    private LocalDateTime publishTime;

    /** 过期时间。 */
    @Column("expire_time")
    private LocalDateTime expireTime;

    /** 优先级。 */
    @Column("priority_level")
    private Integer priorityLevel;

    /** 是否置顶。 */
    @Column("pin_flag")
    private Boolean pinFlag;
}
