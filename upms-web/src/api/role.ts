import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/responses/common'
import type { Role, RoleDetail } from '@/types/entities/role'
import type { PageSortRequest } from '@/types/requests/common'

/**
 * 角色查询请求
 */
export interface RoleQueryRequest extends PageSortRequest {
  code?: string
  name?: string
}

/**
 * 创建角色请求
 */
export interface CreateRoleRequest {
  code: string
  name: string
  description?: string
  permissionIds?: string[]
}

/**
 * 更新角色请求
 */
export interface UpdateRoleRequest {
  name?: string
  description?: string
}

/**
 * 查询角色列表
 */
export function getRoleList(params: RoleQueryRequest) {
  return request.get<ApiResponse<PageResponse<Role>>>('/api/roles', { params })
}

/**
 * 获取所有角色（不分页）
 */
export function getAllRoles() {
  return request.get<ApiResponse<Role[]>>('/api/roles/all')
}

/**
 * 获取角色详情
 */
export function getRoleDetail(id: string) {
  return request.get<ApiResponse<RoleDetail>>(`/api/roles/${id}`)
}

/**
 * 创建角色
 */
export function createRole(data: CreateRoleRequest) {
  return request.post<ApiResponse<Role>>('/api/roles', data)
}

/**
 * 更新角色
 */
export function updateRole(id: string, data: UpdateRoleRequest) {
  return request.put<ApiResponse<Role>>(`/api/roles/${id}`, data)
}

/**
 * 删除角色
 */
export function deleteRole(id: string) {
  return request.delete<ApiResponse<void>>(`/api/roles/${id}`)
}

/**
 * 分配权限
 */
export function assignPermissions(id: string, permissionIds: string[]) {
  return request.post<ApiResponse<void>>(`/api/roles/${id}/permissions`, { permissionIds })
}

/**
 * 获取角色权限列表
 */
export function getRolePermissions(id: string) {
  return request.get<ApiResponse<string[]>>(`/api/roles/${id}/permissions`)
}
