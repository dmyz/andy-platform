import router from '@/router';
import axios, {
  AxiosError,
  AxiosHeaders,
  type GenericAbortSignal,
  type AxiosProgressEvent,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios';
import type { MessageType } from 'antdv-next/dist/message/interface';

import { clearStoredAccessToken, getStoredAccessToken } from './auth';
import { toast } from './feedback';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';
const DEFAULT_TIMEOUT = 10000;
const DEFAULT_LOADING_TEXT = '加载中...';
const UTF8_FILENAME_REGEX = /filename\*=UTF-8''([^;]+)/i;
const NORMAL_FILENAME_REGEX = /filename="?([^";]+)"?/i;

export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

export type ProgressCallback = (percent: number, loaded: number, total: number) => void;

type RequestParams = Record<string, unknown> | object;
type RequestHeaders = AxiosRequestConfig['headers'] | HeadersInit;

export interface RequestOptions extends Omit<
  AxiosRequestConfig,
  | 'baseURL'
  | 'data'
  | 'headers'
  | 'method'
  | 'onDownloadProgress'
  | 'onUploadProgress'
  | 'params'
  | 'signal'
  | 'timeout'
  | 'url'
> {
  params?: RequestParams;
  headers?: RequestHeaders;
  signal?: AbortSignal;
  timeout?: number;
  onUploadProgress?: ProgressCallback;
  onDownloadProgress?: ProgressCallback;
  showLoading?: boolean;
  loadingText?: string;
}

export interface DownloadOptions extends Omit<
  AxiosRequestConfig,
  | 'baseURL'
  | 'headers'
  | 'method'
  | 'onDownloadProgress'
  | 'params'
  | 'responseType'
  | 'signal'
  | 'timeout'
  | 'url'
> {
  params?: RequestParams;
  filename?: string;
  headers?: RequestHeaders;
  signal?: AbortSignal;
  timeout?: number;
  onDownloadProgress?: ProgressCallback;
  showLoading?: boolean;
  loadingText?: string;
}

interface RequestQueueItem {
  controller: AbortController;
  cleanupSignal: () => void;
}

interface ManagedRequestConfig extends InternalAxiosRequestConfig {
  requestId?: string;
  showLoading?: boolean;
  loadingText?: string;
  cleanupSignal?: () => void;
  skipBusinessTransform?: boolean;
}

interface ManagedAxiosRequestConfig extends AxiosRequestConfig {
  showLoading?: boolean;
  loadingText?: string;
  skipBusinessTransform?: boolean;
}

class RequestError extends Error {
  handled: boolean;

  constructor(message: string, handled = false) {
    super(message);
    this.name = 'RequestError';
    this.handled = handled;
  }
}

const requestQueue = new Map<string, RequestQueueItem>();
let requestSeq = 0;
let activeLoadingCount = 0;
let loadingCloser: MessageType | undefined;
let redirectingToLogin = false;

const http = axios.create({
  baseURL: BASE_URL,
  timeout: DEFAULT_TIMEOUT,
});

function getToken() {
  return getStoredAccessToken();
}

function createRequestId() {
  requestSeq += 1;
  return `request-${Date.now()}-${requestSeq}`;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function isApiResponse<T = unknown>(value: unknown): value is ApiResponse<T> {
  return isRecord(value) && typeof value.code === 'number' && 'data' in value;
}

function normalizeParams(params?: RequestParams) {
  const result: Record<string, string> = {};

  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      result[key] = String(value);
    }
  });

  return Object.keys(result).length > 0 ? result : undefined;
}

function buildQueryString(params?: RequestParams) {
  const query = new URLSearchParams();

  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, String(value));
    }
  });

  const result = query.toString();
  return result ? `?${result}` : '';
}

function buildRequestUrl(url?: string, params?: RequestParams) {
  return `${BASE_URL}${url || ''}${buildQueryString(params)}`;
}

function normalizeHeaders(headers?: RequestHeaders) {
  if (!headers) {
    return undefined;
  }

  if (headers instanceof Headers) {
    const result: Record<string, string> = {};
    headers.forEach((value, key) => {
      result[key] = value;
    });
    return result;
  }

  if (Array.isArray(headers)) {
    return Object.fromEntries(headers);
  }

  return headers;
}

function resolveErrorMessage(status?: number, fallback?: string, requestUrl?: string) {
  switch (status) {
    case 401:
      return '未授权，请重新登录';
    case 403:
      return '拒绝访问，权限不足';
    case 404:
      return requestUrl ? `请求地址不存在: ${requestUrl}` : '请求地址不存在';
    case 500:
      return '服务器内部错误';
    default:
      return fallback || (status ? `请求错误: ${status}` : '请求异常');
  }
}

function resolveDownloadFilename(contentDisposition: string | null | undefined, fallback: string) {
  if (!contentDisposition) {
    return fallback;
  }

  const utf8Match = contentDisposition.match(UTF8_FILENAME_REGEX);
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1]);
  }

  const normalMatch = contentDisposition.match(NORMAL_FILENAME_REGEX);
  return normalMatch?.[1] || fallback;
}

