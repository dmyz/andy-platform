<script setup lang="ts">
import { ChevronLeft, ChevronRight, Edit, Plus, RefreshCcw, Search, Trash2 } from '@lucide/vue';
import { message } from 'antdv-next';
import { confirmAction } from '@/utils/feedback';
import { computed, onMounted, reactive, ref } from 'vue';
import {
  getDictionaryPage,
  getDictionary,
  createDictionary,
  updateDictionary,
  deleteDictionary,
  getDictionaryItems,
  createDictionaryItem,
  updateDictionaryItem,
  deleteDictionaryItem,
} from '@/api/dictionary';

interface DictionaryTypeItem {
  id: string;
  name: string;
  code: string;
  status: number;
  remark?: string | null;
  updateTime: string;
}

interface DictionaryTypeDetail extends DictionaryTypeItem {}

interface DictionaryItem {
  id: string;
  dictionaryId: string;
  dictionaryName: string;
  dictionaryCode: string;
  name: string;
  value: string;
  sortOrder: number;
  status: number;
  remark?: string | null;
  updateTime: string;
}

const typeLoading = ref(false);
const itemLoading = ref(false);
const typeSubmitting = ref(false);
const itemSubmitting = ref(false);

const dictionaryTypes = ref<DictionaryTypeItem[]>([]);
const dictionaryItems = ref<DictionaryItem[]>([]);
const selectedTypeId = ref('');
const selectedType = ref<DictionaryTypeItem | null>(null);
const typeTotal = ref(0);

const typePagination = reactive({
  current: 1,
  pageSize: 10,
});

const typeTotalPages = computed(() =>
  Math.max(1, Math.ceil(typeTotal.value / typePagination.pageSize)),
);

const typeSearchParams = reactive({
  name: '',
  code: '',
});

const itemSearchParams = reactive({
  name: '',
  status: undefined as number | undefined,
});

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const typeDialogVisible = ref(false);
const typeDialogTitle = ref('新增字典类型');
const typeFormRef = ref();
const typeFormData = reactive({
  id: '',
  name: '',
  code: '',
  status: 1,
  remark: '',
});

const typeFormRules = {
  name: [{ required: true, message: '字典名称必填', type: 'error' as const }],
  code: [{ required: true, message: '字典编码必填', type: 'error' as const }],
};

const itemDialogVisible = ref(false);
const itemDialogTitle = ref('新增字典项');
const itemFormRef = ref();
const itemFormData = reactive({
  id: '',
  name: '',
  value: '',
  sortOrder: 1,
  status: 1,
  remark: '',
});

const itemFormRules = {
  name: [{ required: true, message: '字典项名称必填', type: 'error' as const }],
  value: [{ required: true, message: '字典项值必填', type: 'error' as const }],
  sortOrder: [{ required: true, message: '排序号必填', type: 'error' as const }],
};

const itemColumns = [
  { dataIndex: 'name', title: '字典项名称', width: 160, ellipsis: true },
  { dataIndex: 'value', title: '字典项值', width: 160, ellipsis: true },
  { dataIndex: 'sortOrder', title: '排序号', width: 90 },
  { dataIndex: 'status', title: '状态', width: 90 },
  { dataIndex: 'remark', title: '备注', ellipsis: true },
  { dataIndex: 'action', title: '操作', width: 180, fixed: 'right' as const },
];

const itemTableScroll = { x: 820 };

async function fetchTypeList(preferredId?: string) {
  typeLoading.value = true;
  try {
    const res = await getDictionaryPage({
      pageNum: typePagination.current,
      pageSize: typePagination.pageSize,
      name: typeSearchParams.name || undefined,
      code: typeSearchParams.code || undefined,
    });
    dictionaryTypes.value = res.list;
    typeTotal.value = res.total;
    typePagination.current = res.pageNum;
    typePagination.pageSize = res.pageSize;

    const nextSelected =
      preferredId && res.list.some((item) => item.id === preferredId)
        ? preferredId
        : res.list.some((item) => item.id === selectedTypeId.value)
          ? selectedTypeId.value
          : res.list[0]?.id || '';

    if (!nextSelected) {
      selectedTypeId.value = '';
      selectedType.value = null;
      dictionaryItems.value = [];
      return;
    }

    const nextType = res.list.find((item) => item.id === nextSelected) || null;
    selectedTypeId.value = nextSelected;
    selectedType.value = nextType;
    await fetchItems();
  } finally {
    typeLoading.value = false;
  }
}

