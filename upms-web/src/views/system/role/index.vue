<script setup lang="ts">
import { Edit, Plus, RefreshCcw, Search, Trash2, Users } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { del, get, post, put } from '@/utils/request'

interface RoleItem {
  id: string
  name: string
  code: string
  dataScope: string
  permissionCount: number
  status: number
  remark?: string
  createTime: string
}

interface RoleDetail {
  id: string
  name: string
  code: string
  dataScope: string
  status: number
  remark?: string
  createTime: string
}

interface PermissionItem {
  code: string
  name: string
  type: string
  category: string
}

interface RelatedUserItem {
  id: string
  username: string
  realName: string
  orgName: string
  status: number
}

const loading = ref(false)
const roleList = ref<RoleItem[]>([])
const total = ref(0)
const allPermissions = ref<PermissionItem[]>([])
const selectedPermissions = ref<string[]>([])
const relatedUsers = ref<RelatedUserItem[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const searchParams = reactive({
  name: '',
  code: '',
  status: undefined as number | undefined,
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const submitting = ref(false)

const permissionDialogVisible = ref(false)
const permissionLoading = ref(false)
const permissionSearchKeyword = ref('')
const permissionCategoryFilter = ref('all')

const userDialogVisible = ref(false)
const relatedUserLoading = ref(false)

const currentRole = ref<Partial<RoleItem>>({})

const formRef = ref()
const formData = reactive({
  id: '',
  name: '',
  code: '',
  dataScope: '本人',
  status: 1,
  remark: '',
})

const formRules = {
  name: [{ required: true, message: '角色名称必填', type: 'error' as const }],
  code: [{ required: true, message: '角色编码必填', type: 'error' as const }],
}

const columns = [
  { colKey: 'name', title: '角色名称', width: 150 },
  { colKey: 'code', title: '角色编码', width: 150 },
  { colKey: 'dataScope', title: '数据权限', width: 120 },
  { colKey: 'permissionCount', title: '权限数量', width: 100 },
  { colKey: 'status', title: '状态', width: 80, cell: 'statusSlot' },
  { colKey: 'remark', title: '备注', ellipsis: true },
  { colKey: 'createTime', title: '创建时间', width: 180 },
  { colKey: 'action', title: '操作', width: 280, fixed: 'right' as const },
]

const userColumns = [
  { colKey: 'username', title: '用户名', width: 120 },
  { colKey: 'realName', title: '真实姓名', width: 120 },
  { colKey: 'orgName', title: '所属组织', ellipsis: true },
  { colKey: 'status', title: '状态', width: 80 },
]

const permissionCategories = computed(() => {
  const categories = Array.from(new Set(allPermissions.value.map(item => item.category)))
  return [
    { key: 'all', label: '全部权限' },
    ...categories.map(category => ({ key: category, label: category })),
  ]
})

const filteredPermissions = computed(() => {
  const keyword = permissionSearchKeyword.value.trim().toLowerCase()
  return allPermissions.value.filter((item) => {
    const matchKeyword = !keyword
      || item.name.toLowerCase().includes(keyword)
      || item.code.toLowerCase().includes(keyword)
    const matchCategory = permissionCategoryFilter.value === 'all' || item.category === permissionCategoryFilter.value
    return matchKeyword && matchCategory
  })
})

async function fetchList() {
  loading.value = true
  try {
    const res = await get<{
      list: RoleItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/role/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      name: searchParams.name || undefined,
      code: searchParams.code || undefined,
      status: searchParams.status,
    })
    roleList.value = res.list
    total.value = res.total
    pagination.current = res.pageNum
    pagination.pageSize = res.pageSize
  }
  finally {
    loading.value = false
  }
}

async function fetchPermissionCatalog() {
  allPermissions.value = await get<PermissionItem[]>('/role/permission/catalog')
}

function handleSearch() {
  pagination.current = 1
  fetchList()
}

function handleReset() {
  searchParams.name = ''
  searchParams.code = ''
  searchParams.status = undefined
  handleSearch()
}

function handlePageChange(pageInfo: { current: number, pageSize: number }) {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchList()
}

function handleAdd() {
  dialogTitle.value = '新增角色'
  Object.assign(formData, {
    id: '',
    name: '',
    code: '',
    dataScope: '本人',
    status: 1,
    remark: '',
  })
  dialogVisible.value = true
}

async function handleEdit(row: RoleItem) {
  const detail = await get<RoleDetail>(`/role/${row.id}`)
  dialogTitle.value = '编辑角色'
  Object.assign(formData, {
    id: detail.id,
    name: detail.name,
    code: detail.code,
    dataScope: detail.dataScope,
    status: detail.status,
    remark: detail.remark || '',
  })
  dialogVisible.value = true
}

async function handleAuthorize(row: RoleItem) {
  permissionLoading.value = true
  try {
    currentRole.value = row
    if (allPermissions.value.length === 0) {
      await fetchPermissionCatalog()
    }
    const assigned = await get<PermissionItem[]>(`/role/${row.id}/permissions`)
    selectedPermissions.value = assigned.map(item => item.code)
    permissionSearchKeyword.value = ''
    permissionCategoryFilter.value = 'all'
    permissionDialogVisible.value = true
  }
  finally {
    permissionLoading.value = false
  }
}

async function handleViewUsers(row: RoleItem) {
  relatedUserLoading.value = true
  try {
    currentRole.value = row
    relatedUsers.value = await get<RelatedUserItem[]>(`/role/${row.id}/users`)
    userDialogVisible.value = true
  }
  finally {
    relatedUserLoading.value = false
  }
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
      dataScope: formData.dataScope,
      status: formData.status,
      remark: formData.remark || null,
    }

    if (formData.id) {
      await put(`/role/${formData.id}`, payload)
      MessagePlugin.success('修改成功')
    }
    else {
      await post('/role', payload)
      MessagePlugin.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  }
  finally {
    submitting.value = false
  }
}

async function handleSubmitPermission() {
  if (!currentRole.value.id) {
    return
  }
  try {
    await post(`/role/${currentRole.value.id}/permissions`, {
      permissionCodes: selectedPermissions.value,
    })
    MessagePlugin.success('权限授权成功')
    permissionDialogVisible.value = false
    fetchList()
  }
  catch (error) {}
}

async function handleStatusChange(status: number, row: RoleItem) {
  const previousStatus = row.status === 1 ? 0 : 1
  try {
    await put(`/role/${row.id}/status`, { status })
    MessagePlugin.success(status === 1 ? '启用成功' : '禁用成功')
    row.status = status
  }
  catch (error) {
    row.status = previousStatus
  }
}

async function handleDelete(row: RoleItem) {
  const confirm = await MessagePlugin.question('确认删除该角色吗？')
  if (!confirm) {
    return
  }

  try {
    await del(`/role/${row.id}`)
    MessagePlugin.success('删除成功')
    fetchList()
  }
  catch (error) {}
}

onMounted(async () => {
  await Promise.all([fetchList(), fetchPermissionCatalog()])
})
</script>

<template>
  <PageContainer title="角色管理" description="管理系统角色，配置权限和数据范围。">
    <template #extra>
      <t-button v-auth="'system:role:create'" theme="primary" @click="handleAdd">
        <template #icon>
          <Plus class="h-4 w-4" />
        </template>
        新增角色
      </t-button>
    </template>

    <div class="flex flex-col gap-4">
      <t-card :bordered="false" class="rounded-lg shadow-sm" size="small">
        <t-form layout="inline" label-width="80px">
          <t-form-item label="角色名称">
            <t-input v-model="searchParams.name" clearable placeholder="请输入角色名称" style="width: 200px" />
          </t-form-item>
          <t-form-item label="角色编码">
            <t-input v-model="searchParams.code" clearable placeholder="请输入角色编码" style="width: 200px" />
          </t-form-item>
          <t-form-item label="状态">
            <t-select v-model="searchParams.status" clearable placeholder="请选择状态" style="width: 150px">
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
          :data="roleList"
          :columns="columns"
          :loading="loading"
          :pagination="{ ...pagination, total }"
          hover
          stripe
          @page-change="handlePageChange"
        >
          <template #statusSlot="{ row }">
            <t-switch
              v-model="row.status"
              :custom-value="[1, 0]"
              @change="(val: number | string | boolean) => handleStatusChange(val as number, row)"
            />
          </template>

          <template #remark="{ row }">
            {{ row.remark || '-' }}
          </template>

          <template #action="{ row }">
            <div class="flex gap-2">
              <t-button v-auth="'system:role:update'" theme="primary" variant="text" size="small" @click="handleEdit(row)">
                <template #icon>
                  <Edit class="h-3.5 w-3.5" />
                </template>
                编辑
              </t-button>
              <t-button v-auth="'system:role:permission:assign'" theme="success" variant="text" size="small" @click="handleAuthorize(row)">
                权限授权
              </t-button>
              <t-button v-auth="'system:role:user:view'" theme="default" variant="text" size="small" @click="handleViewUsers(row)">
                <template #icon>
                  <Users class="h-3.5 w-3.5" />
                </template>
                关联用户
              </t-button>
              <t-button v-auth="'system:role:delete'" theme="danger" variant="text" size="small" @click="handleDelete(row)">
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
      width="600px"
      :on-confirm="handleSubmit"
      :confirm-btn="{ loading: submitting }"
    >
      <t-form ref="formRef" :data="formData" :rules="formRules" label-align="right" label-width="100px">
        <t-form-item label="角色名称" name="name">
          <t-input v-model="formData.name" placeholder="请输入角色名称" />
        </t-form-item>
        <t-form-item label="角色编码" name="code">
          <t-input v-model="formData.code" placeholder="请输入角色编码" />
        </t-form-item>
        <t-form-item label="数据权限" name="dataScope">
          <t-select v-model="formData.dataScope" placeholder="请选择数据权限范围">
            <t-option value="全部" label="全部数据" />
            <t-option value="本组织" label="本组织数据" />
            <t-option value="本组织及下级" label="本组织及下级数据" />
            <t-option value="本人" label="仅本人数据" />
          </t-select>
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

    <t-dialog
      v-model:visible="permissionDialogVisible"
      :header="`权限授权 - ${currentRole.name || ''}`"
      width="900px"
      :on-confirm="handleSubmitPermission"
      :confirm-btn="{ loading: permissionLoading }"
    >
      <div class="flex h-[500px] gap-4">
        <div class="w-48 shrink-0 border-r border-[#e7e7e7] pr-4 dark:border-gray-700">
          <h4 class="mb-3 text-sm font-semibold">权限分类</h4>
          <div class="space-y-2">
            <div
              v-for="cat in permissionCategories"
              :key="cat.key"
              class="cursor-pointer rounded px-3 py-2 hover:bg-[#f5f5f5] dark:hover:bg-gray-700"
              :class="permissionCategoryFilter === cat.key ? 'bg-[#0052d9] text-white' : ''"
              @click="permissionCategoryFilter = cat.key"
            >
              {{ cat.label }}
            </div>
          </div>
        </div>

        <div class="flex-1">
          <div class="mb-4">
            <t-input v-model="permissionSearchKeyword" clearable placeholder="搜索权限名称或编码">
              <template #prefix-icon>
                <Search class="h-4 w-4" />
              </template>
            </t-input>
          </div>
          <t-checkbox-group v-model="selectedPermissions" class="space-y-3">
            <div
              v-for="permission in filteredPermissions"
              :key="permission.code"
              class="flex items-center gap-3 rounded border border-[#e7e7e7] p-3 hover:border-[#0052d9] dark:border-gray-700"
            >
              <t-checkbox :value="permission.code" />
              <div class="flex-1">
                <div class="text-sm font-medium">{{ permission.name }}</div>
                <div class="text-xs text-[#86909c] dark:text-gray-400">{{ permission.code }}</div>
              </div>
              <t-tag size="small" variant="light">
                {{ permission.type }}
              </t-tag>
            </div>
          </t-checkbox-group>
        </div>

        <div class="w-56 shrink-0 border-l border-[#e7e7e7] pl-4 dark:border-gray-700">
          <h4 class="mb-3 text-sm font-semibold">已选权限</h4>
          <div class="mb-4">
            <div class="text-2xl font-bold text-[#0052d9]">{{ selectedPermissions.length }}</div>
            <div class="text-xs text-[#86909c] dark:text-gray-400">个权限</div>
          </div>
          <div class="max-h-[400px] space-y-2 overflow-y-auto">
            <div
              v-for="permCode in selectedPermissions"
              :key="permCode"
              class="rounded bg-[#f5f5f5] p-2 text-xs dark:bg-gray-700"
            >
              {{ allPermissions.find(item => item.code === permCode)?.name || permCode }}
            </div>
          </div>
        </div>
      </div>
    </t-dialog>

    <t-dialog
      v-model:visible="userDialogVisible"
      :header="`关联用户 - ${currentRole.name || ''}`"
      width="700px"
      :confirm-btn="null"
    >
      <t-table
        row-key="id"
        :data="relatedUsers"
        :columns="userColumns"
        :loading="relatedUserLoading"
        :pagination="{ disabled: true }"
        hover
        size="medium"
      >
        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'warning'" variant="light" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </t-tag>
        </template>
      </t-table>
    </t-dialog>
  </PageContainer>
</template>
