<script setup lang="ts">
import { Download, Eye, RefreshCcw, Search } from '@lucide/vue';
import { message } from 'antdv-next';
import { onMounted, reactive, ref } from 'vue';
import {
  exportOperationAuditLogs,
  getOperationAuditDetail,
  getOperationAuditLogs,
} from '@/api/audit';

interface OperationAuditItem {
  id: string;
  operatorName: string;
  moduleName: string;
  actionType: string;
  requestUri: string;
  durationMs: number;
  operationTime: string;
  result: string;
}

interface OperationAuditDetail {
  id: string;
  operatorName: string;
  moduleName: string;
  actionType: string;
  requestMethod: string;
  requestUri: string;
  requestParams: string;
  durationMs: number;
  responseCode: number;
  result: string;
  errorMessage?: string | null;
  operationTime: string;
}

const loading = ref(false);
const exporting = ref(false);
const detailVisible = ref(false);
const detailLoading = ref(false);
const auditList = ref<OperationAuditItem[]>([]);
const detailData = ref<OperationAuditDetail | null>(null);
const total = ref(0);

const searchParams = reactive({
  operatorName: '',
  moduleName: '',
  actionType: '',
  result: '',
  operationTimeRange: [] as string[],
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const actionTypeLabelMap: Record<string, string> = {
  CREATE: '新增',
  UPDATE: '编辑',
  DELETE: '删除',
  ASSIGN_PERMISSION: '授权',
  ASSIGN_ROLE: '分配角色',
  RESET_PASSWORD: '重置密码',
  OFFLINE: '强制下线',
  STATUS: '状态变更',
};

const actionTypeOptions = Object.entries(actionTypeLabelMap).map(([value, label]) => ({
  label,
  value,
}));

const resultOptions = [
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAIL' },
];

function formatActionType(actionType: string) {
  return actionTypeLabelMap[actionType] || actionType;
}

const columns = [
  { dataIndex: 'operatorName', title: '操作人', width: 140 },
  { dataIndex: 'moduleName', title: '模块名称', width: 140 },
  { dataIndex: 'actionType', title: '操作类型', width: 140 },
  { dataIndex: 'requestUri', title: '请求地址', ellipsis: true },
  { dataIndex: 'durationMs', title: '耗时(ms)', width: 110 },
  { dataIndex: 'operationTime', title: '操作时间', width: 180 },
  { dataIndex: 'result', title: '操作结果', width: 110 },
  { dataIndex: 'action', title: '操作', width: 100, fixed: 'right' as const },
];

const tableScroll = { x: 1220 };

function buildQueryParams() {
  const [startTime, endTime] = searchParams.operationTimeRange;
  return {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    operatorName: searchParams.operatorName || undefined,
    moduleName: searchParams.moduleName || undefined,
    actionType: searchParams.actionType || undefined,
    result: searchParams.result || undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
  };
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await getOperationAuditLogs(buildQueryParams());
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
  searchParams.operatorName = '';
  searchParams.moduleName = '';
  searchParams.actionType = '';
  searchParams.result = '';
  searchParams.operationTimeRange = [];
  handleSearch();
}

function handlePageChange(pageInfo: { current?: number; pageSize?: number }) {
  pagination.current = pageInfo.current ?? pagination.current;
  pagination.pageSize = pageInfo.pageSize ?? pagination.pageSize;
  fetchList();
}

async function handleViewDetail(row: OperationAuditItem) {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    detailData.value = await getOperationAuditDetail(row.id);
  } finally {
    detailLoading.value = false;
  }
}

async function handleExport() {
  exporting.value = true;
  try {
    const query = buildQueryParams();
    delete (query as { pageNum?: number }).pageNum;
    delete (query as { pageSize?: number }).pageSize;

    await exportOperationAuditLogs(query);
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
                <a-form-item label="操作人">
                  <a-input
                    v-model:value="searchParams.operatorName"
                    allow-clear
                    placeholder="请输入操作人"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="模块名称">
                  <a-input
                    v-model:value="searchParams.moduleName"
                    allow-clear
                    placeholder="请输入模块名称"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="操作类型">
                  <a-select
                    v-model:value="searchParams.actionType"
                    allow-clear
                    placeholder="请选择操作类型"
                    class="filter-control"
                    :options="actionTypeOptions"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="操作结果">
                  <a-select
                    v-model:value="searchParams.result"
                    allow-clear
                    placeholder="请选择操作结果"
                    class="filter-control"
                    :options="resultOptions"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="操作时间">
                  <a-range-picker
                    v-model:value="searchParams.operationTimeRange"
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
            v-auth="'audit:operation:export'"
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
          <template v-if="column.dataIndex === 'actionType'">
            {{ formatActionType(record.actionType) }}
          </template>

          <template v-else-if="column.dataIndex === 'result'">
            <a-tag :color="record.result === 'SUCCESS' ? 'success' : 'danger'" variant="filled">
              {{ record.result === 'SUCCESS' ? '成功' : '失败' }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'action'">
            <div class="row-actions">
              <a-button
                type="primary"
                variant="text"
                size="small"
                @click="handleViewDetail(record)"
              >
                <template #icon>
                  <Eye class="h-4 w-4" />
                </template>
                详情
              </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="detailVisible" title="操作审计详情" width="720px" :footer="null">
      <div v-if="detailLoading" class="py-10 text-center text-sm text-[#86909c]">详情加载中...</div>
      <div v-else-if="detailData" class="grid gap-4 md:grid-cols-2">
        <div>
          <div class="text-sm text-[#86909c]">操作人</div>
          <div class="mt-1 text-sm text-[#1d2129]">{{ detailData.operatorName }}</div>
        </div>
        <div>
          <div class="text-sm text-[#86909c]">模块名称</div>
          <div class="mt-1 text-sm text-[#1d2129]">{{ detailData.moduleName }}</div>
        </div>
        <div>
          <div class="text-sm text-[#86909c]">操作类型</div>
          <div class="mt-1 text-sm text-[#1d2129]">
            {{ formatActionType(detailData.actionType) }}
          </div>
        </div>
        <div>
          <div class="text-sm text-[#86909c]">请求方法</div>
          <div class="mt-1 text-sm text-[#1d2129]">{{ detailData.requestMethod }}</div>
        </div>
        <div class="md:col-span-2">
          <div class="text-sm text-[#86909c]">请求地址</div>
          <div class="mt-1 break-all text-sm text-[#1d2129]">{{ detailData.requestUri }}</div>
        </div>
        <div>
          <div class="text-sm text-[#86909c]">耗时</div>
          <div class="mt-1 text-sm text-[#1d2129]">{{ detailData.durationMs }} ms</div>
        </div>
        <div>
          <div class="text-sm text-[#86909c]">响应码</div>
          <div class="mt-1 text-sm text-[#1d2129]">{{ detailData.responseCode }}</div>
        </div>
        <div>
          <div class="text-sm text-[#86909c]">操作结果</div>
          <div class="mt-1 text-sm text-[#1d2129]">{{ detailData.result }}</div>
        </div>
        <div>
          <div class="text-sm text-[#86909c]">操作时间</div>
          <div class="mt-1 text-sm text-[#1d2129]">{{ detailData.operationTime }}</div>
        </div>
        <div class="md:col-span-2">
          <div class="text-sm text-[#86909c]">请求参数</div>
          <pre
            class="mt-1 overflow-x-auto rounded-lg bg-[#f5f7fa] p-3 text-xs leading-6 text-[#1d2129]"
            >{{ detailData.requestParams }}</pre
          >
        </div>
        <div class="md:col-span-2">
          <div class="text-sm text-[#86909c]">异常信息</div>
          <div class="mt-1 text-sm text-[#1d2129]">{{ detailData.errorMessage || '-' }}</div>
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
