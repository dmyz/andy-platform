import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/responses/common'
import type { PageSortRequest } from '@/types/requests/common'

/**
 * 认证会话
 */
export interface AuthSession {
  id: string
  userId: string
  username: string
  deviceType: 'WEB' | 'MOBILE' | 'DESKTOP' | 'API'
  deviceName?: string
  ipAddress: string
  userAgent: string
  location?: string
  loginTime: string
  lastActivityTime: string
  expiresAt: string
  isActive: boolean
  isCurrent: boolean
}

/**
 * 会话查询请求
 */
export interface SessionQueryRequest extends PageSortRequest {
  userId?: string
  username?: string
  deviceType?: 'WEB' | 'MOBILE' | 'DESKTOP' | 'API'
  isActive?: boolean
}

/**
 * 查询会话列表
 */
export function getSessions(params: SessionQueryRequest) {
  return request.get<ApiResponse<PageResponse<AuthSession>>>('/api/sessions', { params })
}

/**
 * 获取当前用户的所有会话
 */
export function getCurrentUserSessions() {
  return request.get<ApiResponse<AuthSession[]>>('/api/sessions/current-user')
}

/**
 * 获取会话详情
 */
export function getSession(id: string) {
  return request.get<ApiResponse<AuthSession>>(`/api/sessions/${id}`)
}

/**
 * 终止会话
 */
export function terminateSession(id: string) {
  return request.delete<ApiResponse<void>>(`/api/sessions/${id}`)
}

/**
 * 批量终止会话
 */
export function terminateSessions(ids: string[]) {
  return request.delete<ApiResponse<void>>('/api/sessions/batch', { data: { ids } })
}

/**
 * 终止用户的所有会话（除当前会话外）
 */
export function terminateOtherSessions() {
  return request.delete<ApiResponse<void>>('/api/sessions/others')
}

/**
 * 终止指定用户的所有会话
 */
export function terminateUserSessions(userId: string) {
  return request.delete<ApiResponse<void>>(`/api/sessions/user/${userId}`)
}

/**
 * 刷新会话
 */
export function refreshSession(id: string) {
  return request.post<ApiResponse<AuthSession>>(`/api/sessions/${id}/refresh`)
}

/**
 * 获取在线用户统计
 */
export function getOnlineUserStats() {
  return request.get<ApiResponse<{
    total: number
    web: number
    mobile: number
    desktop: number
    api: number
  }>>('/api/sessions/stats')
}
