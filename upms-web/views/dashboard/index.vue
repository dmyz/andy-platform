<script setup lang="ts">
import {
  Building,
  Calendar,
  Clock3,
  FileText,
  Home,
  LayoutDashboard,
  Megaphone,
  ShieldCheck,
  Shield,
  Settings,
  User,
  Users,
} from '@lucide/vue';
import { computed, onMounted, ref } from 'vue';
import type { Component } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import type { NavigationItem } from '@/types/auth';
import {
  getDashboardStats,
  getRecentActivities,
  getSystemNotifications,
  getQuickActions,
  type AnnouncementItem,
  type DashboardSummary,
  type RecentOperationItem,
  type ShortcutItem,
} from '@/api/dashboard';

const router = useRouter();
const userStore = useUserStore();

const currentDate = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
});

const summary = ref<DashboardSummary | null>(null);
const shortcuts = ref<ShortcutItem[]>([]);
const announcements = ref<AnnouncementItem[]>([]);
const recentOperations = ref<RecentOperationItem[]>([]);
const shortcutFallbackColors = ['#0052d9', '#00a870', '#ed7b2f', '#7b61ff', '#0e7490', '#475569'];

const iconMap: Record<string, Component> = {
  user: User,
  'usergroup-add': Users,
  'menu-unfold': LayoutDashboard,
  setting: Settings,
  settings: Settings,
  shield: Shield,
  catalog: FileText,
  bell: Megaphone,
  file: FileText,
  monitor: FileText,
};

function collectNavigationShortcuts(items: NavigationItem[], result: ShortcutItem[] = []) {
  items.forEach((item) => {
    if (result.length >= 6) {
      return;
    }

    if (item.type === 'PAGE' && item.routePath) {
      result.push({
        label: item.name,
        path: item.routePath,
        icon: item.icon || 'menu-unfold',
        color: shortcutFallbackColors[result.length] || '#0052d9',
      });
    }

    if (item.children?.length) {
      collectNavigationShortcuts(item.children, result);
    }
  });

  return result;
}

const visibleShortcuts = computed(() => {
  if (shortcuts.value.length > 0) {
    return shortcuts.value;
  }

  return collectNavigationShortcuts(userStore.navigations);
});

const announcementColumns = [
  { dataIndex: 'title', title: '公告标题', ellipsis: true },
  { dataIndex: 'publishTime', title: '发布时间', width: 150 },
  { dataIndex: 'top', title: '置顶', width: 70 },
];

const announcementTableScroll = { x: 620 };

const operationColumns = [
  { dataIndex: 'operationTime', title: '操作时间', width: 150 },
  { dataIndex: 'module', title: '操作模块', width: 110 },
  { dataIndex: 'action', title: '操作类型', width: 110 },
  { dataIndex: 'result', title: '操作结果', width: 86 },
];

const operationTableScroll = { x: 560 };

async function fetchDashboard() {
  const [summaryRes, shortcutsRes, announcementsRes, operationsRes] = await Promise.all([
    getDashboardStats(),
    getQuickActions(),
    getSystemNotifications({ limit: 10 }),
    getRecentActivities({ limit: 10 }),
  ]);
  summary.value = summaryRes;
  shortcuts.value = shortcutsRes;
  announcements.value = announcementsRes;
  recentOperations.value = operationsRes;
}

function handleShortcutClick(path: string) {
  if (!path.startsWith('/')) {
    return;
  }
  router.push(path);
}

function openAnnouncementInbox() {
  router.push('/announcement/inbox');
}

function resolveAnnouncementTheme(type: string) {
  const normalizedType = type.toLowerCase();

  if (normalizedType === 'system') {
    return 'primary';
  }
  if (normalizedType === 'notice') {
    return 'warning';
  }
  return 'success';
}

function resolveAnnouncementLabel(type: string) {
  const normalizedType = type.toLowerCase();

  if (normalizedType === 'system') {
    return '系统';
  }
  if (normalizedType === 'notice') {
    return '通知';
  }
  return '功能';
}

function isSuccessResult(result: string) {
  return result === 'SUCCESS' || result === '成功';
}

onMounted(() => {
  fetchDashboard();
});
</script>

