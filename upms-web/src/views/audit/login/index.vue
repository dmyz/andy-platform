<script setup lang="ts">
import { Download, RefreshCcw, Search } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { download, get } from '@/utils/request'

interface LoginAuditItem {
  id: string
  username: string
  realName: string
  loginType: string
  ip: string
  browser: string
  loginTime: string
  result: string
  failureReason?: string | null
}

const loading = ref(false)
const exporting = ref(false)
const auditList = ref<LoginAuditItem[]>([])
const total = ref(0)

const searchParams = reactive({
  username: '',
  loginType: '',
  result: '',
  loginTimeRange: [] as string[],
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const columns = [
  { colKey: 'username', title: '登录账号', width: 140 },
  { colKey: 'realName', title: '用户姓名', width: 140 },
  { colKey: 'loginType', title: '登录方式', width: 120 },
  { colKey: 'ip', title: 'IP', width: 150 },
  { colKey: 'browser', title: '浏览器信息', ellipsis: true },
  { colKey: 'loginTime', title: '登录时间', width: 180 },
  { colKey: 'result', title: '登录结果', width: 110 },
  { colKey: 'failureReason', title: '失败原因', ellipsis: true },
]

function buildQueryParams() {
  const [startTime, endTime] = searchParams.loginTimeRange
  return {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    username: searchParams.username || undefined,
    loginType: searchParams.loginType || undefined,
    result: searchParams.result || undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await get<{
      list: LoginAuditItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/login-audit/page', buildQueryParams())
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
  searchParams.username = ''
  searchParams.loginType = ''
  searchParams.result = ''
  searchParams.loginTimeRange = []
  handleSearch()
}

function handlePageChange(pageInfo: { current: number, pageSize: number }) {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchList()
}

async function handleExport() {
  exporting.value = true
  try {
    const query = buildQueryParams()
    delete (query as { pageNum?: number }).pageNum
    delete (query as { pageSize?: number }).pageSize

    await download('/login-audit/export', { params: query, filename: 'login-audit.csv' })
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
  <PageContainer title="登录审计" description="按账号、登录方式、结果和时间范围检索登录行为，并支持导出。">
    <div class="mb-4 rounded-2xl bg-white p-4 shadow-sm">
      <t-form layout="inline" label-width="80px">
        <t-form-item label="登录账号">
          <t-input v-model="searchParams.username" clearable placeholder="请输入登录账号" style="width: 220px" />
        </t-form-item>
        <t-form-item label="登录方式">
          <t-select v-model="searchParams.loginType" clearable placeholder="请选择登录方式" style="width: 180px">
            <t-option value="PASSWORD" label="账号密码" />
            <t-option value="MOBILE_CODE" label="手机验证码" />
            <t-option value="EMAIL_CODE" label="邮箱验证码" />
          </t-select>
        </t-form-item>
        <t-form-item label="登录结果">
          <t-select v-model="searchParams.result" clearable placeholder="请选择登录结果" style="width: 160px">
            <t-option value="SUCCESS" label="成功" />
            <t-option value="FAIL" label="失败" />
          </t-select>
        </t-form-item>
        <t-form-item label="登录时间">
          <t-date-range-picker
            v-model="searchParams.loginTimeRange"
            clearable
            enable-time-picker
            format="YYYY-MM-DD HH:mm:ss"
            value-type="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择登录时间范围"
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
            <t-button v-auth="'audit:login:export'" theme="default" variant="outline" :loading="exporting" @click="handleExport">
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
        <template #loginType="{ row }">
          {{ row.loginType === 'PASSWORD' ? '账号密码' : row.loginType === 'MOBILE_CODE' ? '手机验证码' : row.loginType === 'EMAIL_CODE' ? '邮箱验证码' : row.loginType }}
        </template>

        <template #result="{ row }">
          <t-tag :theme="row.result === 'SUCCESS' ? 'success' : 'danger'" variant="light">
            {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
          </t-tag>
        </template>

        <template #failureReason="{ row }">
          {{ row.failureReason || '-' }}
        </template>
      </t-table>
    </div>
  </PageContainer>
</template>
