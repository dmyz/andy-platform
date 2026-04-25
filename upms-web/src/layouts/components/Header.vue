<script setup lang="ts">
import {
  ChevronRight,
  Home,
  LayoutDashboard,
  Moon,
  PanelLeftClose,
  PanelLeftOpen,
  Sun,
} from '@lucide/vue'
import FullscreenToggle from '@/components/layout/FullscreenToggle.vue'
import NotificationBell from '@/components/layout/NotificationBell.vue'
import UserMenu from '@/components/layout/UserMenu.vue'
import { useLayoutStore } from '@/stores/layout'
import { usePreferenceStore } from '@/stores/preference'
import { useUserStore } from '@/stores/user'

interface Props {
  showSidebarToggle?: boolean
  showLogo?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showSidebarToggle: true,
  showLogo: true,
})

const route = useRoute()
const preferenceStore = usePreferenceStore()
const layoutStore = useLayoutStore()
const userStore = useUserStore()

// 主题提示文本
const themeTooltipText = computed(() => {
  return preferenceStore.theme === 'dark' ? '切换到亮色模式' : '切换到暗色模式'
})

const breadcrumbs = computed(() => {
  return route.matched
    .filter(item => item.meta?.title)
    .map(item => ({
      title: item.meta?.title as string,
      path: item.path,
    }))
})
</script>

<template>
  <header class="h-[56px] bg-white dark:bg-gray-800 border-b border-[#e7e7e7] dark:border-gray-700 flex items-center justify-between px-4">
    <div class="flex items-center gap-4">
      <router-link v-if="props.showLogo" to="/" class="flex items-center gap-3 min-w-[200px]">
        <div class="w-8 h-8 bg-[#0052d9] rounded-lg flex items-center justify-center flex-shrink-0">
          <LayoutDashboard class="w-5 h-5 text-white" />
        </div>
        <span class="text-lg font-semibold text-[#1d2129] dark:text-white">中台系统</span>
      </router-link>

      <t-button
        v-if="props.showSidebarToggle"
        theme="default"
        variant="text"
        size="medium"
        @click="layoutStore.toggleSidebar"
      >
        <template #icon>
          <PanelLeftOpen v-if="layoutStore.isSidebarCollapsed" class="w-5 h-5 text-[#4e5969] dark:text-gray-300" />
          <PanelLeftClose v-else class="w-5 h-5 text-[#4e5969] dark:text-gray-300" />
        </template>
      </t-button>

      <div class="flex items-center gap-2 text-sm pl-2">
        <router-link to="/" class="flex items-center gap-1.5 text-[#86909c] dark:text-gray-400 hover:text-[#0052d9] dark:hover:text-blue-400 transition-colors">
          <Home class="w-4 h-4" />
        </router-link>
        <template v-for="(item, index) in breadcrumbs" :key="index">
          <ChevronRight class="w-4 h-4 text-[#c9cdd4] dark:text-gray-500" />
          <router-link
            :to="item.path"
            class="hover:text-[#0052d9] dark:hover:text-blue-400 transition-colors"
            :class="index === breadcrumbs.length - 1 ? 'text-[#1d2129] dark:text-white font-medium' : 'text-[#86909c] dark:text-gray-400'"
          >
            {{ item.title }}
          </router-link>
        </template>
      </div>
    </div>

    <div class="flex items-center gap-1">
      <!-- 通知徽章 -->
      <NotificationBell v-if="userStore.hasPermission('announcement:inbox:view')" />

      <!-- 全屏切换按钮 -->
      <t-tooltip content="全屏切换" placement="bottom">
        <FullscreenToggle />
      </t-tooltip>

      <!-- 主题模式切换按钮 -->
      <t-tooltip :content="themeTooltipText" placement="bottom">
        <div
          class="h-9 w-9 flex items-center justify-center cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700 rounded-md transition-colors"
          @click="preferenceStore.toggleTheme"
        >
          <Sun v-if="preferenceStore.theme === 'dark'" class="h-[18px] w-[18px] text-[#4e5969] dark:text-gray-300" />
          <Moon v-else class="h-[18px] w-[18px] text-[#4e5969] dark:text-gray-300" />
        </div>
      </t-tooltip>

      <div class="w-px h-5 bg-[#e7e7e7] dark:bg-gray-700 mx-2 mr-2" />

      <!-- 用户菜单 -->
      <UserMenu />
    </div>
  </header>
</template>
