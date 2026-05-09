import axios, {
  AxiosError,
  CanceledError,
  type AxiosAdapter,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import router from '@/router';
import { clearStoredAccessToken, getStoredAccessToken, setStoredAccessToken } from '@/utils/auth';
import { toast } from '@/utils/feedback';
import { del, download, get, post } from '@/utils/request';

vi.mock('@/router', () => ({
  default: {
    push: vi.fn(),
    replace: vi.fn(),
    currentRoute: {
      value: {
        fullPath: '/system/navigation',
      },
    },
    getRoutes: vi.fn(() => []),
  },
}));

vi.mock('@/utils/feedback', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
    loading: vi.fn(() => vi.fn()),
  },
  confirmAction: vi.fn(),
}));

function createResponse<T>(
  config: InternalAxiosRequestConfig,
  data: T,
  options: Partial<AxiosResponse<T>> = {},
): AxiosResponse<T> {
  return {
    data,
    status: options.status ?? 200,
    statusText: options.statusText ?? 'OK',
    headers: options.headers ?? {},
    config,
    request: {},
  };
}

function successAdapter<T>(data: T): AxiosAdapter {
  return async (config) => createResponse(config, data);
}

function httpErrorAdapter(status: number, data: unknown): AxiosAdapter {
  return async (config) => {
    const response = createResponse(config, data, {
      status,
      statusText: String(status),
      headers: { 'content-type': 'application/json' },
    });
    throw new AxiosError('Request failed', undefined, config, {}, response);
  };
}

function timeoutAdapter(): AxiosAdapter {
  return async (config) => {
    throw new AxiosError('timeout', AxiosError.ECONNABORTED, config);
  };
}

function cancelablePendingAdapter(): AxiosAdapter {
  return (config) =>
    new Promise((_resolve, reject) => {
      config.signal?.addEventListener('abort', () => {
        reject(new CanceledError('canceled', config));
      });
    });
}

function wait(ms: number) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

