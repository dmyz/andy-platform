import request from '@/utils/request'
import type { ApiResponse } from '@/types/responses/common'
import type { LoginRequest, LoginResponse, ChangePasswordRequest, CaptchaResponse } from '@/types/admin/auth'
import type { CurrentUserPayload, LoginPayload, VerificationCodeSendResponse, VerificationCodeScene, VerificationCodeTargetType } from '@/types/admin/auth'

/**
 * 用户登录
 */
export function login(data: LoginRequest) {
  return request.post<ApiResponse<LoginResponse>>('/admin/auth/login', data)
}

/**
 * 用户登出
 */
export function logout() {
  return request.post<ApiResponse<void>>('/admin/auth/logout')
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request.get<ApiResponse<CurrentUserPayload>>('/admin/auth/current-user')
}

/**
 * 修改当前用户密码
 */
export function changePassword(data: ChangePasswordRequest) {
  return request.post<ApiResponse<void>>('/admin/auth/change-password', data)
}

/**
 * 首次登录修改密码
 */
export function firstTimeChangePassword(data: { newPassword: string }) {
  return request.post<ApiResponse<void>>('/admin/auth/first-time-password', data)
}

/**
 * 发送手机验证码
 */
export function sendMobileVerificationCode(data: {
  mobile: string
  scene: VerificationCodeScene
}) {
  return request.post<ApiResponse<VerificationCodeSendResponse>>('/admin/auth/code/mobile/send', data)
}

/**
 * 发送邮箱验证码
 */
export function sendEmailVerificationCode(data: {
  email: string
  scene: VerificationCodeScene
}) {
  return request.post<ApiResponse<VerificationCodeSendResponse>>('/admin/auth/code/email/send', data)
}

/**
 * 发送验证码
 */
export function sendVerificationCode(data: {
  scene: VerificationCodeScene
  targetType: VerificationCodeTargetType
  targetValue: string
}) {
  return request.post<ApiResponse<VerificationCodeSendResponse>>('/admin/auth/verification-code/send', data)
}

/**
 * 验证码登录
 */
export function loginWithCode(data: LoginPayload) {
  return request.post<ApiResponse<LoginResponse>>('/admin/auth/login', data)
}

/**
 * 忘记密码 - 发送重置链接
 */
export function forgotPassword(data: { email: string }) {
  return request.post<ApiResponse<void>>('/admin/auth/forgot-password', data)
}

/**
 * 重置密码（通过验证码）
 */
export function resetPasswordWithCode(data: {
  targetType: 'MOBILE' | 'EMAIL'
  target: string
  code: string
  newPassword: string
}) {
  return request.post<ApiResponse<void>>('/admin/auth/password/reset', data)
}

/**
 * 重置密码
 */
export function resetPassword(data: { token: string, newPassword: string }) {
  return request.post<ApiResponse<void>>('/admin/auth/reset-password', data)
}

/**
 * 获取验证码图片
 */
export function getCaptcha() {
  return request.get<ApiResponse<CaptchaResponse>>('/admin/auth/captcha')
}
