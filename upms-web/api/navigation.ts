import request from '@/utils/request';
import type { PageSortRequest } from '@/types/requests/common';

/**
 * 权限项
 */
export interface PermissionItem {
  code: string;
  name: string;
  type: string;
  category: string;
  moduleName?: string;
}

/**
 * 导航菜单项
 */
export interface NavigationItem {
  id: string;
  parentId?: string | null;
  name: string;
  type: 'GROUP' | 'PAGE' | 'LINK';
  routePath?: string | null;
  componentPath?: string | null;
  externalUrl?: string | null;
  icon?: string | null;
  sortOrder: number;
  visible: boolean;
  status: number;
  children?: NavigationItem[];
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 导航查询请求
 */
export interface NavigationQueryRequest extends PageSortRequest {
  name?: string;
  isEnabled?: boolean;
  isVisible?: boolean;
}

/**
 * 创建导航请求
 */
export interface CreateNavigationRequest {
  parentId?: string | null;
  name: string;
  type: 'GROUP' | 'PAGE' | 'LINK';
  routePath?: string | null;
  componentPath?: string | null;
  externalUrl?: string | null;
  icon?: string | null;
  sortOrder?: number;
  visible?: boolean;
  status?: number;
}

/**
 * 更新导航请求
 */
export interface UpdateNavigationRequest {
  parentId?: string | null;
  name: string;
  type: 'GROUP' | 'PAGE' | 'LINK';
  routePath?: string | null;
  componentPath?: string | null;
  externalUrl?: string | null;
  icon?: string | null;
  sortOrder?: number;
  visible?: boolean;
  status?: number;
}

/**
 * 获取导航树
 */
export function getNavigationTree() {
  return request.get<NavigationItem[]>('/admin/navigation/tree');
}

/**
 * 获取导航详情
 */
export function getNavigation(id: string) {
  return request.get<NavigationItem>(`/admin/navigation/${id}`);
}

/**
 * 创建导航
 */
export function createNavigation(data: CreateNavigationRequest) {
  return request.post<NavigationItem>('/admin/navigation', data);
}

/**
 * 更新导航
 */
export function updateNavigation(id: string, data: UpdateNavigationRequest) {
  return request.put<NavigationItem>(`/admin/navigation/${id}`, data);
}

/**
 * 删除导航
 */
export function deleteNavigation(id: string) {
  return request.delete<void>(`/admin/navigation/${id}`);
}

/**
 * 获取导航权限列表
 */
export function getNavigationPermissions(id: string) {
  return request.get<PermissionItem[]>(`/admin/navigation/${id}/permissions`);
}

/**
 * 分配导航权限
 */
export function assignNavigationPermissions(id: string, data: { permissionCodes: string[] }) {
  return request.post<void>(`/admin/navigation/${id}/permissions`, data);
}
