package net.junanw.upms.core.setting.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.setting.entity.SettingEntity;
import net.junanw.upms.core.setting.mapper.SettingMapper;
import net.junanw.upms.core.setting.model.request.SettingSaveRequest;
import net.junanw.upms.core.setting.model.view.SettingDetailView;
import net.junanw.upms.core.setting.model.view.SettingPageItem;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.security.SensitiveWordService;
import net.junanw.upms.infrastructure.shared.security.TextSecurityService;
import net.junanw.upms.infrastructure.shared.util.ServiceSupport;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * SettingServiceImpl 服务实现。
 *
 * <p>负责承接 Setting 相关业务编排与规则落地。
 */
@Service
@Primary
public class SettingServiceImpl extends ServiceSupport implements SettingService {

    private static final String SECRET_VALUE_MASK = "******";

    private final SettingMapper settingMapper;
    private final TextSecurityService textSecurityService;
    private final SensitiveWordService sensitiveWordService;

    public SettingServiceImpl(
            SettingMapper settingMapper,
            TextSecurityService textSecurityService,
            SensitiveWordService sensitiveWordService
    ) {
        this.settingMapper = settingMapper;
        this.textSecurityService = textSecurityService;
        this.sensitiveWordService = sensitiveWordService;
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 分页查询配置项。
     */
    public PageResponse<SettingPageItem> page(String settingName, String settingKey, String groupCode, Integer status, int pageNum, int pageSize) {
        String normalizedName = normalizeKeyword(settingName);
        String normalizedKey = normalizeKeyword(settingKey);
        String normalizedGroup = trimToNull(groupCode);

        QueryChain<SettingEntity> query = QueryChain.of(settingMapper)
                .eq(SettingEntity::getDeleted, false);

        if (!normalizedName.isBlank()) {
            query.like(SettingEntity::getSettingName, normalizedName);
        }
        if (!normalizedKey.isBlank()) {
            query.like(SettingEntity::getSettingKey, normalizedKey);
        }
        if (normalizedGroup != null) {
            query.eq(SettingEntity::getGroupCode, normalizedGroup);
        }
        if (status != null) {
            query.eq(SettingEntity::getStatus, normalizeStatus(status));
        }

        List<SettingPageItem> items = query.orderByDesc(SettingEntity::getUpdatedAt).list().stream()
                .map(this::toPageItem)
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 查询配置详情。
     */
    public SettingDetailView detail(String id) {
        return toDetail(require(parseId(id, "配置不存在")));
    }

    @Override
    @Transactional
    /**
     * 创建配置项。
     */
    public SettingDetailView create(SettingSaveRequest request) {
        validate(request, null);
        SettingEntity entity = new SettingEntity();
        fill(entity, request);
        settingMapper.save(entity);
        return toDetail(entity);
    }

    @Override
    @Transactional
    /**
     * 更新配置项。
     */
    public SettingDetailView update(String id, SettingSaveRequest request) {
        Long settingId = parseId(id, "配置不存在");
        SettingEntity entity = require(settingId);
        validate(request, settingId);
        fill(entity, request);
        settingMapper.update(entity);
        return toDetail(entity);
    }

    @Override
    @Transactional
    /**
     * 删除配置项。
     */
    public void delete(String id) {
        SettingEntity entity = require(parseId(id, "配置不存在"));
        settingMapper.deleteById(entity.getId());
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 按 Key 查询配置。
     */
    public SettingDetailView getByKey(String settingKey) {
        SettingEntity entity = QueryChain.of(settingMapper)
                .eq(SettingEntity::getSettingKey, settingKey)
                .eq(SettingEntity::getDeleted, false)
                .get();
        if (entity == null) {
            throw new BusinessException(404, "配置不存在");
        }
        return toDetail(entity);
    }

    /**
     * 校验配置 Key 唯一性。
     */
    private void validate(SettingSaveRequest request, Long currentId) {
        SettingEntity existing = QueryChain.of(settingMapper)
                .eq(SettingEntity::getSettingKey, request.settingKey().trim())
                .eq(SettingEntity::getDeleted, false)
                .get();
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException(400, "配置 Key 已存在");
        }
    }

    /**
     * 加载配置实体，不存在则抛异常。
     */
    private SettingEntity require(Long id) {
        SettingEntity entity = QueryChain.of(settingMapper)
                .eq(SettingEntity::getId, id)
                .eq(SettingEntity::getDeleted, false)
                .get();
        if (entity == null) {
            throw new BusinessException(404, "配置不存在");
        }
        return entity;
    }

    /**
     * 填充配置实体字段。
     */
    private void fill(SettingEntity entity, SettingSaveRequest request) {
        sensitiveWordService.rejectIfPresent("配置名称", request.settingName());
        sensitiveWordService.rejectIfPresent("配置备注", request.remark());
        textSecurityService.ensureLength(request.settingValue(), "配置值", TextSecurityService.SETTING_VALUE_MAX_LENGTH);
        entity.setSettingKey(request.settingKey().trim());
        entity.setSettingName(textSecurityService.sanitizePlainText(request.settingName(), "配置名称", true, 128));
        entity.setSettingValue(request.settingValue());
        entity.setValueType(request.valueType().trim());
        entity.setScopeType(request.scopeType().trim());
        entity.setScopeId(trimToNull(request.scopeId()));
        entity.setGroupCode(request.groupCode().trim());
        entity.setSecretFlag(request.secretFlag());
        entity.setEffectiveMode(request.effectiveMode().trim());
        entity.setStatus(normalizeStatus(request.status()));
        entity.setRemark(textSecurityService.sanitizePlainText(request.remark(), "配置备注", false, 200));
    }

    /**
     * 转换为配置分页项视图。
     */
    private SettingPageItem toPageItem(SettingEntity entity) {
        return new SettingPageItem(
                String.valueOf(entity.getId()),
                entity.getSettingKey(),
                textSecurityService.sanitizeForOutput(entity.getSettingName()),
                maskSecretValue(entity),
                entity.getValueType(),
                entity.getScopeType(),
                entity.getScopeId(),
                entity.getGroupCode(),
                entity.getSecretFlag(),
                entity.getEffectiveMode(),
                toNumericStatus(entity.getStatus()),
                textSecurityService.sanitizeForOutput(entity.getRemark()),
                entity.getUpdatedAt()
        );
    }

    /**
     * 转换为配置详情视图。
     */
    private SettingDetailView toDetail(SettingEntity entity) {
        return new SettingDetailView(
                String.valueOf(entity.getId()),
                entity.getSettingKey(),
                textSecurityService.sanitizeForOutput(entity.getSettingName()),
                maskSecretValue(entity),
                entity.getValueType(),
                entity.getScopeType(),
                entity.getScopeId(),
                entity.getGroupCode(),
                entity.getSecretFlag(),
                entity.getEffectiveMode(),
                toNumericStatus(entity.getStatus()),
                textSecurityService.sanitizeForOutput(entity.getRemark()),
                entity.getUpdatedAt()
        );
    }

    /**
     * 对敏感配置值返回固定掩码。
     */
    private String maskSecretValue(SettingEntity entity) {
        return Boolean.TRUE.equals(entity.getSecretFlag()) ? SECRET_VALUE_MASK : entity.getSettingValue();
    }
}
