import { MessagePlugin } from 'tdesign-vue-next'
import router from '@/router'
import { clearStoredAccessToken, getStoredAccessToken } from './auth'

/**
 * 统一请求模块：
 * - 普通 JSON 请求优先使用 fetch
 * - FormData 上传和需要进度回调的场景使用 XMLHttpRequest
 * - 文件下载独立走 XMLHttpRequest，便于处理 blob 与下载进度
 */
const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''
const DEFAULT_TIMEOUT = 10000
const UTF8_FILENAME_REGEX = /filename\*=UTF-8''([^;]+)/i
const NORMAL_FILENAME_REGEX = /filename="?([^";]+)"?/i

// 定义统一响应结构
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/** 通用请求配置，覆盖 query、超时、取消和进度回调等能力。 */
export interface RequestOptions extends Omit<RequestInit, 'body' | 'method' | 'signal'> {
  params?: Record<string, unknown>
  signal?: AbortSignal
  timeout?: number
  onUploadProgress?: ProgressCallback
  onDownloadProgress?: ProgressCallback
}

/** 文件下载配置，支持额外 query、文件名兜底和下载进度回调。 */
export interface DownloadOptions {
  params?: Record<string, unknown>
  filename?: string
  headers?: HeadersInit
  signal?: AbortSignal
  timeout?: number
  onDownloadProgress?: ProgressCallback
}

export type ProgressCallback = (percent: number, loaded: number, total: number) => void

/**
 * handled 表示该错误是否已经完成 UI 提示，避免在 catch 链路中重复弹出消息。
 */
class RequestError extends Error {
  handled: boolean

  constructor(message: string, handled = false) {
    super(message)
    this.name = 'RequestError'
    this.handled = handled
  }
}

function getToken() {
  return getStoredAccessToken()
}

/** 登录态失效时统一清理 token，并跳转到登录页。 */
function handleUnauthorized() {
  clearStoredAccessToken()
  router.push('/login')
}

function resolveErrorMessage(status?: number, fallback?: string, requestUrl?: string) {
  switch (status) {
    case 401:
      return '未授权，请重新登录'
    case 403:
      return '拒绝访问，权限不足'
    case 404:
      return requestUrl ? `请求地址不存在: ${requestUrl}` : '请求地址不存在'
    case 500:
      return '服务器内部错误'
    default:
      return fallback || (status ? `请求错误: ${status}` : '请求异常')
  }
}

function resolveDownloadFilename(contentDisposition: string | null, fallback: string) {
  if (!contentDisposition) {
    return fallback
  }

  const utf8Match = contentDisposition.match(UTF8_FILENAME_REGEX)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }

  const normalMatch = contentDisposition.match(NORMAL_FILENAME_REGEX)
  return normalMatch?.[1] || fallback
}

function buildQueryString(params?: Record<string, unknown>) {
  const query = new URLSearchParams()

  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, String(value))
    }
  })

  const result = query.toString()
  return result ? `?${result}` : ''
}

function buildRequestUrl(url: string, params?: Record<string, unknown>) {
  const requestUrl = `${BASE_URL}${url}${buildQueryString(params)}`
  console.log('buildRequestUrl', requestUrl)

  return requestUrl
}

function isBodyInit(value: unknown): value is BodyInit {
  return value instanceof FormData
    || value instanceof URLSearchParams
    || value instanceof Blob
    || value instanceof ArrayBuffer
    || ArrayBuffer.isView(value)
    || typeof value === 'string'
}

function buildHeaders(headers?: HeadersInit) {
  const result = new Headers(headers)
  const token = getToken()

  if (token) {
    result.set('Authorization', `Bearer ${token}`)
  }

  return result
}

/**
 * 将外部取消信号与内部超时控制合并，调用方必须在 finally 中执行 cleanup。
 * - 超时抛出 TimeoutError
 * - 用户主动取消沿用 AbortError
 */
