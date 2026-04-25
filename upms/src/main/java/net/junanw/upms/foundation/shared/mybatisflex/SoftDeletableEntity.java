package net.junanw.upms.foundation.shared.mybatisflex;

import com.mybatisflex.annotation.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * MyBatis-Flex 软删除实体基类
 *
 * <p>包含逻辑删除标记和审计字段。
 * 适用于需要软删除功能的业务实体。
 */
@Getter
@Setter
public abstract class SoftDeletableEntity {

    /** 逻辑删除标记 */
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
