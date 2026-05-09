package net.junanw.upms.business.content.file.entity;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.infrastructure.persistence.xbatis.BaseEntity;

/**
 * 存储文件实体。
 *
 * <p>用于保存文件元数据、内容摘要、存储定位信息和上传人信息。
 */
@Getter
@Setter
@Table("file_asset")
public class StoredFileEntity extends BaseEntity {

    /** 文件主键。 */
    @TableId(value = IdAutoType.GENERATOR , generator= Generators.nextId)
    private Long id;

    /** 文件编码。 */
    private String fileCode;

    /** 原始文件名。 */
    private String originalName;

    /** 实际存储文件名。 */
    private String storageName;

    /** 分类编码。 */
    private String categoryCode;

    /** 文件扩展名。 */
    private String fileExt;

    /** MIME 类型。 */
    private String mimeType;

    /** 文件大小。 */
    private Long sizeBytes;

    /** 存储提供方。 */
    private String storageProvider;

    /** 存储桶名称。 */
    private String bucketName;

    /** 对象键。 */
    private String objectKey;

    /** 文件内容摘要。 */
    private String checksum;

    /** 可见性。 */
    private String visibility;

    /** 上传人用户主键。 */
    private Long uploaderUserId;

    /** 文件状态。 */
    private String status;

    /** 备注。 */
    private String remark;

    /** 文件二进制内容。 */
    private byte[] contentBlob;
}
