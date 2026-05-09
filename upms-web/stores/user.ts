import type {
  LoginPayload,
  LoginEmailPayload,
  LoginMobilePayload,
  LoginPasswordPayload,
  NavigationItem,
  UserInfo,
} from '@/types/auth';
import { defineStore } from 'pinia';
import { computed, ref, watch } from 'vue';
import { message } from 'antdv-next';
import router from '@/router';
import { clearStoredAccessToken, setStoredAccessToken, storedAccessToken } from '@/utils/auth';
import * as authApi from '@/api/auth';

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<UserInfo | null>(null);
  const permissionCodes = ref<string[]>([]);
  const navigationTree = ref<NavigationItem[]>([]);

  const token = computed(() => storedAccessToken.value);
  const userInfo = computed(() => currentUser.value);
  const permissions = computed(() => permissionCodes.value);
  const navigations = computed(() => navigationTree.value);

  const setToken = (newToken: string) => {
    setStoredAccessToken(newToken);
  };

  const clearUserSession = () => {
    currentUser.value = null;
    permissionCodes.value = [];
    navigationTree.value = [];
  };

  const clearToken = () => {
    clearStoredAccessToken();
    clearUserSession();
  };

  watch(storedAccessToken, (newToken) => {
    if (!newToken) {
      clearUserSession();
    }
  });

  const finishLogin = async (tokenValue: string) => {
    setToken(tokenValue);
    message.success('登录成功');
    try {
      await getUserInfo();
    } catch (error) {
      console.error('获取用户信息失败:', error);
      message.warning('登录成功，但获取用户信息失败');
    }
    return true;
  };

  const login = async (loginForm: LoginPayload) => {
    try {
      const res = await authApi.login(loginForm);
      const accessToken = res.accessToken || '';
      if (!accessToken) {
        message.error('登录成功但未返回访问令牌');
        return false;
      }
      return await finishLogin(accessToken);
    } catch {
      return false;
    }
  };

  const loginByPassword = async (loginForm: Omit<LoginPasswordPayload, 'grantType'>) => {
    return login({ grantType: 'PASSWORD', ...loginForm });
  };

  const loginByMobile = async (loginForm: Omit<LoginMobilePayload, 'grantType'>) => {
    return login({ grantType: 'MOBILE_CODE', ...loginForm });
  };

  const loginByEmail = async (loginForm: Omit<LoginEmailPayload, 'grantType'>) => {
    return login({ grantType: 'EMAIL_CODE', ...loginForm });
  };

  const getUserInfo = async () => {
    const res = await authApi.getCurrentUser();
    currentUser.value = res.user;
    permissionCodes.value = res.permissions ?? [];
    navigationTree.value = res.navigations ?? [];
    return res;
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch (error) {
      console.error(error);
    } finally {
      clearToken();
      router.push('/login');
    }
  };

  const normalizeRole = (roleCode: string) => roleCode.trim().toUpperCase().replace(/^ROLE_/, '');

  const isAdmin = () => {
    return currentUser.value?.roles?.some((role) => {
      const normalized = normalizeRole(role);
      return normalized === 'ADMIN' || normalized === 'SUPER_ADMIN';
    });
  };

  const hasPermission = (permission: string) => {
    const normalized = permission.trim();
    if (!normalized) {
      return false;
    }
    if (normalized.startsWith('profile:')) {
      return !!token.value;
    }
    return !!isAdmin() || permissionCodes.value.includes(normalized);
  };

  return {
    token,
    userInfo,
    permissions,
    navigations,
    setToken,
    clearToken,
    login,
    loginByPassword,
    loginByMobile,
    loginByEmail,
    getUserInfo,
    logout,
    hasPermission,
  };
});
