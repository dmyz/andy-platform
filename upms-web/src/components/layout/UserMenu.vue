<script setup lang="ts">
import type { DropdownProps } from 'tdesign-vue-next'
import { ChevronDown, LogOut, User } from '@lucide/vue'
import { computed, h } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 用户信息
const userName = computed(() => userStore.userInfo?.displayName || userStore.userInfo?.username || '用户')

// 处理退出登录
function handleLogout() {
  userStore.logout()
}

// 处理菜单点击
function handleMenuClick(item: any) {
  if (item.value?.path) {
    router.push(item.value.path)
  }
  else if (item.value?.action === 'logout') {
    handleLogout()
  }
}

// 下拉菜单选项
const options = computed<DropdownProps['options']>(() => [
  {
    content: '个人中心',
    value: { path: '/profile' },
    prefixIcon: () => h(User, { class: 'h-4 w-4 text-[#4e5969]' }),
  },
  {
    content: '退出登录',
    value: { action: 'logout' },
    prefixIcon: () => h(LogOut, { class: 'h-4 w-4' }),
    theme: 'error',
  },
])
</script>

<template>
  <t-dropdown
    trigger="click"
    placement="bottom-right"
    :min-column-width="110"
    :options="options"
    @click="handleMenuClick"
  >
    <t-button
      theme="default"
      variant="text"
      size="medium"
    >
      <template #icon>
        <div class="flex items-center gap-2">
          <t-avatar size="small" :image="userStore.userInfo?.avatar">
            {{ userName.charAt(0) }}
          </t-avatar>
          <span class="text-sm font-medium text-[#1d2129] dark:text-white hidden md:inline-block">
            {{ userName }}
          </span>
          <ChevronDown class="h-4 w-4 text-[#86909c] dark:text-gray-400" />
        </div>
      </template>
    </t-button>
  </t-dropdown>
</template>