function createTimeoutSignal(signal?: AbortSignal, timeout = DEFAULT_TIMEOUT) {
  const controller = new AbortController()
  const onAbort = () => {
    controller.abort(signal?.reason)
  }

  if (signal) {
    if (signal.aborted) {
      onAbort()
    }
    else {
      signal.addEventListener('abort', onAbort, { once: true })
    }
  }

  const timer = window.setTimeout(() => {
    controller.abort(new DOMException('Request timeout', 'TimeoutError'))
  }, timeout)

  return {
    signal: controller.signal,
    cleanup: () => {
      window.clearTimeout(timer)
      signal?.removeEventListener('abort', onAbort)
    },
  }
}

function buildRequestBody(data?: unknown, headers?: Headers) {
  if (data === undefined) {
    return undefined
  }

  if (isBodyInit(data)) {
    return data
  }

  if (headers && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  return JSON.stringify(data)
}

function createAbortError() {
  return new DOMException('The operation was aborted.', 'AbortError')
}

function applyProgress(callback: ProgressCallback | undefined, loaded: number, total: number) {
  if (!callback) {
    return
  }

  const percent = total > 0 ? Math.round((loaded / total) * 100) : 0
  callback(percent, loaded, total)
}

function applyXhrHeaders(xhr: XMLHttpRequest, headers: Headers) {
  headers.forEach((value, key) => {
    xhr.setRequestHeader(key, value)
  })
}

function parseJsonSafely<T>(value: string): T | null {
  try {
    return JSON.parse(value) as T
  }
  catch {
    return null
  }
}

function isJsonContentType(contentType: string | null) {
  return (contentType || '').includes('application/json')
}

function showErrorMessage(message: string) {
  MessagePlugin.error(message)
}

function throwHandledError(message: string): never {
  throw new RequestError(message, true)
}

function handleBusinessResponse<T>(result: ApiResponse<T>, _requestUrl?: string) {
  if (result.code === 0) {
    return result.data
  }

  const message = result.message || '系统错误'
  if (result.code === 401) {
    handleUnauthorized()
  }
  showErrorMessage(message)
  throwHandledError(message)
}

function resolveXhrErrorMessage(xhr: XMLHttpRequest, requestUrl: string) {
  const responseText = xhr.responseText || ''
  const payload = isJsonContentType(xhr.getResponseHeader('content-type'))
    ? parseJsonSafely<Partial<ApiResponse<unknown>>>(responseText)
    : null

  return resolveErrorMessage(xhr.status, payload?.message || '', requestUrl)
}

async function resolveHttpError(response: Response, requestUrl: string): Promise<never> {
  let fallback = ''

  if (isJsonContentType(response.headers.get('content-type'))) {
    const payloadText = await response.text()
    const payload = parseJsonSafely<Partial<ApiResponse<unknown>>>(payloadText)
    fallback = payload?.message || ''
  }

  const message = resolveErrorMessage(response.status, fallback, requestUrl)
  if (response.status === 401) {
    handleUnauthorized()
  }
  showErrorMessage(message)
  throwHandledError(message)
}

function handleUnhandledRequestError(error: unknown): never {
  if (error instanceof RequestError) {
    if (error.handled) {
      throw error
    }

    showErrorMessage(error.message)
    throw new RequestError(error.message, true)
  }

  if (error instanceof Error && error.name === 'TimeoutError') {
    showErrorMessage('请求超时')
    throw new RequestError('请求超时', true)
  }

  if (error instanceof Error && error.name === 'AbortError') {
    throw error
  }

  showErrorMessage('请求异常')
  throw new RequestError('请求异常', true)
}

function triggerBrowserDownload(blob: Blob, filename: string) {
  const blobUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = blobUrl
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(blobUrl)
}

function readBlobText(blob: Blob) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result || ''))
    reader.onerror = () => reject(new RequestError('请求异常', false))
    reader.readAsText(blob)
  })
}

async function resolveBlobErrorMessage(blob: Blob, status: number, requestUrl: string) {
  const contentType = blob.type || ''
  if (!isJsonContentType(contentType)) {
    return resolveErrorMessage(status, '', requestUrl)
  }

  try {
    const payloadText = await readBlobText(blob)
    const payload = parseJsonSafely<Partial<ApiResponse<unknown>>>(payloadText)
    return resolveErrorMessage(status, payload?.message || '', requestUrl)
  }
  catch {
    return resolveErrorMessage(status, '', requestUrl)
  }
}

