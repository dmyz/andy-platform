<script setup lang="ts">
import { Edit, ListTree, Plus, RefreshCcw, Search, Trash2 } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { del, get, post, put } from '@/utils/request'

interface DictionaryTypeItem {
  id: string
  name: string
  code: string
  status: number
  remark?: string | null
  updateTime: string
}

interface DictionaryTypeDetail extends DictionaryTypeItem {}

interface DictionaryItem {
  id: string
  dictionaryId: string
  dictionaryName: string
  dictionaryCode: string
  name: string
  value: string
  sortOrder: number
  status: number
  remark?: string | null
  updateTime: string
}

const typeLoading = ref(false)
const itemLoading = ref(false)
const typeSubmitting = ref(false)
const itemSubmitting = ref(false)

const dictionaryTypes = ref<DictionaryTypeItem[]>([])
const dictionaryItems = ref<DictionaryItem[]>([])
const selectedTypeId = ref('')
const selectedType = ref<DictionaryTypeItem | null>(null)
const typeTotal = ref(0)

const typePagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const typeSearchParams = reactive({
  name: '',
  code: '',
})

const itemSearchParams = reactive({
  name: '',
  status: undefined as number | undefined,
})

const typeDialogVisible = ref(false)
const typeDialogTitle = ref('新增字典类型')
const typeFormRef = ref()
const typeFormData = reactive({
  id: '',
  name: '',
  code: '',
  status: 1,
  remark: '',
})

const typeFormRules = {
  name: [{ required: true, message: '字典名称必填', type: 'error' as const }],
  code: [{ required: true, message: '字典编码必填', type: 'error' as const }],
}

const itemDialogVisible = ref(false)
const itemDialogTitle = ref('新增字典项')
const itemFormRef = ref()
const itemFormData = reactive({
  id: '',
  name: '',
  value: '',
  sortOrder: 1,
  status: 1,
  remark: '',
})

const itemFormRules = {
  name: [{ required: true, message: '字典项名称必填', type: 'error' as const }],
  value: [{ required: true, message: '字典项值必填', type: 'error' as const }],
  sortOrder: [{ required: true, message: '排序号必填', type: 'error' as const }],
}

const typeColumns = [
  { colKey: 'name', title: '字典名称', width: 140, ellipsis: true },
  { colKey: 'code', title: '字典编码', width: 160, ellipsis: true },
  { colKey: 'status', title: '状态', width: 80 },
  { colKey: 'remark', title: '备注', ellipsis: true },
  { colKey: 'action', title: '操作', width: 200, fixed: 'right' as const },
]

const itemColumns = [
  { colKey: 'name', title: '字典项名称', width: 160, ellipsis: true },
  { colKey: 'value', title: '字典项值', width: 160, ellipsis: true },
  { colKey: 'sortOrder', title: '排序号', width: 90 },
  { colKey: 'status', title: '状态', width: 90 },
  { colKey: 'remark', title: '备注', ellipsis: true },
  { colKey: 'action', title: '操作', width: 150, fixed: 'right' as const },
]

async function fetchTypeList(preferredId?: string) {
  typeLoading.value = true
  try {
    const res = await get<{
      list: DictionaryTypeItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/dictionary/page', {
      pageNum: typePagination.current,
      pageSize: typePagination.pageSize,
      name: typeSearchParams.name || undefined,
      code: typeSearchParams.code || undefined,
    })
    dictionaryTypes.value = res.list
    typeTotal.value = res.total
    typePagination.current = res.pageNum
    typePagination.pageSize = res.pageSize

    const nextSelected = preferredId && res.list.some(item => item.id === preferredId)
      ? preferredId
      : res.list.some(item => item.id === selectedTypeId.value)
          ? selectedTypeId.value
          : res.list[0]?.id || ''

    if (!nextSelected) {
      selectedTypeId.value = ''
      selectedType.value = null
      dictionaryItems.value = []
      return
    }

    const nextType = res.list.find(item => item.id === nextSelected) || null
    selectedTypeId.value = nextSelected
    selectedType.value = nextType
    await fetchItems()
  }
  finally {
    typeLoading.value = false
  }
}

