package net.junanw.upms.business.content.file.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 文件详情视图。
 *
 * <p>用于承载文件详情接口的返回字段。
 *
 * @param id 文件 ID
 * @param fileName 文件名称
 * @param fileType 文件类型
 * @param fileSize 文件大小（字节）
 * @param uploaderName 上传人姓名
 * @param categoryCode 分类编码
 * @param remark 备注
 * @param uploadTime 上传时间
 */

public record FileDetailView(
        String id,
        String fileName,
        String fileType,
        Long fileSize,
        String uploaderName,
        String categoryCode,
        String remark,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime uploadTime
) {
}
