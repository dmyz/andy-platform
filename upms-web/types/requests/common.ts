/**
 * 分页请求参数
 */
export interface PageRequest {
  pageNum?: number;
  /** @deprecated 后端分页参数使用 pageNum。 */
  page?: number;
  pageSize?: number;
}

/**
 * 排序请求参数
 */
export interface SortRequest {
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

/**
 * 分页排序请求参数
 */
export interface PageSortRequest extends PageRequest, SortRequest {}