async function fetchItems() {
  if (!selectedTypeId.value) {
    dictionaryItems.value = []
    selectedType.value = null
    return
  }
  itemLoading.value = true
  try {
    if (!selectedType.value) {
      selectedType.value = dictionaryTypes.value.find(item => item.id === selectedTypeId.value) || null
    }
    dictionaryItems.value = await get<DictionaryItem[]>(`/dictionary/${selectedTypeId.value}/items`, {
      name: itemSearchParams.name || undefined,
      status: itemSearchParams.status,
    })
  }
  finally {
    itemLoading.value = false
  }
}

function handleTypeSearch() {
  typePagination.current = 1
  fetchTypeList()
}

function handleTypeReset() {
  typeSearchParams.name = ''
  typeSearchParams.code = ''
  handleTypeSearch()
}

function handleTypePageChange(pageInfo: { current: number, pageSize: number }) {
  typePagination.current = pageInfo.current
  typePagination.pageSize = pageInfo.pageSize
  fetchTypeList()
}

function selectType(row: DictionaryTypeItem) {
  selectedTypeId.value = row.id
  selectedType.value = row
  fetchItems()
}

function handleItemSearch() {
  fetchItems()
}

function handleItemReset() {
  itemSearchParams.name = ''
  itemSearchParams.status = undefined
  fetchItems()
}

function handleAddType() {
  typeDialogTitle.value = '新增字典类型'
  Object.assign(typeFormData, {
    id: '',
    name: '',
    code: '',
    status: 1,
    remark: '',
  })
  typeDialogVisible.value = true
}

async function handleEditType(row: DictionaryTypeItem) {
  const detail = await get<DictionaryTypeDetail>(`/dictionary/${row.id}`)
  typeDialogTitle.value = '编辑字典类型'
  Object.assign(typeFormData, {
    id: detail.id,
    name: detail.name,
    code: detail.code,
    status: detail.status,
    remark: detail.remark || '',
  })
  typeDialogVisible.value = true
}

async function handleSubmitType() {
  const validateResult = await typeFormRef.value?.validate()
  if (validateResult !== true) {
    return
  }
  typeSubmitting.value = true
  try {
    const payload = {
      name: typeFormData.name,
      code: typeFormData.code,
      status: typeFormData.status,
      remark: typeFormData.remark || null,
    }
    if (typeFormData.id) {
      await put(`/dictionary/${typeFormData.id}`, payload)
      MessagePlugin.success('修改成功')
    }
    else {
      await post('/dictionary', payload)
      MessagePlugin.success('新增成功')
    }
    typeDialogVisible.value = false
    await fetchTypeList(typeFormData.id || undefined)
  }
  finally {
    typeSubmitting.value = false
  }
}

async function handleDeleteType(row: DictionaryTypeItem) {
  const confirm = await MessagePlugin.question(`确认删除字典类型“${row.name}”吗？`)
  if (!confirm) {
    return
  }
  try {
    await del(`/dictionary/${row.id}`)
    MessagePlugin.success('删除成功')
    await fetchTypeList()
  }
  catch (error) {}
}

function handleAddItem() {
  if (!selectedTypeId.value) {
    MessagePlugin.warning('请先选择字典类型')
    return
  }
  itemDialogTitle.value = '新增字典项'
  Object.assign(itemFormData, {
    id: '',
    name: '',
    value: '',
    sortOrder: dictionaryItems.value.length + 1,
    status: 1,
    remark: '',
  })
  itemDialogVisible.value = true
}

function handleEditItem(row: DictionaryItem) {
  itemDialogTitle.value = '编辑字典项'
  Object.assign(itemFormData, {
    id: row.id,
    name: row.name,
    value: row.value,
    sortOrder: row.sortOrder,
    status: row.status,
    remark: row.remark || '',
  })
  itemDialogVisible.value = true
}

async function handleSubmitItem() {
  if (!selectedTypeId.value) {
    return
  }
  const validateResult = await itemFormRef.value?.validate()
  if (validateResult !== true) {
    return
  }
  itemSubmitting.value = true
  try {
    const payload = {
      name: itemFormData.name,
      value: itemFormData.value,
      sortOrder: itemFormData.sortOrder,
      status: itemFormData.status,
      remark: itemFormData.remark || null,
    }
    if (itemFormData.id) {
      await put(`/dictionary/item/${itemFormData.id}`, payload)
      MessagePlugin.success('修改成功')
    }
    else {
      await post(`/dictionary/${selectedTypeId.value}/items`, payload)
      MessagePlugin.success('新增成功')
    }
    itemDialogVisible.value = false
    await fetchItems()
  }
  finally {
    itemSubmitting.value = false
  }
}

