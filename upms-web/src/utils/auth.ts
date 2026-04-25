const ACCESS_TOKEN_STORAGE_KEY = 'ACCESS_TOKEN'
const LEGACY_TOKEN_STORAGE_KEY = 'X-Auth-Token'

export function getStoredAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY)
    || localStorage.getItem(LEGACY_TOKEN_STORAGE_KEY)
    || ''
}

export function setStoredAccessToken(token: string) {
  localStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, token)
  localStorage.removeItem(LEGACY_TOKEN_STORAGE_KEY)
}

export function clearStoredAccessToken() {
  localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY)
  localStorage.removeItem(LEGACY_TOKEN_STORAGE_KEY)
}

