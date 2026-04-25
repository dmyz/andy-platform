package net.junanw.upms.system.dictionary.service;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import net.junanw.upms.foundation.shared.util.ServiceSupport;
import net.junanw.upms.system.dictionary.entity.DictionaryItemEntity;
import net.junanw.upms.system.dictionary.entity.DictionaryTypeEntity;
import net.junanw.upms.system.dictionary.model.request.DictionaryItemSaveRequest;
import net.junanw.upms.system.dictionary.model.request.DictionaryTypeSaveRequest;
import net.junanw.upms.system.dictionary.model.view.DictionaryItemView;
import net.junanw.upms.system.dictionary.model.view.DictionaryOptionItem;
import net.junanw.upms.system.dictionary.model.view.DictionaryTypeDetailView;
import net.junanw.upms.system.dictionary.model.view.DictionaryTypePageItem;
import net.junanw.upms.system.dictionary.repository.DictionaryItemMapper;
import net.junanw.upms.system.dictionary.repository.DictionaryTypeMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.junanw.upms.system.dictionary.entity.table.DictionaryItemEntityTableDef.DICTIONARY_ITEM_ENTITY;
import static net.junanw.upms.system.dictionary.entity.table.DictionaryTypeEntityTableDef.DICTIONARY_TYPE_ENTITY;

/**
 * 字典服务实现。
 *
 * <p>负责处理字典类型与字典项的查询、创建、更新、删除以及下拉选项读取逻辑。
 */
@Service
@Primary
public class DictionaryServiceImpl extends ServiceSupport implements DictionaryService {

    private final DictionaryTypeMapper metaDictionaryMapper;
    private final DictionaryItemMapper metaDictionaryItemMapper;
    private final IdGenerator idGenerator;

    public DictionaryServiceImpl(
            DictionaryTypeMapper metaDictionaryMapper,
            DictionaryItemMapper metaDictionaryItemMapper,
            IdGenerator idGenerator
    ) {
        this.metaDictionaryMapper = metaDictionaryMapper;
        this.metaDictionaryItemMapper = metaDictionaryItemMapper;
        this.idGenerator = idGenerator;
    }

    /**
     * 分页查询字典类型列表。
     *
     * <p>通过 MyBatis-Flex QueryWrapper 组合名称、编码和状态条件，再按更新时间倒序分页返回。
     *
     * @param name 名称关键字
     * @param code 编码关键字
     * @param status 状态筛选
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<DictionaryTypePageItem> page(String name, String code, Integer status, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .where(DICTIONARY_TYPE_ENTITY.DELETED.eq(false));

        String normalizedName = normalizeKeyword(name);
        String normalizedCode = normalizeKeyword(code);

        if (!normalizedName.isBlank()) {
            query.and(DICTIONARY_TYPE_ENTITY.DICT_NAME.like("%" + normalizedName + "%"));
        }
        if (!normalizedCode.isBlank()) {
            query.and(DICTIONARY_TYPE_ENTITY.DICT_CODE.like("%" + normalizedCode + "%"));
        }
        if (status != null) {
            query.and(DICTIONARY_TYPE_ENTITY.STATUS.eq(normalizeStatus(status)));
        }

        query.orderBy(DICTIONARY_TYPE_ENTITY.UPDATED_AT.desc());

        List<DictionaryTypePageItem> items = metaDictionaryMapper.selectListByQuery(query).stream()
                .map(this::toTypePage)
                .toList();
        return paginate(items, pageNum, pageSize);
    }

    /**
     * 查询字典详情。
     *
     * @param id 字典主键
     * @return 字典详情
     */
    @Override
    @Transactional(readOnly = true)
    public DictionaryTypeDetailView detail(String id) {
        return toTypeDetail(requireType(parseId(id, "字典不存在")));
    }

    /**
     * 创建字典类型。
     *
     * <p>创建前会校验字典编码唯一性，并初始化逻辑删除与审计时间字段。
     *
     * @param request 保存请求
     * @return 新建后的字典详情
     */
    @Override
    @Transactional
    public DictionaryTypeDetailView create(DictionaryTypeSaveRequest request) {
        validateType(request, null);
        DictionaryTypeEntity entity = new DictionaryTypeEntity();
        entity.setId(idGenerator.nextId());
        fillType(entity, request);
        metaDictionaryMapper.insert(entity);
        return toTypeDetail(entity);
    }

    /**
     * 更新字典类型。
     *
     * @param id 字典主键
     * @param request 保存请求
     * @return 更新后的字典详情
     */
    @Override
    @Transactional
    public DictionaryTypeDetailView update(String id, DictionaryTypeSaveRequest request) {
        Long dictId = parseId(id, "字典不存在");
        DictionaryTypeEntity entity = requireType(dictId);
        validateType(request, dictId);
        fillType(entity, request);
        metaDictionaryMapper.update(entity);
        return toTypeDetail(entity);
    }

