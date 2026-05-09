<script setup lang="ts">
import type { MenuProps } from 'antdv-next';
import { ChevronDown, LogOut, User } from '@lucide/vue';
import { computed, h } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

// 用户信息
const userName = computed(
  () => userStore.userInfo?.displayName || userStore.userInfo?.username || '用户',
);

// 处理退出登录
function handleLogout() {
  userStore.logout();
}

// 处理菜单点击
function handleMenuClick({ key }: { key: string }) {
  if (key === 'profile') {
    router.push('/profile');
  } else if (key === 'logout') {
    handleLogout();
  }
}

// 下拉菜单选项
const menuItems = computed<MenuProps['items']>(() => [
  {
    key: 'profile',
    label: '个人中心',
    icon: () => h(User, { class: 'h-4 w-4 text-[#4e5969]' }),
  },
  {
    key: 'logout',
    label: '退出登录',
    icon: () => h(LogOut, { class: 'h-4 w-4' }),
    danger: true,
  },
]);
</script>

<template>
  <a-dropdown
    :trigger="['click']"
    placement="bottomRight"
    :menu="{ items: menuItems, onClick: handleMenuClick }"
  >
    <a-button type="text" size="middle" class="user-menu-button">
      <div class="flex items-center gap-2 px-0.5">
        <a-avatar size="small" :image="userStore.userInfo?.avatar">
          {{ userName.charAt(0) }}
        </a-avatar>
        <span class="hidden text-sm font-medium text-[#1d2129] dark:text-white md:inline-block">
          {{ userName }}
        </span>
        <ChevronDown class="h-4 w-4 text-[#86909c] dark:text-gray-400" />
      </div>
    </a-button>
  </a-dropdown>
</template>

<style scoped>
.user-menu-button {
  border-radius: 4px;
}

.user-menu-button:hover {
  background: #f2f5f9;
}

.dark .user-menu-button:hover {
  background: rgba(255, 255, 255, 0.08);
}
</style>
