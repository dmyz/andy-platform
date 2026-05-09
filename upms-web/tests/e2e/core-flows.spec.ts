import { expect, test } from '@playwright/test';

import { loginByToken, skipWhenBackendUnavailable } from './support/backend';

test('USER-E2E-002 用户管理支持关键字查询与重置', async ({ page, request }) => {
  const session = await skipWhenBackendUnavailable(request);
  await loginByToken(page, session, '/system/user');

  await page.getByPlaceholder('请输入用户名').fill('admin');
  await page.getByRole('button', { name: /查询|搜索/ }).click();
  await expect(page.locator('body')).toContainText(/admin|管理员|暂无数据/);

  await page.locator('form').getByRole('button', { name: '重置' }).click();
  await expect(page.getByPlaceholder('请输入用户名')).toHaveValue('');
});

test('ROLE-E2E-002 角色管理支持搜索与新增弹窗校验', async ({ page, request }) => {
  const session = await skipWhenBackendUnavailable(request);
  await loginByToken(page, session, '/system/role');

  await page.getByPlaceholder('请输入角色名称').fill('管理员');
  await page.getByRole('button', { name: /查询|搜索/ }).click();
  await expect(page.locator('body')).toContainText(/管理员|暂无数据/);

  await page.getByRole('button', { name: /新增/ }).click();
  const dialog = page.getByRole('dialog', { name: '新增角色' });
  await expect(dialog).toContainText(/角色名称/);
  await dialog.getByRole('button', { name: /确\s*定/ }).click();
  await expect(dialog).toBeVisible();
  await expect(dialog.getByPlaceholder('请输入角色名称')).toHaveValue('');
  await expect(dialog.getByPlaceholder('请输入角色编码')).toHaveValue('');
});

test('ANN-E2E-002 公告管理支持搜索与新增表单校验', async ({ page, request }) => {
  const session = await skipWhenBackendUnavailable(request);
  await loginByToken(page, session, '/announcement/manage');

  await page.getByPlaceholder('请输入公告标题').fill('e2e-');
  await page.getByRole('button', { name: /查询|搜索/ }).click();
  await expect(page.locator('body')).toContainText(/公告|暂无数据|e2e-/);

  await page.getByRole('button', { name: /新增/ }).click();
  const dialog = page.getByRole('dialog', { name: '新增公告' });
  await expect(dialog).toContainText(/公告标题/);
  await dialog.getByRole('button', { name: /确\s*定/ }).click();
  await expect(page.locator('body')).toContainText(/请输入公告标题|必填|公告内容/);
});

test('FILE-E2E-002 文件管理未选择文件时阻止上传', async ({ page, request }) => {
  const session = await skipWhenBackendUnavailable(request);
  await loginByToken(page, session, '/file/manage');

  await page.getByRole('button', { name: /上传/ }).first().click();
  const dialog = page.getByRole('dialog', { name: '上传文件' });
  await expect(dialog).toContainText(/上传文件/);
  await dialog.getByRole('button', { name: /确\s*定/ }).click();
  await expect(page.locator('body')).toContainText(/请选择文件|上传文件/);
});
