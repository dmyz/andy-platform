<script setup lang="ts">
import {
  Bell,
  FileText,
  FolderOpen,
  LayoutDashboard,
  Link2,
  Menu,
  Monitor,
  Settings,
  Shield,
  User,
  Users,
} from '@lucide/vue'
import type { Component } from 'vue'
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePreferenceStore } from '@/stores/preference'
import { useUserStore } from '@/stores/user'

interface Props {
  collapsed: boolean
  showLogo?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showLogo: true,
})

const route = useRoute()
const router = useRouter()
const preferenceStore = usePreferenceStore()
const userStore = useUserStore()

// 图标映射
const iconMap: Record<string, Component> = {
  'dashboard': LayoutDashboard,
  'setting': Settings,
  'user': User,
  'usergroup-add': Users,
  'menu-unfold': Menu,
  'monitor': Monitor,
  'catalog': FileText,
  'secured': Shield,
  'bell': Bell,
  'file': FolderOpen,
  'link': Link2,
}

function resolveIcon(iconName: string | undefined, fallback: Component) {
  return (iconName && iconMap[iconName]) || fallback
}

// 动态菜单数据
const menuList = computed(() => userStore.navigations)

const hasProfileNavigation = computed(() => {
  const walk = (items: Array<{ routePath?: string, children?: any[] }>): boolean => items.some(item => item.routePath === '/profile' || (item.children?.length ? walk(item.children) : false))
  return walk(userStore.navigations as Array<{ routePath?: string, children?: any[] }>)
})

// 菜单值，用于 v-model
const activeMenu = ref(route.path)

// 监听路由变化，更新菜单值
watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
})

// 展开的菜单项
const expandedMenus = ref<string[]>([])

// 监听路由变化，自动展开对应的菜单
watch(() => route.path, (newPath) => {
  const pathSegments = newPath.split('/').filter(Boolean)
  if (pathSegments.length > 1) {
    const parentPath = `/${pathSegments[0]}`
    if (!expandedMenus.value.includes(parentPath)) {
      expandedMenus.value.push(parentPath)
    }
  }
}, { immediate: true })

// 处理菜单点击
function handleMenuChange(value: string | number) {
  if (typeof value === 'string' && value.startsWith('/')) {
    router.push(value)
  }
}

function handleExternalLink(url?: string) {
  if (url) {
    window.open(url, '_blank', 'noopener,noreferrer')
  }
}
</script>

<template>
  <t-menu
    v-model="activeMenu"
    v-model:expanded="expandedMenus"
    :theme="preferenceStore.theme"
    :collapsed="props.collapsed"
    :width="props.collapsed ? '64px' : '240px'"
    class="sidebar-menu"
    @change="handleMenuChange"
  >
    <!-- Logo -->
    <template v-if="props.showLogo" #logo>
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 bg-[#0052d9] rounded-lg flex items-center justify-center flex-shrink-0">
          <LayoutDashboard class="w-5 h-5 text-white" />
        </div>
        <span v-if="!props.collapsed" class="text-lg font-semibold text-white">
          中台系统
        </span>
      </div>
    </template>

    <!-- 动态渲染菜单 -->
    <template v-for="menu in menuList" :key="menu.id">
      <!-- 包含子菜单 (GROUP) -->
      <t-submenu v-if="menu.type === 'GROUP' && menu.children && menu.children.length > 0" :value="menu.routePath" :title="menu.name">
        <template #icon>
          <component :is="resolveIcon(menu.icon, Settings)" class="w-5 h-5" />
        </template>
        <t-menu-item v-for="child in menu.children" :key="child.id" :value="child.routePath">
          <template #icon>
            <component :is="resolveIcon(child.icon, Menu)" class="w-4 h-4" />
          </template>
          {{ child.name }}
        </t-menu-item>
      </t-submenu>

      <!-- 单独页面 (PAGE) -->
      <t-menu-item v-else-if="menu.type === 'PAGE'" :value="menu.routePath">
        <template #icon>
          <component :is="resolveIcon(menu.icon, LayoutDashboard)" class="w-5 h-5" />
        </template>
        {{ menu.name }}
      </t-menu-item>

      <t-menu-item v-else-if="menu.type === 'LINK'" :value="menu.id" @click="handleExternalLink(menu.externalUrl)">
        <template #icon>
          <component :is="resolveIcon(menu.icon, LayoutDashboard)" class="w-5 h-5" />
        </template>
        {{ menu.name }}
      </t-menu-item>
    </template>

    <!-- 固定项: 个人中心 -->
    <t-submenu v-if="userStore.hasPermission('profile:view') && !hasProfileNavigation" value="/user" title="个人中心">
      <template #icon>
        <User class="w-5 h-5" />
      </template>
      <t-menu-item value="/profile">
        个人资料
      </t-menu-item>
    </t-submenu>
  </t-menu>
</template>

<style scoped>
/* 亮色主题样式 */
.sidebar-menu :deep(.t-menu--light) {
  background-color: #001529;
}

.sidebar-menu :deep(.t-menu--light .t-menu__item) {
  color: rgba(255, 255, 255, 0.65);
}

.sidebar-menu :deep(.t-menu--light .t-menu__item:hover) {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.1);
}

.sidebar-menu :deep(.t-menu--light .t-menu__item--active) {
  color: #fff;
  background-color: #0052d9;
}

.sidebar-menu :deep(.t-menu--light .t-menu__item--active .t-menu__icon) {
  color: #fff;
}

.sidebar-menu :deep(.t-menu--light .t-menu__icon) {
  color: rgba(255, 255, 255, 0.65);
}

/* 暗色主题样式 */
.sidebar-menu :deep(.t-menu--dark) {
  background-color: #0f172a;
}

.sidebar-menu :deep(.t-menu--dark .t-menu__item) {
  color: rgba(255, 255, 255, 0.75);
}

.sidebar-menu :deep(.t-menu--dark .t-menu__item:hover) {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.15);
}

.sidebar-menu :deep(.t-menu--dark .t-menu__item--active) {
  color: #fff;
  background-color: #1e40af;
}

.sidebar-menu :deep(.t-menu--dark .t-menu__item--active .t-menu__icon) {
  color: #fff;
}

.sidebar-menu :deep(.t-menu--dark .t-menu__icon) {
  color: rgba(255, 255, 255, 0.75);
}
</style>
