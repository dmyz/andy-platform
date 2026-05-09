package net.junanw.upms.core.dictionary.service;

import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.dictionary.model.request.DictionaryItemSaveRequest;
import net.junanw.upms.core.dictionary.model.request.DictionaryTypeSaveRequest;
import net.junanw.upms.core.dictionary.model.view.DictionaryItemView;
import net.junanw.upms.core.dictionary.model.view.DictionaryOptionItem;
import net.junanw.upms.core.dictionary.model.view.DictionaryTypeDetailView;
import net.junanw.upms.core.dictionary.model.view.DictionaryTypePageItem;

import java.util.List;

/**
 * 字典服务接口。
 *
 * <p>定义字典类型与字典项管理的应用服务边界。
 */
public interface DictionaryService {

    /**
     * 分页查询字典类型。
     *
     * @param name 名称关键字
     * @param code 编码关键字
     * @param status 状态筛选
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResponse<DictionaryTypePageItem> page(String name, String code, Integer status, int pageNum, int pageSize);

    /**
     * 查询字典详情。
     *
     * @param id 字典主键
     * @return 字典详情
     */
    DictionaryTypeDetailView detail(String id);

    /**
     * 创建字典。
     *
     * @param request 保存请求
     * @return 新建后的详情
     */
    DictionaryTypeDetailView create(DictionaryTypeSaveRequest request);

    /**
     * 更新字典。
     *
     * @param id 字典主键
     * @param request 保存请求
     * @return 更新后的详情
     */
    DictionaryTypeDetailView update(String id, DictionaryTypeSaveRequest request);

    /**
     * 删除字典。
     *
     * @param id 字典主键
     */
    void delete(String id);

    /**
     * 查询字典项列表。
     *
     * @param dictionaryId 字典主键
     * @param name 字典项名称关键字
     * @param status 状态筛选
     * @return 字典项列表
     */
    List<DictionaryItemView> items(String dictionaryId, String name, Integer status);

    /**
     * 创建字典项。
     *
     * @param dictionaryId 所属字典主键
     * @param request 保存请求
     * @return 新建后的字典项
     */
    DictionaryItemView createItem(String dictionaryId, DictionaryItemSaveRequest request);

    /**
     * 更新字典项。
     *
     * @param itemId 字典项主键
     * @param request 保存请求
     * @return 更新后的字典项
     */
    DictionaryItemView updateItem(String itemId, DictionaryItemSaveRequest request);

    /**
     * 删除字典项。
     *
     * @param itemId 字典项主键
     */
    void deleteItem(String itemId);

    /**
     * 根据字典编码查询启用中的选项。
     *
     * @param dictCode 字典编码
     * @return 选项列表
     */
    List<DictionaryOptionItem> optionsByCode(String dictCode);
}
