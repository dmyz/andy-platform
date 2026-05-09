package net.junanw.upms.infrastructure.shared.api;

import java.util.List;

/**
 * 分页响应体。
 *
 * @param list 当前页数据
 * @param total 总记录数
 * @param pageNum 当前页码
 * @param pageSize 每页条数
 * @param <T> 列表元素类型
 */
public record PageResponse<T>(List<T> list, long total, int pageNum, int pageSize) {

    /** 创建分页响应。 */
    public static <T> PageResponse<T> of(List<T> list, long total, int pageNum, int pageSize) {
        return new PageResponse<>(list, total, pageNum, pageSize);
    }
}
