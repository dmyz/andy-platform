package net.junanw.upms.business.content.file.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.id.IdGenerator;
import net.junanw.upms.infrastructure.shared.security.FileTypeDetector;
import net.junanw.upms.infrastructure.shared.security.SensitiveWordService;
import net.junanw.upms.infrastructure.shared.security.TextSecurityService;
import net.junanw.upms.business.content.file.model.view.FileDetailView;
import net.junanw.upms.business.content.file.model.view.FilePageItem;
import net.junanw.upms.business.content.file.model.view.FilePreviewView;
import net.junanw.upms.business.content.file.entity.StoredFileEntity;
import net.junanw.upms.business.content.file.mapper.StoredFileMapper;
import net.junanw.upms.core.identity.account.entity.AccountEntity;
import net.junanw.upms.core.identity.account.model.AccountType;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.account.mapper.AccountMapper;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * FileServiceImpl 服务实现。
 *
 * <p>负责承接 File 相关业务编排与规则落地。
 */
@Service("fileServiceImpl")
@Primary
public class FileServiceImpl implements FileService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/png", "image/jpeg", "image/webp", "application/pdf", "text/plain"
    );

    private final StoredFileMapper fileAssetMapper;
    private final AccountMapper iamAccountMapper;
    private final UserMapper iamUserMapper;
    private final TextSecurityService textSecurityService;
    private final SensitiveWordService sensitiveWordService;

    public FileServiceImpl(
            StoredFileMapper fileAssetMapper,
            AccountMapper iamAccountMapper,
            UserMapper iamUserMapper,
            TextSecurityService textSecurityService,
            SensitiveWordService sensitiveWordService
    ) {
        this.fileAssetMapper = fileAssetMapper;
        this.iamAccountMapper = iamAccountMapper;
        this.iamUserMapper = iamUserMapper;
        this.textSecurityService = textSecurityService;
        this.sensitiveWordService = sensitiveWordService;
    }

    /**
     * 查询文件分页。
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<FilePageItem> page(
            String fileName,
            String fileType,
            String uploaderName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize
    ) {
        QueryChain<StoredFileEntity> query = QueryChain.of(fileAssetMapper)
                .eq(StoredFileEntity::getDeleted, false)
                .eq(StoredFileEntity::getStatus, "ACTIVE");

        String normalizedFileName = normalizeKeyword(fileName);
        String normalizedFileType = normalizeNullable(fileType);

        if (!normalizedFileName.isBlank()) {
            query.like(StoredFileEntity::getOriginalName, normalizedFileName);
        }
        if (normalizedFileType != null) {
            query.eq(StoredFileEntity::getMimeType, normalizedFileType);
        }
        if (startTime != null) {
            query.gte(StoredFileEntity::getCreatedAt, startTime);
        }
        if (endTime != null) {
            query.lte(StoredFileEntity::getCreatedAt, endTime);
        }

        query.orderByDesc(StoredFileEntity::getCreatedAt);

        List<StoredFileEntity> entities = query.list();
        UploaderContext uploaderContext = buildUploaderContext(entities);
        String normalizedUploaderName = normalizeKeyword(uploaderName);
        List<FilePageItem> filtered = entities.stream()
                .map(entity -> toPageItem(entity, uploaderContext))
                .filter(item -> normalizedUploaderName.isBlank() || item.uploaderName().toLowerCase(Locale.ROOT).contains(normalizedUploaderName))
                .sorted(Comparator.comparing(FilePageItem::uploadTime).reversed())
                .toList();

        int resolvedPageNum = Math.max(pageNum, 1);
        int resolvedPageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, filtered.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, filtered.size());
        return PageResponse.of(filtered.subList(fromIndex, toIndex), filtered.size(), resolvedPageNum, resolvedPageSize);
    }

    /**
     * 上传文件。
     */
    @Override
    @Transactional
    public FileDetailView upload(String uploaderName, MultipartFile file, String categoryCode, String remark) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "文件大小不能超过 5MB");
        }
        String contentType = FileTypeDetector.normalizeMime(file.getContentType());
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(400, "文件类型不支持");
        }
        sensitiveWordService.rejectIfPresent("文件备注", remark);

        try {
            long fileCodeSequence = IdGenerator.nextId();
            byte[] content = file.getBytes();
            String originalName = resolveFileName(file.getOriginalFilename(), fileCodeSequence);
            String fileExt = resolveFileExt(originalName);
            if (!FileTypeDetector.matches(contentType, fileExt, content)) {
                throw new BusinessException(400, "文件类型与内容不一致");
            }
            StoredFileEntity entity = new StoredFileEntity();
            entity.setFileCode("FILE-" + fileCodeSequence);
            entity.setOriginalName(originalName);
            entity.setStorageName(buildStorageName(entity.getFileCode(), entity.getOriginalName()));
            entity.setCategoryCode(normalizeNullable(categoryCode));
            entity.setFileExt(fileExt);
            entity.setMimeType(contentType);
            entity.setSizeBytes(file.getSize());
            entity.setStorageProvider("DATABASE");
            entity.setBucketName("db");
            entity.setObjectKey(entity.getFileCode());
            entity.setChecksum(sha256(content));
            entity.setVisibility("PRIVATE");
            entity.setUploaderUserId(resolveUploaderUserId(uploaderName));
            entity.setStatus("ACTIVE");
            entity.setRemark(textSecurityService.sanitizePlainText(remark, "文件备注", false, 500));
            entity.setContentBlob(content);
            fileAssetMapper.save(entity);
            return toDetailView(entity, buildUploaderContext(List.of(entity)));
        }
        catch (BusinessException exception) {
            throw exception;
        }
        catch (Exception exception) {
            throw new BusinessException(500, "文件上传失败");
        }
    }

    /**
     * 查询文件详情。
     */
    @Override
    @Transactional(readOnly = true)
    public FileDetailView detail(String id) {
        StoredFileEntity entity = requireActiveFile(id);
        return toDetailView(entity, buildUploaderContext(List.of(entity)));
    }

    /**
     * 下载文件。
     */
    @Override
    @Transactional(readOnly = true)
    public DownloadedFile download(String id) {
        StoredFileEntity entity = requireActiveFile(id);
        return new DownloadedFile(entity.getOriginalName(), entity.getMimeType(), entity.getContentBlob() == null ? new byte[0] : entity.getContentBlob());
    }

    /**
     * 预览文件。
     */
    @Override
    @Transactional(readOnly = true)
    public FilePreviewView preview(String id) {
        StoredFileEntity entity = requireActiveFile(id);
        if (!ALLOWED_TYPES.contains(entity.getMimeType())) {
            throw new BusinessException(400, "当前文件类型不支持预览");
        }
        byte[] content = entity.getContentBlob() == null ? new byte[0] : entity.getContentBlob();
        return new FilePreviewView("data:" + entity.getMimeType() + ";base64," + Base64.getEncoder().encodeToString(content));
    }

    /**
     * 删除文件。
     */
    @Override
    @Transactional
    public void delete(String id) {
        StoredFileEntity entity = requireActiveFile(id);
        fileAssetMapper.deleteById(entity.getId());
    }

    /**
     * 校验文件有效性。
     */
    @Override
    @Transactional(readOnly = true)
    public void validateActiveFile(Long fileId) {
        if (fileId == null) {
            throw new BusinessException(404, "文件不存在");
        }
        StoredFileEntity entity = QueryChain.of(fileAssetMapper)
                .eq(StoredFileEntity::getId, fileId)
                .eq(StoredFileEntity::getDeleted, false)
                .eq(StoredFileEntity::getStatus, "ACTIVE")
                .get();
        if (entity == null) {
            throw new BusinessException(404, "文件不存在");
        }
    }

    /** 加载有效文件实体。 */
    private StoredFileEntity requireActiveFile(String id) {
        try {
            Long fileId = Long.valueOf(id);
            StoredFileEntity entity = QueryChain.of(fileAssetMapper)
                    .eq(StoredFileEntity::getId, fileId)
                    .eq(StoredFileEntity::getDeleted, false)
                    .eq(StoredFileEntity::getStatus, "ACTIVE")
                    .get();
            if (entity == null) {
                throw new BusinessException(404, "文件不存在");
            }
            return entity;
        }
        catch (NumberFormatException exception) {
            throw new BusinessException(404, "文件不存在");
        }
    }

    /** 构建上传人上下文。 */
    private UploaderContext buildUploaderContext(List<StoredFileEntity> entities) {
        List<Long> uploaderIds = entities.stream()
                .map(StoredFileEntity::getUploaderUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (uploaderIds.isEmpty()) {
            return new UploaderContext(Map.of(), Map.of());
        }

        Map<Long, UserEntity> userMap = QueryChain.of(iamUserMapper)
                .in(UserEntity::getId, uploaderIds)
                .eq(UserEntity::getDeleted, false)
                .list().stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));

        Map<Long, String> usernameMap = QueryChain.of(iamAccountMapper)
                .in(AccountEntity::getUserId, uploaderIds)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .list().stream()
                .collect(Collectors.toMap(AccountEntity::getUserId, AccountEntity::getIdentifier, (left, right) -> left));

        return new UploaderContext(userMap, usernameMap);
    }

    /** 转换为文件分页项。 */
    private FilePageItem toPageItem(StoredFileEntity entity, UploaderContext uploaderContext) {
        return new FilePageItem(
                String.valueOf(entity.getId()),
                textSecurityService.sanitizeForOutput(entity.getOriginalName()),
                entity.getMimeType(),
                entity.getSizeBytes(),
                resolveUploaderName(entity, uploaderContext),
                entity.getCategoryCode(),
                textSecurityService.sanitizeForOutput(entity.getRemark()),
                entity.getCreatedAt()
        );
    }

    /** 转换为文件详情视图。 */
    private FileDetailView toDetailView(StoredFileEntity entity, UploaderContext uploaderContext) {
        return new FileDetailView(
                String.valueOf(entity.getId()),
                textSecurityService.sanitizeForOutput(entity.getOriginalName()),
                entity.getMimeType(),
                entity.getSizeBytes(),
                resolveUploaderName(entity, uploaderContext),
                entity.getCategoryCode(),
                textSecurityService.sanitizeForOutput(entity.getRemark()),
                entity.getCreatedAt()
        );
    }

    /** 解析上传人名称。 */
    private String resolveUploaderName(StoredFileEntity entity, UploaderContext uploaderContext) {
        if (entity.getUploaderUserId() == null) {
            return "";
        }
        UserEntity user = uploaderContext.userMap().get(entity.getUploaderUserId());
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }
        return uploaderContext.usernameMap().getOrDefault(entity.getUploaderUserId(), String.valueOf(entity.getUploaderUserId()));
    }

    /** 根据上传人用户名解析用户主键。 */
    private Long resolveUploaderUserId(String uploaderName) {
        String normalized = normalizeKeyword(uploaderName);
        if (normalized.isBlank()) {
            return null;
        }
        AccountEntity account = QueryChain.of(iamAccountMapper)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .eq(AccountEntity::getNormalizedIdentifier, normalized)
                .eq(AccountEntity::getStatus, "ACTIVE")
                .eq(AccountEntity::getIsLoginEnabled, true)
                .get();
        return account != null ? account.getUserId() : null;
    }

    /** 解析最终文件名。 */
    private String resolveFileName(String originalFilename, long id) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "file-" + id;
        }
        String normalized = originalFilename.trim().replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        String safeName = slashIndex >= 0 ? normalized.substring(slashIndex + 1) : normalized;
        return textSecurityService.sanitizePlainText(safeName, "文件名", true, 255);
    }

    /** 构建存储文件名。 */
    private String buildStorageName(String fileCode, String originalName) {
        String ext = resolveFileExt(originalName);
        return ext == null ? fileCode : fileCode + "." + ext;
    }

    /** 解析文件扩展名。 */
    private String resolveFileExt(String fileName) {
        if (fileName == null) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }

    /** 计算文件内容 SHA-256。 */
    private String sha256(byte[] content) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(content));
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    /** 归一化关键字查询条件。 */
    private String normalizeKeyword(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    /** 归一化可空字符串。 */
    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * 上传人上下文聚合。
     */
    private record UploaderContext(Map<Long, UserEntity> userMap, Map<Long, String> usernameMap) {
    }
}
