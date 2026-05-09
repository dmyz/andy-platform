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
} from '@lucide/vue';
import type { Component } from 'vue';
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { usePreferenceStore } from '@/stores/preference';
import { useUserStore } from '@/stores/user';

interface Props {
  collapsed: boolean;
  showLogo?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  showLogo: true,
});

const route = useRoute();
const router = useRouter();
const preferenceStore = usePreferenceStore();
const userStore = useUserStore();

// 图标映射
const iconMap: Record<string, Component> = {
  dashboard: LayoutDashboard,
  setting: Settings,
  user: User,
  'usergroup-add': Users,
  'menu-unfold': Menu,
  monitor: Monitor,
  catalog: FileText,
  secured: Shield,
  bell: Bell,
  file: FolderOpen,
  link: Link2,
};

function resolveIcon(iconName: string | undefined, fallback: Component) {
  return (iconName && iconMap[iconName]) || fallback;
}

// 动态菜单数据
const menuList = computed(() => userStore.navigations);
const menuTheme = computed(() => (preferenceStore.theme === 'dark' ? 'dark' : 'light'));

const hasProfileNavigation = computed(() => {
  const walk = (items: Array<{ routePath?: string; children?: any[] }>): boolean =>
    items.some(
      (item) =>
        item.routePath === '/profile' || (item.children?.length ? walk(item.children) : false),
    );
  return walk(userStore.navigations as Array<{ routePath?: string; children?: any[] }>);
});

// 菜单值，用于 v-model
const selectedMenuKeys = ref<string[]>([route.path]);

// 监听路由变化，更新菜单值
watch(
  () => route.path,
  (newPath) => {
    selectedMenuKeys.value = [newPath];
  },
);

// 展开的菜单项
const expandedMenus = ref<string[]>([]);

// 监听路由变化，自动展开对应的菜单
watch(
  () => route.path,
  (newPath) => {
    const pathSegments = newPath.split('/').filter(Boolean);
    if (pathSegments.length > 1) {
      const parentPath = `/${pathSegments[0]}`;
      if (!expandedMenus.value.includes(parentPath)) {
        expandedMenus.value.push(parentPath);
      }
    }
  },
  { immediate: true },
);

// 处理菜单点击
function handleMenuClick({ key }: { key: string }) {
  if (key.startsWith('/')) {
    router.push(key);
  }
}

function handleExternalLink(url?: string) {
  if (url) {
    window.open(url, '_blank', 'noopener,noreferrer');
  }
}
</script>

