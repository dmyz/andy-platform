package net.junanw.upms.foundation.shared.util;

import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.shared.exception.BusinessException;

import java.util.List;
import java.util.Locale;

/**
 * 服务层公共辅助基类。
 *
 * <p>沉淀各业务服务实现都会复用的轻量工具方法，例如 ID 解析、状态归一化、字符串清洗与内存分页。
 */
public abstract class ServiceSupport {

    /**
     * 解析字符串 ID。
     *
     * @param id 原始字符串 ID
     * @param message 解析失败时抛出的错误信息
     * @return 解析后的长整型 ID
     */
    protected Long parseId(String id, String message) {
        try {
            return Long.valueOf(id);
        }
        catch (RuntimeException exception) {
            throw new BusinessException(404, message);
        }
    }

    /**
     * 归一化关键字查询文本。
     *
     * @param value 原始关键字
     * @return 去空白并转小写后的关键字；空值返回空串
     */
    protected String normalizeKeyword(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 去除首尾空白并把空串转为 {@code null}。
     *
     * @param value 原始字符串
     * @return 清洗后的字符串
     */
    protected String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * 把数值型状态转为字符串状态。
     *
     * @param status 数值状态，1 表示启用
     * @return ACTIVE 或 INACTIVE
     */
    protected String normalizeStatus(Integer status) {
        return status != null && status == 1 ? "ACTIVE" : "INACTIVE";
    }

    /**
     * 把字符串状态转为数值状态。
     *
     * @param status 字符串状态
     * @return 1 表示启用，0 表示停用
     */
    protected Integer toNumericStatus(String status) {
        return "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;
    }

    /**
     * 对内存列表做分页裁剪。
     *
     * @param items 原始列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param <T> 列表元素类型
     * @return 分页结果
     */
    protected <T> PageResponse<T> paginate(List<T> items, int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, items.size());
        int toIndex = Math.min(fromIndex + safePageSize, items.size());
        return PageResponse.of(items.subList(fromIndex, toIndex), items.size(), safePageNum, safePageSize);
    }
}
