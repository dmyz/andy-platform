import request from '@/utils/request';
import type {
  Organization,
  OrganizationMember,
  OrganizationTreeNode,
} from '@/types/entities/organization';

/**
 * 创建组织请求
 */
export interface CreateOrganizationRequest {
  parentId?: string | null;
  code: string;
  name: string;
  leader?: string | null;
  sort: number;
  status: number;
  remark?: string | null;
}

/**
 * 更新组织请求
 */
export interface UpdateOrganizationRequest {
  parentId?: string | null;
  code: string;
  name: string;
  leader?: string | null;
  sort: number;
  status: number;
  remark?: string | null;
}

/**
 * 获取组织树
 */
export function getOrganizationTree() {
  return request.get<OrganizationTreeNode[]>('/admin/org/tree');
}

/**
 * 获取组织列表
 */
export function getOrganizationList() {
  return request.get<OrganizationTreeNode[]>('/admin/org/tree');
}

/**
 * 获取组织详情
 */
export function getOrganizationDetail(id: string) {
  return request.get<Organization>(`/admin/org/${id}`);
}

/**
 * 创建组织
 */
export function createOrganization(data: CreateOrganizationRequest) {
  return request.post<Organization>('/admin/org', data);
}

/**
 * 更新组织
 */
export function updateOrganization(id: string, data: UpdateOrganizationRequest) {
  return request.put<Organization>(`/admin/org/${id}`, data);
}

/**
 * 删除组织
 */
export function deleteOrganization(id: string) {
  return request.delete<void>(`/admin/org/${id}`);
}

/**
 * 获取组织成员
 */
export function getOrganizationMembers(id: string) {
  return request.get<OrganizationMember[]>(`/admin/org/${id}/members`);
}

// 以下是实际项目使用的 API 函数

interface OrgTreeItem {
  id: string;
  parentId?: string | null;
  name: string;
  code: string;
  status: number;
  sort: number;
  children?: OrgTreeItem[];
}

interface OrgDetail {
  id: string;
  parentId?: string | null;
  parentName?: string | null;
  name: string;
  code: string;
  leader?: string | null;
  level: number;
  sort: number;
  status: number;
  remark?: string | null;
}

interface OrgMember {
  id: string;
  username: string;
  displayName: string;
  mobile?: string;
  email?: string;
  positionName?: string;
  status: number;
}

interface OrgFormData {
  parentId?: string | null;
  name: string;
  code: string;
  leader?: string | null;
  sort: number;
  status: number;
  remark?: string | null;
}

/**
 * 获取组织树
 */
export function getOrgTree(params?: { keyword?: string; includeDisabled?: boolean }) {
  return request.get<OrgTreeItem[]>('/admin/org/tree', params);
}

/**
 * 获取组织详情
 */
export function getOrg(id: string) {
  return request.get<OrgDetail>(`/admin/org/${id}`);
}

/**
 * 获取组织成员列表
 */
export function getOrgMembers(id: string, params?: { displayName?: string; status?: number }) {
  return request.get<OrgMember[]>(`/admin/org/${id}/members`, params);
}

/**
 * 创建组织
 */
export function createOrg(data: OrgFormData) {
  return request.post<OrgDetail>('/admin/org', data);
}

/**
 * 更新组织
 */
export function updateOrg(id: string, data: OrgFormData) {
  return request.put<OrgDetail>(`/admin/org/${id}`, data);
}

/**
 * 删除组织
 */
export function deleteOrg(id: string) {
  return request.delete(`/admin/org/${id}`);
}

/**
 * 更新组织状态
 */
export function updateOrgStatus(id: string, status: number) {
  return request.put(`/admin/org/${id}/status`, { status });
}
