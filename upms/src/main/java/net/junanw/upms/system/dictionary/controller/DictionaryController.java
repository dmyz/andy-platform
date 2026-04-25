package net.junanw.upms.system.dictionary.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import net.junanw.upms.foundation.shared.api.ApiResponse;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.system.dictionary.model.request.DictionaryItemSaveRequest;
import net.junanw.upms.system.dictionary.model.request.DictionaryTypeSaveRequest;
import net.junanw.upms.system.dictionary.model.view.DictionaryItemView;
import net.junanw.upms.system.dictionary.model.view.DictionaryOptionItem;
import net.junanw.upms.system.dictionary.model.view.DictionaryTypeDetailView;
import net.junanw.upms.system.dictionary.model.view.DictionaryTypePageItem;
import net.junanw.upms.system.dictionary.service.DictionaryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典管理控制器。
 *
 * <p>负责提供字典类型与字典项的管理接口，并把请求转交给字典服务处理。
 */
@RestController
@RequestMapping("/admin/dictionary")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * 分页查询字典类型。
     *
     * @param name 字典名称关键字
     * @param code 字典编码关键字
     * @param status 状态筛选
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/page")
    @SaCheckPermission("system:dictionary:view")
    public ApiResponse<PageResponse<DictionaryTypePageItem>> page(
            String name,
            String code,
            Integer status,
            Integer pageNum,
            Integer pageSize
    ) {
        return ApiResponse.success(dictionaryService.page(
                name,
                code,
                status,
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        ));
    }

    /**
     * 查询字典类型详情。
     *
     * @param id 字典主键
     * @return 字典详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:dictionary:view")
    public ApiResponse<DictionaryTypeDetailView> detail(@PathVariable String id) {
        return ApiResponse.success(dictionaryService.detail(id));
    }

    /**
     * 创建字典类型。
     *
     * @param request 保存请求
     * @return 新建后的字典详情
     */
    @PostMapping
    @SaCheckPermission("system:dictionary:create")
    public ApiResponse<DictionaryTypeDetailView> create(@Valid @RequestBody DictionaryTypeSaveRequest request) {
        return ApiResponse.success(dictionaryService.create(request));
    }

    /**
     * 更新字典类型。
     *
     * @param id 字典主键
     * @param request 保存请求
     * @return 更新后的字典详情
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:dictionary:update")
    public ApiResponse<DictionaryTypeDetailView> update(@PathVariable String id, @Valid @RequestBody DictionaryTypeSaveRequest request) {
        return ApiResponse.success(dictionaryService.update(id, request));
    }

    /**
     * 删除字典类型。
     *
     * @param id 字典主键
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:dictionary:delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        dictionaryService.delete(id);
        return ApiResponse.success(null);
    }

    /**
     * 查询某个字典下的字典项列表。
     *
     * @param id 字典主键
     * @param name 字典项名称关键字
     * @param status 状态筛选
     * @return 字典项列表
     */
    @GetMapping("/{id}/items")
    @SaCheckPermission("system:dictionary:view")
    public ApiResponse<List<DictionaryItemView>> items(@PathVariable String id, String name, Integer status) {
        return ApiResponse.success(dictionaryService.items(id, name, status));
    }

    /**
     * 创建字典项。
     *
     * @param id 所属字典主键
     * @param request 保存请求
     * @return 新建后的字典项
     */
    @PostMapping("/{id}/items")
    @SaCheckPermission("system:dictionary:item:manage")
    public ApiResponse<DictionaryItemView> createItem(@PathVariable String id, @Valid @RequestBody DictionaryItemSaveRequest request) {
        return ApiResponse.success(dictionaryService.createItem(id, request));
    }

    /**
     * 更新字典项。
     *
     * @param itemId 字典项主键
     * @param request 保存请求
     * @return 更新后的字典项
     */
    @PutMapping("/item/{itemId}")
    @SaCheckPermission("system:dictionary:item:manage")
    public ApiResponse<DictionaryItemView> updateItem(@PathVariable String itemId, @Valid @RequestBody DictionaryItemSaveRequest request) {
        return ApiResponse.success(dictionaryService.updateItem(itemId, request));
    }

    /**
     * 删除字典项。
     *
     * @param itemId 字典项主键
     * @return 空响应
     */
    @DeleteMapping("/item/{itemId}")
    @SaCheckPermission("system:dictionary:item:manage")
    public ApiResponse<Void> deleteItem(@PathVariable String itemId) {
        dictionaryService.deleteItem(itemId);
        return ApiResponse.success(null);
    }

    /**
     * 根据字典编码获取启用中的选项列表。
     *
     * @param dictCode 字典编码
     * @return 下拉选项列表
     */
    @GetMapping("/code/{dictCode}")
    public ApiResponse<List<DictionaryOptionItem>> optionsByCode(@PathVariable String dictCode) {
        return ApiResponse.success(dictionaryService.optionsByCode(dictCode));
    }
}
