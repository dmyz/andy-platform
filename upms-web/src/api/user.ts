import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/responses/common'
import type { User, UserDetail } from '@/types/entities/user'
import type { UserQueryRequest, CreateUserRequest, UpdateUserRequest, AssignRolesRequest, AssignOrganizationsRequest } from '@/types/api/user'

/**
 * 查询用户列表
 */
export function getUserList(params: UserQueryRequest) {
  return request.get<ApiResponse<PageResponse<User>>>('/api/users', { params })
}

/**
 * 获取用户详情
 */
export function getUserDetail(id: string) {
  return request.get<ApiResponse<UserDetail>>(`/api/users/${id}`)
}

/**
 * 创建用户
 */
export function createUser(data: CreateUserRequest) {
  return request.post<ApiResponse<User>>('/api/users', data)
}

/**
 * 更新用户
 */
export function updateUser(id: string, data: UpdateUserRequest) {
  return request.put<ApiResponse<User>>(`/api/users/${id}`, data)
}

/**
 * 删除用户
 */
export function deleteUser(id: string) {
  return request.delete<ApiResponse<void>>(`/api/users/${id}`)
}

/**
 * 更新用户状态
 */
export function updateUserStatus(id: string, status: 'ACTIVE' | 'INACTIVE' | 'LOCKED') {
  return request.put<ApiResponse<void>>(`/api/users/${id}/status`, { status })
}

/**
 * 重置用户密码
 */
export function resetUserPassword(id: string, newPassword: string) {
  return request.post<ApiResponse<void>>(`/api/users/${id}/reset-password`, { newPassword })
}

/**
 * 分配角色
 */
export function assignRoles(id: string, data: AssignRolesRequest) {
  return request.post<ApiResponse<void>>(`/api/users/${id}/roles`, data)
}

/**
 * 分配组织
 */
export function assignOrganizations(id: string, data: AssignOrganizationsRequest) {
  return request.post<ApiResponse<void>>(`/api/users/${id}/organizations`, data)
}

/**
 * 获取用户角色列表
 */
export function getUserRoles(id: string) {
  return request.get<ApiResponse<string[]>>(`/api/users/${id}/roles`)
}

/**
 * 获取用户组织列表
 */
export function getUserOrganizations(id: string) {
  return request.get<ApiResponse<string[]>>(`/api/users/${id}/organizations`)
}

/**
 * 更新个人资料
 */
export function updateProfile(data: UpdateUserRequest) {
  return request.put<ApiResponse<User>>('/api/users/profile', data)
}

/**
 * 上传头像
 */
export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<{ url: string }>>('/api/users/avatar', formData)
}