/**
 * XHR 仅用于 FormData/进度场景，避免 fetch 在上传进度控制上的限制。
 */
function createXhrRequest<T>(
  method: string,
  requestUrl: string,
  body: Document | XMLHttpRequestBodyInit | null | undefined,
  headers: Headers,
  options: {
    timeout: number
    signal?: AbortSignal
    onUploadProgress?: ProgressCallback
    onDownloadProgress?: ProgressCallback
  },
) {
  const { timeout, signal, onUploadProgress, onDownloadProgress } = options

  return new Promise<T>((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    let settled = false
    let handleAbort: (() => void) | undefined

    const cleanup = () => {
      if (handleAbort) {
        signal?.removeEventListener('abort', handleAbort)
      }
    }

    const finishReject = (error: unknown) => {
      if (settled) {
        return
      }
      settled = true
      cleanup()
      reject(error)
    }

    const finishResolve = (value: T) => {
      if (settled) {
        return
      }
      settled = true
      cleanup()
      resolve(value)
    }

    const handleSuccess = () => {
      const contentType = xhr.getResponseHeader('content-type') || ''
      if (!isJsonContentType(contentType)) {
        finishResolve(xhr.response as T)
        return
      }

      const result = parseJsonSafely<ApiResponse<T>>(xhr.responseText)
      if (!result) {
        finishReject(new RequestError('请求异常', false))
        return
      }

      try {
        finishResolve(handleBusinessResponse(result, requestUrl))
      }
      catch (error) {
        finishReject(error)
      }
    }

    const handleHttpError = () => {
      const message = resolveXhrErrorMessage(xhr, requestUrl)
      if (xhr.status === 401) {
        handleUnauthorized()
      }
      showErrorMessage(message)
      finishReject(new RequestError(message, true))
    }

    handleAbort = () => {
      xhr.abort()
      finishReject(createAbortError())
    }

    xhr.open(method, requestUrl)
    xhr.timeout = timeout

    if (onUploadProgress) {
      xhr.upload.onprogress = (event) => {
        if (event.lengthComputable) {
          applyProgress(onUploadProgress, event.loaded, event.total)
        }
      }
    }

    if (onDownloadProgress) {
      xhr.onprogress = (event) => {
        if (event.lengthComputable) {
          applyProgress(onDownloadProgress, event.loaded, event.total)
        }
      }
    }

    if (signal?.aborted) {
      handleAbort()
      return
    }
    signal?.addEventListener('abort', handleAbort, { once: true })

    applyXhrHeaders(xhr, headers)

    xhr.onreadystatechange = () => {
      if (xhr.readyState !== XMLHttpRequest.DONE || settled) {
        return
      }

      if (xhr.status >= 200 && xhr.status < 300) {
        handleSuccess()
        return
      }

      handleHttpError()
    }

    xhr.onerror = () => {
      finishReject(new RequestError('请求异常', false))
    }

    xhr.ontimeout = () => {
      finishReject(new RequestError('请求超时', false))
    }

    xhr.send(body ?? null)
  })
}

/**
 * 通用请求主流程：
 * - 普通 JSON 请求走 fetch
 * - FormData 请求走 XHR 以支持上传进度
 * - 成功响应按统一 ApiResponse 结构解析
 */
async function request<T>(method: string, url: string, data?: unknown, options: RequestOptions = {}): Promise<T> {
  const {
    params,
    timeout = DEFAULT_TIMEOUT,
    signal,
    headers: rawHeaders,
    onUploadProgress,
    onDownloadProgress,
    ...init
  } = options
  const requestUrl = buildRequestUrl(url, params)
  const headers = buildHeaders(rawHeaders)
  const body = buildRequestBody(data, headers)

  if (data instanceof FormData) {
    try {
      return await createXhrRequest<T>(method, requestUrl, data, headers, {
        timeout,
        signal,
        onUploadProgress,
        onDownloadProgress,
      })
    }
    catch (error) {
      handleUnhandledRequestError(error)
    }
  }

  const controller = createTimeoutSignal(signal, timeout)

  try {
    const response = await fetch(requestUrl, {
      ...init,
      method,
      body,
      headers,
      signal: controller.signal,
    })

    if (!response.ok) {
      return await resolveHttpError(response, requestUrl)
    }

    const payloadText = await response.text()
    const result = parseJsonSafely<ApiResponse<T>>(payloadText)
    if (!result) {
      throw new RequestError('请求异常', false)
    }

    return handleBusinessResponse(result, requestUrl)
  }
  catch (error) {
    handleUnhandledRequestError(error)
  }
  finally {
    controller.cleanup()
  }
}

