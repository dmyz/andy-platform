import type { PageSortRequest } from '../requests/common';
import type { UserStatus } from '../entities/user';

/**
 * 用户查询请求
 */
export interface UserQueryRequest extends PageSortRequest {
  username?: string;
  realName?: string;
  email?: string;
  mobile?: string;
  status?: UserStatus;
  orgName?: string;
  startTime?: string;
  endTime?: string;
}

/**
 * 创建用户请求
 */
export interface CreateUserRequest {
  username: string;
  realName: string;
  jobNumber?: string | null;
  mobile: string;
  email?: string;
  gender?: string | null;
  orgId: string;
  position?: string | null;
  password?: string | null;
  status: UserStatus;
  remark?: string | null;
}

/**
 * 更新用户请求
 */
export interface UpdateUserRequest {
  realName?: string;
  jobNumber?: string | null;
  mobile?: string;
  email?: string;
  gender?: string | null;
  orgId?: string;
  position?: string | null;
  status?: UserStatus;
  remark?: string | null;
}

/**
 * 分配角色请求
 */
export interface AssignRolesRequest {
  roleCodes: string[];
}