async function handleDeleteItem(row: DictionaryItem) {
  const confirm = await MessagePlugin.question(`确认删除字典项“${row.name}”吗？`)
  if (!confirm) {
    return
  }
  try {
    await del(`/dictionary/item/${row.id}`)
    MessagePlugin.success('删除成功')
    await fetchItems()
  }
  catch (error) {}
}

onMounted(() => {
  fetchTypeList()
})
</script>

<template>
  <PageContainer title="字典管理" description="维护字典类型和字典项，供系统下拉和枚举统一复用。">
    <div class="grid h-full gap-4 lg:grid-cols-[420px,1fr]">
      <t-card :bordered="false" class="rounded-lg shadow-sm">
        <div class="flex flex-col gap-4">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-base font-semibold">字典类型</div>
              <div class="text-xs text-[#86909c]">支持名称、编码查询</div>
            </div>
            <t-button v-auth="'system:dictionary:create'" theme="primary" @click="handleAddType">
              <template #icon>
                <Plus class="h-4 w-4" />
              </template>
              新增类型
            </t-button>
          </div>

          <t-form layout="inline" label-width="68px">
            <t-form-item label="字典名称">
              <t-input v-model="typeSearchParams.name" clearable placeholder="请输入字典名称" style="width: 150px" />
            </t-form-item>
            <t-form-item label="字典编码">
              <t-input v-model="typeSearchParams.code" clearable placeholder="请输入字典编码" style="width: 170px" />
            </t-form-item>
            <t-form-item>
              <div class="flex gap-2">
                <t-button theme="primary" @click="handleTypeSearch">
                  <template #icon>
                    <Search class="h-4 w-4" />
                  </template>
                  查询
                </t-button>
                <t-button theme="default" variant="outline" @click="handleTypeReset">
                  <template #icon>
                    <RefreshCcw class="h-4 w-4" />
                  </template>
                  重置
                </t-button>
              </div>
            </t-form-item>
          </t-form>

          <t-table
            row-key="id"
            :data="dictionaryTypes"
            :columns="typeColumns"
            :loading="typeLoading"
            :pagination="{ ...typePagination, total: typeTotal }"
            hover
            stripe
            @page-change="handleTypePageChange"
          >
            <template #name="{ row }">
              <div
                class="cursor-pointer rounded px-2 py-1 transition-colors"
                :class="selectedTypeId === row.id ? 'bg-[#e8f3ff] text-[#0052d9]' : 'hover:bg-[#f5f5f5]'"
                @click="selectType(row)"
              >
                {{ row.name }}
              </div>
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
              <div class="flex gap-1">
                <t-button v-auth="'system:dictionary:view'" theme="default" variant="text" size="small" @click="selectType(row)">
                  <template #icon>
                    <ListTree class="h-3.5 w-3.5" />
                  </template>
                  字典项
                </t-button>
                <t-button v-auth="'system:dictionary:update'" theme="primary" variant="text" size="small" @click="handleEditType(row)">
                  <template #icon>
                    <Edit class="h-3.5 w-3.5" />
                  </template>
                  编辑
                </t-button>
                <t-button v-auth="'system:dictionary:delete'" theme="danger" variant="text" size="small" @click="handleDeleteType(row)">
                  <template #icon>
                    <Trash2 class="h-3.5 w-3.5" />
                  </template>
                  删除
                </t-button>
              </div>
            </template>
          </t-table>
        </div>
      </t-card>

      <t-card :bordered="false" class="rounded-lg shadow-sm">
        <div class="flex flex-col gap-4">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-base font-semibold">
                字典项
                <span v-if="selectedType" class="text-[#0052d9]">/ {{ selectedType.name }}</span>
              </div>
              <div class="text-xs text-[#86909c]">
                {{ selectedType ? `字典编码: ${selectedType.code}` : '请选择左侧字典类型后查看字典项' }}
              </div>
            </div>
            <t-button v-auth="'system:dictionary:item:manage'" theme="primary" @click="handleAddItem">
              <template #icon>
                <Plus class="h-4 w-4" />
              </template>
              新增字典项
            </t-button>
          </div>

          <t-form layout="inline" label-width="80px">
            <t-form-item label="字典项名称">
              <t-input v-model="itemSearchParams.name" clearable placeholder="请输入字典项名称" style="width: 200px" />
            </t-form-item>
            <t-form-item label="状态">
              <t-select v-model="itemSearchParams.status" clearable placeholder="请选择状态" style="width: 140px">
                <t-option :value="1" label="启用" />
                <t-option :value="0" label="禁用" />
              </t-select>
            </t-form-item>
            <t-form-item>
              <div class="flex gap-2">
                <t-button theme="primary" @click="handleItemSearch">
                  <template #icon>
                    <Search class="h-4 w-4" />
                  </template>
                  查询
                </t-button>
                <t-button theme="default" variant="outline" @click="handleItemReset">
                  <template #icon>
                    <RefreshCcw class="h-4 w-4" />
                  </template>
                  重置
                </t-button>
              </div>
            </t-form-item>
          </t-form>

          <t-table
            row-key="id"
            :data="dictionaryItems"
            :columns="itemColumns"
            :loading="itemLoading"
            :pagination="{ disabled: true }"
            hover
            stripe
          >
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
                <t-button v-auth="'system:dictionary:item:manage'" theme="primary" variant="text" size="small" @click="handleEditItem(row)">
                  <template #icon>
                    <Edit class="h-3.5 w-3.5" />
                  </template>
                  编辑
                </t-button>
                <t-button v-auth="'system:dictionary:item:manage'" theme="danger" variant="text" size="small" @click="handleDeleteItem(row)">
                  <template #icon>
                    <Trash2 class="h-3.5 w-3.5" />
                  </template>
                  删除
                </t-button>
              </div>
            </template>
          </t-table>
        </div>
      </t-card>
    </div>

    <t-dialog
      v-model:visible="typeDialogVisible"
      :header="typeDialogTitle"
      width="560px"
      :on-confirm="handleSubmitType"
      :confirm-btn="{ loading: typeSubmitting }"
    >
      <t-form ref="typeFormRef" :data="typeFormData" :rules="typeFormRules" label-align="right" label-width="100px">
        <t-form-item label="字典名称" name="name">
          <t-input v-model="typeFormData.name" placeholder="请输入字典名称" />
        </t-form-item>
        <t-form-item label="字典编码" name="code">
          <t-input v-model="typeFormData.code" placeholder="如 user_status" />
        </t-form-item>
        <t-form-item label="状态" name="status">
          <t-radio-group v-model="typeFormData.status">
            <t-radio :value="1">
              启用
            </t-radio>
            <t-radio :value="0">
              禁用
            </t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="备注" name="remark">
          <t-textarea v-model="typeFormData.remark" :maxlength="200" placeholder="请输入备注" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog
      v-model:visible="itemDialogVisible"
      :header="itemDialogTitle"
      width="560px"
      :on-confirm="handleSubmitItem"
      :confirm-btn="{ loading: itemSubmitting }"
    >
      <t-form ref="itemFormRef" :data="itemFormData" :rules="itemFormRules" label-align="right" label-width="100px">
        <t-form-item label="所属字典">
          <t-input :value="selectedType?.name || ''" disabled />
        </t-form-item>
        <t-form-item label="字典项名称" name="name">
          <t-input v-model="itemFormData.name" placeholder="请输入字典项名称" />
        </t-form-item>
        <t-form-item label="字典项值" name="value">
          <t-input v-model="itemFormData.value" placeholder="如 ENABLED" />
        </t-form-item>
        <t-form-item label="排序号" name="sortOrder">
          <t-input-number v-model="itemFormData.sortOrder" :min="1" theme="normal" />
        </t-form-item>
        <t-form-item label="状态" name="status">
          <t-radio-group v-model="itemFormData.status">
            <t-radio :value="1">
              启用
            </t-radio>
            <t-radio :value="0">
              禁用
            </t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="备注" name="remark">
          <t-textarea v-model="itemFormData.remark" :maxlength="200" placeholder="请输入备注" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </PageContainer>
</template>
