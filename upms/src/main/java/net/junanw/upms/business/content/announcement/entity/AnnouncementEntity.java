package net.junanw.upms.business.content.announcement.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.BaseEntity;

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
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 消息编码。 */
    private String messageCode;

    /** 标题。 */
    private String title;

    /** 正文内容。 */
    private String content;

    /** 内容格式。 */
    private String contentFormat;

    /** 分类编码。 */
    private String categoryCode;

    /** 渠道类型。 */
    private String channelType;

    /** 发布状态。 */
    private String publishStatus;

    /** 发布时间。 */
    private LocalDateTime publishTime;

    /** 过期时间。 */
    private LocalDateTime expireTime;

    /** 优先级。 */
    private Integer priorityLevel;

    /** 是否置顶。 */
    private Boolean pinFlag;
}
