import request from '@/utils/request';

export interface AnnouncementPageItem {
  id: string;
  title: string;
  type: string;
  status: string;
  top: boolean;
  targetType: string;
  targetValue?: string | null;
  creatorName: string;
  publishTime?: string | null;
  updateTime: string;
}

export interface AnnouncementDetail {
  id: string;
  title: string;
  content: string;
  type: string;
  status: string;
  top: boolean;
  targetType: string;
  targetValue?: string | null;
  creatorName: string;
  publishTime?: string | null;
  updateTime: string;
}

interface AnnouncementFormData {
  title: string;
  content: string;
  type: string;
  targetType: string;
  targetValue?: string | null;
  top: boolean;
}

interface AnnouncementPageParams {
  pageNum: number;
  pageSize: number;
  title?: string;
  type?: string;
  status?: string;
}

export interface InboxAnnouncementItem {
  id: string;
  title: string;
  content?: string;
  type: string;
  publishTime: string;
  read: boolean;
  top: boolean;
}

/**
 * 分页查询公告列表
 */
export function getAnnouncementPage(params: AnnouncementPageParams) {
  return request.get<{
    list: AnnouncementPageItem[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/admin/announcement/page', params);
}

/**
 * 获取公告详情
 */
export function getAnnouncementDetail(id: string) {
  return request.get<AnnouncementDetail>(`/admin/announcement/${id}`);
}

/**
 * 创建公告
 */
export function createAnnouncement(data: AnnouncementFormData) {
  return request.post('/admin/announcement', data);
}

/**
 * 更新公告
 */
export function updateAnnouncement(id: string, data: AnnouncementFormData) {
  return request.put(`/admin/announcement/${id}`, data);
}

/**
 * 删除公告
 */
export function deleteAnnouncement(id: string) {
  return request.delete(`/admin/announcement/${id}`);
}

/**
 * 发布公告
 */
export function publishAnnouncement(id: string) {
  return request.post(`/admin/announcement/${id}/publish`);
}

/**
 * 撤回公告
 */
export function revokeAnnouncement(id: string) {
  return request.post(`/admin/announcement/${id}/revoke`);
}

/**
 * 获取收件箱公告列表
 */
export function getInboxAnnouncementPage(params: { pageNum: number; pageSize: number }) {
  return request.get<{
    list: InboxAnnouncementItem[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/admin/announcement/my/page', params);
}

/**
 * 标记公告为已读
 */
export function markAnnouncementRead(id: string) {
  return request.post(`/admin/announcement/${id}/read`);
}