/** 发起 GET 请求，query 参数通过第二个参数传入。 */
export function get<T>(url: string, params?: Record<string, unknown>, options: RequestOptions = {}): Promise<T> {
  return request<T>('GET', url, undefined, { ...options, params })
}

/** 发起 POST 请求，普通对象会自动序列化为 JSON。 */
export function post<T>(url: string, data?: unknown, options: RequestOptions = {}): Promise<T> {
  return request<T>('POST', url, data, options)
}

/** 发起 PUT 请求，行为与 POST 保持一致。 */
export function put<T>(url: string, data?: unknown, options: RequestOptions = {}): Promise<T> {
  return request<T>('PUT', url, data, options)
}

/** 发起 DELETE 请求，当前仅支持 query 参数，不支持 body。 */
export function del<T>(url: string, params?: Record<string, unknown>, options: RequestOptions = {}): Promise<T> {
  return request<T>('DELETE', url, undefined, { ...options, params })
}

/**
 * 下载流程独立使用 XHR：
 * - 统一处理 blob 下载和下载进度
 * - 失败时支持从 blob 中读取 JSON 错误信息
 */
export async function download(url: string, options: DownloadOptions = {}) {
  const { params, timeout = DEFAULT_TIMEOUT, signal, headers: rawHeaders, onDownloadProgress } = options
  const requestUrl = buildRequestUrl(url, params)
  const headers = buildHeaders(rawHeaders)

  try {
    return await new Promise<Blob>((resolve, reject) => {
      const xhr = new XMLHttpRequest()
      let settled = false
      let handleAbort: (() => void) | undefined

      const cleanup = () => {
        if (handleAbort) {
          signal?.removeEventListener('abort', handleAbort)
        }
      }

      const finishReject = (error: unknown) => {
        if (settled) {
          return
        }
        settled = true
        cleanup()
        reject(error)
      }

      const finishResolve = (value: Blob) => {
        if (settled) {
          return
        }
        settled = true
        cleanup()
        resolve(value)
      }

      const handleSuccess = () => {
        const filename = resolveDownloadFilename(xhr.getResponseHeader('content-disposition'), options.filename || 'download')
        triggerBrowserDownload(xhr.response, filename)
        finishResolve(xhr.response)
      }

      const handleHttpError = async () => {
        const message = await resolveBlobErrorMessage(xhr.response, xhr.status, requestUrl)
        if (xhr.status === 401) {
          handleUnauthorized()
        }
        showErrorMessage(message)
        finishReject(new RequestError(message, true))
      }

      handleAbort = () => {
        xhr.abort()
        finishReject(createAbortError())
      }

      xhr.open('GET', requestUrl)
      xhr.responseType = 'blob'
      xhr.timeout = timeout

      xhr.onprogress = (event) => {
        if (event.lengthComputable) {
          applyProgress(onDownloadProgress, event.loaded, event.total)
        }
      }

      xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE || settled) {
          return
        }

        if (xhr.status >= 200 && xhr.status < 300) {
          handleSuccess()
          return
        }

        void handleHttpError()
      }

      xhr.onerror = () => {
        finishReject(new RequestError('请求异常', false))
      }

      xhr.ontimeout = () => {
        finishReject(new RequestError('请求超时', false))
      }

      if (signal?.aborted) {
        handleAbort()
        return
      }
      signal?.addEventListener('abort', handleAbort, { once: true })

      applyXhrHeaders(xhr, headers)
      xhr.send()
    })
  }
  catch (error) {
    handleUnhandledRequestError(error)
  }
}

const service = {
  defaults: {
    baseURL: BASE_URL,
    timeout: DEFAULT_TIMEOUT,
  },
  request,
  get,
  post,
  put,
  delete: del,
  del,
  download,
}

export default service
