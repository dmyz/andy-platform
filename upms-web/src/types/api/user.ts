import type { PageSortRequest } from '../requests/common'
import type { UserStatus } from '../entities/user'

/**
 * 用户查询请求
 */
export interface UserQueryRequest extends PageSortRequest {
  username?: string
  displayName?: string
  email?: string
  mobile?: string
  status?: UserStatus
  organizationId?: string
  roleId?: string
}

/**
 * 创建用户请求
 */
export interface CreateUserRequest {
  username: string
  displayName: string
  password: string
  email?: string
  mobile?: string
  status?: UserStatus
  roleIds?: string[]
  organizationIds?: string[]
}

/**
 * 更新用户请求
 */
export interface UpdateUserRequest {
  displayName?: string
  email?: string
  mobile?: string
  status?: UserStatus
  avatar?: string
}

/**
 * 分配角色请求
 */
export interface AssignRolesRequest {
  roleIds: string[]
}

/**
 * 分配组织请求
 */
export interface AssignOrganizationsRequest {
  organizationIds: string[]
}
