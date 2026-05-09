package net.junanw.upms.business.content.announcement.service;

import cn.xbatis.core.sql.executor.chain.DeleteChain;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.business.content.announcement.model.view.AnnouncementDetailView;
import net.junanw.upms.business.content.announcement.model.view.AnnouncementDashboardItem;
import net.junanw.upms.business.content.announcement.model.view.AnnouncementMyItem;
import net.junanw.upms.business.content.announcement.model.view.AnnouncementPageItem;
import net.junanw.upms.business.content.announcement.model.request.AnnouncementSaveRequest;
import net.junanw.upms.business.content.announcement.entity.AnnouncementEntity;
import net.junanw.upms.business.content.announcement.entity.AnnouncementReceiptEntity;
import net.junanw.upms.business.content.announcement.entity.AnnouncementTargetEntity;
import net.junanw.upms.business.content.announcement.mapper.AnnouncementMapper;
import net.junanw.upms.business.content.announcement.mapper.AnnouncementReceiptMapper;
import net.junanw.upms.business.content.announcement.mapper.AnnouncementTargetMapper;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.id.IdGenerator;
import net.junanw.upms.infrastructure.shared.security.SensitiveWordService;
import net.junanw.upms.infrastructure.shared.security.TextSecurityService;
import net.junanw.upms.core.identity.account.entity.AccountEntity;
import net.junanw.upms.core.identity.account.model.AccountType;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.account.mapper.AccountMapper;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AnnouncementServiceImpl 服务实现。
 *
 * <p>负责承接 Announcement 相关业务编排与规则落地。
 */
