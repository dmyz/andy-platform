import { expect, test } from '@playwright/test';

import { expectAppShell, loginByToken, skipWhenBackendUnavailable } from './support/backend';

const routes = [
  { id: 'DASH-E2E-001', path: '/dashboard', text: /概览|仪表盘|Dashboard/ },
  { id: 'PROFILE-E2E-001', path: '/profile', text: /个人中心|基本信息|安全设置/ },
  { id: 'AUDIT-LOGIN-E2E-001', path: '/audit/login', text: /登录审计|登录账号/ },
  { id: 'AUDIT-OP-E2E-001', path: '/audit/operation', text: /操作审计|操作人/ },
  { id: 'ANN-E2E-001', path: '/announcement/manage', text: /公告管理|公告标题/ },
  { id: 'INBOX-E2E-001', path: '/announcement/inbox', text: /消息中心|公告标题/ },
  { id: 'FILE-E2E-001', path: '/file/manage', text: /文件管理|文件名/ },
  { id: 'USER-E2E-001', path: '/system/user', text: /用户管理|用户名/ },
  { id: 'ROLE-E2E-001', path: '/system/role', text: /角色管理|角色名称/ },
  { id: 'NAV-E2E-001', path: '/system/navigation', text: /导航管理|导航类型|访问规则/ },
  { id: 'ORG-E2E-001', path: '/system/org', text: /部门管理|部门名称|组织/ },
  { id: 'SESSION-E2E-001', path: '/system/session', text: /在线会话|用户名/ },
  { id: 'PERM-E2E-001', path: '/system/permission', text: /权限定义|权限名称/ },
  { id: 'DICT-E2E-001', path: '/system/dictionary', text: /字典管理|字典名称/ },
  { id: 'SETTING-E2E-001', path: '/system/setting', text: /系统配置|配置名称/ },
];

test.describe('真实后端路由冒烟', () => {
  for (const route of routes) {
    test(`${route.id} ${route.path} 可登录访问并渲染关键内容`, async ({ page, request }) => {
      const session = await skipWhenBackendUnavailable(request);

      await loginByToken(page, session, route.path);

      await expect(page).toHaveURL(new RegExp(`#${route.path.replace('/', '\\/')}`));
      await expectAppShell(page);
      await expect(page.locator('body')).toContainText(route.text);
    });
  }
});
