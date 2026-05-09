<script setup lang="ts">
import { Edit, Plus, RefreshCcw, Search, ShieldAlert, Trash2 } from '@lucide/vue';
import { message } from 'antdv-next';
import { confirmAction } from '@/utils/feedback';
import { computed, onMounted, reactive, ref } from 'vue';
import {
  getSettingPage,
  getSetting,
  createSetting,
  updateSetting,
  deleteSetting,
  type SettingRequest,
} from '@/api/setting';

type SettingValueType = SettingRequest['valueType'];
type SettingScopeType = SettingRequest['scopeType'];
type SettingEffectiveMode = SettingRequest['effectiveMode'];

interface SettingItem {
  id: string;
  settingKey: string;
  settingName: string;
  settingValue: string;
  valueType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON';
  scopeType: 'GLOBAL' | 'ORG' | 'USER';
  scopeId?: string | null;
  groupCode: string;
  secretFlag: boolean;
  effectiveMode: 'IMMEDIATE' | 'RESTART_REQUIRED';
  status: number;
  remark?: string | null;
  updateTime: string;
}

interface SettingDetail extends SettingItem {}

const valueTypeOptions = [
  { label: '字符串', value: 'STRING' },
  { label: '数字', value: 'NUMBER' },
  { label: '布尔', value: 'BOOLEAN' },
  { label: 'JSON', value: 'JSON' },
];

const scopeTypeOptions = [
  { label: '全局', value: 'GLOBAL' },
  { label: '组织', value: 'ORG' },
  { label: '用户', value: 'USER' },
];

const effectiveModeOptions = [
  { label: '即时生效', value: 'IMMEDIATE' },
  { label: '重启生效', value: 'RESTART_REQUIRED' },
];

const valueTypeLabelMap: Record<string, string> = {
  STRING: '字符串',
  NUMBER: '数字',
  BOOLEAN: '布尔',
  JSON: 'JSON',
};

const scopeTypeLabelMap: Record<string, string> = {
  GLOBAL: '全局',
  ORG: '组织',
  USER: '用户',
};

const effectiveModeLabelMap: Record<string, string> = {
  IMMEDIATE: '即时生效',
  RESTART_REQUIRED: '重启生效',
};

const loading = ref(false);
const settingList = ref<SettingItem[]>([]);
const total = ref(0);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const searchParams = reactive({
  settingName: '',
  settingKey: '',
  groupCode: '',
});

const groupOptions = computed(() => {
  return Array.from(new Set(settingList.value.map((item) => item.groupCode))).map((groupCode) => ({
    label: groupCode,
    value: groupCode,
  }));
});

const dialogVisible = ref(false);
const dialogTitle = ref('新增配置');
const submitting = ref(false);

const formRef = ref();
const formData = reactive({
  id: '',
  settingKey: '',
  settingName: '',
  settingValue: '',
  valueType: 'STRING' as SettingValueType,
  scopeType: 'GLOBAL' as SettingScopeType,
  scopeId: '',
  groupCode: 'security',
  secretFlag: false,
  effectiveMode: 'IMMEDIATE' as SettingEffectiveMode,
  status: 1,
  remark: '',
});

const formRules = {
  settingKey: [{ required: true, message: '配置 Key 必填', type: 'error' as const }],
  settingName: [{ required: true, message: '配置名称必填', type: 'error' as const }],
  settingValue: [{ required: true, message: '配置值必填', type: 'error' as const }],
  valueType: [{ required: true, message: '值类型必填', type: 'error' as const }],
  scopeType: [{ required: true, message: '作用域必填', type: 'error' as const }],
  groupCode: [{ required: true, message: '配置分组必填', type: 'error' as const }],
  effectiveMode: [{ required: true, message: '生效方式必填', type: 'error' as const }],
};

const columns = [
  { dataIndex: 'settingName', title: '配置名称', width: 180, ellipsis: true },
  { dataIndex: 'settingKey', title: '配置 Key', width: 240, ellipsis: true },
  { dataIndex: 'settingValue', title: '配置值', width: 220, ellipsis: true },
  { dataIndex: 'groupCode', title: '配置分组', width: 120 },
  { dataIndex: 'remark', title: '备注', ellipsis: true },
  { dataIndex: 'updateTime', title: '更新时间', width: 180 },
  { dataIndex: 'action', title: '操作', width: 160, fixed: 'right' as const },
];

const tableScroll = { x: 1260 };

async function fetchList() {
  loading.value = true;
  try {
    const res = await getSettingPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      settingName: searchParams.settingName || undefined,
      settingKey: searchParams.settingKey || undefined,
      groupCode: searchParams.groupCode || undefined,
    });
    settingList.value = res.list;
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
  searchParams.settingName = '';
  searchParams.settingKey = '';
  searchParams.groupCode = '';
  handleSearch();
}

