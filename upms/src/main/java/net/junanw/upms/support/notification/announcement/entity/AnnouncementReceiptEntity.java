package net.junanw.upms.support.notification.announcement.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
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
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 公告主键。 */
    @Column("announcement_id")
    private Long announcementId;

    /** 用户主键。 */
    @Column("user_id")
    private Long userId;

    /** 投递状态。 */
    @Column("delivery_status")
    private String deliveryStatus;

    /** 投递时间。 */
    @Column("delivered_time")
    private LocalDateTime deliveredTime;

    /** 是否已读。 */
    @Column("read_flag")
    private Boolean readFlag;

    /** 已读时间。 */
    @Column("read_time")
    private LocalDateTime readTime;

    /** 创建时间。 */
    @Column(value = "created_at", onInsertValue = "now()")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @Column(value = "updated_at", onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updatedAt;
}
