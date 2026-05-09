import { readonly, ref } from 'vue';

const ACCESS_TOKEN_STORAGE_KEY = 'ACCESS_TOKEN';
const LEGACY_TOKEN_STORAGE_KEY = 'X-Auth-Token';

function readStoredAccessToken() {
  return (
    localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY) ||
    localStorage.getItem(LEGACY_TOKEN_STORAGE_KEY) ||
    ''
  );
}

const accessToken = ref<string>(readStoredAccessToken());

export const storedAccessToken = readonly(accessToken);

export function getStoredAccessToken() {
  const latestToken = readStoredAccessToken();
  if (latestToken !== accessToken.value) {
    accessToken.value = latestToken;
  }
  return accessToken.value;
}

export function setStoredAccessToken(token: string) {
  accessToken.value = token;
  localStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, token);
  localStorage.removeItem(LEGACY_TOKEN_STORAGE_KEY);
}

export function clearStoredAccessToken() {
  accessToken.value = '';
  localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY);
  localStorage.removeItem(LEGACY_TOKEN_STORAGE_KEY);
}
