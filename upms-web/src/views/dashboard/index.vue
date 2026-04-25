<script setup lang="ts">
import { Building, Calendar, FileText, Home, LayoutDashboard, Megaphone, Settings, User, Users } from '@lucide/vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { get } from '@/utils/request'

interface DashboardSummary {
  username: string
  displayName: string
  orgName: string
  roleName: string
  loginTime: string
}

interface ShortcutItem {
  label: string
  path: string
  icon: string
  color: string
}

interface AnnouncementItem {
  id: string
  title: string
  publishTime: string
  top: boolean
  type: string
}

interface RecentOperationItem {
  id: string
  operationTime: string
  module: string
  action: string
  result: string
}

const router = useRouter()
const userStore = useUserStore()

const currentDate = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
})

const summary = ref<DashboardSummary | null>(null)
const shortcuts = ref<ShortcutItem[]>([])
const announcements = ref<AnnouncementItem[]>([])
const recentOperations = ref<RecentOperationItem[]>([])

const iconMap: Record<string, any> = {
  user: User,
  'usergroup-add': Users,
  'menu-unfold': LayoutDashboard,
  setting: Settings,
  catalog: FileText,
  bell: Megaphone,
  file: FileText,
  monitor: FileText,
}

const announcementColumns = [
  { colKey: 'title', title: '公告标题', ellipsis: true },
  { colKey: 'publishTime', title: '发布时间', width: 180 },
  { colKey: 'top', title: '置顶', width: 80 },
]

const operationColumns = [
  { colKey: 'operationTime', title: '操作时间', width: 180 },
  { colKey: 'module', title: '操作模块', width: 120 },
  { colKey: 'action', title: '操作类型', width: 140 },
  { colKey: 'result', title: '操作结果', width: 100 },
]

async function fetchDashboard() {
  const [summaryRes, shortcutsRes, announcementsRes, operationsRes] = await Promise.all([
    get<DashboardSummary>('/dashboard/summary'),
    get<ShortcutItem[]>('/dashboard/favorite-navigations'),
    get<AnnouncementItem[]>('/dashboard/announcements'),
    get<RecentOperationItem[]>('/dashboard/recent-operations'),
  ])
  summary.value = summaryRes
  shortcuts.value = shortcutsRes
  announcements.value = announcementsRes
  recentOperations.value = operationsRes
}

function handleShortcutClick(path: string) {
  if (!path.startsWith('/')) {
    return
  }
  router.push(path)
}

function openAnnouncementInbox() {
  router.push('/announcement/inbox')
}

onMounted(() => {
  fetchDashboard()
})
</script>

