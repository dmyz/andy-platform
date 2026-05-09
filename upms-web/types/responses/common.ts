/**
 * 通用 API 响应类型
 */
export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/**
 * 分页响应类型
 */
export interface PageResponse<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

/**
 * 树形节点响应类型
 */
export interface TreeNode<T = unknown> {
  id: string;
  parentId?: string | null;
  children?: TreeNode<T>[];
  data: T;
}
