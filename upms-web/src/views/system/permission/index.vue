<script setup lang="ts">
import { Edit, Plus, RefreshCcw, Search, Trash2 } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { del, get, post, put } from '@/utils/request'

interface PermissionItem {
  id: string
  name: string
  code: string
  type: 'API' | 'UI_ACTION' | 'DATA' | 'NAV_ACCESS'
  resourceType: string
  actionCode?: string | null
  moduleCode: string
  status: number
  remark?: string | null
  updateTime: string
}

interface PermissionDetail extends PermissionItem {}

const typeOptions = [
  { label: 'API', value: 'API' },
  { label: '页面动作', value: 'UI_ACTION' },
  { label: '数据权限', value: 'DATA' },
  { label: '导航访问', value: 'NAV_ACCESS' },
]

const typeLabelMap: Record<string, string> = {
  API: 'API',
  UI_ACTION: '页面动作',
  DATA: '数据权限',
  NAV_ACCESS: '导航访问',
}

const loading = ref(false)
const permissionList = ref<PermissionItem[]>([])
const total = ref(0)

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const searchParams = reactive({
  name: '',
  code: '',
  type: undefined as string | undefined,
  status: undefined as number | undefined,
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增权限')
const submitting = ref(false)

const formRef = ref()
const formData = reactive({
  id: '',
  name: '',
  code: '',
  type: 'UI_ACTION',
  resourceType: 'PERMISSION',
  actionCode: '',
  moduleCode: 'system',
  status: 1,
  remark: '',
})

const formRules = {
  name: [{ required: true, message: '权限名称必填', type: 'error' as const }],
  code: [{ required: true, message: '权限编码必填', type: 'error' as const }],
  type: [{ required: true, message: '权限类型必填', type: 'error' as const }],
  resourceType: [{ required: true, message: '资源类型必填', type: 'error' as const }],
  moduleCode: [{ required: true, message: '模块编码必填', type: 'error' as const }],
}

const columns = [
  { colKey: 'name', title: '权限名称', width: 160, ellipsis: true },
  { colKey: 'code', title: '权限编码', width: 240, ellipsis: true },
  { colKey: 'type', title: '权限类型', width: 110 },
  { colKey: 'resourceType', title: '资源类型', width: 120 },
  { colKey: 'actionCode', title: '动作编码', width: 130 },
  { colKey: 'moduleCode', title: '模块编码', width: 110 },
  { colKey: 'status', title: '状态', width: 90 },
  { colKey: 'remark', title: '备注', ellipsis: true },
  { colKey: 'updateTime', title: '更新时间', width: 180 },
  { colKey: 'action', title: '操作', width: 160, fixed: 'right' as const },
]

async function fetchList() {
  loading.value = true
  try {
    const res = await get<{
      list: PermissionItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/permission/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      name: searchParams.name || undefined,
      code: searchParams.code || undefined,
      type: searchParams.type,
      status: searchParams.status,
    })
    permissionList.value = res.list
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
  searchParams.name = ''
  searchParams.code = ''
  searchParams.type = undefined
  searchParams.status = undefined
  handleSearch()
}

function handlePageChange(pageInfo: { current: number, pageSize: number }) {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchList()
}

function handleAdd() {
  dialogTitle.value = '新增权限'
  Object.assign(formData, {
    id: '',
    name: '',
    code: '',
    type: 'UI_ACTION',
    resourceType: 'PERMISSION',
    actionCode: '',
    moduleCode: 'system',
    status: 1,
    remark: '',
  })
  dialogVisible.value = true
}

async function handleEdit(row: PermissionItem) {
  const detail = await get<PermissionDetail>(`/permission/${row.id}`)
  dialogTitle.value = '编辑权限'
  Object.assign(formData, {
    id: detail.id,
    name: detail.name,
    code: detail.code,
    type: detail.type,
    resourceType: detail.resourceType,
    actionCode: detail.actionCode || '',
    moduleCode: detail.moduleCode,
    status: detail.status,
    remark: detail.remark || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const validateResult = await formRef.value?.validate()
  if (validateResult !== true) {
    return
  }

  submitting.value = true
  try {
    const payload = {
      name: formData.name,
      code: formData.code,
      type: formData.type,
      resourceType: formData.resourceType,
      actionCode: formData.actionCode || null,
      moduleCode: formData.moduleCode,
      status: formData.status,
      remark: formData.remark || null,
    }

    if (formData.id) {
      await put(`/permission/${formData.id}`, payload)
      MessagePlugin.success('修改成功')
    }
    else {
      await post('/permission', payload)
      MessagePlugin.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  }
  finally {
    submitting.value = false
  }
}

async function handleDelete(row: PermissionItem) {
  const confirm = await MessagePlugin.question(`确认删除权限“${row.name}”吗？`)
  if (!confirm) {
    return
  }

  try {
    await del(`/permission/${row.id}`)
    MessagePlugin.success('删除成功')
    fetchList()
  }
  catch (error) {}
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="权限定义" description="统一维护 API、页面动作、数据权限与导航访问权限。">
    <template #extra>
      <t-button v-auth="'system:permission:create'" theme="primary" @click="handleAdd">
        <template #icon>
          <Plus class="h-4 w-4" />
        </template>
        新增权限
      </t-button>
    </template>

    <div class="flex flex-col gap-4">
      <t-card :bordered="false" class="rounded-lg shadow-sm" size="small">
        <t-form layout="inline" label-width="80px">
          <t-form-item label="权限名称">
            <t-input v-model="searchParams.name" clearable placeholder="请输入权限名称" style="width: 200px" />
          </t-form-item>
          <t-form-item label="权限编码">
            <t-input v-model="searchParams.code" clearable placeholder="请输入权限编码" style="width: 240px" />
          </t-form-item>
          <t-form-item label="权限类型">
            <t-select v-model="searchParams.type" clearable placeholder="请选择权限类型" style="width: 160px">
              <t-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </t-select>
          </t-form-item>
          <t-form-item label="状态">
            <t-select v-model="searchParams.status" clearable placeholder="请选择状态" style="width: 140px">
              <t-option :value="1" label="启用" />
              <t-option :value="0" label="禁用" />
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
      </t-card>

      <t-card :bordered="false" class="flex-1 rounded-lg shadow-sm">
        <t-table
          row-key="id"
          :data="permissionList"
          :columns="columns"
          :loading="loading"
          :pagination="{ ...pagination, total }"
          hover
          stripe
          @page-change="handlePageChange"
        >
          <template #type="{ row }">
            <t-tag
              :theme="row.type === 'API' ? 'warning' : row.type === 'DATA' ? 'default' : 'primary'"
              variant="light"
              size="small"
            >
              {{ typeLabelMap[row.type] || row.type }}
            </t-tag>
          </template>

          <template #actionCode="{ row }">
            {{ row.actionCode || '-' }}
          </template>

          <template #status="{ row }">
            <t-tag :theme="row.status === 1 ? 'success' : 'warning'" variant="light" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </t-tag>
          </template>

          <template #remark="{ row }">
            {{ row.remark || '-' }}
          </template>

          <template #action="{ row }">
            <div class="flex gap-2">
              <t-button v-auth="'system:permission:update'" theme="primary" variant="text" size="small" @click="handleEdit(row)">
                <template #icon>
                  <Edit class="h-3.5 w-3.5" />
                </template>
                编辑
              </t-button>
              <t-button v-auth="'system:permission:delete'" theme="danger" variant="text" size="small" @click="handleDelete(row)">
                <template #icon>
                  <Trash2 class="h-3.5 w-3.5" />
                </template>
                删除
              </t-button>
            </div>
          </template>
        </t-table>
      </t-card>
    </div>

    <t-dialog
      v-model:visible="dialogVisible"
      :header="dialogTitle"
      width="680px"
      :on-confirm="handleSubmit"
      :confirm-btn="{ loading: submitting }"
    >
      <t-form ref="formRef" :data="formData" :rules="formRules" label-align="right" label-width="100px">
        <t-form-item label="权限名称" name="name">
          <t-input v-model="formData.name" placeholder="请输入权限名称" />
        </t-form-item>
        <t-form-item label="权限编码" name="code">
          <t-input v-model="formData.code" placeholder="如 system:permission:create" />
        </t-form-item>
        <t-form-item label="权限类型" name="type">
          <t-select v-model="formData.type" placeholder="请选择权限类型">
            <t-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </t-select>
        </t-form-item>
        <t-form-item label="资源类型" name="resourceType">
          <t-input v-model="formData.resourceType" placeholder="如 PERMISSION / USER / NAVIGATION" />
        </t-form-item>
        <t-form-item label="动作编码" name="actionCode">
          <t-input v-model="formData.actionCode" placeholder="如 CREATE / UPDATE / VIEW" />
        </t-form-item>
        <t-form-item label="模块编码" name="moduleCode">
          <t-input v-model="formData.moduleCode" placeholder="如 system / auth / profile" />
        </t-form-item>
        <t-form-item label="状态" name="status">
          <t-radio-group v-model="formData.status">
            <t-radio :value="1">
              启用
            </t-radio>
            <t-radio :value="0">
              禁用
            </t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="备注" name="remark">
          <t-textarea v-model="formData.remark" :maxlength="200" placeholder="请输入备注" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </PageContainer>
</template>
