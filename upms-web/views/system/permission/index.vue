<script setup lang="ts">
import { Edit, Plus, RefreshCcw, Search, Trash2 } from '@lucide/vue';
import { message } from 'antdv-next';
import { confirmAction } from '@/utils/feedback';
import { computed, onMounted, reactive, ref } from 'vue';
import {
  getPermissionPage,
  getPermission,
  createPermission,
  updatePermission,
  deletePermission,
  type PermissionRequest,
  type PermissionItem,
} from '@/api/permission';
import { getPermissionModuleOptions, type PermissionModuleOption } from '@/api/permission-module';

type PermissionType = PermissionRequest['type'];

const typeOptions = [
  { label: '导航访问', value: 'NAV_ACCESS' },
  { label: '菜单', value: 'MENU' },
  { label: '按钮', value: 'BUTTON' },
  { label: 'API', value: 'API' },
  { label: '页面动作', value: 'UI_ACTION' },
  { label: '数据权限', value: 'DATA' },
];

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const typeLabelMap: Record<string, string> = {
  API: 'API',
  UI_ACTION: '页面动作',
  DATA: '数据权限',
  NAV_ACCESS: '导航访问',
  MENU: '菜单',
  BUTTON: '按钮',
};

const loading = ref(false);
const permissionList = ref<PermissionItem[]>([]);
const moduleOptions = ref<PermissionModuleOption[]>([]);
const moduleOptionsLoadFailed = ref(false);
const total = ref(0);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const searchParams = reactive({
  name: '',
  code: '',
  type: undefined as PermissionType | undefined,
  status: undefined as number | undefined,
});

const dialogVisible = ref(false);
const dialogTitle = ref('新增权限');
const submitting = ref(false);

const formRef = ref();
const formData = reactive({
  id: '',
  name: '',
  code: '',
  type: 'BUTTON' as PermissionType,
  resourceType: 'PERMISSION',
  actionCode: '',
  moduleCode: '',
  status: 1,
  remark: '',
});

const formRules = {
  name: [{ required: true, message: '权限名称必填', type: 'error' as const }],
  code: [{ required: true, message: '权限编码必填', type: 'error' as const }],
  type: [{ required: true, message: '权限类型必填', type: 'error' as const }],
  resourceType: [{ required: true, message: '资源类型必填', type: 'error' as const }],
  moduleCode: [{ required: true, message: '模块编码必填', type: 'error' as const }],
};

const columns = [
  { dataIndex: 'name', title: '权限名称', width: 160, ellipsis: true },
  { dataIndex: 'code', title: '权限编码', width: 240, ellipsis: true },
  { dataIndex: 'type', title: '权限类型', width: 110 },
  { dataIndex: 'resourceType', title: '资源类型', width: 120 },
  { dataIndex: 'actionCode', title: '动作编码', width: 130 },
  { dataIndex: 'moduleCode', title: '所属模块', width: 160 },
  { dataIndex: 'status', title: '状态', width: 90 },
  { dataIndex: 'remark', title: '备注', ellipsis: true },
  { dataIndex: 'updateTime', title: '更新时间', width: 180 },
  { dataIndex: 'action', title: '操作', width: 180, fixed: 'right' as const },
];

const tableScroll = { x: 1550 };

const moduleLabelMap = computed(() => {
  const result = new Map<string, string>();
  moduleOptions.value.forEach((item) => {
    result.set(item.value, item.label);
  });
  return result;
});

const moduleSelectOptions = computed(() =>
  moduleOptions.value.map((item) => ({
    label: formatModuleOption(item),
    value: item.value,
  })),
);

function formatModuleOption(item: PermissionModuleOption) {
  return `${item.label}（${item.value}）`;
}

function filterModuleOption(
  input: string,
  option?: { label?: string | number; value?: string | number },
) {
  const keyword = input.toLowerCase();
  return (
    String(option?.label || '')
      .toLowerCase()
      .includes(keyword) ||
    String(option?.value || '')
      .toLowerCase()
      .includes(keyword)
  );
}

