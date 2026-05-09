export type UserStatus = number;

/**
 * 用户实体
 */
export interface User {
  id: string;
  username: string;
  realName: string;
  jobNumber?: string | null;
  mobile: string;
  email?: string | null;
  gender?: string | null;
  orgId: string;
  orgName: string;
  position?: string | null;
  status: UserStatus;
  lastLoginTime?: string | null;
  createTime: string;
}

/**
 * 用户详情（包含关联信息）
 */
export interface UserDetail extends User {
  remark?: string | null;
  passwordResetRequired?: boolean;
}