<template>
  <div class="dashboard-view flex flex-col gap-4">
    <div class="grid grid-cols-1 gap-4 xl:grid-cols-[minmax(0,1fr)_360px]">
      <a-card
        class="dashboard-hero overflow-hidden border border-[#c9d8e8] bg-[#f9fbfd] text-[#172033] shadow-sm dark:border-gray-700 dark:bg-gray-900 dark:text-white"
        variant="borderless"
      >
        <div class="flex flex-col items-stretch justify-between gap-5 md:flex-row md:items-center">
          <div class="flex min-w-0 items-center gap-4">
            <div
              class="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-[#132238] text-[#7dd3fc] shadow-[inset_0_0_0_1px_rgba(255,255,255,0.08)]"
            >
              <Home class="h-6 w-6" />
            </div>
            <div class="min-w-0">
              <div
                class="mb-2 inline-flex items-center rounded-full border border-[#d8e3ef] bg-white px-2.5 py-1 text-xs font-medium text-[#4e667d] dark:border-gray-700 dark:bg-gray-800 dark:text-gray-300"
              >
                <ShieldCheck class="mr-1.5 h-3.5 w-3.5 text-[#00a870]" />
                {{ summary?.roleName || '系统管理员' }}
              </div>
              <h1 class="truncate text-[22px] font-semibold leading-7 tracking-normal">
                欢迎回来,{{ summary?.displayName || userStore.userInfo?.displayName || '管理员' }}!
              </h1>
              <p class="mt-1 max-w-[420px] text-sm leading-5 text-[#5b6f82] dark:text-gray-400">
                当前归属 {{ summary?.orgName || '技术中心' }}，今日继续推进平台主链路。
              </p>
            </div>
          </div>
          <div
            class="grid w-full shrink-0 grid-cols-1 overflow-hidden rounded-xl border border-[#d8e3ef] bg-white text-sm dark:border-gray-700 dark:bg-gray-800 sm:w-[350px] sm:grid-cols-2"
          >
            <div class="border-r border-[#e5edf5] px-4 py-3 dark:border-gray-700">
              <div class="mb-1 flex items-center gap-1.5 text-xs text-[#86909c]">
                <Calendar class="h-3.5 w-3.5" />
                工作日历
              </div>
              <div class="font-medium text-[#1d2129] dark:text-white">
                {{ currentDate }}
              </div>
            </div>
            <div class="px-4 py-3">
              <div class="mb-1 flex items-center gap-1.5 text-xs text-[#86909c]">
                <Clock3 class="h-3.5 w-3.5" />
                登录时间
              </div>
              <div class="truncate font-medium text-[#1d2129] dark:text-white">
                {{ summary?.loginTime || '-' }}
              </div>
            </div>
          </div>
        </div>
      </a-card>

      <a-card variant="borderless" class="dashboard-card shadow-sm">
        <div class="flex items-center gap-3">
          <a-avatar :image="userStore.userInfo?.avatar || undefined" size="40px">
            <User class="h-4.5 w-4.5" />
          </a-avatar>
          <div class="min-w-0">
            <h3 class="truncate text-[15px] font-semibold text-[#1d2129] dark:text-white">
              {{ summary?.displayName || userStore.userInfo?.displayName || '管理员' }}
            </h3>
            <p class="text-xs text-[#86909c] dark:text-gray-400">
              {{ summary?.roleName || '系统管理员' }}
            </p>
          </div>
        </div>
        <div class="mt-4 grid gap-2">
          <div
            class="flex items-center gap-2 rounded-lg bg-[#f6f8fa] px-3 py-2 text-xs text-[#4e5969] dark:bg-gray-800 dark:text-gray-300"
          >
            <Building class="h-4 w-4" />
            <span class="truncate">所属组织: {{ summary?.orgName || '技术中心' }}</span>
          </div>
          <div
            class="flex items-center gap-2 rounded-lg bg-[#f6f8fa] px-3 py-2 text-xs text-[#4e5969] dark:bg-gray-800 dark:text-gray-300"
          >
            <Calendar class="h-4 w-4" />
            <span class="truncate">登录时间: {{ summary?.loginTime || '-' }}</span>
          </div>
        </div>
      </a-card>
    </div>

    <a-card variant="borderless" class="dashboard-card shortcut-card shadow-sm">
      <template #title>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <LayoutDashboard class="h-4.5 w-4.5 text-[#006fbb]" />
            <span class="text-base font-semibold text-[#1d2129] dark:text-white">快捷入口</span>
          </div>
          <span class="text-xs text-[#86909c]">常用功能</span>
        </div>
      </template>
      <div
        class="grid grid-cols-2 gap-2 md:grid-cols-4 xl:grid-cols-6"
        v-if="visibleShortcuts.length"
      >
        <div
          v-for="item in visibleShortcuts"
          :key="item.label"
          class="shortcut-tile group grid cursor-pointer grid-cols-[34px_minmax(0,1fr)] items-center gap-3 rounded-lg border border-[#edf1f5] bg-white px-3 py-2 transition-all hover:-translate-y-0.5 hover:border-[#b8d8f2] hover:bg-[#f7fbff] hover:shadow-sm dark:border-gray-700 dark:bg-gray-800 dark:hover:border-gray-600 dark:hover:bg-gray-700"
          @click="handleShortcutClick(item.path)"
        >
          <div
            class="flex h-[34px] w-[34px] items-center justify-center rounded-lg transition-transform group-hover:scale-105"
            :style="{ backgroundColor: `${item.color}15` }"
          >
            <component
              :is="iconMap[item.icon] || LayoutDashboard"
              class="h-[18px] w-[18px]"
              :style="{ color: item.color }"
            />
          </div>
          <span
            class="truncate text-sm font-medium text-[#344054] group-hover:text-[#006fbb] dark:text-gray-300 dark:group-hover:text-blue-400"
          >
            {{ item.label }}
          </span>
        </div>
      </div>
      <div v-else class="empty-shortcuts flex items-center justify-center text-sm text-[#86909c]">
        暂无常用入口
      </div>
    </a-card>

    <div class="grid grid-cols-1 gap-4 xl:grid-cols-2">
      <a-card variant="borderless" class="dashboard-card shadow-sm">
        <template #title>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Megaphone class="h-4.5 w-4.5 text-[#c75d17]" />
              <span class="text-base font-semibold text-[#1d2129] dark:text-white">公告通知</span>
            </div>
            <span class="text-xs text-[#86909c]">最近 10 条</span>
          </div>
        </template>
        <div class="compact-table">
          <a-table
            :data-source="announcements"
            :columns="announcementColumns"
            row-key="id"
            :scroll="announcementTableScroll"
            hover
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'title'">
                <div class="flex items-center gap-2">
                  <a-tag
                    :color="resolveAnnouncementTheme(record.type)"
                    size="small"
                    variant="filled"
                    class="shrink-0"
                  >
                    {{ resolveAnnouncementLabel(record.type) }}
                  </a-tag>
                  <span
                    class="min-w-0 cursor-pointer truncate text-sm text-[#1d2129] hover:text-[#0052d9] dark:text-white dark:hover:text-blue-400"
                    @click="openAnnouncementInbox"
                  >
                    {{ record.title }}
                  </span>
                </div>
              </template>
              <template v-else-if="column.dataIndex === 'top'">
                <a-tag v-if="record.top" color="red" size="small" variant="filled"> 置顶 </a-tag>
                <span v-else class="text-sm text-[#86909c] dark:text-gray-400">-</span>
              </template>
            </template>
          </a-table>
        </div>
      </a-card>

      <a-card variant="borderless" class="dashboard-card shadow-sm">
        <template #title>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <FileText class="h-4.5 w-4.5 text-[#008f5d]" />
              <span class="text-base font-semibold text-[#1d2129] dark:text-white">
                最近操作记录
              </span>
            </div>
            <span class="text-xs text-[#86909c]">审计追踪</span>
          </div>
        </template>
        <div class="compact-table">
          <a-table
            :data-source="recentOperations"
            :columns="operationColumns"
            row-key="id"
            :scroll="operationTableScroll"
            hover
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'result'">
                <a-tag
                  :color="isSuccessResult(record.result) ? 'success' : 'danger'"
                  size="small"
                  variant="filled"
                >
                  {{ isSuccessResult(record.result) ? '成功' : '失败' }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </div>
      </a-card>
    </div>
  </div>
</template>

<style scoped>
.dashboard-view :deep(.ant-card) {
  border-radius: 10px;
}

.dashboard-card :deep(.ant-table) {
  font-size: 13px;
}

.dashboard-card :deep(.ant-table-thead > tr > th) {
  color: #667085;
  font-weight: 600;
  background: #f8fafc;
}

.dashboard-card :deep(.ant-table-tbody > tr > td),
.dashboard-card :deep(.ant-table-thead > tr > th) {
  padding-top: 8px;
  padding-bottom: 8px;
}

.compact-table {
  overflow-x: auto;
}

.compact-table :deep(.ant-table) {
  min-width: 100%;
}

.dashboard-hero {
  background:
    linear-gradient(135deg, rgba(0, 168, 112, 0.1), transparent 34%),
    linear-gradient(115deg, rgba(0, 111, 187, 0.12), transparent 46%), #f9fbfd;
}

.shortcut-tile {
  min-height: 58px;
}

@media (prefers-color-scheme: dark) {
  .dashboard-hero {
    background:
      linear-gradient(135deg, rgba(0, 168, 112, 0.16), transparent 34%),
      linear-gradient(115deg, rgba(14, 165, 233, 0.16), transparent 46%), #111827;
  }

  .dashboard-card :deep(.ant-card-head) {
    border-bottom-color: #374151;
  }

  .dashboard-card :deep(.ant-table-thead > tr > th) {
    color: #d1d5db;
    background: #1f2937;
  }
}
</style>
