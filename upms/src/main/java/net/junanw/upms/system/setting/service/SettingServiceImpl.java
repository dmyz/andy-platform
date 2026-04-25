package net.junanw.upms.system.setting.service;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.util.ServiceSupport;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import net.junanw.upms.system.setting.model.view.SettingDetailView;
import net.junanw.upms.system.setting.model.view.SettingPageItem;
import net.junanw.upms.system.setting.model.request.SettingSaveRequest;
import net.junanw.upms.system.setting.entity.SettingEntity;
import net.junanw.upms.system.setting.repository.SettingMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.junanw.upms.system.setting.entity.table.SettingEntityTableDef.SETTING_ENTITY;

/**
 * SettingServiceImpl 服务实现。
 *
 * <p>负责承接 Setting 相关业务编排与规则落地。
 */
@Service
@Primary
public class SettingServiceImpl extends ServiceSupport implements SettingService {

    private final SettingMapper settingMapper;
    private final IdGenerator idGenerator;

    public SettingServiceImpl(SettingMapper settingMapper, IdGenerator idGenerator) {
        this.settingMapper = settingMapper;
        this.idGenerator = idGenerator;
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

        QueryWrapper query = QueryWrapper.create()
                .where(SETTING_ENTITY.DELETED.eq(false));

        if (!normalizedName.isBlank()) {
            query.and(SETTING_ENTITY.SETTING_NAME.like(normalizedName));
        }
        if (!normalizedKey.isBlank()) {
            query.and(SETTING_ENTITY.SETTING_KEY.like(normalizedKey));
        }
        if (normalizedGroup != null) {
            query.and(SETTING_ENTITY.GROUP_CODE.eq(normalizedGroup));
        }
        if (status != null) {
            query.and(SETTING_ENTITY.STATUS.eq(normalizeStatus(status)));
        }

        query.orderBy(SETTING_ENTITY.UPDATED_AT.desc());

        List<SettingPageItem> items = settingMapper.selectListByQuery(query).stream()
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
        entity.setId(idGenerator.nextId());
        fill(entity, request);
        settingMapper.insert(entity);
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
        SettingEntity entity = settingMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(SETTING_ENTITY.SETTING_KEY.eq(settingKey))
                        .and(SETTING_ENTITY.DELETED.eq(false))
        );
        if (entity == null) {
            throw new BusinessException(404, "配置不存在");
        }
        return toDetail(entity);
    }

    /**
     * 校验配置 Key 唯一性。
     */
    private void validate(SettingSaveRequest request, Long currentId) {
        SettingEntity existing = settingMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(SETTING_ENTITY.SETTING_KEY.eq(request.settingKey().trim()))
                        .and(SETTING_ENTITY.DELETED.eq(false))
        );
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException(400, "配置 Key 已存在");
        }
    }

    /**
     * 加载配置实体，不存在则抛异常。
     */
    private SettingEntity require(Long id) {
        SettingEntity entity = settingMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(SETTING_ENTITY.ID.eq(id))
                        .and(SETTING_ENTITY.DELETED.eq(false))
        );
        if (entity == null) {
            throw new BusinessException(404, "配置不存在");
        }
        return entity;
    }

    /**
     * 填充配置实体字段。
     */
    private void fill(SettingEntity entity, SettingSaveRequest request) {
        entity.setSettingKey(request.settingKey().trim());
        entity.setSettingName(request.settingName().trim());
        entity.setSettingValue(request.settingValue());
        entity.setValueType(request.valueType().trim());
        entity.setScopeType(request.scopeType().trim());
        entity.setScopeId(trimToNull(request.scopeId()));
        entity.setGroupCode(request.groupCode().trim());
        entity.setSecretFlag(request.secretFlag());
        entity.setEffectiveMode(request.effectiveMode().trim());
        entity.setStatus(normalizeStatus(request.status()));
        entity.setRemark(trimToNull(request.remark()));
    }

    /**
     * 转换为配置分页项视图。
     */
    private SettingPageItem toPageItem(SettingEntity entity) {
        return new SettingPageItem(String.valueOf(entity.getId()), entity.getSettingKey(), entity.getSettingName(), entity.getSettingValue(), entity.getValueType(), entity.getScopeType(), entity.getScopeId(), entity.getGroupCode(), entity.getSecretFlag(), entity.getEffectiveMode(), toNumericStatus(entity.getStatus()), entity.getRemark(), entity.getUpdatedAt());
    }

    /**
     * 转换为配置详情视图。
     */
    private SettingDetailView toDetail(SettingEntity entity) {
        return new SettingDetailView(String.valueOf(entity.getId()), entity.getSettingKey(), entity.getSettingName(), entity.getSettingValue(), entity.getValueType(), entity.getScopeType(), entity.getScopeId(), entity.getGroupCode(), entity.getSecretFlag(), entity.getEffectiveMode(), toNumericStatus(entity.getStatus()), entity.getRemark(), entity.getUpdatedAt());
    }
}
