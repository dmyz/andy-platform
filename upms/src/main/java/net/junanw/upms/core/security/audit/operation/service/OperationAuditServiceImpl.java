package net.junanw.upms.core.security.audit.operation.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.util.ServiceSupport;
import net.junanw.upms.core.identity.account.entity.AccountEntity;
import net.junanw.upms.core.identity.account.model.AccountType;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.account.mapper.AccountMapper;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
import net.junanw.upms.core.security.audit.operation.model.view.OperationAuditDetailView;
import net.junanw.upms.core.security.audit.operation.model.view.OperationAuditPageItem;
import net.junanw.upms.core.security.audit.operation.entity.OperationAuditEventEntity;
import net.junanw.upms.core.security.audit.operation.mapper.OperationAuditEventMapper;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OperationAuditServiceImpl 服务实现。
 *
 * <p>负责承接 OperationAudit 相关业务编排与规则落地。
 */
@Service("operationAuditServiceImpl")
@Primary
public class OperationAuditServiceImpl extends ServiceSupport implements OperationAuditService {

    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();
    private final OperationAuditEventMapper operationAuditEventMapper;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;

    public OperationAuditServiceImpl(
            OperationAuditEventMapper operationAuditEventMapper,
            AccountMapper accountMapper,
            UserMapper userMapper
    ) {
        this.operationAuditEventMapper = operationAuditEventMapper;
        this.accountMapper = accountMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    /** 记录操作审计事件。 */
    public void record(OperationAuditRecord command) {
        if (command == null || isBlank(command.requestUri())) {
            return;
        }
        OperationAuditEventEntity entity = new OperationAuditEventEntity();
        entity.setActorUserId(resolveActorUserId(command.actorUsername()));
        entity.setModuleCode(OperationAuditTypeCatalog.normalizeCode(command.moduleCode()));
        entity.setActionCode(normalizeNullable(command.actionCode()));
        entity.setTargetType(normalizeNullable(command.targetType()));
        entity.setTargetId(command.targetId());
        entity.setRequestMethod(normalizeNullable(command.requestMethod()));
        entity.setRequestUri(command.requestUri().trim());
        entity.setRequestSummary(blankToNull(command.requestSummary()));
        entity.setResponseSummary(blankToNull(command.responseSummary()));
        entity.setResultStatus(normalizeResult(command.resultStatus()));
        entity.setDurationMs(resolveDuration(command.durationMs()));
        entity.setIp(blankToNull(command.ip()));
        entity.setTraceId(blankToNull(command.traceId()));
        entity.setRequestId(blankToNull(command.requestId()));
        entity.setEventTime(command.eventTime() == null ? LocalDateTime.now() : command.eventTime());
        operationAuditEventMapper.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    /** 查询操作审计分页。 */
    public PageResponse<OperationAuditPageItem> page(
            String operatorName,
            String moduleName,
            String actionType,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize
    ) {
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);

        // 使用 XBatis QueryChain 构建查询
        QueryChain<OperationAuditEventEntity> query = QueryChain.of(operationAuditEventMapper);

        // 动作类型过滤
        if (actionType != null && !actionType.isBlank()) {
            query.eq(OperationAuditEventEntity::getActionCode, normalizeNullable(actionType));
        }

        // 结果过滤
        if (result != null && !result.isBlank()) {
            query.eq(OperationAuditEventEntity::getResultStatus, normalizeNullable(result));
        }

        // 模块过滤
        String moduleCode = resolveModuleCode(moduleName);
        if (moduleCode != null) {
            query.eq(OperationAuditEventEntity::getModuleCode, moduleCode);
        }

        // 时间范围过滤
        if (startTime != null) {
            query.gte(OperationAuditEventEntity::getEventTime, startTime);
        }
        if (endTime != null) {
            query.lte(OperationAuditEventEntity::getEventTime, endTime);
        }

        query.orderByDesc(OperationAuditEventEntity::getEventTime);

        List<OperationAuditEventEntity> entities = query.list();
        List<OperationAuditPageItem> items = filterByOperatorName(toPageItems(entities), operatorName);
        return paginate(items, resolvedPageNum, resolvedPageSize);
    }

    @Override
    @Transactional(readOnly = true)
    /** 查询操作审计详情。 */
    public OperationAuditDetailView detail(String id) {
        OperationAuditEventEntity entity = operationAuditEventMapper.getById(Long.valueOf(id));
        if (entity == null) {
            throw new BusinessException(404, "操作审计记录不存在");
        }
        return toDetailView(entity, resolveActorNameMap(List.of(entity)));
    }

    @Override
    @Transactional(readOnly = true)
    /** 导出操作审计列表。 */
    public List<OperationAuditPageItem> export(
            String operatorName,
            String moduleName,
            String actionType,
            String result,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        // 使用 XBatis QueryChain 构建查询
        QueryChain<OperationAuditEventEntity> query = QueryChain.of(operationAuditEventMapper);

        // 动作类型过滤
        if (actionType != null && !actionType.isBlank()) {
            query.eq(OperationAuditEventEntity::getActionCode, normalizeNullable(actionType));
        }

        // 结果过滤
        if (result != null && !result.isBlank()) {
            query.eq(OperationAuditEventEntity::getResultStatus, normalizeNullable(result));
        }

        // 模块过滤
        String moduleCode = resolveModuleCode(moduleName);
        if (moduleCode != null) {
            query.eq(OperationAuditEventEntity::getModuleCode, moduleCode);
        }

        // 时间范围过滤
        if (startTime != null) {
            query.gte(OperationAuditEventEntity::getEventTime, startTime);
        }
        if (endTime != null) {
            query.lte(OperationAuditEventEntity::getEventTime, endTime);
        }

        query.orderByDesc(OperationAuditEventEntity::getEventTime);

        List<OperationAuditEventEntity> entities = query.list();
        return filterByOperatorName(toPageItems(entities), operatorName);
    }

    /** 按操作人展示名称过滤操作审计分页项。 */
    private List<OperationAuditPageItem> filterByOperatorName(List<OperationAuditPageItem> items, String operatorName) {
        if (isBlank(operatorName)) {
            return items;
        }
        String keyword = normalizeKeyword(operatorName);
        return items.stream()
                .filter(item -> containsKeyword(item.operatorName(), keyword))
                .toList();
    }

    /** 判断文本是否包含归一化后的关键字。 */
    private boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private List<OperationAuditPageItem> toPageItems(List<OperationAuditEventEntity> entities) {
        Map<Long, String> actorNameMap = resolveActorNameMap(entities);
        return entities.stream()
                .map(entity -> toPageItem(entity, actorNameMap))
                .toList();
    }

    private int normalizePageNum(int pageNum) {
        return Math.max(pageNum, 1);
    }

    private int normalizePageSize(int pageSize) {
        return Math.max(pageSize, 1);
    }

    /** 批量解析操作人名称。 */
    private Map<Long, String> resolveActorNameMap(List<OperationAuditEventEntity> entities) {
        List<Long> actorIds = entities.stream()
                .map(OperationAuditEventEntity::getActorUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (actorIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.listByIds(actorIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getDisplayName, (left, right) -> left));
    }

    /** 转换为操作审计分页项。 */
    private OperationAuditPageItem toPageItem(OperationAuditEventEntity entity, Map<Long, String> actorNameMap) {
        return new OperationAuditPageItem(
                String.valueOf(entity.getId()),
                actorNameMap.getOrDefault(entity.getActorUserId(), resolveActorFallback(entity)),
                OperationAuditTypeCatalog.moduleName(entity.getModuleCode()),
                entity.getActionCode(),
                entity.getRequestUri(),
                entity.getDurationMs() == null ? null : entity.getDurationMs().longValue(),
                entity.getEventTime(),
                entity.getResultStatus()
        );
    }

    /** 转换为操作审计详情视图。 */
    private OperationAuditDetailView toDetailView(OperationAuditEventEntity entity, Map<Long, String> actorNameMap) {
        return new OperationAuditDetailView(
                String.valueOf(entity.getId()),
                actorNameMap.getOrDefault(entity.getActorUserId(), resolveActorFallback(entity)),
                OperationAuditTypeCatalog.moduleName(entity.getModuleCode()),
                entity.getActionCode(),
                entity.getRequestMethod(),
                entity.getRequestUri(),
                defaultString(entity.getRequestSummary()),
                entity.getDurationMs() == null ? null : entity.getDurationMs().longValue(),
                resolveResponseCode(entity.getResponseSummary(), entity.getResultStatus()),
                entity.getResultStatus(),
                resolveErrorMessage(entity.getResponseSummary(), entity.getResultStatus()),
                entity.getEventTime()
        );
    }

    /** 根据用户名解析操作人主键。 */
    private Long resolveActorUserId(String actorUsername) {
        if (isBlank(actorUsername)) {
            return null;
        }
        String normalizedIdentifier = actorUsername.trim().toLowerCase(Locale.ROOT);
        AccountEntity account = QueryChain.of(accountMapper)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .eq(AccountEntity::getNormalizedIdentifier, normalizedIdentifier)
                .eq(AccountEntity::getStatus, "ACTIVE")
                .eq(AccountEntity::getIsLoginEnabled, true)
                .get();
        return account != null ? account.getUserId() : null;
    }

    /** 解析模块编码。 */
    private String resolveModuleCode(String moduleName) {
        if (isBlank(moduleName)) {
            return null;
        }
        String normalized = moduleName.trim();
        return switch (normalized) {
            case "认证管理" -> "auth";
            case "组织管理" -> "org";
            case "用户管理" -> "user";
            case "角色管理" -> "role";
            case "导航管理" -> "navigation";
            case "权限定义" -> "permission";
            case "字典管理" -> "dictionary";
            case "系统配置" -> "setting";
            case "公告管理" -> "announcement";
            case "文件管理" -> "file";
            case "个人中心" -> "profile";
            default -> OperationAuditTypeCatalog.normalizeCode(normalized);
        };
    }

    /** 从响应摘要中解析响应码。 */
    private Integer resolveResponseCode(String responseSummary, String resultStatus) {
        Map<String, Object> payload = parseJson(responseSummary);
        Object code = payload.get("code");
        if (code instanceof Number number) {
            return number.intValue();
        }
        return "SUCCESS".equalsIgnoreCase(resultStatus) ? 0 : 400;
    }

    /** 从响应摘要中解析错误信息。 */
    private String resolveErrorMessage(String responseSummary, String resultStatus) {
        if (!"FAIL".equalsIgnoreCase(resultStatus)) {
            return null;
        }
        Map<String, Object> payload = parseJson(responseSummary);
        Object message = payload.get("message");
        if (message instanceof String stringMessage && !stringMessage.isBlank()) {
            return stringMessage;
        }
        return blankToNull(responseSummary);
    }

    /** 尝试解析 JSON 文本。 */
    private Map<String, Object> parseJson(String json) {
        if (isBlank(json)) {
            return Map.of();
        }
        try {
            return jsonParser.parseMap(json);
        }
        catch (RuntimeException ignored) {
            return Map.of();
        }
    }

    /** 归一化耗时字段。 */
    private Integer resolveDuration(Long durationMs) {
        if (durationMs == null) {
            return null;
        }
        return Math.toIntExact(Math.max(durationMs, 0L));
    }

    /** 归一化操作结果。 */
    private String normalizeResult(String value) {
        return "FAIL".equalsIgnoreCase(value) ? "FAIL" : "SUCCESS";
    }

    /** 归一化关键字查询条件。 */
    protected String normalizeKeyword(String value) {
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

    /** 去空白并把空串转为 null。 */
    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /** 空值转默认字符串。 */
    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    /** 判断字符串是否为空白。 */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** 解析操作人兜底展示文本。 */
    private String resolveActorFallback(OperationAuditEventEntity entity) {
        return entity.getActorUserId() == null ? "" : String.valueOf(entity.getActorUserId());
    }
}
