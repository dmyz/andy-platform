export type OrganizationStatus = number;

/**
 * 组织实体
 */
export interface Organization {
  id: string;
  parentId?: string | null;
  parentName?: string | null;
  name: string;
  code: string;
  leader?: string | null;
  level: number;
  sort: number;
  status: OrganizationStatus;
  remark?: string | null;
}

/**
 * 组织树节点
 */
export interface OrganizationTreeNode {
  id: string;
  parentId?: string | null;
  name: string;
  code: string;
  status: OrganizationStatus;
  sort: number;
  children?: OrganizationTreeNode[];
}

/**
 * 组织成员
 */
export interface OrganizationMember {
  id: string;
  username: string;
  displayName: string;
  mobile?: string;
  email?: string;
  positionName?: string;
  status: OrganizationStatus;
}
