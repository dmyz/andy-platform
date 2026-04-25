<script setup lang="ts">
import { MailOpen, RefreshCcw, Search } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { get, post } from '@/utils/request'

interface InboxItem {
  id: string
  title: string
  type: string
  publishTime: string
  read: boolean
  top: boolean
  content: string
}

const loading = ref(false)
const dialogVisible = ref(false)
const total = ref(0)
const messageList = ref<InboxItem[]>([])
const currentMessage = ref<InboxItem | null>(null)

const searchParams = reactive({
  title: '',
  read: '' as '' | 'true' | 'false',
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const columns = [
  { colKey: 'title', title: '公告标题', ellipsis: true },
  { colKey: 'type', title: '类型', width: 120 },
  { colKey: 'top', title: '置顶', width: 80 },
  { colKey: 'publishTime', title: '发布时间', width: 180 },
  { colKey: 'read', title: '已读状态', width: 100 },
  { colKey: 'action', title: '操作', width: 180, fixed: 'right' as const },
]

async function fetchList() {
  loading.value = true
  try {
    const res = await get<{
      list: InboxItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/announcement/my/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      title: searchParams.title || undefined,
      read: searchParams.read === '' ? undefined : searchParams.read === 'true',
    })
    messageList.value = res.list
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
  searchParams.title = ''
  searchParams.read = ''
  handleSearch()
}

function handlePageChange(pageInfo: { current: number, pageSize: number }) {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchList()
}

async function markRead(row: InboxItem) {
  if (row.read) {
    return
  }
  await post(`/announcement/${row.id}/read`)
  if (searchParams.read === 'false') {
    await fetchList()
    return
  }
  row.read = true
}

async function openDetail(row: InboxItem) {
  currentMessage.value = row
  dialogVisible.value = true
  if (!row.read) {
    await markRead(row)
  }
}

async function handleRead(row: InboxItem) {
  await markRead(row)
  MessagePlugin.success('已标记为已读')
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="消息中心" description="查看当前用户收到的公告，并支持已读处理。">
    <div class="mb-4 rounded-2xl bg-white p-4 shadow-sm">
      <t-form layout="inline" label-width="80px">
        <t-form-item label="公告标题">
          <t-input v-model="searchParams.title" clearable placeholder="请输入公告标题" style="width: 220px" />
        </t-form-item>
        <t-form-item label="已读状态">
          <t-select v-model="searchParams.read" clearable placeholder="请选择已读状态" style="width: 160px">
            <t-option value="true" label="已读" />
            <t-option value="false" label="未读" />
          </t-select>
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
          </div>
        </t-form-item>
      </t-form>
    </div>

    <div class="rounded-2xl bg-white p-4 shadow-sm">
      <t-table
        row-key="id"
        :data="messageList"
        :columns="columns"
        :loading="loading"
        bordered
        hover
        :pagination="{ ...pagination, total }"
        @page-change="handlePageChange"
      >
        <template #type="{ row }">
          {{ row.type === 'SYSTEM' ? '系统通知' : row.type === 'NOTICE' ? '业务通知' : row.type === 'FEATURE' ? '功能发布' : '安全提醒' }}
        </template>
        <template #top="{ row }">
          {{ row.top ? '是' : '否' }}
        </template>
        <template #read="{ row }">
          <t-tag :theme="row.read ? 'success' : 'warning'" variant="light">
            {{ row.read ? '已读' : '未读' }}
          </t-tag>
        </template>
        <template #action="{ row }">
          <div class="flex items-center gap-2">
            <t-button theme="primary" variant="text" size="small" @click="openDetail(row)">
              查看
            </t-button>
            <t-button v-if="!row.read" v-auth="'announcement:inbox:read'" theme="primary" variant="text" size="small" @click="handleRead(row)">
              <MailOpen class="h-4 w-4" />
            </t-button>
          </div>
        </template>
      </t-table>
    </div>

    <t-dialog v-model:visible="dialogVisible" header="公告详情" width="720px" :footer="false">
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
    </t-dialog>
  </PageContainer>
</template>
