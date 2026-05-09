import request from '@/utils/request';
import type { PageResponse } from '@/types/responses/common';
import type { User, UserDetail } from '@/types/entities/user';
import type {
  UserQueryRequest,
  CreateUserRequest,
  UpdateUserRequest,
  AssignRolesRequest,
} from '@/types/api/user';

/**
 * 查询用户列表
 */
export function getUserList(params: UserQueryRequest) {
  return request.get<PageResponse<User>>('/admin/user/page', params);
}

/**
 * 获取用户详情
 */
export function getUserDetail(id: string) {
  return request.get<UserDetail>(`/admin/user/${id}`);
}

/**
 * 创建用户
 */
export function createUser(data: CreateUserRequest) {
  return request.post<User>('/admin/user', data);
}

/**
 * 更新用户
 */
export function updateUser(id: string, data: UpdateUserRequest) {
  return request.put<User>(`/admin/user/${id}`, data);
}

/**
 * 删除用户
 */
export function deleteUser(id: string) {
  return request.delete<void>(`/admin/user/${id}`);
}

/**
 * 更新用户状态
 */
export function updateUserStatus(id: string, status: number) {
  return request.put<void>(`/admin/user/${id}/status`, { status });
}

/**
 * 重置用户密码
 */
export function resetUserPassword(id: string) {
  return request.post<void>(`/admin/user/${id}/password/reset`);
}

/**
 * 分配角色
 */
export function assignRoles(id: string, data: AssignRolesRequest) {
  return request.post<void>(`/admin/user/${id}/roles`, data);
}

/**
 * 获取用户角色列表
 */
export function getUserRoles(id: string) {
  return request.get<string[]>(`/admin/user/${id}/roles`);
}

/**
 * 更新个人资料
 */
export function updateProfile(data: UpdateUserRequest) {
  return request.put<User>('/admin/profile/me', data);
}

/**
 * 上传头像
 */
export function uploadAvatar(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<{ url: string }>('/admin/profile/avatar', formData);
}

// 以下是实际项目使用的 API 函数

interface UserPageParams {
  pageNum: number;
  pageSize: number;
  username?: string;
  realName?: string;
  mobile?: string;
  orgName?: string;
  status?: number;
  startTime?: string;
  endTime?: string;
}

interface UserPageItem {
  id: string;
  username: string;
  realName: string;
  jobNumber?: string;
  mobile: string;
  email?: string;
  orgId: string;
  orgName: string;
  position?: string;
  status: number;
  lastLoginTime?: string;
  createTime: string;
}

interface UserDetailView extends UserPageItem {
  gender?: string;
  remark?: string;
  passwordResetRequired?: boolean;
}

interface UserRoleItem {
  code: string;
  name: string;
}

interface UserImportResult {
  importedCount: number;
  updatedCount: number;
  skippedCount: number;
}

interface UserFormData {
  username: string;
  realName: string;
  jobNumber?: string | null;
  mobile: string;
  email?: string | null;
  gender: string;
  orgId: string;
  position?: string | null;
  password?: string | null;
  status: number;
  remark?: string | null;
}

/**
 * 分页查询用户列表
 */
export function getUserPage(params: UserPageParams) {
  return request.get<{
    list: UserPageItem[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/admin/user/page', params);
}

/**
 * 获取用户详情
 */
export function getUser(id: string) {
  return request.get<UserDetailView>(`/admin/user/${id}`);
}

/**
 * 创建用户
 */
export function createUserRecord(data: UserFormData) {
  return request.post('/admin/user', data);
}

/**
 * 更新用户
 */
export function updateUserRecord(id: string, data: UserFormData) {
  return request.put(`/admin/user/${id}`, data);
}

/**
 * 删除用户
 */
export function deleteUserRecord(id: string) {
  return request.delete(`/admin/user/${id}`);
}

/**
 * 更新用户状态
 */
export function updateUserRecordStatus(id: string, status: number) {
  return request.put(`/admin/user/${id}/status`, { status });
}

/**
 * 获取用户角色列表
 */
export function getUserRoleList(id: string) {
  return request.get<UserRoleItem[]>(`/admin/user/${id}/roles`);
}

/**
 * 分配用户角色
 */
export function assignUserRoles(id: string, roleCodes: string[]) {
  return request.post(`/admin/user/${id}/roles`, { roleCodes });
}

/**
 * 重置用户密码
 */
export function resetUserPasswordRecord(id: string) {
  return request.post(`/admin/user/${id}/password/reset`);
}

/**
 * 强制用户下线
 */
export function forceUserOffline(id: string) {
  return request.post(`/admin/user/${id}/offline`);
}

/**
 * 导入用户
 */
export function importUsers(formData: FormData) {
  return request.post<UserImportResult>('/admin/user/import', formData);
}

/**
 * 导出用户
 */
export function exportUsers(params: Partial<UserPageParams>) {
  return request.download('/admin/user/export', {
    params,
    filename: 'user-export.csv',
  });
}
