package net.junanw.upms.foundation.shared.mybatisflex;

import com.mybatisflex.annotation.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * MyBatis-Flex 可审计实体基类
 *
 * <p>仅包含审计字段，不包含逻辑删除字段。
 * 适用于关联表或不需要软删除的实体。
 */
@Getter
@Setter
public abstract class AuditableEntity {

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
