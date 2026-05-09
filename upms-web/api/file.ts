import request from '@/utils/request';
import type { PageResponse } from '@/types/responses/common';
import type { PageSortRequest } from '@/types/requests/common';

/**
 * 文件实体
 */
export interface FileInfo {
  id: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  uploaderName: string;
  categoryCode?: string | null;
  remark?: string | null;
  uploadTime: string;
}

/**
 * 文件查询请求
 */
export interface FileQueryRequest extends PageSortRequest {
  fileName?: string;
  fileType?: string;
  uploaderName?: string;
}

/**
 * 查询文件列表
 */
export function getFileList(params: FileQueryRequest) {
  return request.get<PageResponse<FileInfo>>('/admin/file/page', params);
}

/**
 * 获取文件详情
 */
export function getFileDetail(id: string) {
  return request.get<FileInfo>(`/admin/file/${id}`);
}

/**
 * 上传文件
 */
export function uploadFile(file: File, onProgress?: (percent: number) => void) {
  const formData = new FormData();
  formData.append('file', file);

  return request.post<FileInfo>('/admin/file/upload', formData, {
    onUploadProgress: (percent) => {
      onProgress?.(percent);
    },
  });
}

/**
 * 删除文件
 */
export function deleteFile(id: string) {
  return request.delete<void>(`/admin/file/${id}`);
}

/**
 * 下载文件
 */
export function downloadFile(id: string, fileName?: string) {
  return request.download(`/admin/file/${id}/download`, { filename: fileName || 'download' });
}

/**
 * 获取文件预览 URL
 */
export function getFilePreviewUrl(id: string) {
  return `/admin/file/${id}/preview`;
}

// 以下是实际项目使用的 API 函数

interface FileItem {
  id: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  categoryCode: string;
  uploaderName: string;
  uploadTime: string;
  remark?: string;
}

interface FilePageParams {
  pageNum: number;
  pageSize: number;
  fileName?: string;
  fileType?: string;
  uploaderName?: string;
  startTime?: string;
  endTime?: string;
}

/**
 * 分页查询文件列表
 */
export function getFilePage(params: FilePageParams) {
  return request.get<{
    list: FileItem[];
    total: number;
    pageNum: number;
    pageSize: number;
  }>('/admin/file/page', params);
}

/**
 * 上传文件
 */
export function uploadFileRecord(formData: FormData) {
  return request.post('/admin/file/upload', formData);
}

/**
 * 获取文件预览信息
 */
export function getFilePreview(id: string) {
  return request.get<{ previewUrl: string }>(`/admin/file/${id}/preview`);
}

/**
 * 下载文件
 */
export function downloadFileRecord(id: string, filename: string) {
  return request.download(`/admin/file/${id}/download`, { filename });
}

/**
 * 删除文件
 */
export function deleteFileRecord(id: string) {
  return request.delete(`/admin/file/${id}`);
}
