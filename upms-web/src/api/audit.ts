import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/responses/common'
import type { PageSortRequest } from '@/types/requests/common'

/**
 * 登录审计事件
 */
export interface LoginAuditEvent {
  id: string
  userId: string
  username: string
  loginTime: string
  loginMethod: 'PASSWORD' | 'SMS' | 'EMAIL' | 'SSO'
  ipAddress: string
  userAgent: string
  location?: string
  success: boolean
  failureReason?: string
}

/**
 * 操作审计事件
 */
export interface OperationAuditEvent {
  id: string
  userId: string
  username: string
  operationTime: string
  operationType: 'CREATE' | 'UPDATE' | 'DELETE' | 'QUERY' | 'EXPORT' | 'IMPORT'
  resourceType: string
  resourceId?: string
  operationDetail: string
  ipAddress: string
  userAgent: string
  success: boolean
  errorMessage?: string
}

/**
 * 登录审计查询请求
 */
export interface LoginAuditQueryRequest extends PageSortRequest {
  userId?: string
  username?: string
  loginMethod?: 'PASSWORD' | 'SMS' | 'EMAIL' | 'SSO'
  success?: boolean
  startTime?: string
  endTime?: string
}

/**
 * 操作审计查询请求
 */
export interface OperationAuditQueryRequest extends PageSortRequest {
  userId?: string
  username?: string
  operationType?: 'CREATE' | 'UPDATE' | 'DELETE' | 'QUERY' | 'EXPORT' | 'IMPORT'
  resourceType?: string
  success?: boolean
  startTime?: string
  endTime?: string
}

/**
 * 查询登录审计日志
 */
export function getLoginAuditLogs(params: LoginAuditQueryRequest) {
  return request.get<ApiResponse<PageResponse<LoginAuditEvent>>>('/api/audit/login', { params })
}

/**
 * 获取登录审计详情
 */
export function getLoginAuditDetail(id: string) {
  return request.get<ApiResponse<LoginAuditEvent>>(`/api/audit/login/${id}`)
}

/**
 * 查询操作审计日志
 */
export function getOperationAuditLogs(params: OperationAuditQueryRequest) {
  return request.get<ApiResponse<PageResponse<OperationAuditEvent>>>('/api/audit/operation', { params })
}

/**
 * 获取操作审计详情
 */
export function getOperationAuditDetail(id: string) {
  return request.get<ApiResponse<OperationAuditEvent>>(`/api/audit/operation/${id}`)
}

/**
 * 导出登录审计日志
 */
export function exportLoginAuditLogs(params: LoginAuditQueryRequest) {
  return request.get('/api/audit/login/export', {
    params,
    responseType: 'blob',
  }).then((blob) => {
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `login-audit-${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  })
}

/**
 * 导出操作审计日志
 */
export function exportOperationAuditLogs(params: OperationAuditQueryRequest) {
  return request.get('/api/audit/operation/export', {
    params,
    responseType: 'blob',
  }).then((blob) => {
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `operation-audit-${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  })
}
