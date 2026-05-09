<script setup lang="ts">
import { Edit, Plus, RefreshCcw, Search, ShieldCheck, Trash2, Users } from '@lucide/vue';
import { message } from 'antdv-next';
import { confirmAction } from '@/utils/feedback';
import { computed, onMounted, reactive, ref } from 'vue';
import {
  getRolePage,
  getRole,
  createRole,
  updateRole,
  deleteRoleRecord,
  updateRoleStatus,
  getPermissionCatalog,
  getRolePermissionList,
  assignRolePermissions,
  getRoleUsers,
} from '@/api/role';

interface RoleItem {
  id: string;
  name: string;
  code: string;
  dataScope: string;
  permissionCount: number;
  status: number;
  remark?: string;
  createTime: string;
}

interface RoleDetail {
  id: string;
  name: string;
  code: string;
  dataScope: string;
  status: number;
  remark?: string;
  createTime: string;
}

interface PermissionItem {
  code: string;
  name: string;
  type: string;
  category: string;
  moduleName?: string;
}

interface RelatedUserItem {
  id: string;
  username: string;
  realName: string;
  orgName: string;
  status: number;
}

const loading = ref(false);
const roleList = ref<RoleItem[]>([]);
const total = ref(0);
const allPermissions = ref<PermissionItem[]>([]);
const selectedPermissions = ref<string[]>([]);
const relatedUsers = ref<RelatedUserItem[]>([]);

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

const dialogVisible = ref(false);
const dialogTitle = ref('新增角色');
const submitting = ref(false);

const permissionDialogVisible = ref(false);
const permissionLoading = ref(false);
const permissionSearchKeyword = ref('');
const permissionCategoryFilter = ref('all');

const userDialogVisible = ref(false);
const relatedUserLoading = ref(false);

const currentRole = ref<Partial<RoleItem>>({});

const formRef = ref();
const formData = reactive({
  id: '',
  name: '',
  code: '',
  dataScope: '本人',
  status: 1,
  remark: '',
});

const formRules = {
  name: [{ required: true, message: '角色名称必填', type: 'error' as const }],
  code: [{ required: true, message: '角色编码必填', type: 'error' as const }],
};

const columns = [
  { dataIndex: 'name', title: '角色名称', width: 150 },
  { dataIndex: 'code', title: '角色编码', width: 150 },
  { dataIndex: 'dataScope', title: '数据权限', width: 120 },
  { dataIndex: 'permissionCount', title: '权限数量', width: 100 },
  { dataIndex: 'status', title: '状态', width: 80 },
  { dataIndex: 'remark', title: '备注', ellipsis: true },
  { dataIndex: 'createTime', title: '创建时间', width: 180 },
  { dataIndex: 'action', title: '操作', width: 320, fixed: 'right' as const },
];

const tableScroll = { x: 1240 };

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const dataScopeOptions = [
  { label: '全部数据', value: '全部' },
  { label: '本组织数据', value: '本组织' },
  { label: '本组织及下级数据', value: '本组织及下级' },
  { label: '仅本人数据', value: '本人' },
];

const userColumns = [
  { dataIndex: 'username', title: '用户名', width: 120 },
  { dataIndex: 'realName', title: '真实姓名', width: 120 },
  { dataIndex: 'orgName', title: '所属组织', ellipsis: true },
  { dataIndex: 'status', title: '状态', width: 80 },
];

const permissionCategories = computed(() => {
  const categoryMap = new Map<string, string>();
  allPermissions.value.forEach((item) => {
    categoryMap.set(item.category, item.moduleName || item.category);
  });
  return [
    { key: 'all', label: '全部权限' },
    ...Array.from(categoryMap.entries()).map(([category, label]) => ({ key: category, label })),
  ];
});

const filteredPermissions = computed(() => {
  const keyword = permissionSearchKeyword.value.trim().toLowerCase();
  return allPermissions.value.filter((item) => {
    const matchKeyword =
      !keyword ||
      item.name.toLowerCase().includes(keyword) ||
      item.code.toLowerCase().includes(keyword);
    const matchCategory =
      permissionCategoryFilter.value === 'all' || item.category === permissionCategoryFilter.value;
    return matchKeyword && matchCategory;
  });
});

async function fetchList() {
  loading.value = true;
  try {
    const res = await getRolePage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      name: searchParams.name || undefined,
      code: searchParams.code || undefined,
      status: searchParams.status,
    });
    roleList.value = res.list;
    total.value = res.total;
    pagination.current = res.pageNum;
    pagination.pageSize = res.pageSize;
  } finally {
    loading.value = false;
  }
}

