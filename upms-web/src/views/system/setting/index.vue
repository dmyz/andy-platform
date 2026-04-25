<script setup lang="ts">
import { Edit, Plus, RefreshCcw, Search, ShieldAlert, Trash2 } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { del, get, post, put } from '@/utils/request'

interface SettingItem {
  id: string
  settingKey: string
  settingName: string
  settingValue: string
  valueType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON'
  scopeType: 'GLOBAL' | 'ORG' | 'USER'
  scopeId?: string | null
  groupCode: string
  secretFlag: boolean
  effectiveMode: 'IMMEDIATE' | 'RESTART_REQUIRED'
  status: number
  remark?: string | null
  updateTime: string
}

interface SettingDetail extends SettingItem {}

const valueTypeOptions = [
  { label: '字符串', value: 'STRING' },
  { label: '数字', value: 'NUMBER' },
  { label: '布尔', value: 'BOOLEAN' },
  { label: 'JSON', value: 'JSON' },
]

const scopeTypeOptions = [
  { label: '全局', value: 'GLOBAL' },
  { label: '组织', value: 'ORG' },
  { label: '用户', value: 'USER' },
]

const effectiveModeOptions = [
  { label: '即时生效', value: 'IMMEDIATE' },
  { label: '重启生效', value: 'RESTART_REQUIRED' },
]

const valueTypeLabelMap: Record<string, string> = {
  STRING: '字符串',
  NUMBER: '数字',
  BOOLEAN: '布尔',
  JSON: 'JSON',
}

const scopeTypeLabelMap: Record<string, string> = {
  GLOBAL: '全局',
  ORG: '组织',
  USER: '用户',
}

const effectiveModeLabelMap: Record<string, string> = {
  IMMEDIATE: '即时生效',
  RESTART_REQUIRED: '重启生效',
}

const loading = ref(false)
const settingList = ref<SettingItem[]>([])
const total = ref(0)

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const searchParams = reactive({
  settingName: '',
  settingKey: '',
  groupCode: '',
})

