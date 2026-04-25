<script setup lang="ts">
import { Download, Eye, Key, Plus, RefreshCcw, Search, Upload } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { del, download, get, post, put } from '@/utils/request'

interface UserPageItem {
  id: string
  username: string
  realName: string
  jobNumber?: string
  mobile: string
  email?: string
  orgId: string
  orgName: string
  position?: string
  status: number
  lastLoginTime?: string
  createTime: string
}

interface UserDetailView extends UserPageItem {
  gender?: string
  remark?: string
  passwordResetRequired?: boolean
}

interface UserRoleItem {
  code: string
  name: string
}

interface RolePageItem {
  id: string
  name: string
  code: string
  status: number
}

interface UserImportResult {
  importedCount: number
  updatedCount: number
  skippedCount: number
}

interface OrgTreeItem {
  id: string
  name: string
  children?: OrgTreeItem[]
}

const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const exporting = ref(false)
const userList = ref<UserPageItem[]>([])
const total = ref(0)
const orgOptions = ref<Array<{ label: string, value: string }>>([])
const importInputRef = ref<HTMLInputElement | null>(null)

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const searchParams = reactive({
  username: '',
  realName: '',
  mobile: '',
  orgName: '',
  status: undefined as number | undefined,
  createTimeRange: [] as string[],
})

const drawerVisible = ref(false)
const drawerTitle = ref('新增用户')
const detailDrawerVisible = ref(false)
const detailData = ref<Partial<UserDetailView>>({})
const roleDrawerVisible = ref(false)
const roleSubmitting = ref(false)
const roleOptions = ref<Array<{ label: string, value: string }>>([])
const selectedRoles = ref<string[]>([])

const formRef = ref()
const formData = reactive({
  id: '',
  username: '',
  realName: '',
  jobNumber: '',
  mobile: '',
  email: '',
  gender: 'MALE',
  orgId: '',
  position: '',
  password: '',
  status: 1,
  remark: '',
})

const formRules = {
  username: [{ required: true, message: '用户名必填', type: 'error' as const }],
  realName: [{ required: true, message: '真实姓名必填', type: 'error' as const }],
  mobile: [{ required: true, message: '手机号必填', type: 'error' as const }],
  orgId: [{ required: true, message: '所属组织必填', type: 'error' as const }],
}

const columns = [
  { colKey: 'username', title: '用户名', width: 120 },
  { colKey: 'realName', title: '真实姓名', width: 120 },
  { colKey: 'jobNumber', title: '工号', width: 100 },
  { colKey: 'mobile', title: '手机号', width: 130 },
  { colKey: 'email', title: '邮箱', ellipsis: true },
  { colKey: 'orgName', title: '所属组织', width: 140 },
  { colKey: 'position', title: '岗位', width: 120 },
  { colKey: 'status', title: '状态', width: 90, cell: 'statusSlot' },
  { colKey: 'lastLoginTime', title: '最后登录时间', width: 180 },
  { colKey: 'createTime', title: '创建时间', width: 180 },
  { colKey: 'action', title: '操作', width: 280, fixed: 'right' as const },
]

async function fetchList() {
  loading.value = true
  try {
    const [startTime, endTime] = searchParams.createTimeRange
    const res = await get<{
      list: UserPageItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/user/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      username: searchParams.username || undefined,
      realName: searchParams.realName || undefined,
      mobile: searchParams.mobile || undefined,
      orgName: searchParams.orgName || undefined,
      status: searchParams.status,
      startTime: startTime || undefined,
      endTime: endTime || undefined,
    })
    userList.value = res.list
    total.value = res.total
    pagination.current = res.pageNum
    pagination.pageSize = res.pageSize
  }
  finally {
    loading.value = false
  }
}

async function fetchOrgOptions() {
  const tree = await get<OrgTreeItem[]>('/org/tree')
  orgOptions.value = flattenOrgTree(tree)
}

function flattenOrgTree(nodes: OrgTreeItem[], level = 1): Array<{ label: string, value: string }> {
  return nodes.flatMap(node => [
    { label: `${'　'.repeat(Math.max(level - 1, 0))}${node.name}`, value: node.id },
    ...flattenOrgTree(node.children || [], level + 1),
  ])
}

function handleSearch() {
  pagination.current = 1
  fetchList()
}

