<script setup lang="ts">
import { Monitor, RefreshCcw, Search } from '@lucide/vue';
import { message } from 'antdv-next';
import { confirmAction } from '@/utils/feedback';
import { onMounted, reactive, ref } from 'vue';
import { getSessionPage, offlineSession } from '@/api/session';
import type { AuthSession } from '@/api/session';

interface SessionItem {
  sessionKey: string;
  username: string;
  displayName: string;
  orgName?: string;
  ip?: string;
  authType: string;
  status: string;
  loginTime: string;
  lastAccessTime?: string;
  currentSession: boolean;
}

const loading = ref(false);
const sessionList = ref<SessionItem[]>([]);
const total = ref(0);

const searchParams = reactive({
  username: '',
  displayName: '',
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const columns = [
  { dataIndex: 'username', title: '用户名', width: 140 },
  { dataIndex: 'displayName', title: '姓名', width: 140 },
  { dataIndex: 'orgName', title: '所属组织', width: 150, ellipsis: true },
  { dataIndex: 'ip', title: '登录 IP', width: 150 },
  { dataIndex: 'authType', title: '登录方式', width: 120 },
  { dataIndex: 'loginTime', title: '登录时间', width: 180 },
  { dataIndex: 'lastAccessTime', title: '最近访问', width: 180 },
  { dataIndex: 'status', title: '状态', width: 100 },
  { dataIndex: 'action', title: '操作', width: 140, fixed: 'right' as const },
];

const tableScroll = { x: 1320 };

async function fetchList() {
  loading.value = true;
  try {
    const res = await getSessionPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      username: searchParams.username || undefined,
      realName: searchParams.displayName || undefined,
    });
    sessionList.value = res.list.map((item: AuthSession) => ({
      sessionKey: item.id,
      username: item.username,
      displayName: item.realName,
      ip: item.ip,
      authType: item.loginType,
      status: item.status,
      loginTime: item.loginTime,
      lastAccessTime: item.lastAccessTime,
      currentSession: item.current,
    }));
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
  searchParams.username = '';
  searchParams.displayName = '';
  handleSearch();
}

function handlePageChange(pageInfo: { current?: number; pageSize?: number }) {
  pagination.current = pageInfo.current ?? pagination.current;
  pagination.pageSize = pageInfo.pageSize ?? pagination.pageSize;
  fetchList();
}

async function handleOffline(row: SessionItem) {
  const confirm = await confirmAction(`确认强制下线会话 ${row.username} 吗?`);
  if (!confirm) {
    return;
  }

  try {
    await offlineSession(row.sessionKey);
    message.success('已强制下线');
    fetchList();
  } catch (error) {}
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
          <a-form class="filter-form-grid" :label-col="{ style: { width: '72px' } }">
            <a-row :gutter="[24, 18]" align="top">
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="用户名">
                  <a-input
                    v-model:value="searchParams.username"
                    placeholder="请输入用户名"
                    allow-clear
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="姓名">
                  <a-input
                    v-model:value="searchParams.displayName"
                    placeholder="请输入姓名"
                    allow-clear
                    class="filter-control"
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
        <div class="table-toolbar__actions table-toolbar__actions--right">
          <a-button variant="outlined" size="small" @click="fetchList">
            <template #icon>
              <RefreshCcw class="h-4 w-4" />
            </template>
            刷新
          </a-button>
        </div>
      </div>
      <a-table
        row-key="sessionKey"
        :data-source="sessionList"
        :columns="columns"
        :loading="loading"
        :scroll="tableScroll"
        bordered
        hover
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="record.status === 'ONLINE' ? 'success' : 'warning'" variant="filled">
              {{ record.status === 'ONLINE' ? '在线' : record.status }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'orgName'">
            {{ record.orgName || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'ip'">
            {{ record.ip || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'lastAccessTime'">
            {{ record.lastAccessTime || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'authType'">
            <div class="flex items-center gap-2">
              <Monitor class="h-4 w-4 text-[#0052d9]" />
              <span>{{ record.authType }}</span>
            </div>
          </template>

          <template v-else-if="column.dataIndex === 'action'">
            <a-button
              v-auth="'auth:session:offline'"
              danger
              variant="text"
              size="small"
              :disabled="record.currentSession"
              @click="handleOffline(record)"
            >
              {{ record.currentSession ? '当前' : '下线' }}
            </a-button>
          </template>
        </template>
      </a-table>

      <div class="mt-4 flex justify-end">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="total"
          :show-jumper="pagination.showQuickJumper"
          @change="handlePageChange"
        />
      </div>
    </a-card>
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