const groupOptions = computed(() => {
  return Array.from(new Set(settingList.value.map(item => item.groupCode))).map(groupCode => ({
    label: groupCode,
    value: groupCode,
  }))
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增配置')
const submitting = ref(false)

const formRef = ref()
const formData = reactive({
  id: '',
  settingKey: '',
  settingName: '',
  settingValue: '',
  valueType: 'STRING',
  scopeType: 'GLOBAL',
  scopeId: '',
  groupCode: 'security',
  secretFlag: false,
  effectiveMode: 'IMMEDIATE',
  status: 1,
  remark: '',
})

const formRules = {
  settingKey: [{ required: true, message: '配置 Key 必填', type: 'error' as const }],
  settingName: [{ required: true, message: '配置名称必填', type: 'error' as const }],
  settingValue: [{ required: true, message: '配置值必填', type: 'error' as const }],
  valueType: [{ required: true, message: '值类型必填', type: 'error' as const }],
  scopeType: [{ required: true, message: '作用域必填', type: 'error' as const }],
  groupCode: [{ required: true, message: '配置分组必填', type: 'error' as const }],
  effectiveMode: [{ required: true, message: '生效方式必填', type: 'error' as const }],
}

const columns = [
  { colKey: 'settingName', title: '配置名称', width: 180, ellipsis: true },
  { colKey: 'settingKey', title: '配置 Key', width: 240, ellipsis: true },
  { colKey: 'settingValue', title: '配置值', width: 220, ellipsis: true },
  { colKey: 'groupCode', title: '配置分组', width: 120 },
  { colKey: 'remark', title: '备注', ellipsis: true },
  { colKey: 'updateTime', title: '更新时间', width: 180 },
  { colKey: 'action', title: '操作', width: 160, fixed: 'right' as const },
]

async function fetchList() {
  loading.value = true
  try {
    const res = await get<{
      list: SettingItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/setting/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      settingName: searchParams.settingName || undefined,
      settingKey: searchParams.settingKey || undefined,
      groupCode: searchParams.groupCode || undefined,
    })
    settingList.value = res.list
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
  searchParams.settingName = ''
  searchParams.settingKey = ''
  searchParams.groupCode = ''
  handleSearch()
}

function handlePageChange(pageInfo: { current: number, pageSize: number }) {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchList()
}

function handleAdd() {
  dialogTitle.value = '新增配置'
  Object.assign(formData, {
    id: '',
    settingKey: '',
    settingName: '',
    settingValue: '',
    valueType: 'STRING',
    scopeType: 'GLOBAL',
    scopeId: '',
    groupCode: 'security',
    secretFlag: false,
    effectiveMode: 'IMMEDIATE',
    status: 1,
    remark: '',
  })
  dialogVisible.value = true
}

async function handleEdit(row: SettingItem) {
  const detail = await get<SettingDetail>(`/setting/${row.id}`)
  dialogTitle.value = '编辑配置'
  Object.assign(formData, {
    id: detail.id,
    settingKey: detail.settingKey,
    settingName: detail.settingName,
    settingValue: detail.settingValue,
    valueType: detail.valueType,
    scopeType: detail.scopeType,
    scopeId: detail.scopeId || '',
    groupCode: detail.groupCode,
    secretFlag: detail.secretFlag,
    effectiveMode: detail.effectiveMode,
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
      settingKey: formData.settingKey,
      settingName: formData.settingName,
      settingValue: formData.settingValue,
      valueType: formData.valueType,
      scopeType: formData.scopeType,
      scopeId: formData.scopeId || null,
      groupCode: formData.groupCode,
      secretFlag: formData.secretFlag,
      effectiveMode: formData.effectiveMode,
      status: formData.status,
      remark: formData.remark || null,
    }

    if (formData.id) {
      await put(`/setting/${formData.id}`, payload)
      MessagePlugin.success('修改成功')
    }
    else {
      await post('/setting', payload)
      MessagePlugin.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  }
  finally {
    submitting.value = false
  }
}

async function handleDelete(row: SettingItem) {
  const confirm = await MessagePlugin.question(`确认删除配置“${row.settingName}”吗？`)
  if (!confirm) {
    return
  }

  try {
    await del(`/setting/${row.id}`)
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
  <PageContainer title="系统配置" description="维护登录策略、会话时长和文件上传等平台运行参数。">
    <template #extra>
      <t-button v-auth="'system:setting:create'" theme="primary" @click="handleAdd">
        <template #icon>
          <Plus class="h-4 w-4" />
        </template>
        新增配置
      </t-button>
    </template>

    <div class="flex flex-col gap-4">
      <t-card :bordered="false" class="rounded-lg shadow-sm" size="small">
        <t-form layout="inline" label-width="80px">
          <t-form-item label="配置名称">
            <t-input v-model="searchParams.settingName" clearable placeholder="请输入配置名称" style="width: 200px" />
          </t-form-item>
          <t-form-item label="配置 Key">
            <t-input v-model="searchParams.settingKey" clearable placeholder="请输入配置 Key" style="width: 220px" />
          </t-form-item>
          <t-form-item label="配置分组">
            <t-select v-model="searchParams.groupCode" clearable placeholder="请选择配置分组" style="width: 160px">
              <t-option v-for="item in groupOptions" :key="item.value" :label="item.label" :value="item.value" />
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
          :data="settingList"
          :columns="columns"
          :loading="loading"
          :pagination="{ ...pagination, total }"
          hover
          stripe
          @page-change="handlePageChange"
        >
          <template #settingValue="{ row }">
            <div class="flex items-center gap-2">
              <span class="truncate">{{ row.settingValue }}</span>
              <ShieldAlert v-if="row.secretFlag" class="h-3.5 w-3.5 text-[#e34d59]" />
            </div>
          </template>

          <template #groupCode="{ row }">
            <t-tag theme="primary" variant="light" size="small">
              {{ row.groupCode }}
            </t-tag>
          </template>

          <template #remark="{ row }">
            {{ row.remark || '-' }}
          </template>

          <template #action="{ row }">
            <div class="flex gap-2">
              <t-button v-auth="'system:setting:update'" theme="primary" variant="text" size="small" @click="handleEdit(row)">
                <template #icon>
                  <Edit class="h-3.5 w-3.5" />
                </template>
                编辑
              </t-button>
              <t-button v-auth="'system:setting:delete'" theme="danger" variant="text" size="small" @click="handleDelete(row)">
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
      width="760px"
      :on-confirm="handleSubmit"
      :confirm-btn="{ loading: submitting }"
    >
      <t-form ref="formRef" :data="formData" :rules="formRules" label-align="right" label-width="110px">
        <t-form-item label="配置名称" name="settingName">
          <t-input v-model="formData.settingName" placeholder="请输入配置名称" />
        </t-form-item>
        <t-form-item label="配置 Key" name="settingKey">
          <t-input v-model="formData.settingKey" placeholder="如 security.session.timeout-minute" />
        </t-form-item>
        <t-form-item label="配置值" name="settingValue">
          <t-textarea v-model="formData.settingValue" autosize placeholder="请输入配置值" />
        </t-form-item>
        <t-form-item label="值类型" name="valueType">
          <t-select v-model="formData.valueType" placeholder="请选择值类型">
            <t-option v-for="item in valueTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </t-select>
        </t-form-item>
        <t-form-item label="作用域" name="scopeType">
          <t-select v-model="formData.scopeType" placeholder="请选择作用域">
            <t-option v-for="item in scopeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </t-select>
        </t-form-item>
        <t-form-item v-if="formData.scopeType !== 'GLOBAL'" label="作用域对象" name="scopeId">
          <t-input v-model="formData.scopeId" placeholder="请输入组织 ID 或用户 ID" />
        </t-form-item>
        <t-form-item label="配置分组" name="groupCode">
          <t-input v-model="formData.groupCode" placeholder="如 security / file / session" />
        </t-form-item>
        <t-form-item label="生效方式" name="effectiveMode">
          <t-select v-model="formData.effectiveMode" placeholder="请选择生效方式">
            <t-option v-for="item in effectiveModeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </t-select>
        </t-form-item>
        <t-form-item label="敏感配置" name="secretFlag">
          <t-radio-group v-model="formData.secretFlag">
            <t-radio :value="false">
              否
            </t-radio>
            <t-radio :value="true">
              是
            </t-radio>
          </t-radio-group>
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
        <div class="rounded-lg bg-[#f8f9fb] px-4 py-3 text-xs text-[#86909c]">
          当前配置: {{ scopeTypeLabelMap[formData.scopeType] }} / {{ valueTypeLabelMap[formData.valueType] }} / {{ effectiveModeLabelMap[formData.effectiveMode] }}
        </div>
      </t-form>
    </t-dialog>
  </PageContainer>
</template>