function handleReset() {
  searchParams.username = ''
  searchParams.realName = ''
  searchParams.mobile = ''
  searchParams.orgName = ''
  searchParams.status = undefined
  searchParams.createTimeRange = []
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
    username: '',
    realName: '',
    jobNumber: '',
    mobile: '',
    email: '',
    gender: 'MALE',
    orgId: '',
    position: '',
    password: '',
    status: 1,
    remark: '',
  })
}

function handleAdd() {
  drawerTitle.value = '新增用户'
  resetForm()
  drawerVisible.value = true
}

async function handleEdit(row: UserPageItem) {
  drawerTitle.value = '编辑用户'
  const detail = await get<UserDetailView>(`/user/${row.id}`)
  Object.assign(formData, {
    id: detail.id,
    username: detail.username,
    realName: detail.realName,
    jobNumber: detail.jobNumber || '',
    mobile: detail.mobile,
    email: detail.email || '',
    gender: detail.gender || 'MALE',
    orgId: detail.orgId,
    position: detail.position || '',
    password: '',
    status: detail.status,
    remark: detail.remark || '',
  })
  drawerVisible.value = true
}

async function handleDetail(row: UserPageItem) {
  detailData.value = await get<UserDetailView>(`/user/${row.id}`)
  detailDrawerVisible.value = true
}

async function handleAssignRole(row: UserPageItem) {
  detailData.value = row
  const [assignedRoles, rolePage] = await Promise.all([
    get<UserRoleItem[]>(`/user/${row.id}/roles`),
    get<{
      list: RolePageItem[]
    }>('/role/page', { pageNum: 1, pageSize: 100, status: 1 }),
  ])
  selectedRoles.value = assignedRoles.map(item => item.code)
  roleOptions.value = rolePage.list
    .filter(item => item.status === 1)
    .map(item => ({ label: `${item.name} (${item.code})`, value: item.code }))
  roleDrawerVisible.value = true
}

async function handleSaveRoles() {
  if (!detailData.value.id) {
    return
  }
  roleSubmitting.value = true
  try {
    await post(`/user/${detailData.value.id}/roles`, {
      roleCodes: selectedRoles.value,
    })
    MessagePlugin.success('角色分配成功')
    roleDrawerVisible.value = false
  }
  finally {
    roleSubmitting.value = false
  }
}

async function handleResetPassword(row: UserPageItem) {
  const confirm = await MessagePlugin.question('确认重置该用户的密码吗?')
  if (!confirm) {
    return
  }

  try {
    await post(`/user/${row.id}/password/reset`)
    MessagePlugin.success('密码已重置，用户下次登录需修改密码')
  }
  catch (error) {}
}

async function handleForceOffline(row: UserPageItem) {
  const confirm = await MessagePlugin.question('确认强制该用户下线吗?')
  if (!confirm) {
    return
  }

  try {
    await post(`/user/${row.id}/offline`)
    MessagePlugin.success('已强制下线')
  }
  catch (error) {}
}

async function handleSubmit() {
  const validateResult = await formRef.value?.validate()
  if (validateResult !== true) {
    return
  }

  submitting.value = true
  try {
    const payload = {
      username: formData.username,
      realName: formData.realName,
      jobNumber: formData.jobNumber || null,
      mobile: formData.mobile,
      email: formData.email || null,
      gender: formData.gender,
      orgId: formData.orgId,
      position: formData.position || null,
      password: formData.password || null,
      status: formData.status,
      remark: formData.remark || null,
    }

    if (formData.id) {
      await put(`/user/${formData.id}`, payload)
      MessagePlugin.success('修改成功')
    }
    else {
      await post('/user', payload)
      MessagePlugin.success('新增成功')
    }

    drawerVisible.value = false
    fetchList()
  }
  finally {
    submitting.value = false
  }
}

async function handleStatusChange(status: number, row: UserPageItem) {
  const previousStatus = row.status === 1 ? 0 : 1
  try {
    await put(`/user/${row.id}/status`, { status })
    MessagePlugin.success(status === 1 ? '启用成功' : '禁用成功')
    row.status = status
  }
  catch (error) {
    row.status = previousStatus
  }
}

async function handleDelete(row: UserPageItem) {
  const confirm = await MessagePlugin.question('确认删除该用户吗？')
  if (!confirm) {
    return
  }

  try {
    await del(`/user/${row.id}`)
    MessagePlugin.success('删除成功')
    if (userList.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchList()
  }
  catch (error) {}
}

function handleImport() {
  importInputRef.value?.click()
}

async function handleImportSelected(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }

  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const result = await post<UserImportResult>('/user/import', formData)
    MessagePlugin.success(`导入完成：新增 ${result.importedCount}，更新 ${result.updatedCount}，跳过 ${result.skippedCount}`)
    await fetchList()
  }
  finally {
    importing.value = false
    if (importInputRef.value) {
      importInputRef.value.value = ''
    }
  }
}