function handlePageChange(pageInfo: { current?: number; pageSize?: number }) {
  pagination.current = pageInfo.current ?? pagination.current;
  pagination.pageSize = pageInfo.pageSize ?? pagination.pageSize;
  fetchList();
}

function handleAdd() {
  dialogTitle.value = '新增配置';
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
  });
  dialogVisible.value = true;
}

async function handleEdit(row: SettingItem) {
  const detail = await getSetting(row.id);
  dialogTitle.value = '编辑配置';
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
    const payload: SettingRequest = {
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
    };

    if (formData.id) {
      await updateSetting(formData.id, payload);
      message.success('修改成功');
    } else {
      await createSetting(payload);
      message.success('新增成功');
    }
    dialogVisible.value = false;
    fetchList();
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(row: SettingItem) {
  const confirm = await confirmAction(`确认删除配置”${row.settingName}”吗？`);
  if (!confirm) {
    return;
  }

  try {
    await deleteSetting(row.id);
    message.success('删除成功');
    fetchList();
  } catch (error) {}
}

onMounted(() => {
  fetchList();
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
                <a-form-item label="配置名称">
                  <a-input
                    v-model:value="searchParams.settingName"
                    allow-clear
                    placeholder="请输入配置名称"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="配置 Key">
                  <a-input
                    v-model:value="searchParams.settingKey"
                    allow-clear
                    placeholder="请输入配置 Key"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="配置分组">
                  <a-select
                    v-model:value="searchParams.groupCode"
                    allow-clear
                    placeholder="请选择配置分组"
                    class="filter-control"
                    :options="groupOptions"
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
          <a-button v-auth="'system:setting:create'" type="primary" @click="handleAdd">
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
        :data-source="settingList"
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
          <template v-if="column.dataIndex === 'settingValue'">
            <div class="flex items-center gap-2">
              <span class="truncate">{{ record.settingValue }}</span>
              <ShieldAlert v-if="record.secretFlag" class="h-3.5 w-3.5 text-[#e34d59]" />
            </div>
          </template>

          <template v-else-if="column.dataIndex === 'groupCode'">
            <a-tag color="blue" variant="filled" size="small">
              {{ record.groupCode }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'remark'">
            {{ record.remark || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'action'">
            <div class="row-actions">
              <a-button
                v-auth="'system:setting:update'"
                type="primary"
                variant="text"
                size="small"
                @click="handleEdit(record)"
              >
                <template #icon>
                  <Edit class="h-3.5 w-3.5" />
                </template>
                编辑
              </a-button>
              <a-button
                v-auth="'system:setting:delete'"
                danger
                variant="text"
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
    width="760px"
    :confirm-loading="submitting"
    @ok="handleSubmit"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-align="right"
      :label-col="{ style: { width: '110px' } }"
    >
      <a-form-item label="配置名称" name="settingName">
        <a-input v-model:value="formData.settingName" placeholder="请输入配置名称" />
      </a-form-item>
      <a-form-item label="配置 Key" name="settingKey">
        <a-input
          v-model:value="formData.settingKey"
          placeholder="如 security.session.timeout-minute"
        />
      </a-form-item>
      <a-form-item label="配置值" name="settingValue">
        <a-textarea v-model:value="formData.settingValue" auto-size placeholder="请输入配置值" />
      </a-form-item>
      <a-form-item label="值类型" name="valueType">
        <a-select
          v-model:value="formData.valueType"
          placeholder="请选择值类型"
          :options="valueTypeOptions"
        />
      </a-form-item>
      <a-form-item label="作用域" name="scopeType">
        <a-select
          v-model:value="formData.scopeType"
          placeholder="请选择作用域"
          :options="scopeTypeOptions"
        />
      </a-form-item>
      <a-form-item v-if="formData.scopeType !== 'GLOBAL'" label="作用域对象" name="scopeId">
        <a-input v-model:value="formData.scopeId" placeholder="请输入组织 ID 或用户 ID" />
      </a-form-item>
      <a-form-item label="配置分组" name="groupCode">
        <a-input v-model:value="formData.groupCode" placeholder="如 security / file / session" />
      </a-form-item>
      <a-form-item label="生效方式" name="effectiveMode">
        <a-select
          v-model:value="formData.effectiveMode"
          placeholder="请选择生效方式"
          :options="effectiveModeOptions"
        />
      </a-form-item>
      <a-form-item label="敏感配置" name="secretFlag">
        <a-radio-group v-model:value="formData.secretFlag">
          <a-radio :value="false"> 否 </a-radio>
          <a-radio :value="true"> 是 </a-radio>
        </a-radio-group>
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
      <div class="rounded-lg bg-[#f8f9fb] px-4 py-3 text-xs text-[#86909c]">
        当前配置: {{ scopeTypeLabelMap[formData.scopeType] }} /
        {{ valueTypeLabelMap[formData.valueType] }} /
        {{ effectiveModeLabelMap[formData.effectiveMode] }}
      </div>
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
