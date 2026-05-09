/**
 * 角色实体
 */
export interface Role {
  id: string;
  name: string;
  code: string;
  dataScope: string;
  permissionCount?: number;
  status: number;
  remark?: string | null;
  createTime: string;
  updateTime?: string | null;
}

/**
 * 角色详情（包含权限信息）
 */
export interface RoleDetail extends Role {
  permissions?: string[];
}
