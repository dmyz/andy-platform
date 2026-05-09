<script setup lang="ts">
import { Eye, EyeOff, Lock, Mail, Phone, User } from '@lucide/vue';
import { message } from 'antdv-next';
import { onUnmounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import * as authApi from '@/api/auth';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 登录方式: account-账号密码, phone-手机验证码, email-邮箱验证码
const loginType = ref<'account' | 'phone' | 'email'>('account');

// 账号密码登录表单
const accountForm = ref({
  username: '',
  password: '',
  remember: false,
});

// 手机验证码登录表单
const phoneForm = ref({
  phone: '',
  code: '',
});

// 邮箱验证码登录表单
const emailForm = ref({
  email: '',
  code: '',
});

// 密码显示切换
const showPassword = ref(false);

// 加载状态
const loading = ref(false);

// 验证码倒计时
const countdown = reactive({
  phone: 0,
  email: 0,
});
const sendCodeLoading = reactive({
  phone: false,
  email: false,
});
const timers: Record<'phone' | 'email', ReturnType<typeof setInterval> | null> = {
  phone: null,
  email: null,
};

function startCountdown(type: 'phone' | 'email') {
  if (timers[type]) {
    clearInterval(timers[type]!);
  }
  countdown[type] = 60;
  timers[type] = setInterval(() => {
    countdown[type]--;
    if (countdown[type] <= 0 && timers[type]) {
      clearInterval(timers[type]!);
      timers[type] = null;
    }
  }, 1000);
}

// 发送验证码
async function sendCode(type: 'phone' | 'email') {
  if (
    type === 'phone' &&
    (!phoneForm.value.phone || !/^1[3-9]\d{9}$/.test(phoneForm.value.phone))
  ) {
    message.warning('请输入正确的手机号');
    return;
  }

  if (
    type === 'email' &&
    (!emailForm.value.email || !/^[\w.-]+@[a-z0-9.-]+\.[a-z]{2,6}$/i.test(emailForm.value.email))
  ) {
    message.warning('请输入正确的邮箱地址');
    return;
  }

  sendCodeLoading[type] = true;
  try {
    const res =
      type === 'phone'
        ? await authApi.sendMobileVerificationCode({
            mobile: phoneForm.value.phone,
            scene: 'LOGIN',
          })
        : await authApi.sendEmailVerificationCode({
            email: emailForm.value.email,
            scene: 'LOGIN',
          });
    message.success(
      res.devCode
        ? `验证码已发送至 ${res.maskedTarget}，开发验证码：${res.devCode}`
        : `验证码已发送至 ${res.maskedTarget}`,
    );
    startCountdown(type);
  } finally {
    sendCodeLoading[type] = false;
  }
}

// 登录处理
async function handleLogin() {
  loading.value = true;
  try {
    let success = false;
    if (loginType.value === 'account') {
      if (!accountForm.value.username || !accountForm.value.password) {
        message.warning('请输入用户名和密码');
        return;
      }
      success = await userStore.login({
        grantType: 'PASSWORD',
        username: accountForm.value.username,
        password: accountForm.value.password,
      });
    } else if (loginType.value === 'phone') {
      if (!phoneForm.value.phone || !phoneForm.value.code) {
        message.warning('请输入手机号和验证码');
        return;
      }
      success = await userStore.login({
        grantType: 'MOBILE_CODE',
        mobile: phoneForm.value.phone,
        code: phoneForm.value.code,
      });
    } else {
      if (!emailForm.value.email || !emailForm.value.code) {
        message.warning('请输入邮箱和验证码');
        return;
      }
      success = await userStore.login({
        grantType: 'EMAIL_CODE',
        email: emailForm.value.email,
        code: emailForm.value.code,
      });
    }

    if (success) {
      const redirect = (route.query.redirect as string) || '/';
      if (userStore.userInfo?.passwordResetRequired) {
        router.push(`/first-time-password?redirect=${encodeURIComponent(redirect)}`);
      } else {
        router.push(redirect);
      }
    }
  } finally {
    loading.value = false;
  }
}

// 跳转到找回密码页
function goToForgotPassword() {
  router.push('/forgot-password');
}

onUnmounted(() => {
  if (timers.phone) {
    clearInterval(timers.phone);
  }
  if (timers.email) {
    clearInterval(timers.email);
  }
});
</script>

<template>
  <div class="login-container">
    <!-- 动态背景 -->
    <div class="login-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="grid-overlay"></div>
    </div>

    <!-- 左上角 Logo -->
    <div class="logo-section">
      <div class="logo-icon">
        <span>中</span>
      </div>
      <div class="logo-text">
        <h1>中台管理系统</h1>
        <p>Enterprise Management Platform</p>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="content-wrapper">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-title">
            <h1 class="title-line-1">企业级</h1>
            <h1 class="title-line-2">中后台管理系统</h1>
          </div>
          <p class="brand-subtitle">基于 Vue 3 + Antdv Next 构建的现代化企业级管理平台</p>

          <!-- 特性卡片 -->
          <div class="features-grid">
            <div class="feature-card" style="animation-delay: 0.1s">
              <div class="feature-icon">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                  />
                </svg>
              </div>
              <div class="feature-text">
                <h3>安全可靠</h3>
                <p>企业级安全保障</p>
              </div>
            </div>

            <div class="feature-card" style="animation-delay: 0.2s">
              <div class="feature-icon">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M13 10V3L4 14h7v7l9-11h-7z"
                  />
                </svg>
              </div>
              <div class="feature-text">
                <h3>高效快速</h3>
                <p>极致性能体验</p>
              </div>
            </div>

            <div class="feature-card" style="animation-delay: 0.3s">
              <div class="feature-icon">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"
                  />
                </svg>
              </div>
              <div class="feature-text">
                <h3>灵活配置</h3>
                <p>自定义权限管理</p>
              </div>
            </div>

            <div class="feature-card" style="animation-delay: 0.4s">
              <div class="feature-icon">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01"
                  />
                </svg>
              </div>
              <div class="feature-text">
                <h3>易于扩展</h3>
                <p>模块化架构设计</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-section">
        <div class="login-card">
          <!-- Tab 切换 -->
          <div class="login-tabs">
            <button
              type="button"
              :class="['tab-button', { active: loginType === 'account' }]"
              @click="loginType = 'account'"
            >
              账号登录
            </button>
            <button
              type="button"
              :class="['tab-button', { active: loginType === 'phone' }]"
              @click="loginType = 'phone'"
            >
              手机号登录
            </button>
            <button
              type="button"
              :class="['tab-button', { active: loginType === 'email' }]"
              @click="loginType = 'email'"
            >
              邮箱登录
            </button>
          </div>

          <!-- 表单容器 -->
          <div class="form-container">
            <!-- 账号密码登录 -->
            <form v-show="loginType === 'account'" class="login-form" @submit.prevent="handleLogin">
              <div class="form-group">
                <label class="form-label">用户名</label>
                <a-input
                  v-model:value="accountForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  allow-clear
                  class="form-input"
                >
                  <template #prefix>
                    <User class="w-5 h-5 text-gray-400" />
                  </template>
                </a-input>
              </div>

              <div class="form-group">
                <label class="form-label">密码</label>
                <a-input
                  v-model:value="accountForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  size="large"
                  class="form-input"
                >
                  <template #prefix>
                    <Lock class="w-5 h-5 text-gray-400" />
                  </template>
                  <template #suffix>
                    <button
                      type="button"
                      class="password-toggle"
                      @click="showPassword = !showPassword"
                    >
                      <Eye v-if="!showPassword" class="w-5 h-5" />
                      <EyeOff v-else class="w-5 h-5" />
                    </button>
                  </template>
                </a-input>
              </div>

              <div class="form-footer">
                <a-checkbox v-model:checked="accountForm.remember">
                  <span class="remember-text">记住我</span>
                </a-checkbox>
                <a-button type="link" size="small" class="px-0" @click="goToForgotPassword">
                  找回密码
                </a-button>
              </div>

              <a-button
                type="primary"
                size="large"
                block
                html-type="submit"
                :loading="loading"
                class="submit-button"
              >
                登录
              </a-button>
            </form>

            <!-- 手机验证码登录 -->
            <form v-show="loginType === 'phone'" class="login-form" @submit.prevent="handleLogin">
              <div class="form-group">
                <label class="form-label">手机号</label>
                <a-input
                  v-model:value="phoneForm.phone"
                  placeholder="请输入手机号"
                  size="large"
                  :maxlength="11"
                  allow-clear
                  class="form-input"
                >
                  <template #prefix>
                    <Phone class="w-5 h-5 text-gray-400" />
                  </template>
                </a-input>
              </div>

              <div class="form-group">
                <label class="form-label">验证码</label>
                <div class="code-input-group">
                  <a-input
                    v-model:value="phoneForm.code"
                    placeholder="请输入验证码"
                    size="large"
                    :maxlength="6"
                    class="form-input flex-1"
                  >
                    <template #prefix>
                      <Lock class="w-5 h-5 text-gray-400" />
                    </template>
                  </a-input>
                  <a-button
                    type="primary"
                    variant="outlined"
                    size="large"
                    html-type="button"
                    :disabled="countdown.phone > 0"
                    :loading="sendCodeLoading.phone"
                    class="code-button"
                    @click="sendCode('phone')"
                  >
                    {{ countdown.phone > 0 ? `${countdown.phone}秒` : '发送验证码' }}
                  </a-button>
                </div>
              </div>

              <a-button
                type="primary"
                size="large"
                block
                html-type="submit"
                :loading="loading"
                class="submit-button"
              >
                登录
              </a-button>
            </form>

            <!-- 邮箱验证码登录 -->
            <form v-show="loginType === 'email'" class="login-form" @submit.prevent="handleLogin">
              <div class="form-group">
                <label class="form-label">邮箱</label>
                <a-input
                  v-model:value="emailForm.email"
                  placeholder="请输入邮箱地址"
                  size="large"
                  allow-clear
                  class="form-input"
                >
                  <template #prefix>
                    <Mail class="w-5 h-5 text-gray-400" />
                  </template>
                </a-input>
              </div>

              <div class="form-group">
                <label class="form-label">验证码</label>
                <div class="code-input-group">
                  <a-input
                    v-model:value="emailForm.code"
                    placeholder="请输入验证码"
                    size="large"
                    :maxlength="6"
                    class="form-input flex-1"
                  >
                    <template #prefix>
                      <Lock class="w-5 h-5 text-gray-400" />
                    </template>
                  </a-input>
                  <a-button
                    type="primary"
                    variant="outlined"
                    size="large"
                    html-type="button"
                    :disabled="countdown.email > 0"
                    :loading="sendCodeLoading.email"
                    class="code-button"
                    @click="sendCode('email')"
                  >
                    {{ countdown.email > 0 ? `${countdown.email}秒` : '发送验证码' }}
                  </a-button>
                </div>
              </div>

              <a-button
                type="primary"
                size="large"
                block
                html-type="submit"
                :loading="loading"
                class="submit-button"
              >
                登录
              </a-button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== 容器布局 ========== */
.login-container {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', sans-serif;
}

/* ========== 纯色背景 ========== */
.login-bg {
  position: absolute;
  inset: 0;
  background: #0052d9;
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.15;
  animation: float 10s ease-in-out infinite;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: rgba(255, 255, 255, 0.3);
  top: -15%;
  left: -10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: rgba(255, 255, 255, 0.2);
  bottom: -15%;
  right: -10%;
  animation-delay: 3s;
}

.orb-3 {
  width: 450px;
  height: 450px;
  background: rgba(255, 255, 255, 0.25);
  top: 50%;
  left: 50%;
  animation-delay: 6s;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(20px, -20px);
  }
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  opacity: 0.5;
}

