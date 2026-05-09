<script setup lang="ts">
import { ArrowLeft, Mail, Phone } from '@lucide/vue';
import { message } from 'antdv-next';
import { onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { VerificationCodeSendResponse } from '@/types/auth';
import * as authApi from '@/api/auth';

const router = useRouter();

// 找回方式
const recoverMethod = ref<'phone' | 'email'>('phone');

// 表单数据
const formData = ref({
  account: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: '',
  captcha: '',
});

// 图形验证码
const captchaText = ref('ABCD');

// 验证码倒计时
const countdown = ref(0);
let timer: ReturnType<typeof setInterval> | null = null;

// 加载状态
const loading = ref(false);
const sendCodeLoading = ref(false);

// 刷新图形验证码
function refreshCaptcha() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789';
  let text = '';
  for (let i = 0; i < 4; i++) {
    text += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  captchaText.value = text;
  message.success('验证码已刷新');
}

// 发送验证码
async function sendVerifyCode() {
  if (recoverMethod.value === 'phone') {
    if (!formData.value.account || !/^1[3-9]\d{9}$/.test(formData.value.account)) {
      message.warning('请输入正确的手机号');
      return;
    }
  } else {
    if (
      !formData.value.account ||
      !/^[\w.-]+@[a-z0-9.-]+\.[a-z]{2,6}$/i.test(formData.value.account)
    ) {
      message.warning('请输入正确的邮箱地址');
      return;
    }
  }

  sendCodeLoading.value = true;
  try {
    const res =
      recoverMethod.value === 'phone'
        ? await authApi.sendMobileVerificationCode({
            mobile: formData.value.account,
            scene: 'RESET_PASSWORD',
          })
        : await authApi.sendEmailVerificationCode({
            email: formData.value.account,
            scene: 'RESET_PASSWORD',
          });
    message.success(
      res.devCode
        ? `验证码已发送至 ${res.maskedTarget}，开发验证码：${res.devCode}`
        : `验证码已发送至 ${res.maskedTarget}`,
    );
    countdown.value = 60;
    timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0 && timer) {
        clearInterval(timer);
        timer = null;
      }
    }, 1000);
  } finally {
    sendCodeLoading.value = false;
  }
}

// 提交表单
async function handleSubmit() {
  // 验证图形验证码
  if (!formData.value.captcha) {
    message.warning('请输入图形验证码');
    return;
  }

  if (formData.value.captcha.toUpperCase() !== captchaText.value.toUpperCase()) {
    message.error('图形验证码错误');
    refreshCaptcha();
    return;
  }

  // 验证账号
  if (!formData.value.account) {
    message.warning(recoverMethod.value === 'phone' ? '请输入手机号' : '请输入邮箱');
    return;
  }

  // 验证验证码
  if (!formData.value.verifyCode) {
    message.warning('请输入验证码');
    return;
  }

  // 验证新密码
  if (!formData.value.newPassword || formData.value.newPassword.length < 6) {
    message.warning('新密码长度不能少于6位');
    return;
  }

  // 验证确认密码
  if (formData.value.newPassword !== formData.value.confirmPassword) {
    message.warning('两次输入的密码不一致');
    return;
  }

  loading.value = true;
  try {
    await authApi.resetPasswordWithCode({
      targetType: recoverMethod.value === 'phone' ? 'MOBILE' : 'EMAIL',
      target: formData.value.account,
      code: formData.value.verifyCode,
      newPassword: formData.value.newPassword,
    });
    message.success('密码重置成功,请使用新密码登录');
    router.push('/login');
  } finally {
    loading.value = false;
  }
}

