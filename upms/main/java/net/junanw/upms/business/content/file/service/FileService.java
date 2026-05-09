package net.junanw.upms.business.content.file.service;

import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.business.content.file.model.view.FileDetailView;
import net.junanw.upms.business.content.file.model.view.FilePageItem;
import net.junanw.upms.business.content.file.model.view.FilePreviewView;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
/**
 * FileService 服务接口。
 *
 * <p>定义 File 相关业务能力边界。
 */

/**
 * 文件服务接口。
 *
 * <p>定义文件分页、上传、下载、预览和有效性校验能力。
 */
public interface FileService {

    /** 查询文件分页。 */
    PageResponse<FilePageItem> page(String fileName, String fileType, String uploaderName, LocalDateTime startTime, LocalDateTime endTime, int pageNum, int pageSize);

    /** 上传文件。 */
    FileDetailView upload(String uploaderName, MultipartFile file, String categoryCode, String remark);

    /** 查询文件详情。 */
    FileDetailView detail(String id);

    /** 下载文件。 */
    DownloadedFile download(String id);

    /** 预览文件。 */
    FilePreviewView preview(String id);

    /** 删除文件。 */
    void delete(String id);

    /** 校验文件仍处于有效状态。 */
    void validateActiveFile(Long fileId);

    /**
     * 下载文件载体。
     *
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param bytes 文件字节内容
     */
    record DownloadedFile(String fileName, String contentType, byte[] bytes) {
    }
}