/* ========== Logo 区域 ========== */
.logo-section {
  position: absolute;
  top: 2rem;
  left: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  z-index: 10;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.logo-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.logo-icon span {
  font-size: 1.75rem;
  font-weight: 600;
  color: white;
}

.logo-text h1 {
  font-size: 1.125rem;
  font-weight: 600;
  color: white;
  margin: 0;
}

.logo-text p {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  font-weight: 400;
}

/* ========== 主内容区 ========== */
.content-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: 100vh;
}

/* ========== 左侧品牌区 ========== */
.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  color: white;
}

@media (max-width: 1024px) {
  .brand-section {
    display: none;
  }
}

.brand-content {
  max-width: 560px;
  width: 100%;
}

.brand-title {
  margin-bottom: 1.5rem;
}

.title-line-1,
.title-line-2 {
  font-size: 3.5rem;
  font-weight: 700;
  line-height: 1.2;
  margin: 0;
  color: white;
}

.brand-subtitle {
  font-size: 1.125rem;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.7;
  margin-bottom: 2.5rem;
  font-weight: 400;
}

/* ========== 特性卡片 ========== */
.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem;
}

.feature-card {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.25rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.feature-card:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.feature-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-text h3 {
  font-size: 0.9375rem;
  font-weight: 600;
  margin: 0 0 0.25rem 0;
  color: white;
}

.feature-text p {
  font-size: 0.8125rem;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

/* ========== 右侧登录区 ========== */
.login-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.login-card {
  width: 100%;
  max-width: 440px;
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

/* ========== Tab 切换 ========== */
.login-tabs {
  display: flex;
  gap: 0.5rem;
  background: #f3f4f6;
  border-radius: 10px;
  padding: 0.25rem;
  margin-bottom: 2rem;
}

.tab-button {
  flex: 1;
  padding: 0.75rem 0.875rem;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-button:hover {
  background: rgba(0, 82, 217, 0.05);
  color: #0052d9;
}

.tab-button.active {
  background: white;
  color: #0052d9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* ========== 表单样式 ========== */
.form-container {
  min-height: 340px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.form-input :deep(.ant-input),
.form-input :deep(.ant-input-affix-wrapper) {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.form-input :deep(.ant-input:hover),
.form-input :deep(.ant-input-affix-wrapper:hover) {
  border-color: #0052d9;
  background: white;
}

.form-input :deep(.ant-input:focus),
.form-input :deep(.ant-input-affix-wrapper-focused) {
  border-color: #0052d9;
  background: white;
  box-shadow: 0 0 0 3px rgba(0, 82, 217, 0.1);
}

.form-input :deep(.ant-input) {
  font-size: 0.9375rem;
  color: #1f2937;
}

.form-input :deep(.ant-input::placeholder) {
  color: #9ca3af;
}

.password-toggle {
  background: none;
  border: none;
  cursor: pointer;
  color: #9ca3af;
  transition: color 0.2s ease;
  padding: 0;
  display: flex;
  align-items: center;
}

.password-toggle:hover {
  color: #0052d9;
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 0.25rem;
}

.remember-text {
  font-size: 0.875rem;
  color: #6b7280;
}

.code-input-group {
  display: flex;
  gap: 0.75rem;
}

.code-button {
  min-width: 110px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.875rem;
}

.submit-button {
  height: 44px;
  border-radius: 8px;
  font-size: 0.9375rem;
  font-weight: 600;
  margin-top: 0.5rem;
  background: #0052d9;
  border: none;
  transition: all 0.2s ease;
}

.submit-button:hover {
  background: #0041b3;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 82, 217, 0.3);
}

.submit-button:active {
  transform: translateY(0);
}

/* ========== 覆盖浏览器自动填充 ========== */
:deep(.ant-input:-webkit-autofill),
:deep(.ant-input:-webkit-autofill:hover),
:deep(.ant-input:-webkit-autofill:focus),
:deep(.ant-input:-webkit-autofill:active) {
  -webkit-box-shadow: 0 0 0 1000px #f9fafb inset !important;
  box-shadow: 0 0 0 1000px #f9fafb inset !important;
  -webkit-text-fill-color: #1f2937 !important;
}

/* ========== 响应式设计 ========== */
@media (max-width: 1024px) {
  .login-section {
    flex: none;
    width: 100%;
  }
}

@media (max-width: 640px) {
  .logo-section {
    top: 1rem;
    left: 1rem;
  }

  .logo-icon {
    width: 48px;
    height: 48px;
  }

  .logo-icon span {
    font-size: 1.5rem;
  }

  .logo-text h1 {
    font-size: 1rem;
  }

  .logo-text p {
    font-size: 0.625rem;
  }

  .login-card {
    padding: 2rem 1.5rem;
  }

  .title-line-1,
  .title-line-2 {
    font-size: 2.5rem;
  }
}
</style>
