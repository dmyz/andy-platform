import request from '@/utils/request';
import type { LoginResponse, ChangePasswordRequest, CaptchaResponse } from '@/types/api/auth';
import type {
  CurrentUserPayload,
  LoginPayload,
  VerificationCodeSendResponse,
  VerificationCodeScene,
  VerificationCodeTargetType,
} from '@/types/auth';

/**
 * 用户登录
 */
export function login(data: LoginPayload) {
  return request.post<LoginResponse>('/admin/auth/login', data);
}

/**
 * 用户登出
 */
export function logout() {
  return request.post<void>('/admin/auth/logout');
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request.get<CurrentUserPayload>('/admin/auth/current-user');
}

/**
 * 修改当前用户密码
 */
export function changePassword(data: ChangePasswordRequest) {
  return request.post<void>('/admin/auth/password/change', data);
}

/**
 * 发送手机验证码
 */
export function sendMobileVerificationCode(data: { mobile: string; scene: VerificationCodeScene }) {
  return request.post<VerificationCodeSendResponse>('/admin/auth/code/mobile/send', data);
}

/**
 * 发送邮箱验证码
 */
export function sendEmailVerificationCode(data: { email: string; scene: VerificationCodeScene }) {
  return request.post<VerificationCodeSendResponse>('/admin/auth/code/email/send', data);
}

/**
 * 发送验证码
 */
export function sendVerificationCode(data: {
  scene: VerificationCodeScene;
  targetType: VerificationCodeTargetType;
  targetValue: string;
}) {
  const url =
    data.targetType === 'MOBILE' ? '/admin/auth/code/mobile/send' : '/admin/auth/code/email/send';
  const payload =
    data.targetType === 'MOBILE'
      ? { mobile: data.targetValue, scene: data.scene }
      : { email: data.targetValue, scene: data.scene };
  return request.post<VerificationCodeSendResponse>(url, payload);
}

/**
 * 验证码登录
 */
export function loginWithCode(data: LoginPayload) {
  return request.post<LoginResponse>('/admin/auth/login', data);
}

/**
 * 重置密码（通过验证码）
 */
export function resetPasswordWithCode(data: {
  targetType: 'MOBILE' | 'EMAIL';
  target: string;
  code: string;
  newPassword: string;
}) {
  return request.post<void>('/admin/auth/password/reset', data);
}

/**
 * 获取验证码图片
 */
export function getCaptcha() {
  return request.get<CaptchaResponse>('/admin/auth/captcha');
}