    /**
     * 删除字典类型。
     *
     * <p>若字典下仍存在未删除的字典项，则拒绝删除，避免产生悬挂数据。
     *
     * @param id 字典主键
     */
    @Override
    @Transactional
    public void delete(String id) {
        DictionaryTypeEntity entity = requireType(parseId(id, "字典不存在"));
        QueryWrapper itemQuery = QueryWrapper.create()
                .where(DICTIONARY_ITEM_ENTITY.DICT_ID.eq(entity.getId()))
                .and(DICTIONARY_ITEM_ENTITY.DELETED.eq(false));
        if (!metaDictionaryItemMapper.selectListByQuery(itemQuery).isEmpty()) {
            throw new BusinessException(400, "请先删除字典项");
        }
        entity.setDeleted(true);
        metaDictionaryMapper.update(entity);
    }

    /**
     * 查询字典项列表。
     *
     * @param dictionaryId 字典主键
     * @param name 字典项名称关键字
     * @param status 状态筛选
     * @return 字典项列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<DictionaryItemView> items(String dictionaryId, String name, Integer status) {
        DictionaryTypeEntity type = requireType(parseId(dictionaryId, "字典不存在"));
        String normalizedName = normalizeKeyword(name);

        QueryWrapper query = QueryWrapper.create()
                .where(DICTIONARY_ITEM_ENTITY.DICT_ID.eq(type.getId()))
                .and(DICTIONARY_ITEM_ENTITY.DELETED.eq(false));

        if (!normalizedName.isBlank()) {
            query.and(DICTIONARY_ITEM_ENTITY.ITEM_TEXT.like("%" + normalizedName + "%"));
        }
        if (status != null) {
            query.and(DICTIONARY_ITEM_ENTITY.STATUS.eq(normalizeStatus(status)));
        }

        query.orderBy(DICTIONARY_ITEM_ENTITY.SORT_ORDER.asc(), DICTIONARY_ITEM_ENTITY.UPDATED_AT.desc());

        return metaDictionaryItemMapper.selectListByQuery(query).stream()
                .map(item -> toItemView(item, type))
                .toList();
    }

    /**
     * 创建字典项。
     *
     * @param dictionaryId 所属字典主键
     * @param request 保存请求
     * @return 新建后的字典项
     */
    @Override
    @Transactional
    public DictionaryItemView createItem(String dictionaryId, DictionaryItemSaveRequest request) {
        DictionaryTypeEntity type = requireType(parseId(dictionaryId, "字典不存在"));
        validateItem(type.getId(), request, null);
        DictionaryItemEntity entity = new DictionaryItemEntity();
        entity.setId(idGenerator.nextId());
        entity.setDictId(type.getId());
        fillItem(entity, request);
        metaDictionaryItemMapper.insert(entity);
        return toItemView(entity, type);
    }

    /**
     * 更新字典项。
     *
     * @param itemId 字典项主键
     * @param request 保存请求
     * @return 更新后的字典项
     */
    @Override
    @Transactional
    public DictionaryItemView updateItem(String itemId, DictionaryItemSaveRequest request) {
        DictionaryItemEntity entity = requireItem(parseId(itemId, "字典项不存在"));
        DictionaryTypeEntity type = requireType(entity.getDictId());
        validateItem(type.getId(), request, entity.getId());
        fillItem(entity, request);
        metaDictionaryItemMapper.update(entity);
        return toItemView(entity, type);
    }

    /**
     * 删除字典项。
     *
     * @param itemId 字典项主键
     */
    @Override
    @Transactional
    public void deleteItem(String itemId) {
        DictionaryItemEntity entity = requireItem(parseId(itemId, "字典项不存在"));
        entity.setDeleted(true);
        metaDictionaryItemMapper.update(entity);
    }

