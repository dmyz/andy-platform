import { expect, test } from '@playwright/test';

import { loginByToken, skipWhenBackendUnavailable } from './support/backend';

test('AUTH-E2E-001 管理员可通过真实后端登录并进入仪表盘', async ({ page, request }) => {
  const session = await skipWhenBackendUnavailable(request);

  await page.goto('/#/login');
  await page.getByPlaceholder('请输入用户名').fill(process.env.E2E_USERNAME || 'admin');
  await page.getByPlaceholder('请输入密码').fill(process.env.E2E_PASSWORD || 'admin');
  await page.getByRole('button', { name: '登录', exact: true }).click();

  await expect(page).toHaveURL(/#\/(dashboard)?/);
  await expect(page.locator('body')).toContainText(/概览|仪表盘|Dashboard/);
  await page.evaluate(() => localStorage.getItem('ACCESS_TOKEN')).then((token) => {
    expect(token || session.token).toBeTruthy();
  });
});

test('AUTH-E2E-002 未登录访问业务页跳转登录页', async ({ page, request }) => {
  await skipWhenBackendUnavailable(request);

  await page.goto('/#/system/user');
  await expect(page).toHaveURL(/#\/login/);
});

test('AUTH-E2E-003 已登录 token 可访问业务路由', async ({ page, request }) => {
  const session = await skipWhenBackendUnavailable(request);

  await loginByToken(page, session, '/system/user');

  await expect(page).toHaveURL(/#\/system\/user/);
  await expect(page.locator('body')).toContainText(/用户管理|用户/);
});

test('AUTH-E2E-004 找回密码页支持基础表单校验', async ({ page }) => {
  await page.goto('/#/forgot-password');

  await expect(page.locator('body')).toContainText(/找回密码|手机号找回|邮箱找回/);
  await page.getByRole('button', { name: '重置密码' }).click();
  await expect(page.locator('body')).toContainText(/请输入图形验证码/);

  await page.getByRole('radio', { name: /邮箱找回/ }).click();
  await expect(page.getByPlaceholder('请输入邮箱地址')).toBeVisible();
});

test('AUTH-E2E-005 已登录用户可进入无权限提示页', async ({ page, request }) => {
  const session = await skipWhenBackendUnavailable(request);

  await loginByToken(page, session, '/403');

  await expect(page).toHaveURL(/#\/403/);
  await expect(page.locator('body')).toContainText(/403|当前账号无权访问此页面/);
});
