import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/responses/common'
import type { PageSortRequest } from '@/types/requests/common'

/**
 * 系统设置项
 */
export interface SystemSetting {
  id: string
  category: string
  key: string
  value: string
  valueType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON'
  description?: string
  isPublic: boolean
  updatedBy: string
  updatedAt: string
}

/**
 * 系统设置查询请求
 */
export interface SystemSettingQueryRequest extends PageSortRequest {
  category?: string
  key?: string
  isPublic?: boolean
}

/**
 * 更新系统设置请求
 */
export interface UpdateSystemSettingRequest {
  value: string
  description?: string
}

/**
 * 批量更新系统设置请求
 */
export interface BatchUpdateSystemSettingRequest {
  settings: Array<{
    key: string
    value: string
  }>
}

/**
 * 查询系统设置列表
 */
export function getSystemSettings(params: SystemSettingQueryRequest) {
  return request.get<ApiResponse<PageResponse<SystemSetting>>>('/api/settings', { params })
}

/**
 * 获取系统设置详情
 */
export function getSystemSetting(key: string) {
  return request.get<ApiResponse<SystemSetting>>(`/api/settings/${key}`)
}

/**
 * 更新系统设置
 */
export function updateSystemSetting(key: string, data: UpdateSystemSettingRequest) {
  return request.put<ApiResponse<SystemSetting>>(`/api/settings/${key}`, data)
}

/**
 * 批量更新系统设置
 */
export function batchUpdateSystemSettings(data: BatchUpdateSystemSettingRequest) {
  return request.put<ApiResponse<void>>('/api/settings/batch', data)
}

/**
 * 获取公开的系统设置（无需认证）
 */
export function getPublicSettings() {
  return request.get<ApiResponse<Record<string, string>>>('/api/settings/public')
}

/**
 * 按分类获取系统设置
 */
export function getSettingsByCategory(category: string) {
  return request.get<ApiResponse<SystemSetting[]>>(`/api/settings/category/${category}`)
}

/**
 * 重置系统设置为默认值
 */
export function resetSystemSetting(key: string) {
  return request.post<ApiResponse<SystemSetting>>(`/api/settings/${key}/reset`)
}

/**
 * 导出系统设置
 */
export function exportSystemSettings() {
  return request.get('/api/settings/export', {
    responseType: 'blob',
  }).then((blob) => {
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `system-settings-${Date.now()}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  })
}

/**
 * 导入系统设置
 */
export function importSystemSettings(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post<ApiResponse<void>>('/api/settings/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
