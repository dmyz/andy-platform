import type { User } from '../entities/user'

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string
  password: string
  captchaId?: string
  captchaCode?: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  user: User
}

/**
 * 刷新令牌请求
 */
export interface RefreshTokenRequest {
  refreshToken: string
}

/**
 * 刷新令牌响应
 */
export interface RefreshTokenResponse {
  token: string
  refreshToken: string
}

/**
 * 修改密码请求
 */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

/**
 * 重置密码请求
 */
export interface ResetPasswordRequest {
  userId: string
  newPassword: string
}

/**
 * 验证码响应
 */
export interface CaptchaResponse {
  captchaId: string
  captchaImage: string
}
