import request from '@/utils/request';
import type { PageResponse } from '@/types/responses/common';
import type { PageSortRequest } from '@/types/requests/common';

/**
 * 登录审计分页项。
 */
export interface LoginAuditPageItem {
  id: string;
  username: string;
  realName: string;
  loginType: string;
  ip: string;
  browser: string;
  loginTime: string;
  result: string;
  failureReason?: string | null;
}

/**
 * 操作审计分页项。
 */
export interface OperationAuditPageItem {
  id: string;
  operatorName: string;
  moduleName: string;
  actionType: string;
  requestUri: string;
  durationMs: number;
  operationTime: string;
  result: string;
}

/**
 * 操作审计详情。
 */
export interface OperationAuditDetail extends OperationAuditPageItem {
  requestMethod: string;
  requestParams: string;
  responseCode: number;
  errorMessage?: string | null;
}

/**
 * 登录审计查询请求
 */
export interface LoginAuditQueryRequest extends PageSortRequest {
  userId?: string;
  username?: string;
  loginType?: string;
  result?: string;
  startTime?: string;
  endTime?: string;
}

/**
 * 操作审计查询请求
 */
export interface OperationAuditQueryRequest extends PageSortRequest {
  userId?: string;
  operatorName?: string;
  moduleName?: string;
  actionType?: string;
  result?: string;
  startTime?: string;
  endTime?: string;
}

/**
 * 查询登录审计日志
 */
export function getLoginAuditLogs(params: LoginAuditQueryRequest) {
  return request.get<PageResponse<LoginAuditPageItem>>('/admin/login-audit/page', params);
}

/**
 * 查询操作审计日志
 */
export function getOperationAuditLogs(params: OperationAuditQueryRequest) {
  return request.get<PageResponse<OperationAuditPageItem>>('/admin/operation-audit/page', params);
}

/**
 * 获取操作审计详情
 */
export function getOperationAuditDetail(id: string) {
  return request.get<OperationAuditDetail>(`/admin/operation-audit/${id}`);
}

/**
 * 导出登录审计日志
 */
export function exportLoginAuditLogs(params: LoginAuditQueryRequest) {
  return request.download('/admin/login-audit/export', {
    params,
    filename: 'login-audit-export.csv',
  });
}

/**
 * 导出操作审计日志
 */
export function exportOperationAuditLogs(params: OperationAuditQueryRequest) {
  return request.download('/admin/operation-audit/export', {
    params,
    filename: 'operation-audit-export.csv',
  });
}