function showErrorMessage(errorMessage: string) {
  toast.error(errorMessage);
}

function throwHandledError(message: string): never {
  throw new RequestError(message, true);
}

function interruptRequest(): Promise<never> {
  return new Promise<never>(() => undefined);
}

function startGlobalLoading(text = DEFAULT_LOADING_TEXT) {
  activeLoadingCount += 1;

  if (activeLoadingCount === 1) {
    loadingCloser = toast.loading(text, 0);
  }
}

function stopGlobalLoading() {
  if (activeLoadingCount > 0) {
    activeLoadingCount -= 1;
  }

  if (activeLoadingCount === 0 && loadingCloser) {
    loadingCloser();
    loadingCloser = undefined;
  }
}

function closeGlobalLoading() {
  activeLoadingCount = 0;
  if (loadingCloser) {
    loadingCloser();
    loadingCloser = undefined;
  }
}

function mergeAbortSignal(signal: GenericAbortSignal | undefined, controller: AbortController) {
  const handleAbort = () => {
    controller.abort();
  };

  if (signal) {
    if (signal.aborted) {
      handleAbort();
    } else {
      signal.addEventListener?.('abort', handleAbort, { once: true });
    }
  }

  return () => {
    signal?.removeEventListener?.('abort', handleAbort);
  };
}

function applyProgress(callback: ProgressCallback | undefined, event: AxiosProgressEvent) {
  if (!callback) {
    return;
  }

  const loaded = event.loaded;
  const total = event.total ?? 0;
  const percent = total > 0 ? Math.round((loaded / total) * 100) : 0;
  callback(percent, loaded, total);
}

function completeRequest(config?: AxiosRequestConfig) {
  const managedConfig = config as ManagedRequestConfig | undefined;
  const requestId = managedConfig?.requestId;

  if (requestId) {
    requestQueue.delete(requestId);
  }

  managedConfig?.cleanupSignal?.();

  if (managedConfig?.showLoading !== false) {
    stopGlobalLoading();
  }
}

function clearRequestQueue() {
  const abortReason = new DOMException('Unauthorized', 'AbortError');

  requestQueue.forEach((item) => {
    item.cleanupSignal();
    item.controller.abort(abortReason);
  });
  requestQueue.clear();
  closeGlobalLoading();
}

function redirectToLogin() {
  const currentRoute = router.currentRoute.value;
  const redirect = currentRoute.path === '/login' ? '/' : currentRoute.fullPath || '/';
  void router.replace({ path: '/login', query: { redirect } });
}

function handleUnauthorized() {
  if (redirectingToLogin) {
    return false;
  }

  redirectingToLogin = true;
  clearStoredAccessToken();
  clearRequestQueue();

  redirectToLogin();
  return true;
}

function parseJsonSafely<T>(value: string): T | null {
  try {
    return JSON.parse(value) as T;
  } catch {
    return null;
  }
}

function isJsonContentType(contentType: string | null | undefined) {
  return (contentType || '').includes('application/json');
}

function readBlobText(blob: Blob) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result || ''));
    reader.onerror = () => reject(new RequestError('请求异常', false));
    reader.readAsText(blob);
  });
}

async function resolveBlobErrorMessage(
  blob: Blob,
  status: number,
  requestUrl: string,
  contentType?: string,
) {
  if (!isJsonContentType(contentType || blob.type)) {
    return resolveErrorMessage(status, '', requestUrl);
  }

  try {
    const payloadText = await readBlobText(blob);
    const payload = parseJsonSafely<Partial<ApiResponse<unknown>>>(payloadText);
    return resolveErrorMessage(status, payload?.message || '', requestUrl);
  } catch {
    return resolveErrorMessage(status, '', requestUrl);
  }
}

function getResponseHeader(headers: AxiosResponse['headers'], key: string) {
  const value = headers[key] ?? headers[key.toLowerCase()];
  return Array.isArray(value) ? value.join(';') : String(value || '');
}

async function resolveAxiosError(error: unknown): Promise<never> {
  if (error instanceof RequestError) {
    if (!error.handled) {
      showErrorMessage(error.message);
      throw new RequestError(error.message, true);
    }
    throw error;
  }

  if (axios.isCancel(error)) {
    throw error;
  }

  if (error instanceof AxiosError) {
    const config = error.config as ManagedRequestConfig | undefined;
    const response = error.response;

    if (error.code === AxiosError.ECONNABORTED) {
      showErrorMessage('请求超时');
      throwHandledError('请求超时');
    }

    if (response) {
      const status = response.status;
      const requestUrl = buildRequestUrl(config?.url, config?.params as RequestParams | undefined);
      let fallback = '';

      if (response.data instanceof Blob) {
        fallback = await resolveBlobErrorMessage(
          response.data,
          status,
          requestUrl,
          getResponseHeader(response.headers, 'content-type'),
        );
      } else if (isApiResponse(response.data)) {
        fallback = response.data.message || '';
      }

      const message =
        response.data instanceof Blob
          ? fallback
          : resolveErrorMessage(status, fallback, requestUrl);
      if (status === 401) {
        if (handleUnauthorized()) {
          showErrorMessage(message);
        }
        return interruptRequest();
      }
      showErrorMessage(message);
      throwHandledError(message);
    }
  }

  showErrorMessage('请求异常');
  throwHandledError('请求异常');
}

