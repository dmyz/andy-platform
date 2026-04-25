<script setup lang="ts">
import { Eye, EyeOff, Lock, Mail, Phone, User } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { VerificationCodeSendResponse } from '@/types/auth'
import { useUserStore } from '@/stores/user'
import * as authApi from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 登录方式: account-账号密码, phone-手机验证码, email-邮箱验证码
const loginType = ref<'account' | 'phone' | 'email'>('account')

// 账号密码登录表单
const accountForm = ref({
  username: '',
  password: '',
  captcha: '',
  remember: false,
})

// 手机验证码登录表单
const phoneForm = ref({
  phone: '',
  code: '',
  captcha: '',
})

// 邮箱验证码登录表单
const emailForm = ref({
  email: '',
  code: '',
  captcha: '',
})

// 图形验证码
const captchaText = ref('ABCD')

// 密码显示切换
const showPassword = ref(false)

// 加载状态
const loading = ref(false)

// 验证码倒计时
const countdown = reactive({
  phone: 0,
  email: 0,
})
const sendCodeLoading = reactive({
  phone: false,
  email: false,
})
const timers: Record<'phone' | 'email', ReturnType<typeof setInterval> | null> = {
  phone: null,
  email: null,
}

// 刷新图形验证码
function refreshCaptcha() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'
  let text = ''
  for (let i = 0; i < 4; i++) {
    text += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  captchaText.value = text
  MessagePlugin.success('验证码已刷新')
}

function startCountdown(type: 'phone' | 'email') {
  if (timers[type]) {
    clearInterval(timers[type]!)
  }
  countdown[type] = 60
  timers[type] = setInterval(() => {
    countdown[type]--
    if (countdown[type] <= 0 && timers[type]) {
      clearInterval(timers[type]!)
      timers[type] = null
    }
  }, 1000)
}

// 发送验证码
async function sendCode(type: 'phone' | 'email') {
  if (type === 'phone' && (!phoneForm.value.phone || !/^1[3-9]\d{9}$/.test(phoneForm.value.phone))) {
    MessagePlugin.warning('请输入正确的手机号')
    return
  }

  if (type === 'email' && (!emailForm.value.email || !/^[\w.-]+@[a-z0-9.-]+\.[a-z]{2,6}$/i.test(emailForm.value.email))) {
    MessagePlugin.warning('请输入正确的邮箱地址')
    return
  }

  sendCodeLoading[type] = true
  try {
    const res = type === 'phone'
      ? await authApi.sendMobileVerificationCode({
        mobile: phoneForm.value.phone,
        scene: 'LOGIN',
      })
      : await authApi.sendEmailVerificationCode({
        email: emailForm.value.email,
        scene: 'LOGIN',
      })
    MessagePlugin.success(res.data.devCode ? `验证码已发送至 ${res.data.maskedTarget}，开发验证码：${res.data.devCode}` : `验证码已发送至 ${res.data.maskedTarget}`)
    startCountdown(type)
  }
  finally {
    sendCodeLoading[type] = false
  }
}

// 登录处理
async function handleLogin() {
  // 验证图形验证码
  const captcha = loginType.value === 'account'
    ? accountForm.value.captcha
    : loginType.value === 'phone'
      ? phoneForm.value.captcha
      : emailForm.value.captcha

  if (!captcha) {
    MessagePlugin.warning('请输入图形验证码')
    return
  }

  if (captcha.toUpperCase() !== captchaText.value.toUpperCase()) {
    MessagePlugin.error('图形验证码错误')
    refreshCaptcha()
    return
  }

  loading.value = true
  try {
    let success = false
    if (loginType.value === 'account') {
      if (!accountForm.value.username || !accountForm.value.password) {
        MessagePlugin.warning('请输入用户名和密码')
        return
      }
      success = await userStore.login({
        grantType: 'PASSWORD',
        username: accountForm.value.username,
        password: accountForm.value.password,
      })
    }
    else if (loginType.value === 'phone') {
      if (!phoneForm.value.phone || !phoneForm.value.code) {
        MessagePlugin.warning('请输入手机号和验证码')
        return
      }
      success = await userStore.login({
        grantType: 'MOBILE_CODE',
        mobile: phoneForm.value.phone,
        code: phoneForm.value.code,
      })
    }
    else {
      if (!emailForm.value.email || !emailForm.value.code) {
        MessagePlugin.warning('请输入邮箱和验证码')
        return
      }
      success = await userStore.login({
        grantType: 'EMAIL_CODE',
        email: emailForm.value.email,
        code: emailForm.value.code,
      })
    }

    if (success) {
      const redirect = (route.query.redirect as string) || '/'
      if (userStore.userInfo?.passwordResetRequired) {
        router.push(`/first-time-password?redirect=${encodeURIComponent(redirect)}`)
      }
      else {
        router.push(redirect)
      }
    }
  }
  finally {
    loading.value = false
  }
}

