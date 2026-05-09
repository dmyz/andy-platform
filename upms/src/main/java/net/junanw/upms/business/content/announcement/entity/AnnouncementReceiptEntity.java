package net.junanw.upms.business.content.announcement.entity;

import cn.xbatis.db.annotations.TableField;
import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 公告回执实体。
 *
 * <p>用于记录某个用户对公告的投递状态、已读状态与阅读时间。
 */
@Getter
@Setter
@Table("msg_receipt")
public class AnnouncementReceiptEntity {

    /** 回执主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 公告主键。 */
    private Long announcementId;

    /** 用户主键。 */
    private Long userId;

    /** 投递状态。 */
    private String deliveryStatus;

    /** 投递时间。 */
    private LocalDateTime deliveredTime;

    /** 是否已读。 */
    private Boolean readFlag;

    /** 已读时间。 */
    private LocalDateTime readTime;

    /** 创建时间。 */
    @TableField(defaultValue = "{NOW}", neverUpdate = true)
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField(defaultValue = "{NOW}", updateDefaultValue = "{NOW}", updateDefaultValueFillAlways = true)
    private LocalDateTime updatedAt;
}
