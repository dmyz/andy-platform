/**
 * 角色实体
 */
export interface Role {
  id: string
  code: string
  name: string
  description?: string
  createdAt: string
  updatedAt: string
  createdBy: string
  updatedBy: string
}

/**
 * 角色详情（包含权限信息）
 */
export interface RoleDetail extends Role {
  permissions: string[]
}