async function fetchItems() {
  if (!selectedTypeId.value) {
    dictionaryItems.value = [];
    selectedType.value = null;
    return;
  }
  itemLoading.value = true;
  try {
    if (!selectedType.value) {
      selectedType.value =
        dictionaryTypes.value.find((item) => item.id === selectedTypeId.value) || null;
    }
    dictionaryItems.value = await getDictionaryItems(selectedTypeId.value, {
      name: itemSearchParams.name || undefined,
      status: itemSearchParams.status,
    });
  } finally {
    itemLoading.value = false;
  }
}

function handleTypeSearch() {
  typePagination.current = 1;
  fetchTypeList();
}

function handleTypeReset() {
  typeSearchParams.name = '';
  typeSearchParams.code = '';
  handleTypeSearch();
}

function handleTypePrevPage() {
  if (typePagination.current <= 1) {
    return;
  }
  typePagination.current -= 1;
  fetchTypeList();
}

function handleTypeNextPage() {
  if (typePagination.current >= typeTotalPages.value) {
    return;
  }
  typePagination.current += 1;
  fetchTypeList();
}

function selectType(row: DictionaryTypeItem) {
  selectedTypeId.value = row.id;
  selectedType.value = row;
  fetchItems();
}

function handleItemSearch() {
  fetchItems();
}

function handleItemReset() {
  itemSearchParams.name = '';
  itemSearchParams.status = undefined;
  fetchItems();
}

function handleAddType() {
  typeDialogTitle.value = '新增字典类型';
  Object.assign(typeFormData, {
    id: '',
    name: '',
    code: '',
    status: 1,
    remark: '',
  });
  typeDialogVisible.value = true;
}

async function handleEditType(row: DictionaryTypeItem) {
  const detail = await getDictionary(row.id);
  typeDialogTitle.value = '编辑字典类型';
  Object.assign(typeFormData, {
    id: detail.id,
    name: detail.name,
    code: detail.code,
    status: detail.status,
    remark: detail.remark || '',
  });
  typeDialogVisible.value = true;
}

async function handleSubmitType() {
  try {
    await typeFormRef.value?.validate();
  } catch {
    return;
  }
  typeSubmitting.value = true;
  try {
    const payload = {
      name: typeFormData.name,
      code: typeFormData.code,
      status: typeFormData.status,
      remark: typeFormData.remark || null,
    };
    if (typeFormData.id) {
      await updateDictionary(typeFormData.id, payload);
      message.success('修改成功');
    } else {
      await createDictionary(payload);
      message.success('新增成功');
    }
    typeDialogVisible.value = false;
    await fetchTypeList(typeFormData.id || undefined);
  } finally {
    typeSubmitting.value = false;
  }
}

async function handleDeleteType(row: DictionaryTypeItem) {
  const confirm = await confirmAction(`确认删除字典类型“${row.name}”吗？`);
  if (!confirm) {
    return;
  }
  try {
    await deleteDictionary(row.id);
    message.success('删除成功');
    await fetchTypeList();
  } catch (error) {}
}

async function handleTypeStatusChange(status: number, row: DictionaryTypeItem) {
  const previousStatus = row.status;
  row.status = status;
  try {
    await updateDictionary(row.id, {
      name: row.name,
      code: row.code,
      status,
      remark: row.remark || null,
    });
    const currentSelected = selectedType.value;
    if (currentSelected?.id === row.id) {
      currentSelected.status = status;
    }
    message.success(status === 1 ? '启用成功' : '禁用成功');
  } catch {
    row.status = previousStatus;
    const currentSelected = selectedType.value;
    if (currentSelected?.id === row.id) {
      currentSelected.status = previousStatus;
    }
  }
}

function handleAddItem() {
  if (!selectedTypeId.value) {
    message.warning('请先选择字典类型');
    return;
  }
  itemDialogTitle.value = '新增字典项';
  Object.assign(itemFormData, {
    id: '',
    name: '',
    value: '',
    sortOrder: dictionaryItems.value.length + 1,
    status: 1,
    remark: '',
  });
  itemDialogVisible.value = true;
}

