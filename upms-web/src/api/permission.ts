import request from '@/utils/request'
import type { ApiResponse } from '@/types/responses/common'
import type { Permission, PermissionTreeNode, PermissionType } from '@/types/entities/permission'

/**
 * 创建权限请求
 */
export interface CreatePermissionRequest {
  code: string
  name: string
  type: PermissionType
  parentId?: string
  path?: string
  icon?: string
  sortOrder?: number
  description?: string
}

/**
 * 更新权限请求
 */
export interface UpdatePermissionRequest {
  name?: string
  path?: string
  icon?: string
  sortOrder?: number
  description?: string
}

/**
 * 获取权限树
 */
export function getPermissionTree() {
  return request.get<ApiResponse<PermissionTreeNode[]>>('/api/permissions/tree')
}

/**
 * 获取权限列表
 */
export function getPermissionList() {
  return request.get<ApiResponse<Permission[]>>('/api/permissions')
}

/**
 * 获取权限详情
 */
export function getPermissionDetail(id: string) {
  return request.get<ApiResponse<Permission>>(`/api/permissions/${id}`)
}

/**
 * 创建权限
 */
export function createPermission(data: CreatePermissionRequest) {
  return request.post<ApiResponse<Permission>>('/api/permissions', data)
}

/**
 * 更新权限
 */
export function updatePermission(id: string, data: UpdatePermissionRequest) {
  return request.put<ApiResponse<Permission>>(`/api/permissions/${id}`, data)
}

/**
 * 删除权限
 */
export function deletePermission(id: string) {
  return request.delete<ApiResponse<void>>(`/api/permissions/${id}`)
}

/**
 * 获取当前用户权限列表
 */
export function getCurrentUserPermissions() {
  return request.get<ApiResponse<Permission[]>>('/api/permissions/current')
}