function getModuleDisplay(row: PermissionItem) {
  return row.moduleName || moduleLabelMap.value.get(row.moduleCode) || row.moduleCode;
}

function getSelectedModuleLabel(moduleCode: string) {
  const moduleName = moduleLabelMap.value.get(moduleCode);
  return moduleName ? `${moduleName}（${moduleCode}）` : moduleCode;
}

function getDefaultModuleCode() {
  return moduleOptions.value[0]?.value || '';
}

async function fetchModuleOptions() {
  try {
    moduleOptions.value = await getPermissionModuleOptions();
    moduleOptionsLoadFailed.value = false;
    if (!formData.moduleCode) {
      formData.moduleCode = getDefaultModuleCode();
    }
  } catch {
    moduleOptions.value = [];
    moduleOptionsLoadFailed.value = true;
  }
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await getPermissionPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      name: searchParams.name || undefined,
      code: searchParams.code || undefined,
      type: searchParams.type,
      status: searchParams.status,
    });
    permissionList.value = res.list;
    total.value = res.total;
    pagination.current = res.pageNum;
    pagination.pageSize = res.pageSize;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  fetchList();
}

function handleReset() {
  searchParams.name = '';
  searchParams.code = '';
  searchParams.type = undefined;
  searchParams.status = undefined;
  handleSearch();
}

function handlePageChange(pageInfo: { current?: number; pageSize?: number }) {
  pagination.current = pageInfo.current ?? pagination.current;
  pagination.pageSize = pageInfo.pageSize ?? pagination.pageSize;
  fetchList();
}

function handleAdd() {
  dialogTitle.value = '新增权限';
  Object.assign(formData, {
    id: '',
    name: '',
    code: '',
    type: 'BUTTON',
    resourceType: 'PERMISSION',
    actionCode: '',
    moduleCode: getDefaultModuleCode(),
    status: 1,
    remark: '',
  });
  dialogVisible.value = true;
}

async function handleEdit(row: PermissionItem) {
  const detail = await getPermission(row.id);
  dialogTitle.value = '编辑权限';
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
  });
  dialogVisible.value = true;
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  submitting.value = true;
  try {
    const payload: PermissionRequest = {
      name: formData.name,
      code: formData.code,
      type: formData.type,
      resourceType: formData.resourceType,
      actionCode: formData.actionCode || undefined,
      moduleCode: formData.moduleCode.trim(),
      status: formData.status,
      remark: formData.remark || undefined,
    };

    if (formData.id) {
      await updatePermission(formData.id, payload);
      message.success('修改成功');
    } else {
      await createPermission(payload);
      message.success('新增成功');
    }
    dialogVisible.value = false;
    fetchList();
  } finally {
    submitting.value = false;
  }
}

async function handleStatusChange(status: number, row: PermissionItem) {
  const previousStatus = row.status;
  row.status = status;
  try {
    await updatePermission(row.id, {
      name: row.name,
      code: row.code,
      type: row.type,
      resourceType: row.resourceType,
      actionCode: row.actionCode || undefined,
      moduleCode: row.moduleCode,
      status,
      remark: row.remark || undefined,
    });
    message.success(status === 1 ? '启用成功' : '禁用成功');
    fetchList();
  } catch {
    row.status = previousStatus;
  }
}

async function handleDelete(row: PermissionItem) {
  const confirm = await confirmAction(`确认删除权限”${row.name}”吗？`);
  if (!confirm) {
    return;
  }

  try {
    await deletePermission(row.id);
    message.success('删除成功');
    fetchList();
  } catch (error) {}
}

onMounted(() => {
  fetchList();
  fetchModuleOptions();
});
</script>

