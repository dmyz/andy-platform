<script setup lang="ts">
import { Monitor, RefreshCcw, Search } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { get, post } from '@/utils/request'

interface SessionItem {
  sessionKey: string
  username: string
  displayName: string
  orgName?: string
  ip?: string
  authType: string
  status: string
  loginTime: string
  lastAccessTime?: string
  currentSession: boolean
}

const loading = ref(false)
const sessionList = ref<SessionItem[]>([])
const total = ref(0)

const searchParams = reactive({
  username: '',
  displayName: '',
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const columns = [
  { colKey: 'username', title: '用户名', width: 140 },
  { colKey: 'displayName', title: '姓名', width: 140 },
  { colKey: 'orgName', title: '所属组织', width: 150, ellipsis: true },
  { colKey: 'ip', title: '登录 IP', width: 150 },
  { colKey: 'authType', title: '登录方式', width: 120 },
  { colKey: 'loginTime', title: '登录时间', width: 180 },
  { colKey: 'lastAccessTime', title: '最近访问', width: 180 },
  { colKey: 'status', title: '状态', width: 100, cell: 'statusSlot' },
  { colKey: 'action', title: '操作', width: 140, fixed: 'right' as const },
]

async function fetchList() {
  loading.value = true
  try {
    const res = await get<{
      list: SessionItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/auth/session/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      username: searchParams.username || undefined,
      displayName: searchParams.displayName || undefined,
    })
    sessionList.value = res.list
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
  searchParams.displayName = ''
  handleSearch()
}

function handlePageChange(pageInfo: { current: number, pageSize: number }) {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchList()
}

async function handleOffline(row: SessionItem) {
  const confirm = await MessagePlugin.question(`确认强制下线会话 ${row.username} 吗?`)
  if (!confirm) {
    return
  }

  try {
    await post('/auth/session/offline', {
      sessionKey: row.sessionKey,
    })
    MessagePlugin.success('已强制下线')
    fetchList()
  }
  catch (error) {}
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="在线会话" description="查看在线会话并执行强制下线。">
    <div class="mb-4 rounded-2xl bg-white p-4 shadow-sm dark:bg-gray-800">
      <div class="flex flex-wrap gap-3">
        <t-input v-model="searchParams.username" placeholder="请输入用户名" clearable style="width: 220px" />
        <t-input v-model="searchParams.displayName" placeholder="请输入姓名" clearable style="width: 220px" />
        <t-button theme="primary" @click="handleSearch">
          <template #icon>
            <Search class="w-4 h-4" />
          </template>
          查询
        </t-button>
        <t-button theme="default" @click="handleReset">
          <template #icon>
            <RefreshCcw class="w-4 h-4" />
          </template>
          重置
        </t-button>
      </div>
    </div>

    <div class="rounded-2xl bg-white p-4 shadow-sm dark:bg-gray-800">
      <t-table
        row-key="sessionKey"
        :data="sessionList"
        :columns="columns"
        :loading="loading"
        bordered
        hover
      >
        <template #statusSlot="{ row }">
          <t-tag :theme="row.status === 'ONLINE' ? 'success' : 'warning'" variant="light">
            {{ row.status === 'ONLINE' ? '在线' : row.status }}
          </t-tag>
        </template>

        <template #orgName="{ row }">
          {{ row.orgName || '-' }}
        </template>

        <template #ip="{ row }">
          {{ row.ip || '-' }}
        </template>

        <template #lastAccessTime="{ row }">
          {{ row.lastAccessTime || '-' }}
        </template>

        <template #authType="{ row }">
          <div class="flex items-center gap-2">
            <Monitor class="h-4 w-4 text-[#0052d9]" />
            <span>{{ row.authType }}</span>
          </div>
        </template>

        <template #action="{ row }">
          <t-button
            v-auth="'auth:session:offline'"
            theme="danger"
            variant="text"
            size="small"
            :disabled="row.currentSession"
            @click="handleOffline(row)"
          >
            {{ row.currentSession ? '当前会话' : '强制下线' }}
          </t-button>
        </template>
      </t-table>

      <div class="mt-4 flex justify-end">
        <t-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="total"
          :show-jumper="pagination.showJumper"
          @change="handlePageChange"
        />
      </div>
    </div>
  </PageContainer>
</template>
