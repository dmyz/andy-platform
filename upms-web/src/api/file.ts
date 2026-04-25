import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/responses/common'
import type { PageSortRequest } from '@/types/requests/common'

/**
 * 文件实体
 */
export interface FileInfo {
  id: string
  fileName: string
  fileSize: number
  fileType: string
  filePath: string
  downloadUrl: string
  uploadedBy: string
  uploadedAt: string
}

/**
 * 文件查询请求
 */
export interface FileQueryRequest extends PageSortRequest {
  fileName?: string
  fileType?: string
  uploadedBy?: string
}

/**
 * 查询文件列表
 */
export function getFileList(params: FileQueryRequest) {
  return request.get<ApiResponse<PageResponse<FileInfo>>>('/api/files', { params })
}

/**
 * 获取文件详情
 */
export function getFileDetail(id: string) {
  return request.get<ApiResponse<FileInfo>>(`/api/files/${id}`)
}

/**
 * 上传文件
 */
export function uploadFile(file: File, onProgress?: (percent: number) => void) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post<ApiResponse<FileInfo>>('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    },
  })
}

/**
 * 批量上传文件
 */
export function uploadFiles(files: File[], onProgress?: (percent: number) => void) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })

  return request.post<ApiResponse<FileInfo[]>>('/api/files/batch-upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    },
  })
}

/**
 * 删除文件
 */
export function deleteFile(id: string) {
  return request.delete<ApiResponse<void>>(`/api/files/${id}`)
}

/**
 * 下载文件
 */
export function downloadFile(id: string, fileName?: string) {
  return request.get(`/api/files/${id}/download`, {
    responseType: 'blob',
  }).then((blob) => {
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName || 'download'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  })
}

/**
 * 获取文件预览 URL
 */
export function getFilePreviewUrl(id: string) {
  return `/api/files/${id}/preview`
}
