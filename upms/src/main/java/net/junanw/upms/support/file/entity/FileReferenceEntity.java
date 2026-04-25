package net.junanw.upms.support.file.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 文件引用关系实体。
 *
 * <p>用于保存文件与业务对象之间的绑定关系，支持一个文件被不同业务场景复用。
 */
@Getter
@Setter
@Table("file_binding")
public class FileReferenceEntity {

    /** 引用主键。 */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 文件主键。 */
    @Column("file_id")
    private Long fileId;

    /** 归属对象类型。 */
    @Column("owner_type")
    private String ownerType;

    /** 归属对象主键。 */
    @Column("owner_id")
    private Long ownerId;

    /** 使用场景编码。 */
    @Column("usage_code")
    private String usageCode;

    /** 排序号。 */
    @Column("sort_order")
    private Integer sortOrder;

    /** 创建人主键。 */
    @Column("creator_id")
    private Long creatorId;

    /** 创建时间。 */
    @Column(value = "created_at", onInsertValue = "now()")
    private LocalDateTime createdAt;
}
