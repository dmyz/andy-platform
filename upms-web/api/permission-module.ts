import request from '@/utils/request';
import type { PageResponse } from '@/types/responses/common';

/**
 * 权限模块选项
 */
export interface PermissionModuleOption {
  label: string;
  value: string;
  sortOrder: number;
}

/**
 * 权限模块
 */
export interface PermissionModule {
  id: string;
  name: string;
  code: string;
  parentCode?: string | null;
  sortOrder: number;
  status: number;
  remark?: string | null;
  updateTime?: string;
}

/**
 * 权限模块查询请求
 */
export interface PermissionModuleQueryRequest {
  pageNum: number;
  pageSize: number;
  name?: string;
  code?: string;
  status?: number;
}

/**
 * 创建或更新权限模块请求
 */
export interface PermissionModuleRequest {
  name: string;
  code: string;
  parentCode?: string | null;
  sortOrder: number;
  status: number;
  remark?: string | null;
}

/**
 * 查询权限模块选项
 */
export function getPermissionModuleOptions() {
  return request.get<PermissionModuleOption[]>('/admin/permission-module/options');
}

/**
 * 分页查询权限模块
 */
export function getPermissionModulePage(params: PermissionModuleQueryRequest) {
  return request.get<PageResponse<PermissionModule>>('/admin/permission-module/page', params);
}

/**
 * 获取权限模块详情
 */
export function getPermissionModule(id: string) {
  return request.get<PermissionModule>(`/admin/permission-module/${id}`);
}

/**
 * 创建权限模块
 */
export function createPermissionModule(data: PermissionModuleRequest) {
  return request.post<PermissionModule>('/admin/permission-module', data);
}

/**
 * 更新权限模块
 */
export function updatePermissionModule(id: string, data: PermissionModuleRequest) {
  return request.put<PermissionModule>(`/admin/permission-module/${id}`, data);
}

/**
 * 删除权限模块
 */
export function deletePermissionModule(id: string) {
  return request.delete<void>(`/admin/permission-module/${id}`);
}
