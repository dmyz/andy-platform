<script setup lang="ts">
import { Download, RefreshCcw, Search } from '@lucide/vue';
import { message } from 'antdv-next';
import { onMounted, reactive, ref } from 'vue';
import { getLoginAuditLogs, exportLoginAuditLogs } from '@/api/audit';

interface LoginAuditItem {
  id: string;
  username: string;
  realName: string;
  loginType: string;
  ip: string;
  browser: string;
  loginTime: string;
  result: string;
  failureReason?: string | null;
}

const loading = ref(false);
const exporting = ref(false);
const auditList = ref<LoginAuditItem[]>([]);
const total = ref(0);

const searchParams = reactive({
  username: '',
  loginType: '',
  result: '',
  loginTimeRange: [] as string[],
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const loginTypeOptions = [
  { label: '账号密码', value: 'PASSWORD' },
  { label: '手机验证码', value: 'MOBILE_CODE' },
  { label: '邮箱验证码', value: 'EMAIL_CODE' },
];

const resultOptions = [
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAIL' },
];

const columns = [
  { dataIndex: 'username', title: '登录账号', width: 140 },
  { dataIndex: 'realName', title: '用户姓名', width: 140 },
  { dataIndex: 'loginType', title: '登录方式', width: 120 },
  { dataIndex: 'ip', title: 'IP', width: 150 },
  { dataIndex: 'browser', title: '浏览器信息', ellipsis: true },
  { dataIndex: 'loginTime', title: '登录时间', width: 180 },
  { dataIndex: 'result', title: '登录结果', width: 110 },
  { dataIndex: 'failureReason', title: '失败原因', ellipsis: true },
];

const tableScroll = { x: 1180 };

function buildQueryParams() {
  const [startTime, endTime] = searchParams.loginTimeRange;
  return {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    username: searchParams.username || undefined,
    loginType: searchParams.loginType || undefined,
    result: searchParams.result || undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
  };
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await getLoginAuditLogs(buildQueryParams());
    auditList.value = res.list;
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
  searchParams.loginType = '';
  searchParams.result = '';
  searchParams.loginTimeRange = [];
  handleSearch();
}

function handlePageChange(pageInfo: { current?: number; pageSize?: number }) {
  pagination.current = pageInfo.current ?? pagination.current;
  pagination.pageSize = pageInfo.pageSize ?? pagination.pageSize;
  fetchList();
}

async function handleExport() {
  exporting.value = true;
  try {
    const query = buildQueryParams();
    delete (query as { pageNum?: number }).pageNum;
    delete (query as { pageSize?: number }).pageSize;

    await exportLoginAuditLogs(query);
    message.success('导出成功');
  } finally {
    exporting.value = false;
  }
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
                <a-form-item label="登录账号">
                  <a-input
                    v-model:value="searchParams.username"
                    allow-clear
                    placeholder="请输入登录账号"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="登录方式">
                  <a-select
                    v-model:value="searchParams.loginType"
                    allow-clear
                    placeholder="请选择登录方式"
                    class="filter-control"
                    :options="loginTypeOptions"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="登录结果">
                  <a-select
                    v-model:value="searchParams.result"
                    allow-clear
                    placeholder="请选择登录结果"
                    class="filter-control"
                    :options="resultOptions"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="登录时间">
                  <a-range-picker
                    v-model:value="searchParams.loginTimeRange"
                    allow-clear
                    enable-time-picker
                    format="YYYY-MM-DD HH:mm:ss"
                    value-type="YYYY-MM-DD HH:mm:ss"
                    :placeholder="['开始时间', '结束时间']"
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
        <span></span>
        <div class="table-toolbar__actions table-toolbar__actions--right">
          <a-button
            v-auth="'audit:login:export'"
            variant="outlined"
            size="small"
            :loading="exporting"
            @click="handleExport"
          >
            <template #icon>
              <Download class="h-4 w-4" />
            </template>
            导出
          </a-button>
          <a-button variant="outlined" size="small" @click="fetchList">
            <template #icon>
              <RefreshCcw class="h-4 w-4" />
            </template>
            刷新
          </a-button>
        </div>
      </div>
      <a-table
        row-key="id"
        :data-source="auditList"
        :columns="columns"
        :loading="loading"
        hover
        bordered
        :pagination="{ ...pagination, total }"
        :scroll="tableScroll"
        @change="handlePageChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'loginType'">
            {{
              record.loginType === 'PASSWORD'
                ? '账号密码'
                : record.loginType === 'MOBILE_CODE'
                  ? '手机验证码'
                  : record.loginType === 'EMAIL_CODE'
                    ? '邮箱验证码'
                    : record.loginType
            }}
          </template>

          <template v-else-if="column.dataIndex === 'result'">
            <a-tag :color="record.result === 'SUCCESS' ? 'success' : 'danger'" variant="filled">
              {{ record.result === 'SUCCESS' ? '成功' : '失败' }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'failureReason'">
            {{ record.failureReason || '-' }}
          </template>
        </template>
      </a-table>
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