function handleEditItem(row: DictionaryItem) {
  itemDialogTitle.value = '编辑字典项';
  Object.assign(itemFormData, {
    id: row.id,
    name: row.name,
    value: row.value,
    sortOrder: row.sortOrder,
    status: row.status,
    remark: row.remark || '',
  });
  itemDialogVisible.value = true;
}

async function handleSubmitItem() {
  if (!selectedTypeId.value) {
    return;
  }
  try {
    await itemFormRef.value?.validate();
  } catch {
    return;
  }
  itemSubmitting.value = true;
  try {
    const payload = {
      name: itemFormData.name,
      value: itemFormData.value,
      sortOrder: itemFormData.sortOrder,
      status: itemFormData.status,
      remark: itemFormData.remark || null,
    };
    if (itemFormData.id) {
      await updateDictionaryItem(itemFormData.id, payload);
      message.success('修改成功');
    } else {
      await createDictionaryItem(selectedTypeId.value, payload);
      message.success('新增成功');
    }
    itemDialogVisible.value = false;
    await fetchItems();
  } finally {
    itemSubmitting.value = false;
  }
}

async function handleItemStatusChange(status: number, row: DictionaryItem) {
  const previousStatus = row.status;
  row.status = status;
  try {
    await updateDictionaryItem(row.id, {
      name: row.name,
      value: row.value,
      sortOrder: row.sortOrder,
      status,
      remark: row.remark || null,
    });
    message.success(status === 1 ? '启用成功' : '禁用成功');
  } catch {
    row.status = previousStatus;
  }
}

async function handleDeleteItem(row: DictionaryItem) {
  const confirm = await confirmAction(`确认删除字典项“${row.name}”吗？`);
  if (!confirm) {
    return;
  }
  try {
    await deleteDictionaryItem(row.id);
    message.success('删除成功');
    await fetchItems();
  } catch (error) {}
}

onMounted(() => {
  fetchTypeList();
});
</script>