describe('utils/request', () => {
  let closeLoading: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    closeLoading = vi.fn();
    vi.mocked(toast.loading).mockReturnValue(closeLoading);
    vi.spyOn(window.URL, 'createObjectURL').mockReturnValue('blob:download');
    vi.spyOn(window.URL, 'revokeObjectURL').mockImplementation(() => undefined);
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);
    vi.mocked(router.push).mockClear();
    vi.mocked(router.replace).mockClear();
    clearStoredAccessToken();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    clearStoredAccessToken();
  });

  it('REQ-UNIT-001 过滤空 query 参数并解包业务 data', async () => {
    const adapter = vi.fn(successAdapter({ code: 0, message: 'success', data: { total: 1 } }));

    await expect(
      get('/admin/users', { pageNum: 1, keyword: '', status: 0 }, { adapter }),
    ).resolves.toEqual({ total: 1 });

    const config = adapter.mock.calls[0]?.[0] as AxiosRequestConfig | undefined;
    expect(config?.baseURL).toBe('/api');
    expect(config?.url).toBe('/admin/users');
    expect(config?.params).toEqual({ pageNum: '1', status: '0' });
  });

  it('REQ-UNIT-002 普通对象请求体正常传递给 axios', async () => {
    const adapter = vi.fn(successAdapter({ code: 0, message: 'success', data: { ok: true } }));
    const payload = { username: 'admin', password: 'admin' };

    await post('/admin/auth/login', payload, { adapter });

    const config = adapter.mock.calls[0]?.[0] as AxiosRequestConfig | undefined;
    expect(config).toMatchObject({
      method: 'post',
      url: '/admin/auth/login',
      data: JSON.stringify(payload),
    });
  });

  it('REQ-UNIT-003 业务错误只弹出一次错误提示', async () => {
    await expect(
      del('/admin/users/1', undefined, {
        adapter: successAdapter({ code: 50001, message: '业务失败', data: null }),
      }),
    ).rejects.toThrow('业务失败');

    expect(toast.error).toHaveBeenCalledTimes(1);
    expect(toast.error).toHaveBeenCalledWith('业务失败');
  });

  it('REQ-UNIT-004 HTTP 401 清理 token、取消队列并跳转登录页', async () => {
    setStoredAccessToken('expired-token');
    const pendingAdapter = cancelablePendingAdapter();
    const pendingRequest = get('/admin/users/slow', undefined, { adapter: pendingAdapter });
    const pendingCancelExpectation = expect(pendingRequest).rejects.toSatisfy(axios.isCancel);
    await Promise.resolve();

    const unauthorizedRequest = get(
      '/admin/auth/current-user',
      undefined,
      {
        adapter: httpErrorAdapter(401, { code: 401, message: '登录已过期', data: null }),
      },
    );

    await expect(Promise.race([unauthorizedRequest, wait(30)])).resolves.toBeUndefined();

    await pendingCancelExpectation;
    expect(getStoredAccessToken()).toBe('');
    expect(router.replace).toHaveBeenCalledTimes(1);
    expect(router.replace).toHaveBeenCalledWith({
      path: '/login',
      query: { redirect: '/system/navigation' },
    });
  });

  it('REQ-UNIT-005 业务 code 401 清理 token、取消队列并跳转登录页', async () => {
    setStoredAccessToken('expired-token');
    const pendingAdapter = cancelablePendingAdapter();
    const pendingRequest = get('/admin/users/slow', undefined, { adapter: pendingAdapter });
    const pendingCancelExpectation = expect(pendingRequest).rejects.toSatisfy(axios.isCancel);

    const unauthorizedRequest = get('/admin/auth/current-user', undefined, {
      adapter: successAdapter({ code: 401, message: '登录已过期', data: null }),
    });

    await expect(Promise.race([unauthorizedRequest, wait(30)])).resolves.toBeUndefined();

    await pendingCancelExpectation;
    expect(getStoredAccessToken()).toBe('');
    expect(router.replace).toHaveBeenCalledTimes(1);
    expect(router.replace).toHaveBeenCalledWith({
      path: '/login',
      query: { redirect: '/system/navigation' },
    });
  });

  it('REQ-UNIT-006 并发 401 只触发一次未授权提示和跳转', async () => {
    setStoredAccessToken('expired-token');
    const first = get('/admin/auth/current-user', undefined, {
      adapter: httpErrorAdapter(401, { code: 401, message: '登录已过期', data: null }),
    });
    const second = get('/admin/navigation/tree', undefined, {
      adapter: successAdapter({ code: 401, message: '登录已过期', data: null }),
    });

    await expect(Promise.race([Promise.all([first, second]), wait(30)])).resolves.toBeUndefined();

    expect(getStoredAccessToken()).toBe('');
    expect(toast.error).toHaveBeenCalledTimes(1);
    expect(router.replace).toHaveBeenCalledTimes(1);
    expect(router.replace).toHaveBeenCalledWith({
      path: '/login',
      query: { redirect: '/system/navigation' },
    });
  });

  it('REQ-UNIT-007 FormData 请求回传上传进度', async () => {
    const progress = vi.fn();
    const data = new FormData();
    data.append('file', new File(['hello'], 'hello.txt'));
    const adapter: AxiosAdapter = async (config) => {
      config.onUploadProgress?.({
        loaded: 50,
        total: 100,
        bytes: 50,
        progress: 0.5,
      });
      return createResponse(config, { code: 0, message: 'success', data: { id: 'upload-ok' } });
    };

    await expect(
      post('/admin/file/upload', data, { adapter, onUploadProgress: progress }),
    ).resolves.toEqual({
      id: 'upload-ok',
    });
    expect(progress).toHaveBeenCalledWith(50, 50, 100);
  });

  it('REQ-UNIT-008 文件下载解析响应文件名并触发浏览器下载', async () => {
    const appendSpy = vi.spyOn(document.body, 'appendChild');
    const blob = new Blob(['download'], { type: 'application/octet-stream' });

    await expect(
      download('/admin/file/1/download', {
        adapter: async (config) =>
          createResponse(config, blob, {
            headers: { 'content-disposition': "attachment; filename*=UTF-8''report.csv" },
          }),
      }),
    ).resolves.toBe(blob);

    expect(appendSpy).toHaveBeenCalled();
    expect(HTMLAnchorElement.prototype.click).toHaveBeenCalled();
  });

  it('REQ-UNIT-009 并发请求只开启一次 loading，全部结束后关闭', async () => {
    const resolvers: Array<() => void> = [];
    const adapter: AxiosAdapter = (config) =>
      new Promise((resolve) => {
        resolvers.push(() => {
          resolve(createResponse(config, { code: 0, message: 'success', data: { ok: true } }));
        });
      });

    const first = get('/admin/one', undefined, { adapter });
    const second = get('/admin/two', undefined, { adapter });

    await vi.waitFor(() => {
      expect(toast.loading).toHaveBeenCalledTimes(1);
    });
    resolvers[0]?.();
    await Promise.resolve();
    expect(closeLoading).not.toHaveBeenCalled();

    resolvers[1]?.();
    await expect(Promise.all([first, second])).resolves.toEqual([{ ok: true }, { ok: true }]);
    expect(closeLoading).toHaveBeenCalledTimes(1);
  });

  it('REQ-UNIT-010 showLoading 为 false 时不触发全局 loading', async () => {
    await expect(
      get('/admin/silent', undefined, {
        showLoading: false,
        adapter: successAdapter({ code: 0, message: 'success', data: { ok: true } }),
      }),
    ).resolves.toEqual({ ok: true });

    expect(toast.loading).not.toHaveBeenCalled();
    expect(closeLoading).not.toHaveBeenCalled();
  });

  it('REQ-UNIT-011 超时显示请求超时', async () => {
    await expect(
      get('/admin/timeout', undefined, {
        adapter: timeoutAdapter(),
      }),
    ).rejects.toThrow('请求超时');

    expect(toast.error).toHaveBeenCalledWith('请求超时');
  });

  it('REQ-UNIT-012 取消请求保留 axios 取消异常', async () => {
    const controller = new AbortController();
    const pendingRequest = get('/admin/cancel', undefined, {
      signal: controller.signal,
      adapter: cancelablePendingAdapter(),
    });

    controller.abort();

    await expect(pendingRequest).rejects.toSatisfy(axios.isCancel);
    expect(toast.error).not.toHaveBeenCalled();
  });
});