async function handleExport() {
  exporting.value = true
  try {
    const [startTime, endTime] = searchParams.createTimeRange
    await download('/user/export', {
      params: {
        username: searchParams.username || undefined,
        realName: searchParams.realName || undefined,
        mobile: searchParams.mobile || undefined,
        orgName: searchParams.orgName || undefined,
        status: searchParams.status,
        startTime: startTime || undefined,
        endTime: endTime || undefined,
      },
      filename: 'user-export.csv',
    })
    MessagePlugin.success('导出成功')
  }
  finally {
    exporting.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchList(), fetchOrgOptions()])
})
</script>

<template>
  <PageContainer title="用户管理" description="管理系统用户，配置角色和归属组织。">
    <input ref="importInputRef" type="file" class="hidden" accept=".csv,text/csv" @change="handleImportSelected" />
    <template #extra>
      <div class="flex gap-2">
        <t-button v-auth="'system:user:import'" theme="default" variant="outline" :loading="importing" @click="handleImport">
          <template #icon>
            <Upload class="h-4 w-4" />
          </template>
          导入用户
        </t-button>
        <t-button v-auth="'system:user:export'" theme="default" variant="outline" :loading="exporting" @click="handleExport">
          <template #icon>
            <Download class="h-4 w-4" />
          </template>
          导出用户
        </t-button>
        <t-button v-auth="'system:user:create'" theme="primary" @click="handleAdd">
          <template #icon>
            <Plus class="h-4 w-4" />
          </template>
          新增用户
        </t-button>
      </div>
    </template>

    <div class="flex flex-col gap-4">
      <t-card :bordered="false" class="rounded-lg shadow-sm" size="small">
        <t-form layout="inline" label-width="80px">
          <t-form-item label="用户名">
            <t-input v-model="searchParams.username" clearable placeholder="请输入用户名" style="width: 200px" />
          </t-form-item>
          <t-form-item label="真实姓名">
            <t-input v-model="searchParams.realName" clearable placeholder="请输入真实姓名" style="width: 200px" />
          </t-form-item>
          <t-form-item label="手机号">
            <t-input v-model="searchParams.mobile" clearable placeholder="请输入手机号" style="width: 200px" />
          </t-form-item>
          <t-form-item label="所属组织">
            <t-input v-model="searchParams.orgName" clearable placeholder="请输入所属组织" style="width: 200px" />
          </t-form-item>
          <t-form-item label="状态">
            <t-select v-model="searchParams.status" clearable placeholder="请选择状态" style="width: 200px">
              <t-option :value="1" label="启用" />
              <t-option :value="0" label="禁用" />
            </t-select>
          </t-form-item>
          <t-form-item label="创建时间">
            <t-date-range-picker
              v-model="searchParams.createTimeRange"
              clearable
              placeholder="请选择创建时间范围"
              style="width: 300px"
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
            </div>
          </t-form-item>
        </t-form>
      </t-card>

      <t-card :bordered="false" class="flex-1 rounded-lg shadow-sm">
        <t-table
          row-key="id"
          :data="userList"
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

          <template #jobNumber="{ row }">
            {{ row.jobNumber || '-' }}
          </template>

          <template #email="{ row }">
            {{ row.email || '-' }}
          </template>

          <template #position="{ row }">
            {{ row.position || '-' }}
          </template>

          <template #lastLoginTime="{ row }">
            {{ row.lastLoginTime || '-' }}
          </template>

          <template #action="{ row }">
            <div class="flex gap-2">
              <t-button v-auth="'system:user:view'" theme="primary" variant="text" size="small" @click="handleDetail(row)">
                <template #icon>
                  <Eye class="h-3.5 w-3.5" />
                </template>
                详情
              </t-button>
              <t-button v-auth="'system:user:update'" theme="primary" variant="text" size="small" @click="handleEdit(row)">
                编辑
              </t-button>
              <t-button v-auth="'system:user:role:assign'" theme="default" variant="text" size="small" @click="handleAssignRole(row)">
                分配角色
              </t-button>
              <t-button v-auth="'system:user:password:reset'" theme="warning" variant="text" size="small" @click="handleResetPassword(row)">
                <template #icon>
                  <Key class="h-3.5 w-3.5" />
                </template>
                重置密码
              </t-button>
              <t-button v-auth="'system:user:offline'" theme="warning" variant="text" size="small" @click="handleForceOffline(row)">
                强制下线
              </t-button>
              <t-button v-auth="'system:user:delete'" theme="danger" variant="text" size="small" @click="handleDelete(row)">
                删除
              </t-button>
            </div>
          </template>
        </t-table>
      </t-card>
    </div>

    <t-drawer
      v-model:visible="drawerVisible"
      :header="drawerTitle"
      size="600px"
      :on-confirm="handleSubmit"
      :confirm-btn="{ loading: submitting }"
    >
      <t-form ref="formRef" :data="formData" :rules="formRules" label-align="right" label-width="100px">
        <t-form-item label="用户名" name="username">
          <t-input v-model="formData.username" :disabled="!!formData.id" placeholder="请输入唯一登录名" />
        </t-form-item>
        <t-form-item label="真实姓名" name="realName">
          <t-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </t-form-item>
        <t-form-item label="工号" name="jobNumber">
          <t-input v-model="formData.jobNumber" placeholder="请输入工号" />
        </t-form-item>
        <t-form-item label="手机号" name="mobile">
          <t-input v-model="formData.mobile" placeholder="请输入手机号" />
        </t-form-item>
        <t-form-item label="邮箱" name="email">
          <t-input v-model="formData.email" placeholder="请输入邮箱" />
        </t-form-item>
        <t-form-item label="性别" name="gender">
          <t-radio-group v-model="formData.gender">
            <t-radio value="MALE">
              男
            </t-radio>
            <t-radio value="FEMALE">
              女
            </t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="所属组织" name="orgId">
          <t-select v-model="formData.orgId" clearable placeholder="请选择所属组织">
            <t-option v-for="item in orgOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>
        <t-form-item label="岗位" name="position">
          <t-input v-model="formData.position" placeholder="请输入岗位" />
        </t-form-item>
        <t-form-item v-if="!formData.id" label="初始密码" name="password">
          <t-input v-model="formData.password" type="password" placeholder="请输入初始密码" />
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
    </t-drawer>

    <t-drawer
      v-model:visible="detailDrawerVisible"
      header="用户详情"
      size="600px"
      :confirm-btn="null"
    >
      <t-descriptions :column="2">
        <t-descriptions-item label="用户名">
          {{ detailData.username }}
        </t-descriptions-item>
        <t-descriptions-item label="真实姓名">
          {{ detailData.realName }}
        </t-descriptions-item>
        <t-descriptions-item label="工号">
          {{ detailData.jobNumber || '-' }}
        </t-descriptions-item>
        <t-descriptions-item label="手机号">
          {{ detailData.mobile || '-' }}
        </t-descriptions-item>
        <t-descriptions-item label="邮箱">
          {{ detailData.email || '-' }}
        </t-descriptions-item>
        <t-descriptions-item label="所属组织">
          {{ detailData.orgName || '-' }}
        </t-descriptions-item>
        <t-descriptions-item label="岗位">
          {{ detailData.position || '-' }}
        </t-descriptions-item>
        <t-descriptions-item label="状态">
          <t-tag :theme="detailData.status === 1 ? 'success' : 'warning'" variant="light">
            {{ detailData.status === 1 ? '启用' : '禁用' }}
          </t-tag>
        </t-descriptions-item>
        <t-descriptions-item label="创建时间">
          {{ detailData.createTime || '-' }}
        </t-descriptions-item>
        <t-descriptions-item label="最后登录">
          {{ detailData.lastLoginTime || '-' }}
        </t-descriptions-item>
      </t-descriptions>
    </t-drawer>

    <t-drawer
      v-model:visible="roleDrawerVisible"
      header="分配角色"
      size="500px"
      :on-confirm="handleSaveRoles"
      :confirm-btn="{ loading: roleSubmitting }"
    >
      <div class="mb-4 rounded-lg bg-[#f5f5f5] p-4 dark:bg-gray-700">
        <p class="mb-2 text-sm text-[#86909c] dark:text-gray-400">
          当前用户: {{ detailData.realName }} ({{ detailData.username }})
        </p>
      </div>
      <t-checkbox-group v-model="selectedRoles">
        <div class="space-y-3">
          <t-checkbox v-for="item in roleOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </t-checkbox>
        </div>
      </t-checkbox-group>
    </t-drawer>
  </PageContainer>
</template>

<style scoped>
:deep(.t-card__body) {
  padding: 16px;
}
</style>
