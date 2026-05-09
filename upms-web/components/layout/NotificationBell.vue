<script setup lang="ts">
import { Bell, Check, ExternalLink, Trash2 } from '@lucide/vue';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getInboxAnnouncementPage, markAnnouncementRead } from '@/api/announcement';

interface Notification {
  id: string;
  title: string;
  message: string;
  time: string;
  read: boolean;
  type: 'info' | 'success' | 'warning' | 'error';
}

const router = useRouter();
const notifications = ref<Notification[]>([]);

const unreadCount = computed(() => {
  return notifications.value.filter((n) => !n.read).length;
});

const unreadNotifications = computed(() => {
  return notifications.value.filter((n) => !n.read);
});

const readNotifications = computed(() => {
  return notifications.value.filter((n) => n.read);
});

async function fetchNotifications() {
  const res = await getInboxAnnouncementPage({ pageNum: 1, pageSize: 6 });
  notifications.value = res.list.map((item) => ({
    id: item.id,
    title: item.title,
    message: item.content || '',
    time: item.publishTime,
    read: item.read,
    type:
      item.type === 'SECURITY'
        ? 'error'
        : item.type === 'FEATURE'
          ? 'success'
          : item.type === 'NOTICE'
            ? 'warning'
            : 'info',
  }));
}

// 标记为已读
async function markAsRead(id: string) {
  const notification = notifications.value.find((n) => n.id === id);
  if (notification && !notification.read) {
    await markAnnouncementRead(id);
    notification.read = true;
  }
}

// 全部标记为已读
async function markAllAsRead() {
  const unreadIds = notifications.value.filter((n) => !n.read).map((n) => n.id);
  for (const id of unreadIds) {
    await markAsRead(id);
  }
}

// 删除通知
function deleteNotification(id: string) {
  const index = notifications.value.findIndex((n) => n.id === id);
  if (index > -1) {
    notifications.value.splice(index, 1);
  }
}

function goInbox() {
  router.push('/announcement/inbox');
}

// 获取通知类型对应的样式
function getTypeStyles(type: Notification['type']) {
  const styles = {
    info: 'bg-blue-50 text-blue-600',
    success: 'bg-green-50 text-green-600',
    warning: 'bg-orange-50 text-orange-600',
    error: 'bg-red-50 text-red-600',
  };
  return styles[type];
}

onMounted(() => {
  fetchNotifications();
});
</script>

<template>
  <a-popover trigger="click" placement="bottomRight">
    <!-- 触发器：使用图标而不是按钮 -->
    <div
      class="notification-trigger relative flex h-9 w-9 cursor-pointer items-center justify-center transition-colors"
    >
      <Bell class="h-[18px] w-[18px] text-[#4e5969] dark:text-gray-300" />
      <a-badge
        v-if="unreadCount > 0"
        :count="unreadCount"
        :overflow-count="99"
        :offset="[-2, -15]"
        class="select-none"
      />
    </div>

    <!-- 弹窗内容 -->
    <template #content>
      <div class="w-80">
        <!-- 顶部：标题和全部已读按钮 -->
        <div
          class="flex items-center justify-between p-4 border-b border-[#e7e7e7] dark:border-gray-700"
        >
          <span class="text-base font-semibold text-[#1d2129] dark:text-white">通知中心</span>
          <button
            v-if="unreadCount > 0"
            class="flex items-center gap-1 px-2 py-1 text-xs text-[#0052d9] dark:text-blue-400 hover:bg-[#e6f4ff] dark:hover:bg-gray-700 rounded transition-colors"
            @click="markAllAsRead"
          >
            <Check class="h-3 w-3" />
            全部已读
          </button>
        </div>

        <!-- 通知列表 -->
        <div class="max-h-[400px] overflow-y-auto">
          <!-- 未读通知 -->
          <template v-if="unreadNotifications.length > 0">
            <div
              v-for="notification in unreadNotifications"
              :key="notification.id"
              class="flex cursor-pointer gap-3 px-4 py-3 transition-colors hover:bg-gray-50 dark:hover:bg-gray-700"
              @click="markAsRead(notification.id)"
            >
              <div class="pt-2">
                <div class="w-2 h-2 rounded-full" :class="[getTypeStyles(notification.type)]" />
              </div>

              <div class="min-w-0 flex-1">
                <span class="text-sm font-medium text-[#1d2129] dark:text-white">{{
                  notification.title
                }}</span>

                <p class="text-xs text-[#86909c] dark:text-gray-400 mt-1 line-clamp-2">
                  {{ notification.message }}
                </p>
                <p class="text-xs text-[#86909c] dark:text-gray-400 mt-1">
                  {{ notification.time }}
                </p>
              </div>

              <button
                class="h-8 w-8 flex shrink-0 items-center justify-center text-[#86909c] dark:text-gray-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded transition-colors"
                @click.stop="deleteNotification(notification.id)"
              >
                <Trash2 class="h-4 w-4" />
              </button>
            </div>
          </template>

          <!-- 已读通知 -->
          <template v-if="readNotifications.length > 0">
            <div
              v-for="notification in readNotifications"
              :key="notification.id"
              class="flex cursor-pointer gap-3 px-4 py-3 opacity-60 transition-colors hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <div class="pt-2">
                <div class="w-2 h-2 rounded-full" :class="[getTypeStyles(notification.type)]" />
              </div>

              <div class="min-w-0 flex-1">
                <span class="text-sm font-medium text-[#1d2129] dark:text-white">{{
                  notification.title
                }}</span>

                <p class="text-xs text-[#86909c] dark:text-gray-400 mt-1 line-clamp-2">
                  {{ notification.message }}
                </p>
                <p class="text-xs text-[#86909c] dark:text-gray-400 mt-1">
                  {{ notification.time }}
                </p>
              </div>
            </div>
          </template>

          <!-- 无通知 -->
          <template v-if="notifications.length === 0">
            <div class="flex flex-col items-center justify-center py-12">
              <Bell class="h-12 w-12 text-[#86909c] dark:text-gray-400 mb-4" />
              <p class="text-sm text-[#86909c] dark:text-gray-400">暂无通知</p>
            </div>
          </template>
        </div>

        <!-- 底部：查看全部通知按钮 -->
        <div class="p-4 border-t border-[#e7e7e7] dark:border-gray-700">
          <button
            class="w-full flex items-center justify-center gap-2 px-4 py-2 text-sm text-[#0052d9] dark:text-blue-400 border border-[#0052d9] dark:border-blue-400 rounded-md hover:bg-[#e6f4ff] dark:hover:bg-gray-700 transition-colors"
            @click="goInbox"
          >
            <ExternalLink class="h-4 w-4" />
            全部通知
          </button>
        </div>
      </div>
    </template>
  </a-popover>
</template>

<style scoped>
.notification-trigger:hover {
  background: #f2f5f9;
}

.dark .notification-trigger:hover {
  background: rgba(255, 255, 255, 0.08);
}
</style>