async function fetchPermissionCatalog() {
  allPermissions.value = await getPermissionCatalog();
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

function handleAdd() {
  dialogTitle.value = '新增角色';
  Object.assign(formData, {
    id: '',
    name: '',
    code: '',
    dataScope: '本人',
    status: 1,
    remark: '',
  });
  dialogVisible.value = true;
}

async function handleEdit(row: RoleItem) {
  const detail = await getRole(row.id);
  dialogTitle.value = '编辑角色';
  Object.assign(formData, {
    id: detail.id,
    name: detail.name,
    code: detail.code,
    dataScope: detail.dataScope,
    status: detail.status,
    remark: detail.remark || '',
  });
  dialogVisible.value = true;
}

async function handleAuthorize(row: RoleItem) {
  permissionLoading.value = true;
  try {
    currentRole.value = row;
    if (allPermissions.value.length === 0) {
      await fetchPermissionCatalog();
    }
    const assigned = await getRolePermissionList(row.id);
    selectedPermissions.value = assigned.map((item) => item.code);
    permissionSearchKeyword.value = '';
    permissionCategoryFilter.value = 'all';
    permissionDialogVisible.value = true;
  } finally {
    permissionLoading.value = false;
  }
}

async function handleViewUsers(row: RoleItem) {
  relatedUserLoading.value = true;
  try {
    currentRole.value = row;
    relatedUsers.value = await getRoleUsers(row.id);
    userDialogVisible.value = true;
  } finally {
    relatedUserLoading.value = false;
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  submitting.value = true;
  try {
    const payload = {
      name: formData.name,
      code: formData.code,
      dataScope: formData.dataScope,
      status: formData.status,
      remark: formData.remark || null,
    };

    if (formData.id) {
      await updateRole(formData.id, payload);
      message.success('修改成功');
    } else {
      await createRole(payload);
      message.success('新增成功');
    }
    dialogVisible.value = false;
    fetchList();
  } finally {
    submitting.value = false;
  }
}

async function handleStatusChange(status: number, row: RoleItem) {
  const previousStatus = row.status;
  row.status = status;
  try {
    await updateRoleStatus(row.id, { status });
    message.success(status === 1 ? '启用成功' : '禁用成功');
  } catch {
    row.status = previousStatus;
  }
}

async function handleSubmitPermission() {
  if (!currentRole.value.id) {
    return;
  }
  try {
    await assignRolePermissions(currentRole.value.id, {
      permissionCodes: selectedPermissions.value,
    });
    message.success('权限授权成功');
    permissionDialogVisible.value = false;
    fetchList();
  } catch (error) {}
}

async function handleDelete(row: RoleItem) {
  const confirm = await confirmAction('确认删除该角色吗？');
  if (!confirm) {
    return;
  }

  try {
    await deleteRoleRecord(row.id);
    message.success('删除成功');
    fetchList();
  } catch (error) {}
}

onMounted(async () => {
  await Promise.all([fetchList(), fetchPermissionCatalog()]);
});
</script>

<template>
  <div class="role-page flex flex-col gap-4">
    <a-card variant="borderless" class="shadow-sm">
      <a-row :gutter="[16, 12]" class="filter-layout" align="top">
        <a-col :xs="24" :xl="20">
          <a-form class="filter-form-grid" :label-col="{ style: { width: '72px' } }">
            <a-row :gutter="[24, 18]" align="top">
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="角色名称">
                  <a-input
                    v-model:value="searchParams.name"
                    allow-clear
                    placeholder="请输入角色名称"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="角色编码">
                  <a-input
                    v-model:value="searchParams.code"
                    allow-clear
                    placeholder="请输入角色编码"
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

    <a-card variant="borderless" class="table-card shadow-sm">
      <div class="table-toolbar">
        <div class="table-toolbar__actions">
          <a-button v-auth="'system:role:create'" type="primary" @click="handleAdd">
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
        :data-source="roleList"
        :columns="columns"
        :loading="loading"
        :pagination="{ ...pagination, total }"
        :scroll="tableScroll"
        bordered
        hover
        stripe
        size="small"
        @change="handlePageChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
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
                v-auth="'system:role:update'"
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
                v-auth="'system:role:permission:assign'"
                type="primary"
                size="small"
                @click="handleAuthorize(record)"
              >
                <template #icon>
                  <ShieldCheck class="h-3.5 w-3.5" />
                </template>
                授权
              </a-button>
              <a-button
                v-auth="'system:role:user:view'"
                variant="outlined"
                size="small"
                @click="handleViewUsers(record)"
              >
                <template #icon>
                  <Users class="h-3.5 w-3.5" />
                </template>
                用户
              </a-button>
              <a-button
                v-auth="'system:role:delete'"
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
    width="600px"
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
      <a-form-item label="角色名称" name="name">
        <a-input v-model:value="formData.name" placeholder="请输入角色名称" />
      </a-form-item>
      <a-form-item label="角色编码" name="code">
        <a-input v-model:value="formData.code" placeholder="请输入角色编码" />
      </a-form-item>
      <a-form-item label="数据权限" name="dataScope">
        <a-select
          v-model:value="formData.dataScope"
          placeholder="请选择数据权限范围"
          :options="dataScopeOptions"
        />
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

  <a-modal
    v-model:open="permissionDialogVisible"
    :title="`权限授权 - ${currentRole.name || ''}`"
    width="900px"
    :confirm-loading="permissionLoading"
    @ok="handleSubmitPermission"
  >
    <div class="permission-editor flex h-[500px] gap-4">
      <div class="permission-sidebar w-48 shrink-0">
        <h4 class="mb-3 text-sm font-semibold">权限分类</h4>
        <div class="space-y-1">
          <div
            v-for="cat in permissionCategories"
            :key="cat.key"
            class="permission-category"
            :class="permissionCategoryFilter === cat.key ? 'permission-category--active' : ''"
            @click="permissionCategoryFilter = cat.key"
          >
            {{ cat.label }}
          </div>
        </div>
      </div>

      <div class="min-w-0 flex-1">
        <div class="mb-4">
          <a-input
            v-model:value="permissionSearchKeyword"
            allow-clear
            placeholder="搜索权限名称或编码"
          >
            <template #prefix>
              <Search class="h-4 w-4" />
            </template>
          </a-input>
        </div>
        <a-checkbox-group v-model:value="selectedPermissions" class="permission-list space-y-2">
          <div
            v-for="permission in filteredPermissions"
            :key="permission.code"
            class="permission-item"
          >
            <a-checkbox :value="permission.code" />
            <div class="flex-1">
              <div class="text-sm font-medium">{{ permission.name }}</div>
              <div class="text-xs text-[#86909c] dark:text-gray-400">
                {{ permission.code }} · {{ permission.moduleName || permission.category }}
              </div>
            </div>
            <a-tag size="small" variant="filled">
              {{ permission.type }}
            </a-tag>
          </div>
        </a-checkbox-group>
      </div>

      <div class="selected-panel w-56 shrink-0">
        <h4 class="mb-3 text-sm font-semibold">已选权限</h4>
        <div class="mb-4">
          <div class="text-2xl font-bold text-[#0052d9]">{{ selectedPermissions.length }}</div>
          <div class="text-xs text-[#86909c] dark:text-gray-400">个权限</div>
        </div>
        <div class="max-h-[400px] space-y-2 overflow-y-auto">
          <div
            v-for="permCode in selectedPermissions"
            :key="permCode"
            class="rounded border border-[#e7edf6] bg-[#f7faff] p-2 text-xs dark:border-gray-700 dark:bg-gray-700"
          >
            {{ allPermissions.find((item) => item.code === permCode)?.name || permCode }}
          </div>
        </div>
      </div>
    </div>
  </a-modal>

  <a-modal
    v-model:open="userDialogVisible"
    :title="`关联用户 - ${currentRole.name || ''}`"
    width="700px"
    :footer="null"
  >
    <a-table
      row-key="id"
      :data-source="relatedUsers"
      :columns="userColumns"
      :loading="relatedUserLoading"
      :pagination="{ disabled: true }"
      bordered
      hover
      size="middle"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 1 ? 'success' : 'warning'" variant="filled" size="small">
            {{ record.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </template>
      </template>
    </a-table>
  </a-modal>
</template>

<style scoped>
.role-page {
  color: #1d2129;
}

.filter-control {
  width: 190px;
}

.filter-control--short {
  width: 150px;
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
  gap: 16px;
  padding-bottom: 12px;
}

.table-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.permission-editor {
  border-radius: 8px;
  background: #fbfcff;
}

.permission-sidebar {
  border-right: 1px solid #e7edf6;
  padding-right: 14px;
}

.permission-category {
  cursor: pointer;
  border-radius: 6px;
  padding: 8px 10px;
  color: #4e5969;
  transition:
    background-color 0.16s ease,
    color 0.16s ease;
}

.permission-category:hover {
  background: #f1f5fb;
}

.permission-category--active {
  background: #0052d9;
  color: #ffffff;
}

.permission-list {
  max-height: 444px;
  overflow: auto;
  padding-right: 4px;
}

.permission-item {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #e7edf6;
  border-radius: 8px;
  background: #ffffff;
  padding: 10px 12px;
  transition:
    border-color 0.16s ease,
    box-shadow 0.16s ease;
}

.permission-item:hover {
  border-color: #0052d9;
  box-shadow: 0 8px 18px rgb(0 82 217 / 8%);
}

.selected-panel {
  border-left: 1px solid #e7edf6;
  padding-left: 14px;
}

.role-page :deep(.ant-form-item) {
  margin-right: 12px;
  margin-bottom: 10px;
}

.role-page :deep(.ant-table-thead > tr > th),
.role-page :deep(.ant-table-tbody > tr > td) {
  padding-top: 9px;
  padding-bottom: 9px;
}

.role-page :deep(.ant-table-pagination) {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef2f7;
}
</style>
