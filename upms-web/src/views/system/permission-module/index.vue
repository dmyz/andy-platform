<script setup lang="ts">
import { Edit, Plus, RefreshCcw, Search, Trash2 } from '@lucide/vue';
import { message } from 'antdv-next';
import { computed, onMounted, reactive, ref } from 'vue';
import {
  createPermissionModule,
  deletePermissionModule,
  getPermissionModule,
  getPermissionModuleOptions,
  getPermissionModulePage,
  updatePermissionModule,
  type PermissionModule,
  type PermissionModuleOption,
  type PermissionModuleRequest,
} from '@/api/permission-module';
import { confirmAction } from '@/utils/feedback';

const loading = ref(false);
const submitting = ref(false);
const moduleList = ref<PermissionModule[]>([]);
const moduleCatalog = ref<PermissionModule[]>([]);
const moduleOptions = ref<PermissionModuleOption[]>([]);
const total = ref(0);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const searchParams = reactive({
  name: '',
  code: '',
  status: undefined as number | undefined,
});

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const dialogVisible = ref(false);
const dialogTitle = ref('新增权限模块');
const formRef = ref();

const formData = reactive({
  id: '',
  name: '',
  code: '',
  parentCode: '',
  sortOrder: 1,
  status: 1,
  remark: '',
});

const formRules = {
  name: [{ required: true, message: '模块名称必填', type: 'error' as const }],
  code: [{ required: true, message: '模块编码必填', type: 'error' as const }],
  sortOrder: [{ required: true, message: '排序号必填', type: 'error' as const }],
};

const columns = [
  { dataIndex: 'name', title: '模块名称', width: 160, ellipsis: true },
  { dataIndex: 'code', title: '模块编码', width: 180, ellipsis: true },
  { dataIndex: 'parentCode', title: '父模块编码', width: 140 },
  { dataIndex: 'sortOrder', title: '排序号', width: 90 },
  { dataIndex: 'status', title: '状态', width: 90 },
  { dataIndex: 'remark', title: '备注', ellipsis: true },
  { dataIndex: 'updateTime', title: '更新时间', width: 180 },
  { dataIndex: 'action', title: '操作', width: 180, fixed: 'right' as const },
];

const tableScroll = { x: 1120 };

function normalizeModuleCode(code?: string | null) {
  return code?.trim().toLowerCase() || '';
}

function getUnavailableParentCodes() {
  const currentCode = normalizeModuleCode(formData.code);
  if (!formData.id || !currentCode) {
    return new Set<string>();
  }

  const childMap = new Map<string, string[]>();
  moduleCatalog.value.forEach((item) => {
    const parentCode = normalizeModuleCode(item.parentCode);
    if (!parentCode) {
      return;
    }
    const children = childMap.get(parentCode) ?? [];
    children.push(normalizeModuleCode(item.code));
    childMap.set(parentCode, children);
  });

  const unavailableCodes = new Set<string>([currentCode]);
  const stack = [...(childMap.get(currentCode) ?? [])];
  while (stack.length > 0) {
    const code = stack.pop();
    if (!code || unavailableCodes.has(code)) {
      continue;
    }
    unavailableCodes.add(code);
    stack.push(...(childMap.get(code) ?? []));
  }
  return unavailableCodes;
}

async function fetchOptions() {
  const [options, catalog] = await Promise.all([
    getPermissionModuleOptions(),
    getPermissionModulePage({ pageNum: 1, pageSize: 1000 }),
  ]);
  moduleOptions.value = options;
  moduleCatalog.value = catalog.list;
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await getPermissionModulePage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      name: searchParams.name || undefined,
      code: searchParams.code || undefined,
      status: searchParams.status,
    });
    moduleList.value = res.list;
    total.value = res.total;
    pagination.current = res.pageNum;
    pagination.pageSize = res.pageSize;
  } finally {
    loading.value = false;
  }
}

async function refreshData() {
  await Promise.all([fetchList(), fetchOptions()]);
}

function handleSearch() {
  pagination.current = 1;
  fetchList();
}

function handleReset() {
  searchParams.name = '';
  searchParams.code = '';
  searchParams.status = undefined;
  handleSearch();
}

function handlePageChange(pageInfo: { current?: number; pageSize?: number }) {
  pagination.current = pageInfo.current ?? pagination.current;
  pagination.pageSize = pageInfo.pageSize ?? pagination.pageSize;
  fetchList();
}

function getParentOptions() {
  const unavailableCodes = getUnavailableParentCodes();
  return moduleOptions.value.filter((item) => !unavailableCodes.has(normalizeModuleCode(item.value)));
}

const parentModuleOptions = computed(() => [
  { label: '无父模块', value: '' },
  ...getParentOptions(),
]);

function resetForm() {
  Object.assign(formData, {
    id: '',
    name: '',
    code: '',
    parentCode: '',
    sortOrder: 1,
    status: 1,
    remark: '',
  });
}

function handleAdd() {
  dialogTitle.value = '新增权限模块';
  resetForm();
  dialogVisible.value = true;
}

