import request from '@/utils/request';
import type { PageResponse } from '@/types/responses/common';
import type { PageSortRequest } from '@/types/requests/common';

/**
 * 认证会话
 */
export interface AuthSession {
  id: string;
  username: string;
  realName: string;
  loginType: string;
  clientType: string;
  ip: string;
  userAgent: string;
  status: string;
  loginTime: string;
  lastAccessTime?: string;
  expireTime?: string;
  current: boolean;
}

/**
 * 会话查询请求
 */
export interface SessionQueryRequest extends PageSortRequest {
  userId?: string;
  username?: string;
  realName?: string;
  displayName?: string;
  loginType?: string;
  ip?: string;
  status?: string;
}

/**
 * 查询会话列表（分页）
 */
export function getSessionPage(params: SessionQueryRequest) {
  return request.get<PageResponse<AuthSession>>('/admin/auth/session/page', params);
}

/**
 * 获取当前用户的所有会话
 */
export function getCurrentUserSessions() {
  return request
    .get<PageResponse<AuthSession>>('/admin/auth/session/page', { pageNum: 1, pageSize: 1000 })
    .then((res) => res.list);
}

/**
 * 强制会话下线
 */
export function offlineSession(id: string) {
  return request.post<void>(`/admin/auth/session/${id}/offline`);
}

/**
 * 批量终止会话
 */
export function terminateSessions(ids: string[]) {
  return Promise.all(ids.map((id) => offlineSession(id))).then(() => undefined);
}
