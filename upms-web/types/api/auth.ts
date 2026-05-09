/**
 * 登录请求
 */
export interface LoginRequest {
  username: string;
  password: string;
  captchaId?: string;
  captchaCode?: string;
}

/**
 * 登录响应
 */
export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

/**
 * 修改密码请求
 */
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

/**
 * 验证码响应
 */
export interface CaptchaResponse {
  captchaId: string;
  captchaImage: string;
}
