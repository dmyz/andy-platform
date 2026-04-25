<script setup lang="ts">
import { Download, Eye, RefreshCcw, Search } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { download, get } from '@/utils/request'

interface OperationAuditItem {
  id: string
  operatorName: string
  moduleName: string
  actionType: string
  requestUri: string
  durationMs: number
  operationTime: string
  result: string
}

interface OperationAuditDetail {
  id: string
  operatorName: string
  moduleName: string
  actionType: string
  requestMethod: string
  requestUri: string
  requestParams: string
  durationMs: number
  responseCode: number
  result: string
  errorMessage?: string | null
  operationTime: string
}

const loading = ref(false)
const exporting = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const auditList = ref<OperationAuditItem[]>([])
const detailData = ref<OperationAuditDetail | null>(null)
const total = ref(0)

const searchParams = reactive({
  operatorName: '',
  moduleName: '',
  actionType: '',
  result: '',
  operationTimeRange: [] as string[],
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const actionTypeLabelMap: Record<string, string> = {
  CREATE: '新增',
  UPDATE: '编辑',
  DELETE: '删除',
  ASSIGN_PERMISSION: '授权',
  ASSIGN_ROLE: '分配角色',
  RESET_PASSWORD: '重置密码',
  OFFLINE: '强制下线',
  STATUS: '状态变更',
}

function formatActionType(actionType: string) {
  return actionTypeLabelMap[actionType] || actionType
}

const columns = [
  { colKey: 'operatorName', title: '操作人', width: 140 },
  { colKey: 'moduleName', title: '模块名称', width: 140 },
  { colKey: 'actionType', title: '操作类型', width: 140 },
  { colKey: 'requestUri', title: '请求地址', ellipsis: true },
  { colKey: 'durationMs', title: '耗时(ms)', width: 110 },
  { colKey: 'operationTime', title: '操作时间', width: 180 },
  { colKey: 'result', title: '操作结果', width: 110 },
  { colKey: 'action', title: '操作', width: 100, fixed: 'right' as const },
]

function buildQueryParams() {
  const [startTime, endTime] = searchParams.operationTimeRange
  return {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    operatorName: searchParams.operatorName || undefined,
    moduleName: searchParams.moduleName || undefined,
    actionType: searchParams.actionType || undefined,
    result: searchParams.result || undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await get<{
      list: OperationAuditItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/operation-audit/page', buildQueryParams())
    auditList.value = res.list
    total.value = res.total
    pagination.current = res.pageNum
    pagination.pageSize = res.pageSize
  }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchList()
}

function handleReset() {
  searchParams.operatorName = ''
  searchParams.moduleName = ''
  searchParams.actionType = ''
  searchParams.result = ''
  searchParams.operationTimeRange = []
  handleSearch()
}

function handlePageChange(pageInfo: { current: number, pageSize: number }) {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchList()
}

async function handleViewDetail(row: OperationAuditItem) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailData.value = await get<OperationAuditDetail>(`/operation-audit/${row.id}`)
  }
  finally {
    detailLoading.value = false
  }
}

async function handleExport() {
  exporting.value = true
  try {
    const query = buildQueryParams()
    delete (query as { pageNum?: number }).pageNum
    delete (query as { pageSize?: number }).pageSize

    await download('/operation-audit/export', { params: query, filename: 'operation-audit.csv' })
    MessagePlugin.success('导出成功')
  }
  finally {
    exporting.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="操作审计" description="按操作人、模块、类型、结果和时间范围检索操作行为，支持查看详情和导出。">
    <div class="mb-4 rounded-2xl bg-white p-4 shadow-sm">
      <t-form layout="inline" label-width="80px">
        <t-form-item label="操作人">
          <t-input v-model="searchParams.operatorName" clearable placeholder="请输入操作人" style="width: 200px" />
        </t-form-item>
        <t-form-item label="模块名称">
          <t-input v-model="searchParams.moduleName" clearable placeholder="请输入模块名称" style="width: 200px" />
        </t-form-item>
        <t-form-item label="操作类型">
          <t-select v-model="searchParams.actionType" clearable placeholder="请选择操作类型" style="width: 180px">
            <t-option value="CREATE" label="新增" />
            <t-option value="UPDATE" label="编辑" />
            <t-option value="DELETE" label="删除" />
            <t-option value="ASSIGN_PERMISSION" label="授权" />
            <t-option value="ASSIGN_ROLE" label="分配角色" />
            <t-option value="RESET_PASSWORD" label="重置密码" />
            <t-option value="OFFLINE" label="强制下线" />
            <t-option value="STATUS" label="状态变更" />
          </t-select>
        </t-form-item>
        <t-form-item label="操作结果">
          <t-select v-model="searchParams.result" clearable placeholder="请选择操作结果" style="width: 160px">
            <t-option value="SUCCESS" label="成功" />
            <t-option value="FAIL" label="失败" />
          </t-select>
        </t-form-item>
        <t-form-item label="操作时间">
          <t-date-range-picker
            v-model="searchParams.operationTimeRange"
            clearable
            enable-time-picker
            format="YYYY-MM-DD HH:mm:ss"
            value-type="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择操作时间范围"
            style="width: 340px"
          />
        </t-form-item>
        <t-form-item>
          <div class="flex gap-2">
            <t-button theme="primary" @click="handleSearch">
              <template #icon>
                <Search class="h-4 w-4" />
              </template>
              查询
            </t-button>
            <t-button theme="default" variant="outline" @click="handleReset">
              <template #icon>
                <RefreshCcw class="h-4 w-4" />
              </template>
              重置
            </t-button>
            <t-button v-auth="'audit:operation:export'" theme="default" variant="outline" :loading="exporting" @click="handleExport">
              <template #icon>
                <Download class="h-4 w-4" />
              </template>
              导出
            </t-button>
          </div>
        </t-form-item>
      </t-form>
    </div>

    <div class="rounded-2xl bg-white p-4 shadow-sm">
      <t-table
        row-key="id"
        :data="auditList"
        :columns="columns"
        :loading="loading"
        hover
        bordered
        :pagination="{ ...pagination, total }"
        @page-change="handlePageChange"
      >
        <template #actionType="{ row }">
          {{ formatActionType(row.actionType) }}
        </template>

        <template #result="{ row }">
          <t-tag :theme="row.result === 'SUCCESS' ? 'success' : 'danger'" variant="light">
            {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
          </t-tag>
        </template>

        <template #action="{ row }">
          <t-button theme="primary" variant="text" size="small" @click="handleViewDetail(row)">
            <template #icon>
              <Eye class="h-4 w-4" />
            </template>
            详情
          </t-button>
        </template>
      </t-table>
    </div>

    <t-dialog v-model:visible="detailVisible" header="操作审计详情" width="720px" :footer="false">
      <div v-if="detailLoading" class="py-10 text-center text-sm text-[#86909c]">
        详情加载中...
      </div>
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
          <div class="mt-1 text-sm text-[#1d2129]">{{ formatActionType(detailData.actionType) }}</div>
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
          <pre class="mt-1 overflow-x-auto rounded-lg bg-[#f5f7fa] p-3 text-xs leading-6 text-[#1d2129]">{{ detailData.requestParams }}</pre>
        </div>
        <div class="md:col-span-2">
          <div class="text-sm text-[#86909c]">异常信息</div>
          <div class="mt-1 text-sm text-[#1d2129]">{{ detailData.errorMessage || '-' }}</div>
        </div>
      </div>
    </t-dialog>
  </PageContainer>
</template>
