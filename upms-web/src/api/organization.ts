import request from '@/utils/request'
import type { ApiResponse } from '@/types/responses/common'
import type { Organization, OrganizationTreeNode, OrganizationType } from '@/types/entities/organization'

/**
 * 创建组织请求
 */
export interface CreateOrganizationRequest {
  code: string
  name: string
  type: OrganizationType
  parentId?: string
  sortOrder?: number
  description?: string
}

/**
 * 更新组织请求
 */
export interface UpdateOrganizationRequest {
  name?: string
  sortOrder?: number
  description?: string
}

/**
 * 获取组织树
 */
export function getOrganizationTree() {
  return request.get<ApiResponse<OrganizationTreeNode[]>>('/api/organizations/tree')
}

/**
 * 获取组织列表
 */
export function getOrganizationList() {
  return request.get<ApiResponse<Organization[]>>('/api/organizations')
}

/**
 * 获取组织详情
 */
export function getOrganizationDetail(id: string) {
  return request.get<ApiResponse<Organization>>(`/api/organizations/${id}`)
}

/**
 * 创建组织
 */
export function createOrganization(data: CreateOrganizationRequest) {
  return request.post<ApiResponse<Organization>>('/api/organizations', data)
}

/**
 * 更新组织
 */
export function updateOrganization(id: string, data: UpdateOrganizationRequest) {
  return request.put<ApiResponse<Organization>>(`/api/organizations/${id}`, data)
}

/**
 * 删除组织
 */
export function deleteOrganization(id: string) {
  return request.delete<ApiResponse<void>>(`/api/organizations/${id}`)
}

/**
 * 获取组织成员
 */
export function getOrganizationMembers(id: string) {
  return request.get<ApiResponse<string[]>>(`/api/organizations/${id}/members`)
}

/**
 * 添加组织成员
 */
export function addOrganizationMembers(id: string, userIds: string[]) {
  return request.post<ApiResponse<void>>(`/api/organizations/${id}/members`, { userIds })
}

/**
 * 移除组织成员
 */
export function removeOrganizationMember(id: string, userId: string) {
  return request.delete<ApiResponse<void>>(`/api/organizations/${id}/members/${userId}`)
}
