package net.junanw.upms.infrastructure.persistence.xbatis;

import cn.xbatis.db.annotations.LogicDelete;
import cn.xbatis.db.annotations.TableField;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * XBatis 软删除实体
 * 包含审计字段和逻辑删除字段
 */
@Getter
@Setter
public abstract class SoftDeletableEntity {

    /** 逻辑删除标记（0=未删除，1=已删除） */
    @LogicDelete(beforeValue = "0", afterValue = "1")
    private Boolean deleted = false;

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
