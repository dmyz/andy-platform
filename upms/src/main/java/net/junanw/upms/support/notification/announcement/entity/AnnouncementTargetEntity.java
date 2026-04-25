package net.junanw.upms.support.notification.announcement.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 公告投放目标实体。
 *
 * <p>用于描述公告投放范围，例如全部用户、指定组织、角色或指定用户。
 */
@Getter
@Setter
@Table("msg_target")
public class AnnouncementTargetEntity {

    /** 投放目标主键。 */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 公告主键。 */
    @Column("announcement_id")
    private Long announcementId;

    /** 目标类型。 */
    @Column("target_type")
    private String targetType;

    /** 目标值。 */
    @Column("target_value")
    private String targetValue;

    /** 解析后的目标主键。 */
    @Column("target_id")
    private Long targetId;

    /** 创建人主键。 */
    @Column("creator_id")
    private Long creatorId;

    /** 创建时间。 */
    @Column(value = "created_at", onInsertValue = "now()")
    private LocalDateTime createdAt;
}
