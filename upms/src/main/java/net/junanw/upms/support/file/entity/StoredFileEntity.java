package net.junanw.upms.support.file.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import net.junanw.upms.foundation.shared.mybatisflex.BaseEntity;

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
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 文件编码。 */
    @Column("file_code")
    private String fileCode;

    /** 原始文件名。 */
    @Column("original_name")
    private String originalName;

    /** 实际存储文件名。 */
    @Column("storage_name")
    private String storageName;

    /** 分类编码。 */
    @Column("category_code")
    private String categoryCode;

    /** 文件扩展名。 */
    @Column("file_ext")
    private String fileExt;

    /** MIME 类型。 */
    @Column("mime_type")
    private String mimeType;

    /** 文件大小。 */
    @Column("size_bytes")
    private Long sizeBytes;

    /** 存储提供方。 */
    @Column("storage_provider")
    private String storageProvider;

    /** 存储桶名称。 */
    @Column("bucket_name")
    private String bucketName;

    /** 对象键。 */
    @Column("object_key")
    private String objectKey;

    /** 文件内容摘要。 */
    @Column("checksum")
    private String checksum;

    /** 可见性。 */
    @Column("visibility")
    private String visibility;

    /** 上传人用户主键。 */
    @Column("uploader_user_id")
    private Long uploaderUserId;

    /** 文件状态。 */
    @Column("status")
    private String status;

    /** 备注。 */
    @Column("remark")
    private String remark;

    /** 文件二进制内容。 */
    @Column("content_blob")
    private byte[] contentBlob;
}
