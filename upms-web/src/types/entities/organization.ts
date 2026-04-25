/**
 * 组织状态枚举
 */
export type OrganizationStatus = 'ACTIVE' | 'INACTIVE'

/**
 * 组织实体
 */
export interface Organization {
  id: string
  parentId?: string | null
  code: string
  name: string
  leaderUserId?: string | null
  level: number
  sortOrder: number
  status: OrganizationStatus
  createdAt: string
  updatedAt: string
  createdBy: string
  updatedBy: string
}

/**
 * 组织树节点
 */
export interface OrganizationTreeNode extends Organization {
  children?: OrganizationTreeNode[]
}

/**
 * 组织成员
 */
export interface OrganizationMember {
  id: string
  userId: string
  organizationId: string
  isPrimary: boolean
  joinedAt: string
}
