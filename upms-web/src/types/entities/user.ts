/**
 * 用户状态枚举
 */
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED'

/**
 * 用户实体
 */
export interface User {
  id: string
  username: string
  displayName: string
  avatar?: string
  email?: string
  mobile?: string
  status: UserStatus
  passwordResetRequired: boolean
  createdAt: string
  updatedAt: string
  createdBy: string
  updatedBy: string
}

/**
 * 用户详情（包含关联信息）
 */
export interface UserDetail extends User {
  roles: string[]
  organizations: string[]
  permissions: string[]
}