async function handleEdit(row: PermissionModule) {
  const detail = await getPermissionModule(row.id);
  dialogTitle.value = '编辑权限模块';
  Object.assign(formData, {
    id: detail.id,
    name: detail.name,
    code: detail.code,
    parentCode: detail.parentCode || '',
    sortOrder: detail.sortOrder,
    status: detail.status,
    remark: detail.remark || '',
  });
  dialogVisible.value = true;
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  const parentCode = normalizeModuleCode(formData.parentCode);
  const currentCode = normalizeModuleCode(formData.code);
  if (parentCode && parentCode === currentCode) {
    message.warning('父模块不能选择自身');
    return;
  }
  if (parentCode && getUnavailableParentCodes().has(parentCode)) {
    message.warning('父模块不能选择当前模块的下级模块');
    return;
  }

  submitting.value = true;
  try {
    const payload: PermissionModuleRequest = {
      name: formData.name,
      code: formData.code,
      parentCode: formData.parentCode || null,
      sortOrder: formData.sortOrder,
      status: formData.status,
      remark: formData.remark || null,
    };

    if (formData.id) {
      await updatePermissionModule(formData.id, payload);
      message.success('修改成功');
    } else {
      await createPermissionModule(payload);
      message.success('新增成功');
    }

    dialogVisible.value = false;
    await refreshData();
  } finally {
    submitting.value = false;
  }
}

async function handleStatusChange(status: number, row: PermissionModule) {
  const previousStatus = row.status;
  row.status = status;
  try {
    await updatePermissionModule(row.id, {
      name: row.name,
      code: row.code,
      parentCode: row.parentCode || null,
      sortOrder: row.sortOrder,
      status,
      remark: row.remark || null,
    });
    message.success(status === 1 ? '启用成功' : '停用成功');
    await refreshData();
  } catch {
    row.status = previousStatus;
  }
}

async function handleDelete(row: PermissionModule) {
  const confirm = await confirmAction(`确认删除权限模块”${row.name}”吗？`);
  if (!confirm) {
    return;
  }

  try {
    await deletePermissionModule(row.id);
    message.success('删除成功');
    await refreshData();
  } catch (error) {}
}

onMounted(() => {
  refreshData();
});
</script>

<template>
  <div class="permission-module-page flex flex-col gap-4">
    <a-card variant="borderless" class="rounded-lg shadow-sm">
      <a-row :gutter="[16, 12]" class="filter-layout" align="top">
        <a-col :xs="24" :xl="20">
          <a-form class="filter-form-grid" :label-col="{ style: { width: '80px' } }">
            <a-row :gutter="[24, 18]" align="top">
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="模块名称">
                  <a-input
                    v-model:value="searchParams.name"
                    allow-clear
                    placeholder="请输入模块名称"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="模块编码">
                  <a-input
                    v-model:value="searchParams.code"
                    allow-clear
                    placeholder="请输入模块编码"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="状态">
                  <a-select
                    v-model:value="searchParams.status"
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
            <a-button type="primary" @click="handleSearch">
              <template #icon>
                <Search class="h-4 w-4" />
              </template>
              查询
            </a-button>
            <a-button variant="outlined" @click="handleReset">
              <template #icon>
                <RefreshCcw class="h-4 w-4" />
              </template>
              重置
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <a-card variant="borderless" class="table-card flex-1 rounded-lg shadow-sm">
      <div class="table-toolbar">
        <div class="table-toolbar__actions">
          <a-button v-auth="'system:permission-module:create'" type="primary" @click="handleAdd">
            <template #icon>
              <Plus class="h-4 w-4" />
            </template>
            新增
          </a-button>
        </div>
        <a-button variant="outlined" size="small" @click="refreshData">
          <template #icon>
            <RefreshCcw class="h-4 w-4" />
          </template>
          刷新
        </a-button>
      </div>
      <a-table
        row-key="id"
        :data-source="moduleList"
        :columns="columns"
        :loading="loading"
        :pagination="{ ...pagination, total }"
        :scroll="tableScroll"
        bordered
        hover
        stripe
        @change="handlePageChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'parentCode'">
            {{ record.parentCode || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'status'">
            <a-switch
              :checked="record.status"
              :checked-value="1"
              :un-checked-value="0"
              @change="
                (val: number | string | boolean) => handleStatusChange(val as number, record)
              "
            />
          </template>

          <template v-else-if="column.dataIndex === 'remark'">
            {{ record.remark || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'updateTime'">
            {{ record.updateTime || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'action'">
            <div class="row-actions">
              <a-button
                v-auth="'system:permission-module:update'"
                type="primary"
                size="small"
                @click="handleEdit(record)"
              >
                <template #icon>
                  <Edit class="h-3.5 w-3.5" />
                </template>
                编辑
              </a-button>
              <a-button
                v-auth="'system:permission-module:delete'"
                danger
                variant="outlined"
                size="small"
                @click="handleDelete(record)"
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
    </a-card>

    <a-modal
      v-model:open="dialogVisible"
      :title="dialogTitle"
      width="640px"
      :confirm-loading="submitting"
      @ok="handleSubmit"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-align="right"
        :label-col="{ style: { width: '100px' } }"
      >
        <a-form-item label="模块名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入模块名称" />
        </a-form-item>
        <a-form-item label="模块编码" name="code">
          <a-input v-model:value="formData.code" placeholder="如 user / system / navigation" />
        </a-form-item>
        <a-form-item label="父模块" name="parentCode">
          <a-select
            v-model:value="formData.parentCode"
            allow-clear
            placeholder="请选择父模块"
            :options="parentModuleOptions"
          />
        </a-form-item>
        <a-form-item label="排序号" name="sortOrder">
          <a-input-number v-model:value="formData.sortOrder" :min="0" class="w-full" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1"> 启用 </a-radio>
            <a-radio :value="0"> 停用 </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="formData.remark" :maxlength="200" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.filter-control {
  width: 190px;
}

.filter-actions,
.row-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.table-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.permission-module-page :deep(.ant-form-item) {
  margin-right: 12px;
  margin-bottom: 10px;
}
</style>
