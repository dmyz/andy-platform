package net.junanw.upms.infrastructure.persistence.xbatis;

import cn.xbatis.db.annotations.TableField;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * XBatis 可审计实体
 * 仅包含审计字段，不包含逻辑删除字段
 */
@Getter
@Setter
public abstract class AuditableEntity {

    /** 创建人ID */
    private Long creatorId;

    /** 创建时间 */
    @TableField(defaultValue = "{NOW}", neverUpdate = true)
    private LocalDateTime createdAt;

    /** 更新人ID */
    private Long updaterId;

    /** 更新时间 */
    @TableField(defaultValue = "{NOW}", updateDefaultValue = "{NOW}", updateDefaultValueFillAlways = true)
    private LocalDateTime updatedAt;
}
