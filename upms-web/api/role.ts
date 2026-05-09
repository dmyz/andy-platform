import request from '@/utils/request';
import type { PageResponse } from '@/types/responses/common';
import type { Role } from '@/types/entities/role';
import type { PageSortRequest } from '@/types/requests/common';

/**
 * 角色查询请求
 */
export interface RoleQueryRequest extends PageSortRequest {
  code?: string;
  name?: string;
}

/**
 * 创建角色请求
 */
export interface CreateRoleRequest {
  code: string;
  name: string;
  dataScope: string;
  status: number;
  remark?: string | null;
}

/**
 * 更新角色请求
 */
export interface UpdateRoleRequest {
  code: string;
  name: string;
  dataScope: string;
  status: number;
  remark?: string | null;
}

/**
 * 查询角色列表
 */
export function getRoleList(params: RoleQueryRequest) {
  return request.get<PageResponse<Role>>('/admin/role/page', params);
}

/**
 * 获取所有角色（不分页）
 */
export function getAllRoles() {
  return request.get<PageResponse<Role>>('/admin/role/page', { pageNum: 1, pageSize: 1000 });
}

/**
 * 获取角色详情
 */
export function getRoleDetail(id: string) {
  return request.get<RoleDetail>(`/admin/role/${id}`);
}

/**
 * 创建角色
 */
export function createRole(data: CreateRoleRequest) {
  return request.post<Role>('/admin/role', data);
}

/**
 * 更新角色
 */
export function updateRole(id: string, data: UpdateRoleRequest) {
  return request.put<Role>(`/admin/role/${id}`, data);
}

/**
 * 删除角色
 */
export function deleteRole(id: string) {
  return request.delete<void>(`/admin/role/${id}`);
}

/**
 * 分配权限
 */
export function assignPermissions(id: string, permissionCodes: string[]) {
  return request.post<void>(`/admin/role/${id}/permissions`, { permissionCodes });
}

/**
 * 获取角色权限列表
 */
export function getRolePermissions(id: string) {
  return request.get<string[]>(`/admin/role/${id}/permissions`);
}

// 以下是实际项目使用的 API 函数

interface RolePageParams {
  pageNum: number;
  pageSize: number;
  name?: string;
  code?: string;
  status?: number;
}

export interface RolePageItem {
  id: string;
  name: string;
  code: string;
  dataScope: string;
  permissionCount: number;
  status: number;
  remark?: string;
  createTime: string;
}

export interface RoleDetail {
  id: string;
  name: string;
  code: string;
  dataScope: string;
  status: number;
  remark?: string;
  createTime: string;
  updateTime?: string;
}

export interface PermissionItem {
  code: string;
  name: string;
  type: string;
  category: string;
  moduleName?: string;
}

export interface RelatedUserItem {
  id: string;
  username: string;
  realName: string;
  orgName: string;
  status: number;
}

interface RoleFormData {
  name: string;
  code: string;
  dataScope: string;
  status: number;
  remark?: string | null;
}

/**
 * 分页查询角色列表
 */
export function getRolePage(params: RolePageParams) {
  return request.get<{
    list: RolePageItem[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/admin/role/page', params);
}

/**
 * 获取角色详情
 */
export function getRole(id: string) {
  return request.get<RoleDetail>(`/admin/role/${id}`);
}

/**
 * 创建角色
 */
export function createRoleRecord(data: RoleFormData) {
  return request.post('/admin/role', data);
}

/**
 * 更新角色
 */
export function updateRoleRecord(id: string, data: RoleFormData) {
  return request.put(`/admin/role/${id}`, data);
}

/**
 * 删除角色
 */
export function deleteRoleRecord(id: string) {
  return request.delete(`/admin/role/${id}`);
}

/**
 * 更新角色状态
 */
export function updateRoleStatus(id: string, data: { status: number }) {
  return request.put(`/admin/role/${id}/status`, data);
}

/**
 * 获取权限目录（树形结构）
 */
export function getPermissionCatalog() {
  return request.get<PermissionItem[]>('/admin/role/permission/catalog');
}

/**
 * 获取角色已分配的权限列表
 */
export function getRolePermissionList(id: string) {
  return request.get<PermissionItem[]>(`/admin/role/${id}/permissions`);
}

/**
 * 分配角色权限
 */
export function assignRolePermissions(id: string, data: { permissionCodes: string[] }) {
  return request.post(`/admin/role/${id}/permissions`, data);
}

/**
 * 获取角色关联的用户列表
 */
export function getRoleUsers(id: string) {
  return request.get<RelatedUserItem[]>(`/admin/role/${id}/users`);
}