// 跳转到找回密码页
function goToForgotPassword() {
  router.push('/forgot-password')
}

onUnmounted(() => {
  if (timers.phone) {
    clearInterval(timers.phone)
  }
  if (timers.email) {
    clearInterval(timers.email)
  }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-[#0052d9] to-[#003a8c] flex">
    <!-- 左侧品牌宣传区 -->
    <div class="hidden lg:flex flex-1 flex-col justify-center items-center px-16 text-white">
      <div class="max-w-lg">
        <div class="w-20 h-20 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center mb-8">
          <span class="text-4xl font-bold">中</span>
        </div>
        <h1 class="text-4xl font-bold mb-6">
          企业级中后台管理系统
        </h1>
        <p class="text-lg text-white/80 mb-8 leading-relaxed">
          基于Vue3 + TDesign构建的现代化企业级管理平台，提供完善的权限管理、组织架构、用户管理等功能，助力企业数字化转型。
        </p>
        <div class="flex gap-8">
          <div>
            <div class="text-3xl font-bold">
              100+
            </div>
            <div class="text-sm text-white/60 mt-1">
              企业用户
            </div>
          </div>
          <div>
            <div class="text-3xl font-bold">
              50+
            </div>
            <div class="text-sm text-white/60 mt-1">
              功能模块
            </div>
          </div>
          <div>
            <div class="text-3xl font-bold">
              99.9%
            </div>
            <div class="text-sm text-white/60 mt-1">
              系统可用性
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录卡片区 -->
    <div class="flex-1 flex items-center justify-center px-6 py-12">
      <div class="w-full max-w-md">
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl p-8">
          <!-- 系统名称 -->
          <div class="text-center mb-8">
            <div class="w-16 h-16 bg-[#0052d9] rounded-xl flex items-center justify-center mx-auto mb-4">
              <span class="text-white text-2xl font-bold">中</span>
            </div>
            <h2 class="text-2xl font-semibold text-[#1d2129] dark:text-white">
              中台管理系统
            </h2>
            <p class="text-sm text-[#86909c] dark:text-gray-400 mt-2">
              登录您的账户继续使用
            </p>
          </div>

          <!-- 登录方式切换Tab -->
          <t-tabs v-model="loginType" class="mb-6">
            <t-tab-panel value="account" label="账号密码登录" />
            <t-tab-panel value="phone" label="手机验证码登录" />
            <t-tab-panel value="email" label="邮箱验证码登录" />
          </t-tabs>

          <!-- 账号密码登录表单 -->
          <form v-show="loginType === 'account'" class="space-y-5" @submit.prevent="handleLogin">
            <div>
              <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">用户名</label>
              <t-input v-model="accountForm.username" placeholder="请输入用户名(admin)" size="large" clearable>
                <template #prefix-icon>
                  <User class="w-5 h-5 text-[#86909c] dark:text-gray-400" />
                </template>
              </t-input>
            </div>

            <div>
              <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">密码</label>
              <t-input v-model="accountForm.password" :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码(admin)" size="large">
                <template #prefix-icon>
                  <Lock class="w-5 h-5 text-[#86909c] dark:text-gray-400" />
                </template>
                <template #suffix-icon>
                  <button type="button" class="focus:outline-none" @click="showPassword = !showPassword">
                    <Eye v-if="!showPassword" class="w-5 h-5 text-[#86909c] dark:text-gray-400" />
                    <EyeOff v-else class="w-5 h-5 text-[#86909c] dark:text-gray-400" />
                  </button>
                </template>
              </t-input>
            </div>

            <div>
              <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">图形验证码</label>
              <div class="flex gap-3">
                <t-input v-model="accountForm.captcha" placeholder="请输入验证码" size="large" maxlength="4" class="flex-1" />
                <div
                  class="w-[120px] h-[40px] rounded border border-[#e7e7e7] dark:border-gray-600 overflow-hidden cursor-pointer flex items-center justify-center bg-gray-50 dark:bg-gray-700"
                  :title="captchaText" @click="refreshCaptcha">
                  <span class="text-lg font-bold tracking-wider text-[#0052d9]">{{ captchaText }}</span>
                </div>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <t-checkbox v-model="accountForm.remember">
                <span class="text-sm text-[#4e5969] dark:text-gray-300">记住我</span>
              </t-checkbox>
              <t-link theme="primary" size="small" @click="goToForgotPassword">
                找回密码
              </t-link>
            </div>

            <t-button theme="primary" size="large" block type="submit" :loading="loading">
              登录
            </t-button>
          </form>

          <!-- 手机验证码登录表单 -->
          <form v-show="loginType === 'phone'" class="space-y-5" @submit.prevent="handleLogin">
            <div>
              <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">手机号</label>
              <t-input v-model="phoneForm.phone" placeholder="请输入手机号(13800138000)" size="large" maxlength="11"
                clearable>
                <template #prefix-icon>
                  <Phone class="w-5 h-5 text-[#86909c] dark:text-gray-400" />
                </template>
              </t-input>
            </div>

            <div>
              <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">手机验证码</label>
              <div class="flex gap-3">
                <t-input v-model="phoneForm.code" placeholder="请输入验证码" size="large" maxlength="6" class="flex-1" />
                <t-button theme="primary" variant="outline" size="large" type="button" :disabled="countdown.phone > 0"
                  :loading="sendCodeLoading.phone" @click="sendCode('phone')">
                  {{ countdown.phone > 0 ? `${countdown.phone}秒` : '发送验证码' }}
                </t-button>
              </div>
            </div>

            <div>
              <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">图形验证码</label>
              <div class="flex gap-3">
                <t-input v-model="phoneForm.captcha" placeholder="请输入验证码" size="large" maxlength="4" class="flex-1" />
                <div
                  class="w-[120px] h-[40px] rounded border border-[#e7e7e7] dark:border-gray-600 overflow-hidden cursor-pointer flex items-center justify-center bg-gray-50 dark:bg-gray-700"
                  :title="captchaText" @click="refreshCaptcha">
                  <span class="text-lg font-bold tracking-wider text-[#0052d9]">{{ captchaText }}</span>
                </div>
              </div>
            </div>

            <t-button theme="primary" size="large" block type="submit" :loading="loading">
              登录
            </t-button>
          </form>

          <!-- 邮箱验证码登录表单 -->
          <form v-show="loginType === 'email'" class="space-y-5" @submit.prevent="handleLogin">
            <div>
              <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">邮箱</label>
              <t-input v-model="emailForm.email" placeholder="请输入邮箱地址(admin@example.com)" size="large" clearable>
                <template #prefix-icon>
                  <Mail class="w-5 h-5 text-[#86909c] dark:text-gray-400" />
                </template>
              </t-input>
            </div>

            <div>
              <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">邮箱验证码</label>
              <div class="flex gap-3">
                <t-input v-model="emailForm.code" placeholder="请输入验证码" size="large" maxlength="6" class="flex-1" />
                <t-button theme="primary" variant="outline" size="large" type="button" :disabled="countdown.email > 0"
                  :loading="sendCodeLoading.email" @click="sendCode('email')">
                  {{ countdown.email > 0 ? `${countdown.email}秒` : '发送验证码' }}
                </t-button>
              </div>
            </div>

            <div>
              <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">图形验证码</label>
              <div class="flex gap-3">
                <t-input v-model="emailForm.captcha" placeholder="请输入验证码" size="large" maxlength="4" class="flex-1" />
                <div
                  class="w-[120px] h-[40px] rounded border border-[#e7e7e7] dark:border-gray-600 overflow-hidden cursor-pointer flex items-center justify-center bg-gray-50 dark:bg-gray-700"
                  :title="captchaText" @click="refreshCaptcha">
                  <span class="text-lg font-bold tracking-wider text-[#0052d9]">{{ captchaText }}</span>
                </div>
              </div>
            </div>

            <t-button theme="primary" size="large" block type="submit" :loading="loading">
              登录
            </t-button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