function triggerBrowserDownload(blob: Blob, filename: string) {
  const blobUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = blobUrl;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(blobUrl);
}

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const managedConfig = config as ManagedRequestConfig;

  if (managedConfig.url?.includes('/auth/login')) {
    redirectingToLogin = false;
  }

  const requestId = createRequestId();
  const queueController = new AbortController();
  const cleanupSignal = mergeAbortSignal(managedConfig.signal, queueController);
  const showLoading = managedConfig.showLoading !== false;
  const loadingText = managedConfig.loadingText || DEFAULT_LOADING_TEXT;
  const headers = AxiosHeaders.from(managedConfig.headers);
  const token = getToken();

  if (token) {
    redirectingToLogin = false;
    headers.set('Authorization', `Bearer ${token}`);
  }

  managedConfig.headers = headers;
  managedConfig.signal = queueController.signal;
  managedConfig.requestId = requestId;
  managedConfig.cleanupSignal = cleanupSignal;
  managedConfig.showLoading = showLoading;
  managedConfig.loadingText = loadingText;

  requestQueue.set(requestId, {
    controller: queueController,
    cleanupSignal,
  });

  if (showLoading) {
    startGlobalLoading(loadingText);
  }

  return managedConfig;
});

http.interceptors.response.use(
  (response: AxiosResponse) => {
    const config = response.config as ManagedRequestConfig;
    completeRequest(config);

    if (config.skipBusinessTransform) {
      return response;
    }

    const result = response.data;
    if (!isApiResponse(result)) {
      throw new RequestError('请求异常', false);
    }

    if (result.code === 0) {
      response.data = result.data;
      return response;
    }

    const message = result.message || '系统错误';
    if (result.code === 401) {
      if (handleUnauthorized()) {
        showErrorMessage(message);
      }
      return interruptRequest();
    }
    showErrorMessage(message);
    throwHandledError(message);
  },
  async (error: unknown) => {
    if (error instanceof AxiosError) {
      completeRequest(error.config);
    }

    return resolveAxiosError(error);
  },
);

async function request<T>(
  method: string,
  url: string,
  data?: unknown,
  options: RequestOptions = {},
): Promise<T> {
  const {
    params,
    timeout = DEFAULT_TIMEOUT,
    signal,
    headers,
    onUploadProgress,
    onDownloadProgress,
    showLoading = true,
    loadingText = DEFAULT_LOADING_TEXT,
    ...config
  } = options;

  const response = await http.request<unknown, AxiosResponse<T>>({
    ...config,
    method,
    url,
    data,
    params: normalizeParams(params),
    timeout,
    signal,
    headers: normalizeHeaders(headers),
    showLoading,
    loadingText,
    onUploadProgress: (event) => applyProgress(onUploadProgress, event),
    onDownloadProgress: (event) => applyProgress(onDownloadProgress, event),
  } as ManagedAxiosRequestConfig);
  return response.data;
}

export function get<T>(
  url: string,
  params?: RequestParams,
  options: RequestOptions = {},
): Promise<T> {
  return request<T>('GET', url, undefined, { ...options, params });
}

export function post<T>(url: string, data?: unknown, options: RequestOptions = {}): Promise<T> {
  return request<T>('POST', url, data, options);
}

export function put<T>(url: string, data?: unknown, options: RequestOptions = {}): Promise<T> {
  return request<T>('PUT', url, data, options);
}

export function del<T>(
  url: string,
  params?: RequestParams,
  options: RequestOptions = {},
): Promise<T> {
  return request<T>('DELETE', url, undefined, { ...options, params });
}

export async function download(url: string, options: DownloadOptions = {}) {
  const {
    params,
    filename = 'download',
    timeout = DEFAULT_TIMEOUT,
    signal,
    headers,
    onDownloadProgress,
    showLoading = true,
    loadingText = DEFAULT_LOADING_TEXT,
    ...config
  } = options;

  const response = await http.request<Blob, AxiosResponse<Blob>>({
    ...config,
    method: 'GET',
    url,
    params: normalizeParams(params),
    timeout,
    signal,
    headers: normalizeHeaders(headers),
    responseType: 'blob',
    skipBusinessTransform: true,
    showLoading,
    loadingText,
    onDownloadProgress: (event) => applyProgress(onDownloadProgress, event),
  } as ManagedAxiosRequestConfig);

  const resolvedFilename = resolveDownloadFilename(
    getResponseHeader(response.headers, 'content-disposition'),
    filename,
  );
  triggerBrowserDownload(response.data, resolvedFilename);
  return response.data;
}

const service = {
  defaults: http.defaults,
  interceptors: http.interceptors,
  request,
  get,
  post,
  put,
  delete: del,
  del,
  download,
};

export default service;
