import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';

import * as authApi from '@/api/auth';
import { clearStoredAccessToken, getStoredAccessToken } from '@/utils/auth';
import { useUserStore } from '@/stores/user';
import router from '@/router';

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  getCurrentUser: vi.fn(),
}));

vi.mock('@/router', () => ({
  default: {
    push: vi.fn(),
    getRoutes: vi.fn(() => [
      { path: '/dashboard', meta: { permission: 'dashboard:view' } },
      { path: '/system/user', meta: { permission: 'system:user:view' } },
    ]),
  },
}));

vi.mock('antdv-next', () => ({
  message: {
    success: vi.fn(),
    warning: vi.fn(),
    error: vi.fn(),
  },
}));

const currentUserPayload = {
  user: {
    id: '1',
    username: 'admin',
    displayName: '管理员',
    roles: ['ADMIN'],
  },
  permissions: ['dashboard:view'],
  navigations: [
    {
      id: 'dash',
      name: '仪表盘',
      type: 'PAGE',
      routePath: '/dashboard',
      sortOrder: 1,
    },
    {
      id: 'user',
      name: '用户管理',
      type: 'PAGE',
      routePath: '/system/user',
      sortOrder: 2,
    },
  ],
};

describe('stores/user', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    clearStoredAccessToken();
  });

  it('USER-UNIT-001 密码登录成功后保存 token 并加载用户信息', async () => {
    vi.mocked(authApi.login).mockResolvedValue({ accessToken: 'token-1' });
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(currentUserPayload);

    const store = useUserStore();
    await expect(store.loginByPassword({ username: 'admin', password: 'admin' })).resolves.toBe(
      true,
    );

    expect(store.token).toBe('token-1');
    expect(getStoredAccessToken()).toBe('token-1');
    expect(store.userInfo?.username).toBe('admin');
  });

  it('USER-UNIT-002 当前用户导航来自后端且权限按编码判断', async () => {
    vi.mocked(authApi.getCurrentUser).mockResolvedValue({
      ...currentUserPayload,
      user: {
        ...currentUserPayload.user,
        roles: ['USER'],
      },
    });

    const store = useUserStore();
    await store.getUserInfo();

    expect(store.navigations.map((item) => item.routePath)).toEqual(['/dashboard', '/system/user']);
    expect(store.hasPermission('dashboard:view')).toBe(true);
    expect(store.hasPermission('system:user:view')).toBe(false);
  });

  it('USER-UNIT-003 登出始终清理登录态并回到登录页', async () => {
    vi.mocked(authApi.logout).mockRejectedValue(new Error('network down'));

    const store = useUserStore();
    store.setToken('token-2');
    await store.logout();

    expect(store.token).toBe('');
    expect(getStoredAccessToken()).toBe('');
    expect(router.push).toHaveBeenCalledWith('/login');
  });

  it('USER-UNIT-004 token 清空时同步清理内存态', async () => {
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(currentUserPayload);

    const store = useUserStore();
    store.setToken('token-3');
    await store.getUserInfo();

    clearStoredAccessToken();
    await nextTick();

    expect(store.token).toBe('');
    expect(store.userInfo).toBeNull();
    expect(store.permissions).toEqual([]);
    expect(store.navigations).toEqual([]);
    expect(getStoredAccessToken()).toBe('');
  });
});
