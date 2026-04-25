import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/responses/common'
import type { PageSortRequest } from '@/types/requests/common'

/**
 * 字典类型
 */
export interface DictionaryType {
  id: string
  code: string
  name: string
  description?: string
  isSystem: boolean
  createdAt: string
  updatedAt: string
}

/**
 * 字典项
 */
export interface DictionaryItem {
  id: string
  typeCode: string
  code: string
  label: string
  value: string
  sortOrder: number
  isEnabled: boolean
  description?: string
  parentCode?: string
  createdAt: string
  updatedAt: string
}

/**
 * 字典类型查询请求
 */
export interface DictionaryTypeQueryRequest extends PageSortRequest {
  code?: string
  name?: string
  isSystem?: boolean
}

/**
 * 字典项查询请求
 */
export interface DictionaryItemQueryRequest extends PageSortRequest {
  typeCode?: string
  code?: string
  label?: string
  isEnabled?: boolean
}

/**
 * 创建字典类型请求
 */
export interface CreateDictionaryTypeRequest {
  code: string
  name: string
  description?: string
}

/**
 * 更新字典类型请求
 */
export interface UpdateDictionaryTypeRequest {
  name: string
  description?: string
}

/**
 * 创建字典项请求
 */
export interface CreateDictionaryItemRequest {
  typeCode: string
  code: string
  label: string
  value: string
  sortOrder?: number
  isEnabled?: boolean
  description?: string
  parentCode?: string
}

/**
 * 更新字典项请求
 */
export interface UpdateDictionaryItemRequest {
  label: string
  value: string
  sortOrder?: number
  isEnabled?: boolean
  description?: string
  parentCode?: string
}

/**
 * 查询字典类型列表
 */
export function getDictionaryTypes(params: DictionaryTypeQueryRequest) {
  return request.get<ApiResponse<PageResponse<DictionaryType>>>('/api/dictionaries/types', { params })
}

/**
 * 获取字典类型详情
 */
export function getDictionaryType(id: string) {
  return request.get<ApiResponse<DictionaryType>>(`/api/dictionaries/types/${id}`)
}

/**
 * 创建字典类型
 */
export function createDictionaryType(data: CreateDictionaryTypeRequest) {
  return request.post<ApiResponse<DictionaryType>>('/api/dictionaries/types', data)
}

/**
 * 更新字典类型
 */
export function updateDictionaryType(id: string, data: UpdateDictionaryTypeRequest) {
  return request.put<ApiResponse<DictionaryType>>(`/api/dictionaries/types/${id}`, data)
}

/**
 * 删除字典类型
 */
export function deleteDictionaryType(id: string) {
  return request.delete<ApiResponse<void>>(`/api/dictionaries/types/${id}`)
}

/**
 * 查询字典项列表
 */
export function getDictionaryItems(params: DictionaryItemQueryRequest) {
  return request.get<ApiResponse<PageResponse<DictionaryItem>>>('/api/dictionaries/items', { params })
}

/**
 * 根据类型代码获取字典项
 */
export function getDictionaryItemsByType(typeCode: string) {
  return request.get<ApiResponse<DictionaryItem[]>>(`/api/dictionaries/types/${typeCode}/items`)
}

/**
 * 获取字典项详情
 */
export function getDictionaryItem(id: string) {
  return request.get<ApiResponse<DictionaryItem>>(`/api/dictionaries/items/${id}`)
}

/**
 * 创建字典项
 */
export function createDictionaryItem(data: CreateDictionaryItemRequest) {
  return request.post<ApiResponse<DictionaryItem>>('/api/dictionaries/items', data)
}

/**
 * 更新字典项
 */
export function updateDictionaryItem(id: string, data: UpdateDictionaryItemRequest) {
  return request.put<ApiResponse<DictionaryItem>>(`/api/dictionaries/items/${id}`, data)
}

/**
 * 删除字典项
 */
export function deleteDictionaryItem(id: string) {
  return request.delete<ApiResponse<void>>(`/api/dictionaries/items/${id}`)
}

/**
 * 批量更新字典项排序
 */
export function updateDictionaryItemsOrder(items: Array<{ id: string; sortOrder: number }>) {
  return request.put<ApiResponse<void>>('/api/dictionaries/items/order', { items })
}

/**
 * 启用/禁用字典项
 */
export function toggleDictionaryItem(id: string, isEnabled: boolean) {
  return request.put<ApiResponse<DictionaryItem>>(`/api/dictionaries/items/${id}/toggle`, { isEnabled })
}
