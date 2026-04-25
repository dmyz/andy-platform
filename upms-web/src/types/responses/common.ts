/**
 * 通用 API 响应类型
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 分页响应类型
 */
export interface PageResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

/**
 * 树形节点响应类型
 */
export interface TreeNode<T = any> {
  id: string
  parentId?: string | null
  children?: TreeNode<T>[]
  data: T
}
