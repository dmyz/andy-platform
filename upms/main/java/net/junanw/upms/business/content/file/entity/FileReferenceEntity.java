package net.junanw.upms.business.content.file.entity;

import cn.xbatis.db.annotations.TableField;
import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
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
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 文件主键。 */
    private Long fileId;

    /** 归属对象类型。 */
    private String ownerType;

    /** 归属对象主键。 */
    private Long ownerId;

    /** 使用场景编码。 */
    private String usageCode;

    /** 排序号。 */
    private Integer sortOrder;

    /** 创建人主键。 */
    private Long creatorId;

    /** 创建时间。 */
    @TableField(defaultValue = "{NOW}", neverUpdate = true)
    private LocalDateTime createdAt;
}
