import request from '@/utils/request';

/**
 * 字典类型
 */
export interface DictionaryType {
  id: string;
  name: string;
  code: string;
  status: number;
  remark?: string | null;
  updateTime: string;
}

/**
 * 字典项
 */
export interface DictionaryItem {
  id: string;
  dictionaryId: string;
  dictionaryName: string;
  dictionaryCode: string;
  name: string;
  value: string;
  sortOrder: number;
  status: number;
  remark?: string | null;
  updateTime: string;
}

/**
 * 字典类型查询请求
 */
export interface DictionaryTypeQueryRequest {
  pageNum: number;
  pageSize: number;
  name?: string;
  code?: string;
  [key: string]: unknown;
}

/**
 * 字典项查询请求
 */
export interface DictionaryItemQueryRequest {
  name?: string;
  status?: number;
  [key: string]: unknown;
}

/**
 * 创建字典类型请求
 */
export interface CreateDictionaryTypeRequest {
  name: string;
  code: string;
  status: number;
  remark?: string | null;
}

/**
 * 更新字典类型请求
 */
export interface UpdateDictionaryTypeRequest {
  name: string;
  code: string;
  status: number;
  remark?: string | null;
}

/**
 * 创建字典项请求
 */
export interface CreateDictionaryItemRequest {
  name: string;
  value: string;
  sortOrder: number;
  status: number;
  remark?: string | null;
}

/**
 * 更新字典项请求
 */
export interface UpdateDictionaryItemRequest {
  name: string;
  value: string;
  sortOrder: number;
  status: number;
  remark?: string | null;
}

/**
 * 分页查询字典类型列表
 */
export function getDictionaryPage(params: DictionaryTypeQueryRequest) {
  return request.get<{
    list: DictionaryType[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/admin/dictionary/page', params);
}

/**
 * 获取字典类型详情
 */
export function getDictionary(id: string) {
  return request.get<DictionaryType>(`/admin/dictionary/${id}`);
}

/**
 * 创建字典类型
 */
export function createDictionary(data: CreateDictionaryTypeRequest) {
  return request.post<DictionaryType>('/admin/dictionary', data);
}

/**
 * 更新字典类型
 */
export function updateDictionary(id: string, data: UpdateDictionaryTypeRequest) {
  return request.put<DictionaryType>(`/admin/dictionary/${id}`, data);
}

/**
 * 删除字典类型
 */
export function deleteDictionary(id: string) {
  return request.delete<void>(`/admin/dictionary/${id}`);
}

/**
 * 获取字典项列表
 */
export function getDictionaryItems(dictionaryId: string, params: DictionaryItemQueryRequest) {
  return request.get<DictionaryItem[]>(`/admin/dictionary/${dictionaryId}/items`, params);
}

/**
 * 创建字典项
 */
export function createDictionaryItem(dictionaryId: string, data: CreateDictionaryItemRequest) {
  return request.post<DictionaryItem>(`/admin/dictionary/${dictionaryId}/items`, data);
}

/**
 * 更新字典项
 */
export function updateDictionaryItem(itemId: string, data: UpdateDictionaryItemRequest) {
  return request.put<DictionaryItem>(`/admin/dictionary/item/${itemId}`, data);
}

/**
 * 删除字典项
 */
export function deleteDictionaryItem(itemId: string) {
  return request.delete<void>(`/admin/dictionary/item/${itemId}`);
}

/**
 * 根据字典编码获取字典项
 */
export function getDictionaryOptionsByCode(dictCode: string) {
  return request.get<DictionaryItem[]>(`/admin/dictionary/code/${dictCode}`);
}
