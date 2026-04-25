package net.junanw.upms.support.file.service;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import net.junanw.upms.support.file.model.view.FileDetailView;
import net.junanw.upms.support.file.model.view.FilePageItem;
import net.junanw.upms.support.file.model.view.FilePreviewView;
import net.junanw.upms.support.file.entity.StoredFileEntity;
import net.junanw.upms.support.file.repository.StoredFileMapper;
import net.junanw.upms.system.iam.account.entity.AccountEntity;
import net.junanw.upms.system.iam.account.model.AccountType;
import net.junanw.upms.system.iam.user.entity.UserEntity;
import net.junanw.upms.system.iam.account.mapper.AccountMapper;
import net.junanw.upms.system.iam.user.mapper.UserMapper;
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

import static net.junanw.upms.support.file.entity.table.StoredFileEntityTableDef.STORED_FILE_ENTITY;
import static net.junanw.upms.system.iam.account.entity.table.AccountEntityTableDef.ACCOUNT_ENTITY;
import static net.junanw.upms.system.iam.user.entity.table.UserEntityTableDef.USER_ENTITY;

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
    private final IdGenerator idGenerator;

    public FileServiceImpl(
            StoredFileMapper fileAssetMapper,
            AccountMapper iamAccountMapper,
            UserMapper iamUserMapper,
            IdGenerator idGenerator
    ) {
        this.fileAssetMapper = fileAssetMapper;
        this.iamAccountMapper = iamAccountMapper;
        this.iamUserMapper = iamUserMapper;
        this.idGenerator = idGenerator;
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
        QueryWrapper query = QueryWrapper.create()
                .where(STORED_FILE_ENTITY.DELETED.eq(false))
                .and(STORED_FILE_ENTITY.STATUS.eq("ACTIVE"));

        String normalizedFileName = normalizeKeyword(fileName);
        String normalizedFileType = normalizeNullable(fileType);

        if (!normalizedFileName.isBlank()) {
            query.and(STORED_FILE_ENTITY.ORIGINAL_NAME.like("%" + normalizedFileName + "%"));
        }
        if (normalizedFileType != null) {
            query.and(STORED_FILE_ENTITY.MIME_TYPE.eq(normalizedFileType));
        }
        if (startTime != null) {
            query.and(STORED_FILE_ENTITY.CREATED_AT.ge(startTime));
        }
        if (endTime != null) {
            query.and(STORED_FILE_ENTITY.CREATED_AT.le(endTime));
        }

        query.orderBy(STORED_FILE_ENTITY.CREATED_AT.desc());

        List<StoredFileEntity> entities = fileAssetMapper.selectListByQuery(query);
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
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(400, "文件类型不支持");
        }

        try {
            long id = idGenerator.nextId();
            byte[] content = file.getBytes();
            StoredFileEntity entity = new StoredFileEntity();
            entity.setId(id);
            entity.setFileCode("FILE-" + id);
            entity.setOriginalName(resolveFileName(file.getOriginalFilename(), id));
            entity.setStorageName(buildStorageName(entity.getFileCode(), entity.getOriginalName()));
            entity.setCategoryCode(normalizeNullable(categoryCode));
            entity.setFileExt(resolveFileExt(entity.getOriginalName()));
            entity.setMimeType(contentType);
            entity.setSizeBytes(file.getSize());
            entity.setStorageProvider("DATABASE");
            entity.setBucketName("db");
            entity.setObjectKey(entity.getFileCode());
            entity.setChecksum(sha256(content));
            entity.setVisibility("PRIVATE");
            entity.setUploaderUserId(resolveUploaderUserId(uploaderName));
            entity.setStatus("ACTIVE");
            entity.setRemark(normalizeNullable(remark));
            entity.setContentBlob(content);
            fileAssetMapper.insert(entity);
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
        if (!(entity.getMimeType().startsWith("image/")
                || "application/pdf".equals(entity.getMimeType())
                || entity.getMimeType().startsWith("text/"))) {
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
        entity.setDeleted(true);
        fileAssetMapper.update(entity);
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
        QueryWrapper query = QueryWrapper.create()
                .where(STORED_FILE_ENTITY.ID.eq(fileId))
                .and(STORED_FILE_ENTITY.DELETED.eq(false))
                .and(STORED_FILE_ENTITY.STATUS.eq("ACTIVE"));
        StoredFileEntity entity = fileAssetMapper.selectOneByQuery(query);
        if (entity == null) {
            throw new BusinessException(404, "文件不存在");
        }
    }

    /** 加载有效文件实体。 */
    private StoredFileEntity requireActiveFile(String id) {
        try {
            Long fileId = Long.valueOf(id);
            QueryWrapper query = QueryWrapper.create()
                    .where(STORED_FILE_ENTITY.ID.eq(fileId))
                    .and(STORED_FILE_ENTITY.DELETED.eq(false))
                    .and(STORED_FILE_ENTITY.STATUS.eq("ACTIVE"));
            StoredFileEntity entity = fileAssetMapper.selectOneByQuery(query);
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

        QueryWrapper userQuery = QueryWrapper.create()
                .where(USER_ENTITY.ID.in(uploaderIds))
                .and(USER_ENTITY.DELETED.eq(false));
        Map<Long, UserEntity> userMap = iamUserMapper.selectListByQuery(userQuery).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));

        QueryWrapper accountQuery = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.USER_ID.in(uploaderIds))
                .and(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.USERNAME.name()));
        Map<Long, String> usernameMap = iamAccountMapper.selectListByQuery(accountQuery).stream()
                .collect(Collectors.toMap(AccountEntity::getUserId, AccountEntity::getIdentifier, (left, right) -> left));

        return new UploaderContext(userMap, usernameMap);
    }

    /** 转换为文件分页项。 */
    private FilePageItem toPageItem(StoredFileEntity entity, UploaderContext uploaderContext) {
        return new FilePageItem(
                String.valueOf(entity.getId()),
                entity.getOriginalName(),
                entity.getMimeType(),
                entity.getSizeBytes(),
                resolveUploaderName(entity, uploaderContext),
                entity.getCategoryCode(),
                entity.getRemark(),
                entity.getCreatedAt()
        );
    }

    /** 转换为文件详情视图。 */
    private FileDetailView toDetailView(StoredFileEntity entity, UploaderContext uploaderContext) {
        return new FileDetailView(
                String.valueOf(entity.getId()),
                entity.getOriginalName(),
                entity.getMimeType(),
                entity.getSizeBytes(),
                resolveUploaderName(entity, uploaderContext),
                entity.getCategoryCode(),
                entity.getRemark(),
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
        QueryWrapper query = QueryWrapper.create()
                .where(ACCOUNT_ENTITY.ACCOUNT_TYPE.eq(AccountType.USERNAME.name()))
                .and(ACCOUNT_ENTITY.NORMALIZED_IDENTIFIER.eq(normalized))
                .and(ACCOUNT_ENTITY.STATUS.eq("ACTIVE"))
                .and(ACCOUNT_ENTITY.IS_LOGIN_ENABLED.eq(true));
        AccountEntity account = iamAccountMapper.selectOneByQuery(query);
        return account != null ? account.getUserId() : null;
    }

    /** 解析最终文件名。 */
    private String resolveFileName(String originalFilename, long id) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "file-" + id;
        }
        return originalFilename.trim();
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
