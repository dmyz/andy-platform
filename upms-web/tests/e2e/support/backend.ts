import { expect, type APIRequestContext, type Page, test } from '@playwright/test';

export const apiBaseURL = process.env.E2E_API_BASE_URL || 'http://localhost:8004';

export interface LoginSession {
  token: string;
  endpoint: string;
}

const loginCandidates = ['/admin/auth/login', '/auth/login'];

export async function tryLogin(request: APIRequestContext): Promise<LoginSession | null> {
  for (const endpoint of loginCandidates) {
    try {
      const response = await request.post(`${apiBaseURL}${endpoint}`, {
        data: {
          grantType: 'PASSWORD',
          username: process.env.E2E_USERNAME || 'admin',
          password: process.env.E2E_PASSWORD || 'admin',
        },
        timeout: 10_000,
      });

      if (!response.ok()) {
        continue;
      }

      const payload = (await response.json()) as {
        code?: number;
        data?: { accessToken?: string; token?: string };
      };
      const token = payload.data?.accessToken || payload.data?.token || '';
      if (payload.code === 0 && token) {
        return { token, endpoint };
      }
    } catch {
      continue;
    }
  }

  return null;
}

export async function skipWhenBackendUnavailable(request: APIRequestContext) {
  const session = await tryLogin(request);
  test.skip(
    !session,
    `后端登录接口不可用或临时断开，已跳过真实后端 E2E。检查 ${apiBaseURL}`,
  );
  return session as LoginSession;
}

export async function loginByToken(page: Page, session: LoginSession, targetPath = '/dashboard') {
  await page.addInitScript((token) => {
    localStorage.setItem('ACCESS_TOKEN', token);
    localStorage.removeItem('X-Auth-Token');
  }, session.token);
  await page.goto(`/#${targetPath}`);
  await expect(page).not.toHaveURL(/#\/login/);
}

export async function expectAppShell(page: Page) {
  await expect(page.locator('body')).toBeVisible();
  await expect(page.locator('text=404 - 页面不存在或仍在开发中')).toHaveCount(0);
}