    /**
     * 根据字典编码查询启用中的下拉选项。
     *
     * @param dictCode 字典编码
     * @return 选项列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<DictionaryOptionItem> optionsByCode(String dictCode) {
        QueryWrapper typeQuery = QueryWrapper.create()
                .where(DICTIONARY_TYPE_ENTITY.DICT_CODE.eq(dictCode))
                .and(DICTIONARY_TYPE_ENTITY.DELETED.eq(false));
        DictionaryTypeEntity type = metaDictionaryMapper.selectOneByQuery(typeQuery);
        if (type == null) {
            throw new BusinessException(404, "字典不存在");
        }

        QueryWrapper itemQuery = QueryWrapper.create()
                .where(DICTIONARY_ITEM_ENTITY.DICT_ID.eq(type.getId()))
                .and(DICTIONARY_ITEM_ENTITY.DELETED.eq(false))
                .orderBy(DICTIONARY_ITEM_ENTITY.SORT_ORDER.asc(), DICTIONARY_ITEM_ENTITY.UPDATED_AT.desc());

        return metaDictionaryItemMapper.selectListByQuery(itemQuery).stream()
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .map(item -> new DictionaryOptionItem(item.getItemText(), item.getItemValue(), item.getSortOrder()))
                .toList();
    }

    /**
     * 校验字典类型编码唯一性。
     *
     * @param request 保存请求
     * @param currentId 当前字典主键，创建时为 {@code null}
     */
    private void validateType(DictionaryTypeSaveRequest request, Long currentId) {
        QueryWrapper query = QueryWrapper.create()
                .where(DICTIONARY_TYPE_ENTITY.DICT_CODE.eq(request.code().trim()))
                .and(DICTIONARY_TYPE_ENTITY.DELETED.eq(false));
        DictionaryTypeEntity existing = metaDictionaryMapper.selectOneByQuery(query);
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new BusinessException(400, "字典编码已存在");
        }
    }

    /**
     * 校验字典项值在同一字典下唯一。
     *
     * @param dictId 字典主键
     * @param request 保存请求
     * @param currentId 当前字典项主键，创建时为 {@code null}
     */
    private void validateItem(Long dictId, DictionaryItemSaveRequest request, Long currentId) {
        QueryWrapper query = QueryWrapper.create()
                .where(DICTIONARY_ITEM_ENTITY.DICT_ID.eq(dictId))
                .and(DICTIONARY_ITEM_ENTITY.ITEM_VALUE.eq(request.value().trim()))
                .and(DICTIONARY_ITEM_ENTITY.DELETED.eq(false));
        DictionaryItemEntity existing = metaDictionaryItemMapper.selectOneByQuery(query);
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new BusinessException(400, "字典项值已存在");
        }
    }

    /**
     * 按主键加载字典类型，不存在则抛异常。
     *
     * @param id 字典主键
     * @return 字典类型实体
     */
    private DictionaryTypeEntity requireType(Long id) {
        QueryWrapper query = QueryWrapper.create()
                .where(DICTIONARY_TYPE_ENTITY.ID.eq(id))
                .and(DICTIONARY_TYPE_ENTITY.DELETED.eq(false));
        DictionaryTypeEntity entity = metaDictionaryMapper.selectOneByQuery(query);
        if (entity == null) {
            throw new BusinessException(404, "字典不存在");
        }
        return entity;
    }

    /**
     * 按主键加载字典项，不存在则抛异常。
     *
     * @param id 字典项主键
     * @return 字典项实体
     */
    private DictionaryItemEntity requireItem(Long id) {
        QueryWrapper query = QueryWrapper.create()
                .where(DICTIONARY_ITEM_ENTITY.ID.eq(id))
                .and(DICTIONARY_ITEM_ENTITY.DELETED.eq(false));
        DictionaryItemEntity entity = metaDictionaryItemMapper.selectOneByQuery(query);
        if (entity == null) {
            throw new BusinessException(404, "字典项不存在");
        }
        return entity;
    }

    /**
     * 填充字典类型实体字段。
     *
     * @param entity 实体对象
     * @param request 保存请求
     */
    private void fillType(DictionaryTypeEntity entity, DictionaryTypeSaveRequest request) {
        entity.setDictName(request.name().trim());
        entity.setDictCode(request.code().trim());
        entity.setStatus(normalizeStatus(request.status()));
        entity.setRemark(trimToNull(request.remark()));
    }

    /**
     * 填充字典项实体字段。
     *
     * @param entity 实体对象
     * @param request 保存请求
     */
    private void fillItem(DictionaryItemEntity entity, DictionaryItemSaveRequest request) {
        entity.setItemText(request.name().trim());
        entity.setItemValue(request.value().trim());
        entity.setSortOrder(request.sortOrder());
        entity.setStatus(normalizeStatus(request.status()));
        entity.setRemark(trimToNull(request.remark()));
    }

    /**
     * 转换为字典分页项视图。
     *
     * @param entity 字典类型实体
     * @return 分页项视图
     */
    private DictionaryTypePageItem toTypePage(DictionaryTypeEntity entity) {
        return new DictionaryTypePageItem(
                String.valueOf(entity.getId()),
                entity.getDictName(),
                entity.getDictCode(),
                toNumericStatus(entity.getStatus()),
                entity.getRemark(),
                entity.getUpdatedAt()
        );
    }

    /**
     * 转换为字典详情视图。
     *
     * @param entity 字典类型实体
     * @return 详情视图
     */
    private DictionaryTypeDetailView toTypeDetail(DictionaryTypeEntity entity) {
        return new DictionaryTypeDetailView(
                String.valueOf(entity.getId()),
                entity.getDictName(),
                entity.getDictCode(),
                toNumericStatus(entity.getStatus()),
                entity.getRemark(),
                entity.getUpdatedAt()
        );
    }

    /**
     * 转换为字典项视图。
     *
     * @param item 字典项实体
     * @param type 所属字典类型实体
     * @return 字典项视图
     */
    private DictionaryItemView toItemView(DictionaryItemEntity item, DictionaryTypeEntity type) {
        return new DictionaryItemView(
                String.valueOf(item.getId()),
                String.valueOf(type.getId()),
                type.getDictName(),
                type.getDictCode(),
                item.getItemText(),
                item.getItemValue(),
                item.getSortOrder(),
                toNumericStatus(item.getStatus()),
                item.getRemark(),
                item.getUpdatedAt()
        );
    }
}