<template>
  <div>
    <div class="grid h-full gap-4 lg:grid-cols-[420px_minmax(0,1fr)]">
      <a-card variant="borderless" class="table-card rounded-lg shadow-sm">
        <div class="flex flex-col gap-4">
          <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label class="flex min-w-0 flex-col gap-1">
              <span class="text-xs text-[#4e5969]">字典名称</span>
              <a-input
                v-model:value="typeSearchParams.name"
                allow-clear
                placeholder="请输入字典名称"
              />
            </label>
            <label class="flex min-w-0 flex-col gap-1">
              <span class="text-xs text-[#4e5969]">字典编码</span>
              <a-input
                v-model:value="typeSearchParams.code"
                allow-clear
                placeholder="请输入字典编码"
              />
            </label>
          </div>

          <a-space :size="8" class="filter-actions">
            <a-button type="primary" @click="handleTypeSearch">
              <template #icon>
                <Search class="h-4 w-4" />
              </template>
              查询
            </a-button>
            <a-button variant="outlined" @click="handleTypeReset">
              <template #icon>
                <RefreshCcw class="h-4 w-4" />
              </template>
              重置
            </a-button>
          </a-space>

          <div class="table-toolbar">
            <div class="table-toolbar__actions">
              <a-button v-auth="'system:dictionary:create'" type="primary" @click="handleAddType">
                <template #icon>
                  <Plus class="h-4 w-4" />
                </template>
                新增
              </a-button>
            </div>
            <div class="table-toolbar__actions table-toolbar__actions--right">
              <a-button variant="outlined" size="small" @click="fetchTypeList()">
                <template #icon>
                  <RefreshCcw class="h-4 w-4" />
                </template>
                刷新
              </a-button>
            </div>
          </div>

          <div class="flex flex-col gap-2">
            <div
              v-for="type in dictionaryTypes"
              :key="type.id"
              class="group flex cursor-pointer items-center justify-between gap-3 rounded-md border px-3 py-2 transition-colors"
              :class="
                selectedTypeId === type.id
                  ? 'border-[#c7ddff] bg-[#f2f7ff]'
                  : 'border-[#edf1f5] bg-white hover:border-[#d5e3f7] hover:bg-[#f7fbff]'
              "
              @click="selectType(type)"
            >
              <div class="min-w-0 flex-1">
                <div class="flex min-w-0 items-center gap-2">
                  <span
                    class="truncate text-sm font-medium"
                    :class="selectedTypeId === type.id ? 'text-[#0052d9]' : 'text-[#1f2329]'"
                  >
                    {{ type.name }}
                  </span>
                  <a-switch
                    :checked="type.status"
                    :checked-value="1"
                    :un-checked-value="0"
                    size="small"
                    @click.stop
                    @change="
                      (val: number | string | boolean) =>
                        handleTypeStatusChange(val as number, type)
                    "
                  />
                </div>
                <div class="mt-1 truncate text-xs text-[#86909c]">{{ type.code }}</div>
              </div>
              <div class="flex shrink-0 items-center gap-1" @click.stop>
                <a-button
                  v-auth="'system:dictionary:update'"
                  type="primary"
                  size="small"
                  @click="handleEditType(type)"
                >
                  <template #icon>
                    <Edit class="h-3.5 w-3.5" />
                  </template>
                  编辑
                </a-button>
                <a-button
                  v-auth="'system:dictionary:delete'"
                  danger
                  variant="outlined"
                  size="small"
                  @click="handleDeleteType(type)"
                >
                  <template #icon>
                    <Trash2 class="h-3.5 w-3.5" />
                  </template>
                  删除
                </a-button>
              </div>
            </div>

            <div
              v-if="!typeLoading && dictionaryTypes.length === 0"
              class="rounded-md border border-dashed border-[#dcdfe6] py-8 text-center text-sm text-[#86909c]"
            >
              暂无字典类型
            </div>
            <div v-if="typeLoading" class="py-3 text-center text-sm text-[#86909c]">加载中...</div>
          </div>

          <div v-if="typeTotal > typePagination.pageSize" class="flex items-center justify-between">
            <span class="text-xs text-[#86909c]">共 {{ typeTotal }} 条</span>
            <div class="flex items-center gap-2">
              <a-button
                variant="outlined"
                shape="square"
                size="small"
                :disabled="typePagination.current <= 1"
                @click="handleTypePrevPage"
              >
                <ChevronLeft class="h-4 w-4" />
              </a-button>
              <span class="min-w-[48px] text-center text-xs text-[#4e5969]">
                {{ typePagination.current }} / {{ typeTotalPages }}
              </span>
              <a-button
                variant="outlined"
                shape="square"
                size="small"
                :disabled="typePagination.current >= typeTotalPages"
                @click="handleTypeNextPage"
              >
                <ChevronRight class="h-4 w-4" />
              </a-button>
            </div>
          </div>
        </div>
      </a-card>

      <a-card variant="borderless" class="rounded-lg shadow-sm">
        <div class="flex flex-col gap-4">
          <a-row :gutter="[16, 12]" class="filter-layout" align="top">
            <a-col :xs="24" :xl="20">
              <a-form class="filter-form-grid" :label-col="{ style: { width: '80px' } }">
                <a-row :gutter="[24, 18]" align="top">
                  <a-col :xs="24" :md="12" :lg="10">
                    <a-form-item label="字典项名称">
                      <a-input
                        v-model:value="itemSearchParams.name"
                        allow-clear
                        placeholder="请输入字典项名称"
                        class="filter-control"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12" :lg="10">
                    <a-form-item label="状态">
                      <a-select
                        v-model:value="itemSearchParams.status"
                        allow-clear
                        placeholder="请选择状态"
                        class="filter-control"
                        :options="statusOptions"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>
              </a-form>
            </a-col>
            <a-col :xs="24" :xl="4" class="filter-actions-col">
              <a-space :size="8" class="filter-actions">
                <a-button type="primary" @click="handleItemSearch">
                  <template #icon>
                    <Search class="h-4 w-4" />
                  </template>
                  查询
                </a-button>
                <a-button variant="outlined" @click="handleItemReset">
                  <template #icon>
                    <RefreshCcw class="h-4 w-4" />
                  </template>
                  重置
                </a-button>
              </a-space>
            </a-col>
          </a-row>

          <div class="table-toolbar">
            <div class="table-toolbar__actions min-w-0">
              <a-button
                v-auth="'system:dictionary:item:manage'"
                type="primary"
                @click="handleAddItem"
              >
                <template #icon>
                  <Plus class="h-4 w-4" />
                </template>
                新增项
              </a-button>
              <a-tag v-if="selectedType" color="blue" variant="filled" class="min-w-0">
                <span class="truncate">{{ selectedType.name }} / {{ selectedType.code }}</span>
              </a-tag>
              <span v-else class="truncate text-xs text-[#86909c]">请选择左侧字典类型</span>
            </div>
            <div class="table-toolbar__actions table-toolbar__actions--right">
              <a-button variant="outlined" size="small" @click="fetchItems()">
                <template #icon>
                  <RefreshCcw class="h-4 w-4" />
                </template>
                刷新
              </a-button>
            </div>
          </div>

          <a-table
            row-key="id"
            :data-source="dictionaryItems"
            :columns="itemColumns"
            :loading="itemLoading"
            :scroll="itemTableScroll"
            bordered
            hover
            stripe
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'status'">
                <a-switch
                  :checked="record.status"
                  :checked-value="1"
                  :un-checked-value="0"
                  @change="
                    (val: number | string | boolean) =>
                      handleItemStatusChange(val as number, record)
                  "
                />
              </template>

              <template v-else-if="column.dataIndex === 'remark'">
                {{ record.remark || '-' }}
              </template>

              <template v-else-if="column.dataIndex === 'action'">
                <div class="row-actions">
                  <a-button
                    v-auth="'system:dictionary:item:manage'"
                    type="primary"
                    size="small"
                    @click="handleEditItem(record)"
                  >
                    <template #icon>
                      <Edit class="h-3.5 w-3.5" />
                    </template>
                    编辑
                  </a-button>
                  <a-button
                    v-auth="'system:dictionary:item:manage'"
                    danger
                    variant="outlined"
                    size="small"
                    @click="handleDeleteItem(record)"
                  >
                    <template #icon>
                      <Trash2 class="h-3.5 w-3.5" />
                    </template>
                    删除
                  </a-button>
                </div>
              </template>
            </template>
          </a-table>
        </div>
      </a-card>
    </div>

    <a-modal
      v-model:open="typeDialogVisible"
      :title="typeDialogTitle"
      width="560px"
      :confirm-loading="typeSubmitting"
      @ok="handleSubmitType"
    >
      <a-form
        ref="typeFormRef"
        :model="typeFormData"
        :rules="typeFormRules"
        label-align="right"
        :label-col="{ style: { width: '100px' } }"
      >
        <a-form-item label="字典名称" name="name">
          <a-input v-model:value="typeFormData.name" placeholder="请输入字典名称" />
        </a-form-item>
        <a-form-item label="字典编码" name="code">
          <a-input v-model:value="typeFormData.code" placeholder="如 user_status" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="typeFormData.status">
            <a-radio :value="1"> 启用 </a-radio>
            <a-radio :value="0"> 禁用 </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="typeFormData.remark"
            :maxlength="200"
            placeholder="请输入备注"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="itemDialogVisible"
      :title="itemDialogTitle"
      width="560px"
      :confirm-loading="itemSubmitting"
      @ok="handleSubmitItem"
    >
      <a-form
        ref="itemFormRef"
        :model="itemFormData"
        :rules="itemFormRules"
        label-align="right"
        :label-col="{ style: { width: '100px' } }"
      >
        <a-form-item label="所属字典">
          <a-input :value="selectedType?.name || ''" disabled />
        </a-form-item>
        <a-form-item label="字典项名称" name="name">
          <a-input v-model:value="itemFormData.name" placeholder="请输入字典项名称" />
        </a-form-item>
        <a-form-item label="字典项值" name="value">
          <a-input v-model:value="itemFormData.value" placeholder="如 ENABLED" />
        </a-form-item>
        <a-form-item label="排序号" name="sortOrder">
          <a-input-number v-model:value="itemFormData.sortOrder" :min="1" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="itemFormData.status">
            <a-radio :value="1"> 启用 </a-radio>
            <a-radio :value="0"> 禁用 </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="itemFormData.remark"
            :maxlength="200"
            placeholder="请输入备注"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
