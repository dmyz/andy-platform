<script setup lang="ts">
import { Key, Lock } from '@lucide/vue';
import { message } from 'antdv-next';
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import * as authApi from '@/api/auth';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 表单数据
const formData = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

// 加载状态
const loading = ref(false);

// 提交表单
async function handleSubmit() {
  // 验证原密码
  if (!formData.value.oldPassword) {
    message.warning('请输入原密码');
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

  // 验证新密码不能与原密码相同
  if (formData.value.newPassword === formData.value.oldPassword) {
    message.warning('新密码不能与原密码相同');
    return;
  }

  loading.value = true;
  try {
    await authApi.changePassword({
      oldPassword: formData.value.oldPassword,
      newPassword: formData.value.newPassword,
    });
    await userStore.getUserInfo();
    message.success('密码修改成功');
    const redirect = (route.query.redirect as string) || '/dashboard';
    router.push(redirect === '/first-time-password' ? '/dashboard' : redirect);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div
    class="min-h-screen bg-gradient-to-br from-[#0052d9] to-[#003a8c] flex items-center justify-center px-6"
  >
    <div class="w-full max-w-md">
      <a-card variant="borderless" class="auth-flow-card shadow-2xl">
        <!-- 提示说明区 -->
        <div class="mb-8">
          <div
            class="w-16 h-16 bg-[#0052d9] rounded-xl flex items-center justify-center mx-auto mb-4"
          >
            <Key class="w-8 h-8 text-white" />
          </div>
          <h2 class="text-2xl font-semibold text-[#1d2129] dark:text-white text-center">
            首次登录修改密码
          </h2>
          <p class="text-sm text-[#86909c] dark:text-gray-400 mt-2 text-center">
            为了您的账号安全,首次登录需要修改密码
          </p>
        </div>

        <!-- 提示信息 -->
        <a-alert type="warning" class="mb-6">
          <template #message>
            <div class="text-sm">
              <p class="font-medium mb-1">密码要求:</p>
              <ul class="list-disc list-inside space-y-1">
                <li>密码长度至少6位</li>
                <li>新密码不能与原密码相同</li>
                <li>建议使用字母、数字、特殊字符组合</li>
              </ul>
            </div>
          </template>
        </a-alert>

        <!-- 表单 -->
        <form class="space-y-5" @submit.prevent="handleSubmit">
          <!-- 原密码 -->
          <div>
            <label class="block text-sm text-[#4e5969] dark:text-gray-300 mb-2">原密码</label>
            <a-input
              v-model:value="formData.oldPassword"
              type="password"
              auto-complete="current-password"
              placeholder="请输入原密码"
              size="large"
            >
              <template #prefix>
                <Lock class="w-5 h-5 text-[#86909c] dark:text-gray-400" />
              </template>
            </a-input>
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
            >
              <template #prefix>
                <Key class="w-5 h-5 text-[#86909c] dark:text-gray-400" />
              </template>
            </a-input>
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
            >
              <template #prefix>
                <Key class="w-5 h-5 text-[#86909c] dark:text-gray-400" />
              </template>
            </a-input>
          </div>

          <!-- 提交按钮 -->
          <a-button type="primary" size="large" block html-type="submit" :loading="loading">
            确认修改
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