// 返回登录页
function goBack() {
  router.push('/login');
}

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<template>
  <div
    class="min-h-screen bg-gradient-to-br from-[#0052d9] to-[#003a8c] flex items-center justify-center px-6"
  >
    <div class="w-full max-w-md">
      <a-card variant="borderless" class="auth-flow-card shadow-2xl">
        <!-- 返回按钮 -->
        <div class="mb-6">
          <a-button variant="text" @click="goBack">
            <template #icon>
              <ArrowLeft class="w-4 h-4" />
            </template>
            返回登录
          </a-button>
        </div>

        <!-- 标题 -->
        <div class="text-center mb-8">
          <h2 class="text-2xl font-semibold text-[#1d2129] dark:text-white">找回密码</h2>
          <p class="text-sm text-[#86909c] dark:text-gray-400 mt-2">通过手机号或邮箱重置您的密码</p>
        </div>

        <!-- 找回方式选择 -->
        <div class="mb-6">
          <a-radio-group v-model:value="recoverMethod">
            <a-radio value="phone">
              <div class="flex items-center gap-2">
                <Phone class="w-4 h-4" />
                <span>手机号找回</span>
              </div>
            </a-radio>
            <a-radio value="email">
              <div class="flex items-center gap-2">
                <Mail class="w-4 h-4" />
                <span>邮箱找回</span>
              </div>
            </a-radio>
          </a-radio-group>
        </div>

        <!-- 表单 -->
        <form class="space-y-5" @submit.prevent="handleSubmit">
          <!-- 手机号/邮箱 -->
          <div>
            <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">
              {{ recoverMethod === 'phone' ? '手机号' : '邮箱' }}
            </label>
            <a-input
              v-model:value="formData.account"
              :placeholder="recoverMethod === 'phone' ? '请输入手机号' : '请输入邮箱地址'"
              size="large"
              :maxlength="recoverMethod === 'phone' ? 11 : 50"
              allow-clear
            >
              <template #prefix>
                <Phone
                  v-if="recoverMethod === 'phone'"
                  class="w-5 h-5 text-[#86909c] dark:text-gray-400"
                />
                <Mail v-else class="w-5 h-5 text-[#86909c] dark:text-gray-400" />
              </template>
            </a-input>
          </div>

          <!-- 验证码 -->
          <div>
            <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">验证码</label>
            <div class="flex gap-3">
              <a-input
                v-model:value="formData.verifyCode"
                placeholder="请输入验证码"
                size="large"
                :maxlength="6"
                class="flex-1"
              />
              <a-button
                type="primary"
                variant="outlined"
                size="large"
                html-type="button"
                :disabled="countdown > 0"
                :loading="sendCodeLoading"
                @click="sendVerifyCode"
              >
                {{ countdown > 0 ? `${countdown}秒` : '发送验证码' }}
              </a-button>
            </div>
          </div>

          <!-- 图形验证码 -->
          <div>
            <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">图形验证码</label>
            <div class="flex gap-3">
              <a-input
                v-model:value="formData.captcha"
                placeholder="请输入验证码"
                size="large"
                :maxlength="4"
                class="flex-1"
              />
              <div
                class="w-[120px] h-[40px] rounded border border-[#e7e7e7] dark:border-gray-600 overflow-hidden cursor-pointer flex items-center justify-center bg-gray-50 dark:bg-gray-700"
                :title="captchaText"
                @click="refreshCaptcha"
              >
                <span class="text-lg font-bold tracking-wider text-[#0052d9]">{{
                  captchaText
                }}</span>
              </div>
            </div>
          </div>

          <!-- 新密码 -->
          <div>
            <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">新密码</label>
            <a-input
              v-model:value="formData.newPassword"
              type="password"
              auto-complete="new-password"
              placeholder="请输入新密码(至少6位)"
              size="large"
            />
          </div>

          <!-- 确认密码 -->
          <div>
            <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">确认密码</label>
            <a-input
              v-model:value="formData.confirmPassword"
              type="password"
              auto-complete="new-password"
              placeholder="请再次输入新密码"
              size="large"
            />
          </div>

          <!-- 提交按钮 -->
          <a-button type="primary" size="large" block html-type="submit" :loading="loading">
            重置密码
          </a-button>
        </form>
      </a-card>
    </div>
  </div>
</template>

<style scoped>
.auth-flow-card :deep(.ant-card-body) {
  padding: 32px;
}
</style>
