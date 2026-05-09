<script setup lang="ts">
import { Eye, MailOpen, RefreshCcw, Search } from '@lucide/vue';
import { message } from 'antdv-next';
import { onMounted, reactive, ref } from 'vue';
import { getInboxAnnouncementPage, markAnnouncementRead } from '@/api/announcement';

interface InboxItem {
  id: string;
  title: string;
  type: string;
  publishTime: string;
  read: boolean;
  top: boolean;
  content: string;
}

const loading = ref(false);
const dialogVisible = ref(false);
const total = ref(0);
const messageList = ref<InboxItem[]>([]);
const currentMessage = ref<InboxItem | null>(null);

const searchParams = reactive({
  title: '',
  read: '' as '' | 'true' | 'false',
});

const readStatusOptions = [
  { label: '已读', value: 'true' },
  { label: '未读', value: 'false' },
];

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const columns = [
  { dataIndex: 'title', title: '公告标题', ellipsis: true },
  { dataIndex: 'type', title: '类型', width: 120 },
  { dataIndex: 'top', title: '置顶', width: 80 },
  { dataIndex: 'publishTime', title: '发布时间', width: 180 },
  { dataIndex: 'read', title: '已读状态', width: 100 },
  { dataIndex: 'action', title: '操作', width: 200, fixed: 'right' as const },
];

const tableScroll = { x: 940 };

async function fetchList() {
  loading.value = true;
  try {
    const res = await getInboxAnnouncementPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    });
    messageList.value = res.list as any;
    total.value = res.total;
    pagination.current = res.pageNum;
    pagination.pageSize = res.pageSize;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  fetchList();
}

function handleReset() {
  searchParams.title = '';
  searchParams.read = '';
  handleSearch();
}

function handlePageChange(pageInfo: { current?: number; pageSize?: number }) {
  pagination.current = pageInfo.current ?? pagination.current;
  pagination.pageSize = pageInfo.pageSize ?? pagination.pageSize;
  fetchList();
}

async function markRead(row: InboxItem) {
  if (row.read) {
    return;
  }
  await markAnnouncementRead(row.id);
  if (searchParams.read === 'false') {
    await fetchList();
    return;
  }
  row.read = true;
}

async function openDetail(row: InboxItem) {
  currentMessage.value = row;
  dialogVisible.value = true;
  if (!row.read) {
    await markRead(row);
  }
}

async function handleRead(row: InboxItem) {
  await markRead(row);
  message.success('已标记为已读');
}

onMounted(() => {
  fetchList();
});
</script>

<template>
  <div class="flex flex-col gap-4">
    <a-card variant="borderless" class="shadow-sm">
      <a-row :gutter="[16, 12]" class="filter-layout" align="top">
        <a-col :xs="24" :xl="20">
          <a-form class="filter-form-grid" :label-col="{ style: { width: '80px' } }">
            <a-row :gutter="[24, 18]" align="top">
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="公告标题">
                  <a-input
                    v-model:value="searchParams.title"
                    allow-clear
                    placeholder="请输入公告标题"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="已读状态">
                  <a-select
                    v-model:value="searchParams.read"
                    allow-clear
                    placeholder="请选择已读状态"
                    class="filter-control"
                    :options="readStatusOptions"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-col :xs="24" :xl="4" class="filter-actions-col">
          <a-space :size="8" class="filter-actions">
            <a-button type="primary" @click="handleSearch">
              <template #icon>
                <Search class="h-4 w-4" />
              </template>
              查询
            </a-button>
            <a-button variant="outlined" @click="handleReset">
              <template #icon>
                <RefreshCcw class="h-4 w-4" />
              </template>
              重置
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <a-card variant="borderless" class="table-card shadow-sm">
      <div class="table-toolbar">
        <span></span>
        <a-button variant="outlined" size="small" @click="fetchList">
          <template #icon>
            <RefreshCcw class="h-4 w-4" />
          </template>
          刷新
        </a-button>
      </div>
      <a-table
        row-key="id"
        :data-source="messageList"
        :columns="columns"
        :loading="loading"
        bordered
        hover
        :pagination="{ ...pagination, total }"
        :scroll="tableScroll"
        @change="handlePageChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'type'">
            {{
              record.type === 'SYSTEM'
                ? '系统通知'
                : record.type === 'NOTICE'
                  ? '业务通知'
                  : record.type === 'FEATURE'
                    ? '功能发布'
                    : '安全提醒'
            }}
          </template>
          <template v-else-if="column.dataIndex === 'top'">
            {{ record.top ? '是' : '否' }}
          </template>
          <template v-else-if="column.dataIndex === 'read'">
            <a-tag :color="record.read ? 'success' : 'warning'" variant="filled">
              {{ record.read ? '已读' : '未读' }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <div class="row-actions">
              <a-button type="primary" size="small" @click="openDetail(record)">
                <template #icon>
                  <Eye class="h-3.5 w-3.5" />
                </template>
                详情
              </a-button>
              <a-button
                v-if="!record.read"
                v-auth="'announcement:inbox:read'"
                variant="outlined"
                size="small"
                @click="handleRead(record)"
              >
                <template #icon>
                  <MailOpen class="h-3.5 w-3.5" />
                </template>
                已读
              </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="dialogVisible" title="公告详情" width="720px" :footer="null">
      <div v-if="currentMessage" class="space-y-4">
        <div class="text-xl font-semibold text-[#1d2129]">{{ currentMessage.title }}</div>
        <div class="flex items-center gap-4 text-sm text-[#86909c]">
          <span>{{ currentMessage.publishTime }}</span>
          <span>{{ currentMessage.type }}</span>
          <span>{{ currentMessage.read ? '已读' : '未读' }}</span>
        </div>
        <div class="rounded-xl bg-[#f5f7fa] p-4 text-sm leading-7 text-[#1d2129]">
          {{ currentMessage.content }}
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
</style>
