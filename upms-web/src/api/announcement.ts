import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/responses/common'
import type { PageSortRequest } from '@/types/requests/common'

/**
 * 公告实体
 */
export interface Announcement {
  id: string
  title: string
  content: string
  type: 'SYSTEM' | 'NOTICE' | 'ACTIVITY'
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  publishTime?: string
  expireTime?: string
  createdBy: string
  createdAt: string
  updatedAt: string
}

/**
 * 公告查询请求
 */
export interface AnnouncementQueryRequest extends PageSortRequest {
  title?: string
  type?: 'SYSTEM' | 'NOTICE' | 'ACTIVITY'
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
}

/**
 * 创建公告请求
 */
export interface CreateAnnouncementRequest {
  title: string
  content: string
  type: 'SYSTEM' | 'NOTICE' | 'ACTIVITY'
  priority?: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
  publishTime?: string
  expireTime?: string
}

/**
 * 更新公告请求
 */
export interface UpdateAnnouncementRequest {
  title?: string
  content?: string
  type?: 'SYSTEM' | 'NOTICE' | 'ACTIVITY'
  priority?: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
  publishTime?: string
  expireTime?: string
}

/**
 * 查询公告列表
 */
export function getAnnouncementList(params: AnnouncementQueryRequest) {
  return request.get<ApiResponse<PageResponse<Announcement>>>('/api/announcements', { params })
}

/**
 * 获取公告详情
 */
export function getAnnouncementDetail(id: string) {
  return request.get<ApiResponse<Announcement>>(`/api/announcements/${id}`)
}

/**
 * 创建公告
 */
export function createAnnouncement(data: CreateAnnouncementRequest) {
  return request.post<ApiResponse<Announcement>>('/api/announcements', data)
}

/**
 * 更新公告
 */
export function updateAnnouncement(id: string, data: UpdateAnnouncementRequest) {
  return request.put<ApiResponse<Announcement>>(`/api/announcements/${id}`, data)
}

/**
 * 删除公告
 */
export function deleteAnnouncement(id: string) {
  return request.delete<ApiResponse<void>>(`/api/announcements/${id}`)
}

/**
 * 发布公告
 */
export function publishAnnouncement(id: string) {
  return request.post<ApiResponse<void>>(`/api/announcements/${id}/publish`)
}

/**
 * 归档公告
 */
export function archiveAnnouncement(id: string) {
  return request.post<ApiResponse<void>>(`/api/announcements/${id}/archive`)
}

/**
 * 获取用户收件箱公告
 */
export function getInboxAnnouncements(params: PageSortRequest) {
  return request.get<ApiResponse<PageResponse<Announcement>>>('/api/announcements/inbox', { params })
}

/**
 * 标记公告为已读
 */
export function markAnnouncementAsRead(id: string) {
  return request.post<ApiResponse<void>>(`/api/announcements/${id}/read`)
}
