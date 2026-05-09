import { get } from '@/utils/request';

export interface DashboardSummary {
  username: string;
  displayName: string;
  orgName: string;
  roleName: string;
  loginTime: string;
}

export interface ShortcutItem {
  label: string;
  path: string;
  icon: string;
  color: string;
}

export interface AnnouncementItem {
  id: string;
  title: string;
  publishTime: string;
  top: boolean;
  type: string;
}

export interface RecentOperationItem {
  id: string;
  operationTime: string;
  module: string;
  action: string;
  result: string;
}

/**
 * 获取仪表盘统计数据
 */
export function getDashboardStats() {
  return get<DashboardSummary>('/admin/dashboard/summary');
}

/**
 * 获取最近活动列表
 */
export function getRecentActivities(_params?: { limit?: number }) {
  return get<RecentOperationItem[]>('/admin/dashboard/recent-operations');
}

/**
 * 获取系统通知列表
 */
export function getSystemNotifications(_params?: { limit?: number }) {
  return get<AnnouncementItem[]>('/admin/dashboard/announcements');
}

/**
 * 获取快捷操作列表
 */
export function getQuickActions() {
  return get<ShortcutItem[]>('/admin/dashboard/favorite-navigations');
}
