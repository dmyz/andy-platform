package net.junanw.upms.support.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import net.junanw.upms.foundation.platform.auth.authentication.context.LoginUserContext;
import net.junanw.upms.foundation.shared.api.ApiResponse;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.support.file.model.view.FileDetailView;
import net.junanw.upms.support.file.model.view.FilePageItem;
import net.junanw.upms.support.file.model.view.FilePreviewView;
import net.junanw.upms.support.file.service.FileService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * FileController 控制器。
 *
 * <p>负责提供 File 相关 HTTP 接口并委托服务层处理具体业务。
 */
@RestController
@RequestMapping("/admin/file")
public class FileController {

    private final FileService fileService;
    private final LoginUserContext loginUserContext;

    public FileController(FileService fileService, LoginUserContext loginUserContext) {
        this.fileService = fileService;
        this.loginUserContext = loginUserContext;
    }

    @GetMapping("/page")
    @SaCheckPermission("file:manage:view")
    /** 查询文件分页。 */
    public ApiResponse<PageResponse<FilePageItem>> page(
            String fileName,
            String fileType,
            String uploaderName,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            Integer pageNum,
            Integer pageSize
    ) {
        return ApiResponse.success(fileService.page(fileName, fileType, uploaderName, startTime, endTime, pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("file:manage:upload")
    /** 上传文件。 */
    public ApiResponse<FileDetailView> upload(MultipartFile file, @RequestParam(required = false) String categoryCode, @RequestParam(required = false) String remark) {
        return ApiResponse.success(fileService.upload(loginUserContext.getLoginUsername(), file, categoryCode, remark));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("file:manage:view")
    /** 查询文件详情。 */
    public ApiResponse<FileDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(fileService.detail(id));
    }

    @GetMapping("/{id}/download")
    @SaCheckPermission("file:manage:download")
    /** 下载文件。 */
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        FileService.DownloadedFile downloadedFile = fileService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(downloadedFile.fileName(), StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(MediaType.parseMediaType(downloadedFile.contentType()))
                .body(downloadedFile.bytes());
    }

    @GetMapping("/{id}/preview")
    @SaCheckPermission("file:manage:preview")
    /** 预览文件。 */
    public ApiResponse<FilePreviewView> preview(@PathVariable String id) {
        return ApiResponse.success(fileService.preview(id));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("file:manage:delete")
    /** 删除文件。 */
    public ApiResponse<Void> delete(@PathVariable String id) {
        fileService.delete(id);
        return ApiResponse.success(null);
    }
}
