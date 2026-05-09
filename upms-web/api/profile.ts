import request from '@/utils/request';
import type { PageResponse } from '@/types/responses/common';

/**
 * 个人资料信息
 */
export interface ProfileInfo {
  id: string;
  username: string;
  realName: string;
  employeeNo?: string | null;
  mobile: string;
  email: string;
  gender?: string | null;
  avatar?: string | null;
  orgName?: string | null;
  positionName?: string | null;
  remark?: string | null;
}

/**
 * 登录审计记录
 */
export interface LoginAuditItem {
  id: string;
  loginTime: string;
  loginType: string;
  ip: string;
  browser: string;
  result: string;
}

/**
 * 消息记录
 */
export interface MessageItem {
  id: string;
  title: string;
  type: string;
  publishTime: string;
  read: boolean;
  content: string;
}

/**
 * 分页查询参数
 */
export interface PageParams {
  pageNum: number;
  pageSize: number;
}

/**
 * 获取个人资料
 */
export function getProfile() {
  return request.get<ProfileInfo>('/admin/profile/me');
}

/**
 * 获取登录审计记录
 */
export function getLoginAuditList(params: PageParams) {
  return request.get<PageResponse<LoginAuditItem>>('/admin/profile/login-audit/page', params);
}

/**
 * 获取我的消息列表
 */
export function getProfileMessages(params: PageParams) {
  return request.get<PageResponse<MessageItem>>('/admin/profile/messages/page', params);
}

/**
 * 更新头像
 */
export function updateAvatar(data: { avatarUrl: string } | string) {
  const avatarUrl = typeof data === 'string' ? data : data.avatarUrl;
  return request.post<ProfileInfo>('/admin/profile/avatar', { avatarUrl });
}

/**
 * 更新个人资料
 */
export function updateProfile(data: Partial<ProfileInfo>) {
  return request.put<ProfileInfo>('/admin/profile/me', data);
}

/**
 * 发送手机验证码
 */
export function sendMobileCode() {
  return request.post<{ targetType: string; maskedTarget: string; expireSeconds: number }>(
    '/admin/profile/mobile/code/send',
  );
}

/**
 * 发送邮箱验证码
 */
export function sendEmailCode() {
  return request.post<{ targetType: string; maskedTarget: string; expireSeconds: number }>(
    '/admin/profile/email/code/send',
  );
}

/**
 * 修改手机号
 */
export function changeMobile(code: string, newMobile: string) {
  return request.post<ProfileInfo>('/admin/profile/mobile/change', { code, newMobile });
}

/**
 * 修改邮箱
 */
export function changeEmail(code: string, newEmail: string) {
  return request.post<ProfileInfo>('/admin/profile/email/change', { code, newEmail });
}
