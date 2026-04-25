import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/responses/common'
import type { PageSortRequest } from '@/types/requests/common'

/**
 * 导航菜单项
 */
export interface NavigationItem {
  id: string
  parentId?: string
  name: string
  path?: string
  icon?: string
  sortOrder: number
  isEnabled: boolean
  isVisible: boolean
  permissionCode?: string
  children?: NavigationItem[]
  createdAt: string
  updatedAt: string
}

/**
 * 导航查询请求
 */
export interface NavigationQueryRequest extends PageSortRequest {
  name?: string
  isEnabled?: boolean
  isVisible?: boolean
}

/**
 * 创建导航请求
 */
export interface CreateNavigationRequest {
  parentId?: string
  name: string
  path?: string
  icon?: string
  sortOrder?: number
  isEnabled?: boolean
  isVisible?: boolean
  permissionCode?: string
}

/**
 * 更新导航请求
 */
export interface UpdateNavigationRequest {
  parentId?: string
  name: string
  path?: string
  icon?: string
  sortOrder?: number
  isEnabled?: boolean
  isVisible?: boolean
  permissionCode?: string
}

/**
 * 查询导航列表
 */
export function getNavigations(params: NavigationQueryRequest) {
  return request.get<ApiResponse<PageResponse<NavigationItem>>>('/api/navigations', { params })
}

/**
 * 获取导航树
 */
export function getNavigationTree() {
  return request.get<ApiResponse<NavigationItem[]>>('/api/navigations/tree')
}

/**
 * 获取当前用户可见的导航菜单
 */
export function getUserNavigations() {
  return request.get<ApiResponse<NavigationItem[]>>('/api/navigations/user')
}

/**
 * 获取导航详情
 */
export function getNavigation(id: string) {
  return request.get<ApiResponse<NavigationItem>>(`/api/navigations/${id}`)
}

/**
 * 创建导航
 */
export function createNavigation(data: CreateNavigationRequest) {
  return request.post<ApiResponse<NavigationItem>>('/api/navigations', data)
}

/**
 * 更新导航
 */
export function updateNavigation(id: string, data: UpdateNavigationRequest) {
  return request.put<ApiResponse<NavigationItem>>(`/api/navigations/${id}`, data)
}

/**
 * 删除导航
 */
export function deleteNavigation(id: string) {
  return request.delete<ApiResponse<void>>(`/api/navigations/${id}`)
}

/**
 * 批量更新导航排序
 */
export function updateNavigationsOrder(items: Array<{ id: string; sortOrder: number }>) {
  return request.put<ApiResponse<void>>('/api/navigations/order', { items })
}

/**
 * 启用/禁用导航
 */
export function toggleNavigation(id: string, isEnabled: boolean) {
  return request.put<ApiResponse<NavigationItem>>(`/api/navigations/${id}/toggle`, { isEnabled })
}
