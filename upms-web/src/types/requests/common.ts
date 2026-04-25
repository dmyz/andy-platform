/**
 * 分页请求参数
 */
export interface PageRequest {
  page?: number
  pageSize?: number
}

/**
 * 排序请求参数
 */
export interface SortRequest {
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

/**
 * 分页排序请求参数
 */
export interface PageSortRequest extends PageRequest, SortRequest {}