<template>
  <a-menu
    v-model:selectedKeys="selectedMenuKeys"
    v-model:openKeys="expandedMenus"
    :theme="menuTheme"
    :inline-collapsed="props.collapsed"
    mode="inline"
    class="sidebar-menu"
    :class="{ 'sidebar-menu--collapsed': props.collapsed }"
    :style="{ width: props.collapsed ? '64px' : '232px', flexShrink: 0 }"
    @click="handleMenuClick"
  >
    <!-- Logo -->
    <template v-if="props.showLogo" #logo>
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 bg-[#0052d9] rounded-lg flex items-center justify-center flex-shrink-0">
          <LayoutDashboard class="w-5 h-5 text-white" />
        </div>
        <span v-if="!props.collapsed" class="text-lg font-semibold text-white"> 中台系统 </span>
      </div>
    </template>

    <!-- 动态渲染菜单 -->
    <template v-for="menu in menuList" :key="menu.id">
      <!-- 包含子菜单 (GROUP) -->
      <a-sub-menu
        v-if="menu.type === 'GROUP' && menu.children && menu.children.length > 0"
        :key="menu.routePath"
        :title="menu.name"
      >
        <template #icon>
          <component :is="resolveIcon(menu.icon, Settings)" class="w-5 h-5" />
        </template>
        <a-menu-item v-for="child in menu.children" :key="child.routePath">
          <template #icon>
            <component :is="resolveIcon(child.icon, Menu)" class="w-4 h-4" />
          </template>
          {{ child.name }}
        </a-menu-item>
      </a-sub-menu>

      <!-- 单独页面 (PAGE) -->
      <a-menu-item v-else-if="menu.type === 'PAGE'" :key="menu.routePath">
        <template #icon>
          <component :is="resolveIcon(menu.icon, LayoutDashboard)" class="w-5 h-5" />
        </template>
        {{ menu.name }}
      </a-menu-item>

      <a-menu-item
        v-else-if="menu.type === 'LINK'"
        :key="menu.id"
        @click="handleExternalLink(menu.externalUrl)"
      >
        <template #icon>
          <component :is="resolveIcon(menu.icon, LayoutDashboard)" class="w-5 h-5" />
        </template>
        {{ menu.name }}
      </a-menu-item>
    </template>

    <!-- 固定项: 个人中心 -->
    <a-sub-menu
      v-if="userStore.hasPermission('profile:view') && !hasProfileNavigation"
      key="/user"
      title="个人中心"
    >
      <template #icon>
        <User class="w-5 h-5" />
      </template>
      <a-menu-item key="/profile"> 个人资料 </a-menu-item>
    </a-sub-menu>
  </a-menu>
</template>

<style scoped>
.sidebar-menu {
  width: 232px;
  height: 100%;
  flex-shrink: 0;
  border-right: 1px solid #e5e8ef;
  background: #fff;
}

.sidebar-menu--collapsed {
  width: 64px;
}

.sidebar-menu :deep(.ant-menu) {
  height: 100%;
  padding: 8px;
  border-right: 0;
  overflow-y: auto;
}

.sidebar-menu :deep(.ant-menu-item),
.sidebar-menu :deep(.ant-menu-submenu-title) {
  height: 38px;
  margin: 1px 0;
  border-radius: 4px;
  font-weight: 500;
  color: #4e5969;
  transition:
    background-color 0.18s ease,
    color 0.18s ease;
}

.sidebar-menu :deep(.ant-menu-item .ant-menu-item-icon),
.sidebar-menu :deep(.ant-menu-submenu-title .ant-menu-item-icon) {
  transition: color 0.18s ease;
}

.sidebar-menu :deep(.ant-menu-overflow) {
  border-top: 0;
}

.sidebar-menu :deep(.ant-menu-light),
.sidebar-menu :deep(.ant-menu-light .ant-menu-submenu) {
  background: #fff;
}

.sidebar-menu :deep(.ant-menu-light .ant-menu-item) {
  color: #4e5969;
}

.sidebar-menu :deep(.ant-menu-light .ant-menu-submenu-title) {
  color: #4e5969;
}

.sidebar-menu :deep(.ant-menu-light .ant-menu-item:hover),
.sidebar-menu :deep(.ant-menu-light .ant-menu-submenu-title:hover) {
  color: #1d2129;
  background-color: #f2f5f9;
}

.sidebar-menu :deep(.ant-menu-light .ant-menu-item-selected) {
  color: #0052d9;
  background: #edf4ff;
}

.sidebar-menu :deep(.ant-menu-light .ant-menu-item-selected .ant-menu-item-icon) {
  color: #0052d9;
}

.sidebar-menu :deep(.ant-menu-light .ant-menu-item-icon) {
  color: #86909c;
}

.sidebar-menu :deep(.ant-menu-light .ant-menu-submenu-title:hover .ant-menu-item-icon),
.sidebar-menu :deep(.ant-menu-light .ant-menu-item:hover .ant-menu-item-icon) {
  color: #4e5969;
}

.sidebar-menu :deep(.ant-menu-dark) {
  background: #111827;
}

.sidebar-menu :deep(.ant-menu-dark .ant-menu-item) {
  color: rgba(255, 255, 255, 0.75);
}

.sidebar-menu :deep(.ant-menu-dark .ant-menu-item:hover),
.sidebar-menu :deep(.ant-menu-dark .ant-menu-submenu-title:hover) {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.08);
}

.sidebar-menu :deep(.ant-menu-dark .ant-menu-item-selected) {
  color: #fff;
  background: rgba(37, 99, 235, 0.26);
}

.sidebar-menu :deep(.ant-menu-dark .ant-menu-item-selected .ant-menu-item-icon) {
  color: #fff;
}

.sidebar-menu :deep(.ant-menu-dark .ant-menu-item-icon) {
  color: rgba(255, 255, 255, 0.75);
}

.sidebar-menu :deep(.ant-menu-item-selected .ant-menu-title-content),
.sidebar-menu :deep(.ant-menu-item-selected .ant-menu-item-icon) {
  transform: none;
}

.dark .sidebar-menu {
  border-right-color: rgba(31, 41, 55, 0.92);
  background: #111827;
}
</style>
