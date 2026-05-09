import type { RouteRecordRaw, RouteLocationNormalized } from 'vue-router';
import { h } from 'vue';
import { createRouter, createWebHashHistory } from 'vue-router';
import BasicLayout from '@/layouts/BasicLayout.vue';
import { useUserStore } from '@/stores/user';

// 静态路由配置
const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/login/forgot-password.vue'),
    meta: { title: '找回密码' },
  },
  {
    path: '/first-time-password',
    name: 'FirstTimePassword',
    component: () => import('@/views/login/first-time-password.vue'),
    meta: { title: '首次修改密码' },
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限访问' },
  },
  {
    path: '/',
    name: 'Root',
    component: BasicLayout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '概览仪表盘', permission: 'dashboard:view' },
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/user/profile.vue'),
        meta: { title: '个人中心' },
      },
      {
        path: '/audit',
        name: 'Audit',
        redirect: '/audit/login',
        children: [
          {
            path: '/audit/login',
            name: 'AuditLogin',
            component: () => import('@/views/audit/login/index.vue'),
            meta: { title: '登录审计', permission: 'audit:login:view' },
          },
          {
            path: '/audit/operation',
            name: 'AuditOperation',
            component: () => import('@/views/audit/operation/index.vue'),
            meta: { title: '操作审计', permission: 'audit:operation:view' },
          },
        ],
      },
      {
        path: '/announcement',
        name: 'Announcement',
        redirect: '/announcement/manage',
        children: [
          {
            path: '/announcement/manage',
            name: 'AnnouncementManage',
            component: () => import('@/views/announcement/index.vue'),
            meta: { title: '公告管理', permission: 'announcement:manage:view' },
          },
          {
            path: '/announcement/inbox',
            name: 'AnnouncementInbox',
            component: () => import('@/views/announcement/inbox.vue'),
            meta: { title: '消息中心', permission: 'announcement:inbox:view' },
          },
        ],
      },
      {
        path: '/file',
        name: 'FileCenter',
        redirect: '/file/manage',
        children: [
          {
            path: '/file/manage',
            name: 'FileManage',
            component: () => import('@/views/file/index.vue'),
            meta: { title: '文件管理', permission: 'file:manage:view' },
          },
        ],
      },
      // ====== 以下为静态配置的系统管理模块 ======
      {
        path: '/system',
        name: 'System',
        redirect: '/system/user',
        children: [
          {
            path: '/system/user',
            name: 'SystemUser',
            component: () => import('@/views/system/user/index.vue'),
            meta: { title: '用户管理', permission: 'system:user:view' },
          },
          {
            path: '/system/role',
            name: 'SystemRole',
            component: () => import('@/views/system/role/index.vue'),
            meta: { title: '角色管理', permission: 'system:role:view' },
          },
          {
            path: '/system/navigation',
            name: 'SystemNavigation',
            component: () => import('@/views/system/navigation/index.vue'),
            meta: { title: '导航管理', permission: 'system:navigation:view' },
          },
          {
            path: '/system/org',
            name: 'SystemOrg',
            component: () => import('@/views/system/organization/index.vue'),
            meta: { title: '部门管理', permission: 'system:org:view' },
          },
          {
            path: '/system/session',
            name: 'SystemSession',
            component: () => import('@/views/system/session/index.vue'),
            meta: { title: '在线会话', permission: 'auth:session:view' },
          },
          {
            path: '/system/permission',
            name: 'SystemPermission',
            component: () => import('@/views/system/permission/index.vue'),
            meta: { title: '权限定义', permission: 'system:permission:view' },
          },
          {
            path: '/system/permission-module',
            name: 'SystemPermissionModule',
            component: () => import('@/views/system/permission-module/index.vue'),
            meta: { title: '权限模块', permission: 'system:permission-module:view' },
          },
          {
            path: '/system/dictionary',
            name: 'SystemDictionary',
            component: () => import('@/views/system/dictionary/index.vue'),
            meta: { title: '字典管理', permission: 'system:dictionary:view' },
          },
          {
            path: '/system/setting',
            name: 'SystemSetting',
            component: () => import('@/views/system/setting/index.vue'),
            meta: { title: '系统配置', permission: 'system:setting:view' },
          },
        ],
      },
    ],
  },
  // 404 兜底路由
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () =>
      Promise.resolve({
        setup() {
          return () =>
            h(
              'div',
              { class: 'flex items-center justify-center h-full text-2xl text-gray-400' },
              '404 - 页面不存在或仍在开发中',
            );
        },
      }),
  },
];

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
});

const whiteList = ['/login', '/forgot-password']; // 免登录白名单

function canAccessRoute(to: RouteLocationNormalized, userStore: ReturnType<typeof useUserStore>) {
  if (to.path === '/403') {
    return true;
  }
  const permission = to.meta.permission;
  if (typeof permission === 'string') {
    return userStore.hasPermission(permission);
  }
  if (Array.isArray(permission)) {
    return permission.every((item) => typeof item === 'string' && userStore.hasPermission(item));
  }
  return true;
}

// 路由拦截
router.beforeEach(async (to) => {
  const userStore = useUserStore();
  const hasToken = userStore.token;
  const isFirstTimePasswordRoute = to.path === '/first-time-password';

  if (hasToken) {
    if (to.path === '/login') {
      // 已登录，跳转到首页
      if (userStore.userInfo?.passwordResetRequired) {
        return '/first-time-password';
      }
      return { path: '/' };
    }

    // 判断是否已经获取过用户信息
    const hasUserInfo = !!userStore.userInfo;
    if (hasUserInfo) {
      if (userStore.userInfo?.passwordResetRequired && !isFirstTimePasswordRoute) {
        return `/first-time-password?redirect=${encodeURIComponent(to.fullPath)}`;
      }
      if (!userStore.userInfo?.passwordResetRequired && isFirstTimePasswordRoute) {
        return '/';
      }
      if (!canAccessRoute(to, userStore)) {
        return { path: '/403', replace: true, query: { redirect: to.fullPath } };
      }
      return true;
    }

    try {
      // 获取用户信息 (侧边栏会根据返回结果动态渲染可见菜单，但路由不再动态注册)
      const currentUser = (await userStore.getUserInfo()).user;
      if (currentUser.passwordResetRequired && !isFirstTimePasswordRoute) {
        return `/first-time-password?redirect=${encodeURIComponent(to.fullPath)}`;
      }
      if (!currentUser.passwordResetRequired && isFirstTimePasswordRoute) {
        return '/';
      }
      if (!canAccessRoute(to, userStore)) {
        return { path: '/403', replace: true, query: { redirect: to.fullPath } };
      }
      return { ...to, replace: true };
    } catch {
      // 获取用户信息失败，清理登录态并跳转登录页
      userStore.clearToken();
      return `/login?redirect=${to.path}`;
    }
  }

  // 未登录
  if (whiteList.includes(to.path)) {
    return true;
  }
  return `/login?redirect=${to.path}`;
});

export default router;
