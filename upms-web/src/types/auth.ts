export interface UserInfo {
  id: string
  username: string
  displayName: string
  avatar?: string
  roles: string[]
  passwordResetRequired?: boolean
}

export type NavigationType = 'GROUP' | 'PAGE' | 'LINK'

export interface NavigationItem {
  id: string
  parentId?: string
  navCode?: string
  name: string
  type: NavigationType
  routePath: string
  componentPath?: string
  icon?: string
  sortOrder: number
  externalUrl?: string
  children?: NavigationItem[]
}

export interface CurrentUserPayload {
  user: UserInfo
  permissions: string[]
  navigations: NavigationItem[]
}

export interface LoginResponsePayload {
  accessToken?: string
  tokenType?: string
  expiresIn?: number
  token?: string
}

export type LoginGrantType = 'PASSWORD' | 'MOBILE_CODE' | 'EMAIL_CODE'

export interface LoginPasswordPayload {
  grantType: 'PASSWORD'
  username: string
  password: string
}

export interface LoginMobilePayload {
  grantType: 'MOBILE_CODE'
  mobile: string
  code: string
}

export interface LoginEmailPayload {
  grantType: 'EMAIL_CODE'
  email: string
  code: string
}

export type LoginPayload = LoginPasswordPayload | LoginMobilePayload | LoginEmailPayload

export type VerificationCodeScene = 'LOGIN' | 'RESET_PASSWORD' | 'CHANGE_MOBILE' | 'CHANGE_EMAIL'
export type VerificationCodeTargetType = 'MOBILE' | 'EMAIL'

export interface VerificationCodeSendResponse {
  scene: VerificationCodeScene
  targetType: VerificationCodeTargetType
  maskedTarget: string
  expireSeconds: number
  devCode?: string | null
}

export interface OnlineSessionItem {
  sessionKey: string
  username: string
  displayName: string
  orgName?: string
  ip?: string
  authType: string
  status: string
  loginTime: string
  lastAccessTime?: string
  currentSession: boolean
}
