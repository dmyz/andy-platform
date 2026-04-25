package net.junanw.upms.foundation.shared.mybatisflex;

import com.mybatisflex.annotation.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * MyBatis-Flex 基础实体
 * 包含审计字段和逻辑删除字段
 */
@Getter
@Setter
public abstract class BaseEntity {

    /** 逻辑删除标记（0=未删除，1=已删除） */
    @Column(value = "deleted", isLogicDelete = true)
    private Boolean deleted = false;

    /** 创建人ID */
    @Column("creator_id")
    private Long creatorId;

    /** 创建时间 */
    @Column(value = "created_at", onInsertValue = "now()")
    private LocalDateTime createdAt;

    /** 更新人ID */
    @Column("updater_id")
    private Long updaterId;

    /** 更新时间 */
    @Column(value = "updated_at", onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updatedAt;
}
