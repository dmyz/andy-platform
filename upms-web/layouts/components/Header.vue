<script setup lang="ts">
import {
  ChevronRight,
  Home,
  LayoutDashboard,
  Moon,
  PanelLeftClose,
  PanelLeftOpen,
  Sun,
} from '@lucide/vue';
import FullscreenToggle from '@/components/layout/FullscreenToggle.vue';
import NotificationBell from '@/components/layout/NotificationBell.vue';
import UserMenu from '@/components/layout/UserMenu.vue';
import { useLayoutStore } from '@/stores/layout';
import { usePreferenceStore } from '@/stores/preference';
import { useUserStore } from '@/stores/user';

interface Props {
  showSidebarToggle?: boolean;
  showLogo?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  showSidebarToggle: true,
  showLogo: true,
});

const route = useRoute();
const preferenceStore = usePreferenceStore();
const layoutStore = useLayoutStore();
const userStore = useUserStore();

// 主题提示文本
const themeTooltipText = computed(() => {
  return preferenceStore.theme === 'dark' ? '切换到亮色模式' : '切换到暗色模式';
});

const breadcrumbs = computed(() => {
  return route.matched
    .filter((item) => item.meta?.title)
    .map((item) => ({
      title: item.meta?.title as string,
      path: item.path,
    }));
});
</script>

<template>
  <header
    class="app-header flex h-14 shrink-0 items-center justify-between border-b border-[#e5e8ef] bg-white px-4 dark:border-gray-800 dark:bg-[#111827]"
  >
    <div class="flex min-w-0 items-center gap-2">
      <router-link
        v-if="props.showLogo"
        to="/"
        class="brand-link flex h-10 min-w-[132px] items-center gap-3 border-r border-[#e5e8ef] pr-3 transition-colors dark:border-gray-800 sm:min-w-[232px] sm:pr-4"
      >
        <div
          class="flex h-8 w-8 shrink-0 items-center justify-center bg-[#1f2937] text-white dark:bg-[#2563eb]"
        >
          <LayoutDashboard class="h-[18px] w-[18px]" />
        </div>
        <div class="min-w-0">
          <span
            class="block truncate text-[15px] font-semibold leading-5 tracking-normal text-[#111827] dark:text-white"
          >
            中台系统
          </span>
          <span class="hidden text-[11px] leading-4 text-[#8a94a6] dark:text-gray-500 sm:block">
            Admin Console
          </span>
        </div>
      </router-link>

      <a-button
        v-if="props.showSidebarToggle"
        variant="text"
        size="middle"
        class="header-action-button"
        @click="layoutStore.toggleSidebar"
      >
        <template #icon>
          <PanelLeftOpen
            v-if="layoutStore.isSidebarCollapsed"
            class="h-5 w-5 text-[#4e5969] dark:text-gray-300"
          />
          <PanelLeftClose v-else class="h-5 w-5 text-[#4e5969] dark:text-gray-300" />
        </template>
      </a-button>

      <div
        class="breadcrumb-bar hidden min-w-0 items-center gap-2 px-2 text-sm text-[#4e5969] md:flex"
      >
        <router-link
          to="/"
          class="flex items-center gap-1.5 text-[#8a94a6] transition-colors hover:text-[#0052d9] dark:text-gray-400 dark:hover:text-blue-300"
        >
          <Home class="h-4 w-4" />
        </router-link>
        <template v-for="(item, index) in breadcrumbs" :key="index">
          <ChevronRight class="h-4 w-4 shrink-0 text-[#c9d2dc] dark:text-gray-600" />
          <router-link
            :to="item.path"
            class="truncate transition-colors hover:text-[#0052d9] dark:hover:text-blue-300"
            :class="
              index === breadcrumbs.length - 1
                ? 'text-[#172033] dark:text-white font-medium'
                : 'text-[#86909c] dark:text-gray-400'
            "
          >
            {{ item.title }}
          </router-link>
        </template>
      </div>
    </div>

    <div class="header-tools flex h-10 shrink-0 items-center gap-0 pl-2 sm:pl-3">
      <!-- 通知徽章 -->
      <NotificationBell v-if="userStore.hasPermission('announcement:inbox:view')" />

      <!-- 全屏切换按钮 -->
      <a-tooltip content="全屏切换" placement="bottom">
        <FullscreenToggle />
      </a-tooltip>

      <!-- 主题模式切换按钮 -->
      <a-tooltip :content="themeTooltipText" placement="bottom">
        <div
          class="header-icon-button flex h-9 w-9 cursor-pointer items-center justify-center transition-colors"
          @click="preferenceStore.toggleTheme"
        >
          <Sun
            v-if="preferenceStore.theme === 'dark'"
            class="h-[18px] w-[18px] text-[#4e5969] dark:text-gray-300"
          />
          <Moon v-else class="h-[18px] w-[18px] text-[#4e5969] dark:text-gray-300" />
        </div>
      </a-tooltip>

      <div class="mx-2 h-5 w-px bg-[#e5e8ef] dark:bg-gray-700" />

      <!-- 用户菜单 -->
      <UserMenu />
    </div>
  </header>
</template>

<style scoped>
.app-header :deep(.header-action-button) {
  border-radius: 4px;
}

.app-header :deep(.ant-btn-text:hover),
.header-icon-button:hover {
  background: #f2f5f9;
}

.dark .app-header :deep(.ant-btn-text:hover),
.dark .header-icon-button:hover {
  background: rgba(255, 255, 255, 0.08);
}

.brand-link:focus-visible,
.breadcrumb-bar a:focus-visible,
.header-icon-button:focus-visible {
  outline: 2px solid rgba(0, 82, 217, 0.35);
  outline-offset: 2px;
}
</style>