<template>
  <div class="space-y-6">
    <div class="grid grid-cols-3 gap-6">
      <t-card class="col-span-2 bg-gradient-to-r from-[#0052d9] to-[#003a8c] text-white" :bordered="false">
        <div class="flex items-center gap-6">
          <div class="flex h-20 w-20 items-center justify-center rounded-2xl bg-white/20 backdrop-blur-sm">
            <Home class="h-10 w-10" />
          </div>
          <div class="flex-1">
            <h1 class="mb-2 text-2xl font-bold">
              欢迎回来,{{ summary?.displayName || userStore.userInfo?.displayName || '管理员' }}!
            </h1>
            <p class="mb-3 text-white/80">
              {{ currentDate }}
            </p>
            <p class="text-sm text-white/60">
              当前归属 {{ summary?.orgName || '技术中心' }}，今日继续推进平台主链路。
            </p>
          </div>
        </div>
      </t-card>

      <t-card :bordered="false" class="shadow-sm">
        <div class="mb-4 flex items-center gap-4">
          <t-avatar :image="userStore.userInfo?.avatar || undefined" size="56px">
            <User class="h-7 w-7" />
          </t-avatar>
          <div>
            <h3 class="text-lg font-semibold text-[#1d2129] dark:text-white">
              {{ summary?.displayName || userStore.userInfo?.displayName || '管理员' }}
            </h3>
            <p class="text-sm text-[#86909c] dark:text-gray-400">
              {{ summary?.roleName || '系统管理员' }}
            </p>
          </div>
        </div>
        <div class="space-y-2">
          <div class="flex items-center gap-2 text-sm text-[#4e5969] dark:text-gray-300">
            <Building class="h-4 w-4" />
            <span>所属组织: {{ summary?.orgName || '技术中心' }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-[#4e5969] dark:text-gray-300">
            <Calendar class="h-4 w-4" />
            <span>登录时间: {{ summary?.loginTime || '-' }}</span>
          </div>
        </div>
      </t-card>
    </div>

    <t-card :bordered="false" class="shadow-sm">
      <template #header>
        <div class="flex items-center gap-2">
          <LayoutDashboard class="h-5 w-5 text-[#0052d9]" />
          <span class="text-lg font-semibold text-[#1d2129] dark:text-white">快捷入口</span>
        </div>
      </template>
      <div class="grid grid-cols-6 gap-4">
        <div
          v-for="item in shortcuts"
          :key="item.label"
          class="group flex cursor-pointer flex-col items-center gap-3 rounded-lg p-4 transition-all hover:bg-[#f5f5f5] dark:hover:bg-gray-700"
          @click="handleShortcutClick(item.path)"
        >
          <div
            class="flex h-14 w-14 items-center justify-center rounded-xl transition-transform group-hover:scale-110"
            :style="{ backgroundColor: `${item.color}15` }"
          >
            <component :is="iconMap[item.icon] || LayoutDashboard" class="h-7 w-7" :style="{ color: item.color }" />
          </div>
          <span class="text-sm text-[#4e5969] group-hover:text-[#0052d9] dark:text-gray-300 dark:group-hover:text-blue-400">
            {{ item.label }}
          </span>
        </div>
      </div>
    </t-card>

    <div class="grid grid-cols-2 gap-6">
      <t-card :bordered="false" class="shadow-sm">
        <template #header>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Megaphone class="h-5 w-5 text-[#ed7b2f]" />
              <span class="text-lg font-semibold text-[#1d2129] dark:text-white">公告通知</span>
            </div>
          </div>
        </template>
        <t-table :data="announcements" :columns="announcementColumns" :pagination="{ disabled: true }" row-key="id" hover size="small">
          <template #title="{ row }">
            <div class="flex items-center gap-2">
              <t-tag v-if="row.type === 'system'" theme="primary" size="small" variant="light">
                系统
              </t-tag>
              <t-tag v-else-if="row.type === 'notice'" theme="warning" size="small" variant="light">
                通知
              </t-tag>
              <t-tag v-else theme="success" size="small" variant="light">
                功能
              </t-tag>
              <span class="cursor-pointer text-sm text-[#1d2129] hover:text-[#0052d9] dark:text-white dark:hover:text-blue-400" @click="openAnnouncementInbox">
                {{ row.title }}
              </span>
            </div>
          </template>
          <template #top="{ row }">
            <t-tag v-if="row.top" theme="danger" size="small" variant="light">
              置顶
            </t-tag>
            <span v-else class="text-sm text-[#86909c] dark:text-gray-400">-</span>
          </template>
        </t-table>
      </t-card>

      <t-card :bordered="false" class="shadow-sm">
        <template #header>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <FileText class="h-5 w-5 text-[#00a870]" />
              <span class="text-lg font-semibold text-[#1d2129] dark:text-white">最近操作记录</span>
            </div>
          </div>
        </template>
        <t-table :data="recentOperations" :columns="operationColumns" :pagination="{ disabled: true }" row-key="id" hover size="small">
          <template #result="{ row }">
            <t-tag :theme="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small" variant="light">
              {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
            </t-tag>
          </template>
        </t-table>
      </t-card>
    </div>
  </div>
</template>