@Service("announcementServiceImpl")
@Primary
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final AnnouncementTargetMapper targetMapper;
    private final AnnouncementReceiptMapper receiptMapper;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;
    private final UserContextService userContextService;
    private final TextSecurityService textSecurityService;
    private final SensitiveWordService sensitiveWordService;

    public AnnouncementServiceImpl(
            AnnouncementMapper announcementMapper,
            AnnouncementTargetMapper targetMapper,
            AnnouncementReceiptMapper receiptMapper,
            AccountMapper accountMapper,
            UserMapper userMapper,
            UserContextService userContextService,
            TextSecurityService textSecurityService,
            SensitiveWordService sensitiveWordService
    ) {
        this.announcementMapper = announcementMapper;
        this.targetMapper = targetMapper;
        this.receiptMapper = receiptMapper;
        this.accountMapper = accountMapper;
        this.userMapper = userMapper;
        this.userContextService = userContextService;
        this.textSecurityService = textSecurityService;
        this.sensitiveWordService = sensitiveWordService;
    }

    @Override
    @Transactional(readOnly = true)
    /** 查询公告分页。 */
    public PageResponse<AnnouncementPageItem> page(String title, String type, String status, int pageNum, int pageSize) {
        String normalizedTitle = normalizeKeyword(title);
        String normalizedType = normalizeNullable(type);
        String normalizedStatus = normalizeNullable(status);

        QueryChain<AnnouncementEntity> query = QueryChain.of(announcementMapper)
                .eq(AnnouncementEntity::getDeleted, false);

        if (!normalizedTitle.isBlank()) {
            query.like(AnnouncementEntity::getTitle, normalizedTitle);
        }
        if (normalizedType != null) {
            query.eq(AnnouncementEntity::getCategoryCode, normalizedType);
        }
        if (normalizedStatus != null) {
            query.eq(AnnouncementEntity::getPublishStatus, normalizedStatus);
        }

        query.orderByDesc(AnnouncementEntity::getUpdatedAt);

        List<AnnouncementEntity> entities = query.list();
        ViewContext context = buildViewContext(entities.stream().map(AnnouncementEntity::getId).toList(), entities.stream().map(AnnouncementEntity::getCreatorId).filter(Objects::nonNull).toList());
        List<AnnouncementPageItem> items = entities.stream()
                .map(entity -> toPageItem(entity, context))
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    /** 查询公告详情。 */
    public AnnouncementDetailView detail(String id) {
        AnnouncementEntity entity = requireAnnouncement(id);
        ViewContext context = buildViewContext(List.of(entity.getId()), List.of(entity.getCreatorId()));
        return toDetailView(entity, context);
    }

    @Override
    @Transactional
    /** 创建公告。 */
    public AnnouncementDetailView create(String operatorName, AnnouncementSaveRequest request) {
        Long creatorId = resolveUserIdByUsername(operatorName);
        AnnouncementEntity entity = new AnnouncementEntity();
        entity.setMessageCode("MSG-" + IdGenerator.nextId());
        fillAnnouncement(entity, request);
        entity.setContentFormat("TEXT");
        entity.setChannelType("IN_APP");
        entity.setPublishStatus("DRAFT");
        entity.setPublishTime(null);
        entity.setExpireTime(null);
        entity.setPriorityLevel(Boolean.TRUE.equals(request.top()) ? 100 : 50);
        entity.setPinFlag(request.top());
        entity.setCreatorId(creatorId);
        entity.setUpdaterId(creatorId);
        announcementMapper.save(entity);
        replaceTargets(entity.getId(), creatorId, request.targetType(), request.targetValue());
        return detail(String.valueOf(entity.getId()));
    }

    @Override
    @Transactional
    /** 更新公告。 */
    public AnnouncementDetailView update(String id, AnnouncementSaveRequest request) {
        AnnouncementEntity entity = requireAnnouncement(id);
        fillAnnouncement(entity, request);
        entity.setPriorityLevel(Boolean.TRUE.equals(request.top()) ? 100 : 50);
        entity.setPinFlag(request.top());
        announcementMapper.update(entity);
        replaceTargets(entity.getId(), entity.getUpdaterId(), request.targetType(), request.targetValue());
        return detail(String.valueOf(entity.getId()));
    }

    @Override
    @Transactional
    /** 删除公告。 */
    public void delete(String id) {
        AnnouncementEntity entity = requireAnnouncement(id);
        DeleteChain.of(receiptMapper).eq(AnnouncementReceiptEntity::getAnnouncementId, entity.getId()).execute();
        DeleteChain.of(targetMapper).eq(AnnouncementTargetEntity::getAnnouncementId, entity.getId()).execute();
        announcementMapper.deleteById(entity.getId());
    }

    @Override
    @Transactional
    /** 发布公告。 */
    public AnnouncementDetailView publish(String id) {
        AnnouncementEntity entity = requireAnnouncement(id);
        entity.setPublishStatus("PUBLISHED");
        entity.setPublishTime(LocalDateTime.now());
        announcementMapper.update(entity);
        return detail(String.valueOf(entity.getId()));
    }

    @Override
    @Transactional
    /** 撤回公告。 */
    public AnnouncementDetailView revoke(String id) {
        AnnouncementEntity entity = requireAnnouncement(id);
        entity.setPublishStatus("REVOKED");
        announcementMapper.update(entity);
        return detail(String.valueOf(entity.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    /** 查询当前用户公告分页。 */
    public PageResponse<AnnouncementMyItem> myPage(String username, String title, Boolean read, int pageNum, int pageSize) {
        UserProfileSnapshot profile = userContextService.requireByUsername(username);
        Long userId = resolveUserIdByUsername(username);

        QueryChain<AnnouncementEntity> query = QueryChain.of(announcementMapper)
                .eq(AnnouncementEntity::getDeleted, false)
                .eq(AnnouncementEntity::getPublishStatus, "PUBLISHED")
                .orderByDesc(AnnouncementEntity::getPublishTime);

        List<AnnouncementEntity> published = query.list();
        ViewContext context = buildViewContext(published.stream().map(AnnouncementEntity::getId).toList(), published.stream().map(AnnouncementEntity::getCreatorId).filter(Objects::nonNull).toList());

        List<Long> publishedIds = published.stream().map(AnnouncementEntity::getId).toList();
        Map<Long, AnnouncementReceiptEntity> receiptMap = userId == null || publishedIds.isEmpty() ? Map.of() :
                QueryChain.of(receiptMapper)
                        .in(AnnouncementReceiptEntity::getAnnouncementId, publishedIds)
                        .eq(AnnouncementReceiptEntity::getUserId, userId)
                .list()
                .stream().collect(Collectors.toMap(AnnouncementReceiptEntity::getAnnouncementId, Function.identity(), (left, right) -> left));

        String normalizedTitle = normalizeKeyword(title);
        List<AnnouncementMyItem> items = published.stream()
                .filter(entity -> matchTarget(context.targetsByAnnouncementId().getOrDefault(entity.getId(), List.of()), profile))
                .filter(entity -> normalizedTitle.isBlank() || entity.getTitle().toLowerCase(Locale.ROOT).contains(normalizedTitle))
                .sorted(Comparator.comparing((AnnouncementEntity entity) -> Boolean.TRUE.equals(entity.getPinFlag())).reversed()
                        .thenComparing(entity -> entity.getPublishTime() == null ? LocalDateTime.MIN : entity.getPublishTime(), Comparator.reverseOrder()))
                .map(entity -> {
                    AnnouncementReceiptEntity receipt = receiptMap.get(entity.getId());
                    boolean readFlag = receipt != null && Boolean.TRUE.equals(receipt.getReadFlag());
                    return new AnnouncementMyItem(
                            String.valueOf(entity.getId()),
                            entity.getTitle(),
                            entity.getCategoryCode(),
                            entity.getPublishTime(),
                            readFlag,
                            entity.getPinFlag(),
                            textSecurityService.sanitizeForOutput(entity.getContent())
                    );
                })
                .filter(item -> read == null || Objects.equals(read, item.read()))
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    @Override
    @Transactional
    /** 标记公告已读。 */
    public void markRead(String username, String id) {
        AnnouncementEntity entity = requireAnnouncement(id);
        if (!"PUBLISHED".equals(entity.getPublishStatus())) {
            throw new BusinessException(400, "仅已发布公告可标记已读");
        }
        UserProfileSnapshot profile = userContextService.requireByUsername(username);
        List<AnnouncementTargetEntity> targets = QueryChain.of(targetMapper)
                .eq(AnnouncementTargetEntity::getAnnouncementId, entity.getId())
                .list();
        if (!matchTarget(targets, profile)) {
            throw new BusinessException(403, "该公告未投放给当前用户");
        }
        Long userId = resolveUserIdByUsername(username);
        if (userId == null) {
            throw new BusinessException(404, "当前用户不存在");
        }
        AnnouncementReceiptEntity receipt = QueryChain.of(receiptMapper)
                .eq(AnnouncementReceiptEntity::getAnnouncementId, entity.getId())
                .eq(AnnouncementReceiptEntity::getUserId, userId)
                .get();
        if (receipt == null) {
            receipt = new AnnouncementReceiptEntity();
            receipt.setAnnouncementId(entity.getId());
            receipt.setUserId(userId);
            receipt.setDeliveryStatus("DELIVERED");
            receipt.setReadFlag(false);
            receipt.setDeliveredTime(entity.getPublishTime() == null ? LocalDateTime.now() : entity.getPublishTime());
        }
        receipt.setReadFlag(true);
        receipt.setReadTime(LocalDateTime.now());
        if (receipt.getDeliveredTime() == null) {
            receipt.setDeliveredTime(entity.getPublishTime() == null ? LocalDateTime.now() : entity.getPublishTime());
        }
        receipt.setDeliveryStatus("DELIVERED");
        receiptMapper.save(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    /** 查询工作台公告。 */
    public List<AnnouncementDashboardItem> dashboardAnnouncements(String username, int limit) {
        UserProfileSnapshot profile = userContextService.requireByUsername(username);
        List<AnnouncementEntity> published = QueryChain.of(announcementMapper)
                .eq(AnnouncementEntity::getDeleted, false)
                .eq(AnnouncementEntity::getPublishStatus, "PUBLISHED")
                .orderByDesc(AnnouncementEntity::getPublishTime)
                .list();
        ViewContext context = buildViewContext(published.stream().map(AnnouncementEntity::getId).toList(), List.of());
        return published.stream()
                .filter(entity -> matchTarget(context.targetsByAnnouncementId().getOrDefault(entity.getId(), List.of()), profile))
                .sorted(Comparator.comparing((AnnouncementEntity entity) -> Boolean.TRUE.equals(entity.getPinFlag())).reversed()
                        .thenComparing(entity -> entity.getPublishTime() == null ? LocalDateTime.MIN : entity.getPublishTime(), Comparator.reverseOrder()))
                .limit(Math.max(limit, 1))
                .map(entity -> new AnnouncementDashboardItem(
                        String.valueOf(entity.getId()),
                        textSecurityService.sanitizeForOutput(entity.getTitle()),
                        entity.getPublishTime(),
                        entity.getPinFlag(),
                        entity.getCategoryCode().toLowerCase(Locale.ROOT)
                ))
                .toList();
    }

    /** 填充公告实体字段。 */
    private void fillAnnouncement(AnnouncementEntity entity, AnnouncementSaveRequest request) {
        sensitiveWordService.rejectIfPresent("公告标题", request.title());
        sensitiveWordService.rejectIfPresent("公告内容", request.content());
        entity.setTitle(textSecurityService.sanitizePlainText(request.title(), "公告标题", true, TextSecurityService.ANNOUNCEMENT_TITLE_MAX_LENGTH));
        entity.setContent(textSecurityService.sanitizePlainText(request.content(), "公告内容", true, TextSecurityService.ANNOUNCEMENT_CONTENT_MAX_LENGTH));
        entity.setCategoryCode(normalizeRequiredEnum(request.type(), List.of("SYSTEM", "NOTICE", "FEATURE", "SECURITY"), "公告类型不支持"));
    }

    /** 重建公告投放目标。 */
    private void replaceTargets(Long announcementId, Long creatorId, String targetType, String targetValue) {
        String normalizedTargetType = normalizeRequiredEnum(targetType, List.of("ALL", "ORG", "ROLE", "USER"), "投放范围不支持");
        String normalizedTargetValue = normalizeTargetValue(normalizedTargetType, targetValue);
        DeleteChain.of(targetMapper).eq(AnnouncementTargetEntity::getAnnouncementId, announcementId).execute();
        AnnouncementTargetEntity target = new AnnouncementTargetEntity();
        target.setAnnouncementId(announcementId);
        target.setTargetType(normalizedTargetType);
        target.setTargetId(resolveTargetId(normalizedTargetType, normalizedTargetValue));
        target.setTargetValue(normalizedTargetValue);
        target.setCreatorId(creatorId);
        targetMapper.save(target);
    }

    /** 判断公告是否命中当前用户。 */
    private boolean matchTarget(List<AnnouncementTargetEntity> targets, UserProfileSnapshot profile) {
        return targets.stream().anyMatch(target -> switch (target.getTargetType()) {
            case "ALL" -> true;
            case "ORG" -> target.getTargetValue() != null && target.getTargetValue().equals(profile.orgName());
            case "ROLE" -> target.getTargetValue() != null && profile.roleCodes().stream().anyMatch(role -> role.equalsIgnoreCase(target.getTargetValue()));
            case "USER" -> target.getTargetValue() != null && target.getTargetValue().equalsIgnoreCase(profile.username());
            default -> false;
        });
    }

    /** 构建公告视图上下文。 */
    private ViewContext buildViewContext(List<Long> announcementIds, List<Long> creatorIds) {
        List<Long> distinctAnnouncementIds = announcementIds.stream().filter(Objects::nonNull).distinct().toList();
        Map<Long, List<AnnouncementTargetEntity>> targetsByAnnouncementId = distinctAnnouncementIds.isEmpty() ? Map.of() :
                QueryChain.of(targetMapper)
                        .in(AnnouncementTargetEntity::getAnnouncementId, distinctAnnouncementIds)
                        .list().stream().collect(Collectors.groupingBy(AnnouncementTargetEntity::getAnnouncementId));

        List<Long> distinctCreatorIds = creatorIds.stream().filter(Objects::nonNull).distinct().toList();
        Map<Long, UserEntity> creatorUserMap = distinctCreatorIds.isEmpty() ? Map.of() :
                userMapper.listByIds(distinctCreatorIds).stream()
                        .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));
        Map<Long, String> creatorUsernameMap = distinctCreatorIds.isEmpty() ? Map.of() :
                QueryChain.of(accountMapper)
                        .in(AccountEntity::getUserId, distinctCreatorIds)
                        .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                        .list().stream().collect(Collectors.toMap(AccountEntity::getUserId, AccountEntity::getIdentifier, (left, right) -> left));

        return new ViewContext(targetsByAnnouncementId, creatorUserMap, creatorUsernameMap);
    }

    /** 转换为公告分页项。 */
    private AnnouncementPageItem toPageItem(AnnouncementEntity entity, ViewContext context) {
        AnnouncementTargetEntity target = context.targetsByAnnouncementId().getOrDefault(entity.getId(), List.of()).stream().findFirst().orElse(null);
        return new AnnouncementPageItem(
                String.valueOf(entity.getId()),
                textSecurityService.sanitizeForOutput(entity.getTitle()),
                entity.getCategoryCode(),
                entity.getPublishStatus(),
                entity.getPinFlag(),
                target == null ? "ALL" : target.getTargetType(),
                target == null ? null : target.getTargetValue(),
                resolveCreatorName(entity, context),
                entity.getPublishTime(),
                entity.getUpdatedAt()
        );
    }

    /** 转换为公告详情视图。 */
    private AnnouncementDetailView toDetailView(AnnouncementEntity entity, ViewContext context) {
        AnnouncementTargetEntity target = context.targetsByAnnouncementId().getOrDefault(entity.getId(), List.of()).stream().findFirst().orElse(null);
        return new AnnouncementDetailView(
                String.valueOf(entity.getId()),
                textSecurityService.sanitizeForOutput(entity.getTitle()),
                textSecurityService.sanitizeForOutput(entity.getContent()),
                entity.getCategoryCode(),
                entity.getPublishStatus(),
                entity.getPinFlag(),
                target == null ? "ALL" : target.getTargetType(),
                target == null ? null : target.getTargetValue(),
                resolveCreatorName(entity, context),
                entity.getPublishTime(),
                entity.getUpdatedAt()
        );
    }

    /** 解析公告创建人名称。 */
    private String resolveCreatorName(AnnouncementEntity entity, ViewContext context) {
        if (entity.getCreatorId() == null) {
            return "";
        }
        UserEntity creator = context.creatorUserMap().get(entity.getCreatorId());
        if (creator != null && creator.getDisplayName() != null && !creator.getDisplayName().isBlank()) {
            return creator.getDisplayName();
        }
        return context.creatorUsernameMap().getOrDefault(entity.getCreatorId(), String.valueOf(entity.getCreatorId()));
    }

    /** 根据用户名解析用户主键。 */
    private Long resolveUserIdByUsername(String username) {
        String normalized = normalizeKeyword(username);
        if (normalized.isBlank()) {
            return null;
        }
        AccountEntity account = QueryChain.of(accountMapper)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .eq(AccountEntity::getNormalizedIdentifier, normalized)
                .eq(AccountEntity::getStatus, "ACTIVE")
                .eq(AccountEntity::getIsLoginEnabled, true)
                .get();
        return account == null ? null : account.getUserId();
    }

    /** 解析投放目标主键。 */
    private Long resolveTargetId(String targetType, String targetValue) {
        if ("USER".equals(targetType) && targetValue != null) {
            return resolveUserIdByUsername(targetValue);
        }
        return null;
    }

    /** 归一化投放目标值。 */
    private String normalizeTargetValue(String targetType, String targetValue) {
        if ("ALL".equals(targetType)) {
            return null;
        }
        if (targetValue == null || targetValue.isBlank()) {
            throw new BusinessException(400, "非全部投放时目标不能为空");
        }
        return targetValue.trim();
    }

    /** 校验并归一化枚举值。 */
    private String normalizeRequiredEnum(String value, List<String> allowedValues, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(400, message);
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!allowedValues.contains(normalized)) {
            throw new BusinessException(400, message);
        }
        return normalized;
    }

    /** 归一化关键字查询条件。 */
    private String normalizeKeyword(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    /** 归一化可空枚举值。 */
    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized.toUpperCase(Locale.ROOT);
    }

    /** 加载公告实体，不存在则抛异常。 */
    private AnnouncementEntity requireAnnouncement(String id) {
        try {
            AnnouncementEntity entity = announcementMapper.getById(Long.valueOf(id));
            if (entity == null) {
                throw new BusinessException(404, "公告不存在");
            }
            return entity;
        }
        catch (NumberFormatException exception) {
            throw new BusinessException(404, "公告不存在");
        }
    }

    /** 对结果列表执行内存分页。 */
    private <T> PageResponse<T> paginate(List<T> items, int pageNum, int pageSize) {
        int resolvedPageNum = Math.max(pageNum, 1);
        int resolvedPageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, items.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, items.size());
        return PageResponse.of(items.subList(fromIndex, toIndex), items.size(), resolvedPageNum, resolvedPageSize);
    }

    /**
     * 公告视图上下文。
     */
    private record ViewContext(
            Map<Long, List<AnnouncementTargetEntity>> targetsByAnnouncementId,
            Map<Long, UserEntity> creatorUserMap,
            Map<Long, String> creatorUsernameMap
    ) {
    }
}
