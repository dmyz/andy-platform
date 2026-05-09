import request from '@/utils/request';

/**
 * 权限项
 */
export interface PermissionItem {
  id: string;
  name: string;
  code: string;
  type: 'API' | 'UI_ACTION' | 'DATA' | 'NAV_ACCESS' | 'MENU' | 'BUTTON';
  resourceType: string;
  actionCode?: string | null;
  moduleCode: string;
  moduleName?: string;
  parentId?: string;
  status: number;
  remark?: string | null;
  updateTime: string;
}

/**
 * 权限查询参数
 */
export interface PermissionQueryParams {
  pageNum: number;
  pageSize: number;
  name?: string;
  code?: string;
  type?: string;
  status?: number;
}

/**
 * 创建/更新权限请求
 */
export interface PermissionRequest {
  name: string;
  code: string;
  type: 'API' | 'UI_ACTION' | 'DATA' | 'NAV_ACCESS' | 'MENU' | 'BUTTON';
  resourceType: string;
  actionCode?: string | null;
  moduleCode: string;
  status: number;
  remark?: string | null;
}

/**
 * 分页查询权限
 */
export function getPermissionPage(params: PermissionQueryParams) {
  return request.get<{
    list: PermissionItem[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/admin/permission/page', params);
}

/**
 * 获取权限详情
 */
export function getPermission(id: string) {
  return request.get<PermissionItem>(`/admin/permission/${id}`);
}

/**
 * 创建权限
 */
export function createPermission(data: PermissionRequest) {
  return request.post<PermissionItem>('/admin/permission', data);
}

/**
 * 更新权限
 */
export function updatePermission(id: string, data: PermissionRequest) {
  return request.put<PermissionItem>(`/admin/permission/${id}`, data);
}

/**
 * 删除权限
 */
export function deletePermission(id: string) {
  return request.delete<void>(`/admin/permission/${id}`);
}

/**
 * 获取所有权限列表（不分页）
 */
export function getPermissionList() {
  return request
    .get<{
      list: PermissionItem[];
      total: number;
      pageNum: number;
      pageSize: number;
    }>('/admin/permission/page', { pageNum: 1, pageSize: 1000 })
    .then((res) => res.list);
}

/**
 * 获取当前用户权限编码
 */
export function getCurrentUserPermissionCodes() {
  return request.get<string[]>('/admin/permission/current-user/codes');
}
