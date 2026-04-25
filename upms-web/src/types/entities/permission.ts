/**
 * 权限类型枚举
 */
export type PermissionType = 'MENU' | 'BUTTON' | 'API'

/**
 * 权限实体
 */
export interface Permission {
  id: string
  code: string
  name: string
  type: PermissionType
  parentId?: string
  path?: string
  icon?: string
  sortOrder: number
  description?: string
  createdAt: string
  updatedAt: string
  createdBy: string
  updatedBy: string
}

/**
 * 权限树节点
 */
export interface PermissionTreeNode extends Permission {
  children?: PermissionTreeNode[]
}