<template>
  <div class="flex flex-col gap-4">
    <a-card variant="borderless" class="rounded-lg shadow-sm">
      <a-row :gutter="[16, 12]" class="filter-layout" align="top">
        <a-col :xs="24" :xl="20">
          <a-form class="filter-form-grid" :label-col="{ style: { width: '80px' } }">
            <a-row :gutter="[24, 18]" align="top">
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="权限名称">
                  <a-input
                    v-model:value="searchParams.name"
                    allow-clear
                    placeholder="请输入权限名称"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="权限编码">
                  <a-input
                    v-model:value="searchParams.code"
                    allow-clear
                    placeholder="请输入权限编码"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="权限类型">
                  <a-select
                    v-model:value="searchParams.type"
                    allow-clear
                    placeholder="请选择权限类型"
                    class="filter-control"
                    :options="typeOptions"
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
          <a-button v-auth="'system:permission:create'" type="primary" @click="handleAdd">
            <template #icon>
              <Plus class="h-4 w-4" />
            </template>
            新增
          </a-button>
        </div>
        <a-button variant="outlined" size="small" @click="fetchList">
          <template #icon>
            <RefreshCcw class="h-4 w-4" />
          </template>
          刷新
        </a-button>
      </div>
      <a-table
        row-key="id"
        :data-source="permissionList"
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
          <template v-if="column.dataIndex === 'type'">
            <a-tag
              :color="
                record.type === 'API' ? 'warning' : record.type === 'DATA' ? 'default' : 'primary'
              "
              variant="filled"
              size="small"
            >
              {{ typeLabelMap[record.type] || record.type }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'actionCode'">
            {{ record.actionCode || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'moduleCode'">
            <div class="flex flex-col">
              <span>{{ getModuleDisplay(record) }}</span>
              <span class="text-xs text-[#86909c]">{{ record.moduleCode }}</span>
            </div>
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

          <template v-else-if="column.dataIndex === 'action'">
            <div class="row-actions">
              <a-button
                v-auth="'system:permission:update'"
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
                v-auth="'system:permission:delete'"
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
  </div>

  <a-modal
    v-model:open="dialogVisible"
    :title="dialogTitle"
    width="680px"
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
      <a-form-item label="权限名称" name="name">
        <a-input v-model:value="formData.name" placeholder="请输入权限名称" />
      </a-form-item>
      <a-form-item label="权限编码" name="code">
        <a-input v-model:value="formData.code" placeholder="如 system:permission:create" />
      </a-form-item>
      <a-form-item label="权限类型" name="type">
        <a-select
          v-model:value="formData.type"
          placeholder="请选择权限类型"
          :options="typeOptions"
        />
      </a-form-item>
      <a-form-item label="资源类型" name="resourceType">
        <a-input
          v-model:value="formData.resourceType"
          placeholder="如 PERMISSION / USER / NAVIGATION"
        />
      </a-form-item>
      <a-form-item label="动作编码" name="actionCode">
        <a-input v-model:value="formData.actionCode" placeholder="如 CREATE / UPDATE / VIEW" />
      </a-form-item>
      <a-form-item label="所属模块" name="moduleCode">
        <a-select
          v-if="moduleSelectOptions.length > 0"
          v-model:value="formData.moduleCode"
          show-search
          placeholder="请选择所属模块"
          :options="moduleSelectOptions"
          :filter-option="filterModuleOption"
        />
        <a-input
          v-else
          v-model:value="formData.moduleCode"
          placeholder="请输入模块编码，如 user / permission"
        />
        <div
          v-if="moduleOptionsLoadFailed"
          class="mt-1 text-xs text-[#e37318] dark:text-amber-300"
        >
          模块下拉加载失败，可手动填写已启用的模块编码。
        </div>
        <div v-else-if="formData.moduleCode" class="mt-1 text-xs text-[#86909c]">
          当前选择：{{ getSelectedModuleLabel(formData.moduleCode) }}
        </div>
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="1"> 启用 </a-radio>
          <a-radio :value="0"> 禁用 </a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="备注" name="remark">
        <a-textarea v-model:value="formData.remark" :maxlength="200" placeholder="请输入备注" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<style scoped>
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
</style>
