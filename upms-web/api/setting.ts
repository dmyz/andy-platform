import request from '@/utils/request';

/**
 * 系统配置项
 */
export interface SettingItem {
  id: string;
  settingKey: string;
  settingName: string;
  settingValue: string;
  valueType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON';
  scopeType: 'GLOBAL' | 'ORG' | 'USER';
  scopeId?: string | null;
  groupCode: string;
  secretFlag: boolean;
  effectiveMode: 'IMMEDIATE' | 'RESTART_REQUIRED';
  status: number;
  remark?: string | null;
  updateTime: string;
}

/**
 * 系统配置查询参数
 */
export interface SettingQueryParams {
  pageNum: number;
  pageSize: number;
  settingName?: string;
  settingKey?: string;
  groupCode?: string;
}

/**
 * 创建/更新系统配置请求
 */
export interface SettingRequest {
  settingKey: string;
  settingName: string;
  settingValue: string;
  valueType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON';
  scopeType: 'GLOBAL' | 'ORG' | 'USER';
  scopeId?: string | null;
  groupCode: string;
  secretFlag: boolean;
  effectiveMode: 'IMMEDIATE' | 'RESTART_REQUIRED';
  status: number;
  remark?: string | null;
}

/**
 * 分页查询系统配置
 */
export function getSettingPage(params: SettingQueryParams) {
  return request.get<{
    list: SettingItem[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/admin/setting/page', params);
}

/**
 * 获取系统配置详情
 */
export function getSetting(id: string) {
  return request.get<SettingItem>(`/admin/setting/${id}`);
}

/**
 * 创建系统配置
 */
export function createSetting(data: SettingRequest) {
  return request.post<SettingItem>('/admin/setting', data);
}

/**
 * 更新系统配置
 */
export function updateSetting(id: string, data: SettingRequest) {
  return request.put<SettingItem>(`/admin/setting/${id}`, data);
}

/**
 * 删除系统配置
 */
export function deleteSetting(id: string) {
  return request.delete<void>(`/admin/setting/${id}`);
}

/**
 * 根据配置键获取系统配置
 */
export function getSettingByKey(settingKey: string) {
  return request.get<SettingItem>(`/admin/setting/key/${settingKey}`);
}
