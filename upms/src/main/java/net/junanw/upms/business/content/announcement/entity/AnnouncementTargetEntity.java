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
 * 公告投放目标实体。
 *
 * <p>用于描述公告投放范围，例如全部用户、指定组织、角色或指定用户。
 */
@Getter
@Setter
@Table("msg_target")
public class AnnouncementTargetEntity {

    /** 投放目标主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 公告主键。 */
    private Long announcementId;

    /** 目标类型。 */
    private String targetType;

    /** 目标值。 */
    private String targetValue;

    /** 解析后的目标主键。 */
    private Long targetId;

    /** 创建人主键。 */
    private Long creatorId;

    /** 创建时间。 */
    @TableField(defaultValue = "{NOW}", neverUpdate = true)
    private LocalDateTime createdAt;
}
