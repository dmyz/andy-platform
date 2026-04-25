<script setup lang="ts">
import { Edit, Plus, RefreshCcw, Search, Send, Trash2, Undo2 } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { del, get, post, put } from '@/utils/request'

interface AnnouncementItem {
  id: string
  title: string
  type: string
  status: string
  top: boolean
  targetType: string
  targetValue?: string | null
  creatorName: string
  publishTime?: string | null
  updateTime: string
}

interface AnnouncementDetail extends AnnouncementItem {
  content: string
}

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const total = ref(0)
const announcementList = ref<AnnouncementItem[]>([])

const searchParams = reactive({
  title: '',
  type: '',
  status: '',
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const formRef = ref()
const formData = reactive({
  id: '',
  title: '',
  content: '',
  type: 'NOTICE',
  targetType: 'ALL',
  targetValue: '',
  top: false,
})

const formRules = {
  title: [{ required: true, message: '公告标题必填', type: 'error' as const }],
  content: [{ required: true, message: '公告内容必填', type: 'error' as const }],
  type: [{ required: true, message: '公告类型必填', type: 'error' as const }],
  targetType: [{ required: true, message: '投放范围必填', type: 'error' as const }],
}

const columns = [
  { colKey: 'title', title: '公告标题', ellipsis: true },
  { colKey: 'type', title: '类型', width: 120 },
  { colKey: 'status', title: '状态', width: 110 },
  { colKey: 'targetType', title: '投放范围', width: 120 },
  { colKey: 'targetValue', title: '目标值', ellipsis: true },
  { colKey: 'top', title: '置顶', width: 80 },
  { colKey: 'publishTime', title: '发布时间', width: 180 },
  { colKey: 'action', title: '操作', width: 280, fixed: 'right' as const },
]

async function fetchList() {
  loading.value = true
  try {
    const res = await get<{
      list: AnnouncementItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/announcement/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      title: searchParams.title || undefined,
      type: searchParams.type || undefined,
      status: searchParams.status || undefined,
    })
    announcementList.value = res.list
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
  searchParams.type = ''
  searchParams.status = ''
  handleSearch()
}

function handlePageChange(pageInfo: { current: number, pageSize: number }) {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchList()
}

function resetForm() {
  Object.assign(formData, {
    id: '',
    title: '',
    content: '',
    type: 'NOTICE',
    targetType: 'ALL',
    targetValue: '',
    top: false,
  })
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(row: AnnouncementItem) {
  const detail = await get<AnnouncementDetail>(`/announcement/${row.id}`)
  Object.assign(formData, {
    id: detail.id,
    title: detail.title,
    content: detail.content,
    type: detail.type,
    targetType: detail.targetType,
    targetValue: detail.targetValue || '',
    top: detail.top,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const validateResult = await formRef.value?.validate()
  if (validateResult !== true) {
    return
  }
  if (formData.targetType !== 'ALL' && !formData.targetValue.trim()) {
    MessagePlugin.warning('非全部投放时目标值不能为空')
    return
  }
  submitting.value = true
  try {
    const payload = {
      title: formData.title,
      content: formData.content,
      type: formData.type,
      targetType: formData.targetType,
      targetValue: formData.targetType === 'ALL' ? null : formData.targetValue.trim(),
      top: formData.top,
    }
    if (formData.id) {
      await put(`/announcement/${formData.id}`, payload)
      MessagePlugin.success('公告修改成功')
    }
    else {
      await post('/announcement', payload)
      MessagePlugin.success('公告新增成功')
    }
    dialogVisible.value = false
    fetchList()
  }
  finally {
    submitting.value = false
  }
}

async function handleDelete(row: AnnouncementItem) {
  if (!window.confirm(`确认删除公告「${row.title}」吗？`)) {
    return
  }
  await del(`/announcement/${row.id}`)
  MessagePlugin.success('公告已删除')
  fetchList()
}

async function handlePublish(row: AnnouncementItem) {
  await post(`/announcement/${row.id}/publish`)
  MessagePlugin.success('公告已发布')
  fetchList()
}

async function handleRevoke(row: AnnouncementItem) {
  await post(`/announcement/${row.id}/revoke`)
  MessagePlugin.success('公告已撤回')
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="公告管理" description="管理公告草稿、发布状态和投放范围。">
    <div class="mb-4 rounded-2xl bg-white p-4 shadow-sm">
      <t-form layout="inline" label-width="80px">
        <t-form-item label="公告标题">
          <t-input v-model="searchParams.title" clearable placeholder="请输入公告标题" style="width: 220px" />
        </t-form-item>
        <t-form-item label="公告类型">
          <t-select v-model="searchParams.type" clearable placeholder="请选择类型" style="width: 160px">
            <t-option value="SYSTEM" label="系统通知" />
            <t-option value="NOTICE" label="业务通知" />
            <t-option value="FEATURE" label="功能发布" />
            <t-option value="SECURITY" label="安全提醒" />
          </t-select>
        </t-form-item>
        <t-form-item label="公告状态">
          <t-select v-model="searchParams.status" clearable placeholder="请选择状态" style="width: 160px">
            <t-option value="DRAFT" label="草稿" />
            <t-option value="PUBLISHED" label="已发布" />
            <t-option value="REVOKED" label="已撤回" />
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
            <t-button v-auth="'announcement:manage:create'" theme="primary" @click="openCreateDialog">
              <template #icon>
                <Plus class="h-4 w-4" />
              </template>
              新增公告
            </t-button>
          </div>
        </t-form-item>
      </t-form>
    </div>

    <div class="rounded-2xl bg-white p-4 shadow-sm">
      <t-table
        row-key="id"
        :data="announcementList"
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
        <template #status="{ row }">
          <t-tag :theme="row.status === 'PUBLISHED' ? 'success' : row.status === 'REVOKED' ? 'warning' : 'default'" variant="light">
            {{ row.status === 'PUBLISHED' ? '已发布' : row.status === 'REVOKED' ? '已撤回' : '草稿' }}
          </t-tag>
        </template>
        <template #targetType="{ row }">
          {{ row.targetType === 'ALL' ? '全部用户' : row.targetType === 'ORG' ? '组织' : row.targetType === 'ROLE' ? '角色' : '用户' }}
        </template>
        <template #targetValue="{ row }">
          {{ row.targetValue || '-' }}
        </template>
        <template #top="{ row }">
          {{ row.top ? '是' : '否' }}
        </template>
        <template #publishTime="{ row }">
          {{ row.publishTime || '-' }}
        </template>
        <template #action="{ row }">
          <div class="flex items-center gap-1">
            <t-button v-auth="'announcement:manage:update'" theme="primary" variant="text" size="small" @click="openEditDialog(row)">
              <Edit class="h-4 w-4" />
            </t-button>
            <t-button v-if="row.status !== 'PUBLISHED'" v-auth="'announcement:manage:publish'" theme="primary" variant="text" size="small" @click="handlePublish(row)">
              <Send class="h-4 w-4" />
            </t-button>
            <t-button v-if="row.status === 'PUBLISHED'" v-auth="'announcement:manage:revoke'" theme="warning" variant="text" size="small" @click="handleRevoke(row)">
              <Undo2 class="h-4 w-4" />
            </t-button>
            <t-button v-auth="'announcement:manage:delete'" theme="danger" variant="text" size="small" @click="handleDelete(row)">
              <Trash2 class="h-4 w-4" />
            </t-button>
          </div>
        </template>
      </t-table>
    </div>

    <t-dialog v-model:visible="dialogVisible" :header="formData.id ? '编辑公告' : '新增公告'" width="720px" :on-confirm="handleSubmit" :confirm-btn="{ loading: submitting }">
      <t-form ref="formRef" :data="formData" :rules="formRules" label-width="100px">
        <t-form-item label="公告标题" name="title">
          <t-input v-model="formData.title" maxlength="60" />
        </t-form-item>
        <t-form-item label="公告类型" name="type">
          <t-select v-model="formData.type">
            <t-option value="SYSTEM" label="系统通知" />
            <t-option value="NOTICE" label="业务通知" />
            <t-option value="FEATURE" label="功能发布" />
            <t-option value="SECURITY" label="安全提醒" />
          </t-select>
        </t-form-item>
        <t-form-item label="投放范围" name="targetType">
          <t-select v-model="formData.targetType">
            <t-option value="ALL" label="全部用户" />
            <t-option value="ORG" label="组织" />
            <t-option value="ROLE" label="角色" />
            <t-option value="USER" label="用户" />
          </t-select>
        </t-form-item>
        <t-form-item label="目标值">
          <t-input v-model="formData.targetValue" :disabled="formData.targetType === 'ALL'" placeholder="组织名 / 角色编码 / 用户名" />
        </t-form-item>
        <t-form-item label="是否置顶">
          <t-switch v-model="formData.top" />
        </t-form-item>
        <t-form-item label="公告内容" name="content">
          <t-textarea v-model="formData.content" :maxlength="1000" autosize />
        </t-form-item>
      </t-form>
    </t-dialog>
  </PageContainer>
</template>
