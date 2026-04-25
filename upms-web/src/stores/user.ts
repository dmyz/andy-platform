import type {
  LoginPayload,
  LoginEmailPayload,
  LoginMobilePayload,
  LoginPasswordPayload,
  NavigationItem,
  UserInfo,
} from '@/types/auth'
import { defineStore } from 'pinia'
import { MessagePlugin } from 'tdesign-vue-next'
import router from '@/router'
import { clearStoredAccessToken, getStoredAccessToken, setStoredAccessToken } from '@/utils/auth'
import * as authApi from '@/api/auth'

function resolveRoutePermission(routePath?: string) {
  if (!routePath) {
    return undefined
  }
  return router.getRoutes().find(route => route.path === routePath)?.meta?.permission as string | undefined
}

function filterNavigationsByPermission(items: NavigationItem[], permissionCodes: string[]): NavigationItem[] {
  return items.reduce<NavigationItem[]>((result, item) => {
    const filteredChildren = item.children?.length
      ? filterNavigationsByPermission(item.children, permissionCodes)
      : []

    if (item.type === 'GROUP') {
      if (filteredChildren.length > 0) {
        result.push({ ...item, children: filteredChildren })
      }
      return result
    }

    const requiredPermission = resolveRoutePermission(item.routePath)
    if (requiredPermission && !permissionCodes.includes(requiredPermission)) {
      return result
    }

    result.push({ ...item, children: filteredChildren })
    return result
  }, [])
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getStoredAccessToken())
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])
  const navigations = ref<NavigationItem[]>([])

  const setToken = (newToken: string) => {
    token.value = newToken
    setStoredAccessToken(newToken)
  }

  const clearToken = () => {
    token.value = ''
    clearStoredAccessToken()
    userInfo.value = null
    permissions.value = []
    navigations.value = []
  }

  const finishLogin = async (tokenValue: string) => {
    setToken(tokenValue)
    MessagePlugin.success('登录成功')
    await getUserInfo()
    return true
  }

  const login = async (loginForm: LoginPayload) => {
    try {
      const res = await authApi.login(loginForm)
      const accessToken = res.data.accessToken || res.data.token || ''
      if (!accessToken) {
        MessagePlugin.error('登录成功但未返回访问令牌')
        return false
      }
      return await finishLogin(accessToken)
    }
    catch (error) {
      return false
    }
  }

  const loginByPassword = async (loginForm: Omit<LoginPasswordPayload, 'grantType'>) => {
    return login({ grantType: 'PASSWORD', ...loginForm })
  }

  const loginByMobile = async (loginForm: Omit<LoginMobilePayload, 'grantType'>) => {
    return login({ grantType: 'MOBILE_CODE', ...loginForm })
  }

  const loginByEmail = async (loginForm: Omit<LoginEmailPayload, 'grantType'>) => {
    return login({ grantType: 'EMAIL_CODE', ...loginForm })
  }

  const getUserInfo = async () => {
    try {
      const res = await authApi.getCurrentUser()
      userInfo.value = res.data.user
      permissions.value = res.data.permissions
      navigations.value = filterNavigationsByPermission(res.data.navigations, res.data.permissions)
      return res.data
    }
    catch (error) {
      clearToken()
      throw error
    }
  }

  const logout = async () => {
    try {
      await authApi.logout()
    }
    catch (error) {
      console.error(error)
    }
    finally {
      clearToken()
      router.push('/login')
    }
  }

  // 检查权限
  const hasPermission = (permission: string) => {
    return permissions.value.includes(permission)
  }

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
  }
})
