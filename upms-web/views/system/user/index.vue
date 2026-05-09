<script setup lang="ts">
import type { MenuProps } from 'antdv-next';
import {
  Building,
  Download,
  Edit,
  Eye,
  Key,
  LogOut,
  MoreHorizontal,
  Plus,
  RefreshCcw,
  Search,
  Trash2,
  Upload,
  Users,
} from '@lucide/vue';
import { message } from 'antdv-next';
import { confirmAction } from '@/utils/feedback';
import { computed, h, onMounted, reactive, ref } from 'vue';
import {
  getUserPage,
  getUser as getUserDetail,
  createUserRecord,
  updateUserRecord,
  deleteUserRecord,
  updateUserRecordStatus,
  getUserRoleList,
  assignUserRoles,
  resetUserPasswordRecord,
  forceUserOffline,
  importUsers,
  exportUsers,
} from '@/api/user';
import { getOrgTree } from '@/api/organization';
import { getRolePage } from '@/api/role';
import { usePermission } from '@/composables/usePermission';

interface UserPageItem {
  id: string;
  username: string;
  realName: string;
  jobNumber?: string;
  mobile: string;
  email?: string;
  orgId: string;
  orgName: string;
  position?: string;
  status: number;
  lastLoginTime?: string;
  createTime: string;
}

interface UserDetailView extends UserPageItem {
  gender?: string;
  remark?: string;
  passwordResetRequired?: boolean;
}

interface OrgTreeItem {
  id: string | number;
  code?: string;
  name: string;
  status?: number;
  children?: OrgTreeItem[];
}

interface OrgOption {
  label: string;
  value: string;
  code?: string;
  rawName: string;
}

interface OrgTreeSelectNode {
  title: string;
  value: string;
  disabled?: boolean;
  children?: OrgTreeSelectNode[];
}

const { hasPermission } = usePermission();

const loading = ref(false);
const submitting = ref(false);
const importing = ref(false);
const exporting = ref(false);
const treeLoading = ref(false);
const userList = ref<UserPageItem[]>([]);
const total = ref(0);
const orgOptions = ref<OrgOption[]>([]);
const orgTree = ref<OrgTreeItem[]>([]);
const orgSelectTree = ref<OrgTreeItem[]>([]);
const extraOrgTreeNodes = ref<OrgTreeSelectNode[]>([]);
const selectedOrgId = ref('');
const selectedOrgName = ref('全部部门');
const treeKeyword = ref('');
const importInputRef = ref<HTMLInputElement | null>(null);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const searchParams = reactive({
  username: '',
  realName: '',
  mobile: '',
  orgName: '',
  status: undefined as number | undefined,
  createTimeRange: [] as string[],
});

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const drawerVisible = ref(false);
const drawerTitle = ref('新增用户');
const detailDrawerVisible = ref(false);
const detailData = ref<Partial<UserDetailView>>({});
const roleDrawerVisible = ref(false);
const roleSubmitting = ref(false);
const roleOptions = ref<Array<{ label: string; value: string }>>([]);
const selectedRoles = ref<string[]>([]);

const formRef = ref();
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
});

const formRules = {
  username: [{ required: true, message: '用户名必填', type: 'error' as const }],
  realName: [{ required: true, message: '真实姓名必填', type: 'error' as const }],
  mobile: [{ required: true, message: '手机号必填', type: 'error' as const }],
  orgId: [{ required: true, message: '所属部门必填', type: 'error' as const }],
};

const columns = [
  { dataIndex: 'username', title: '用户名', width: 120 },
  { dataIndex: 'realName', title: '真实姓名', width: 120 },
  { dataIndex: 'jobNumber', title: '工号', width: 100 },
  { dataIndex: 'mobile', title: '手机号', width: 130 },
  { dataIndex: 'email', title: '邮箱', width: 220, ellipsis: true },
  { dataIndex: 'orgName', title: '所属部门', width: 140 },
  { dataIndex: 'position', title: '岗位', width: 120 },
  { dataIndex: 'status', title: '状态', width: 90 },
  { dataIndex: 'lastLoginTime', title: '最后登录时间', width: 180 },
  { dataIndex: 'createTime', title: '创建时间', width: 180 },
  { dataIndex: 'action', title: '操作', width: 240, fixed: 'right' as const },
];

const tableScroll = { x: 1520 };

const treeKeys = {
  key: 'id',
  title: 'name',
  children: 'children',
};

const orgTreeSelectData = computed(() => [
  ...mapOrgTreeToSelectData(orgSelectTree.value),
  ...extraOrgTreeNodes.value,
]);

const orgTreeSelectStyles = {
  popup: {
    root: {
      maxHeight: '360px',
      overflow: 'auto',
    },
  },
};

const rowMoreActionPermissions = [
  'system:user:role:assign',
  'system:user:password:reset',
  'system:user:offline',
  'system:user:delete',
];

const hasMoreRowActions = computed(() =>
  rowMoreActionPermissions.some((permission) => hasPermission(permission)),
);

async function fetchList() {
  loading.value = true;
  try {
    const [startTime, endTime] = searchParams.createTimeRange;
    const res = await getUserPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      username: searchParams.username || undefined,
      realName: searchParams.realName || undefined,
      mobile: searchParams.mobile || undefined,
      orgName: searchParams.orgName || undefined,
      status: searchParams.status,
      startTime: startTime || undefined,
      endTime: endTime || undefined,
    });
    userList.value = res.list;
    total.value = res.total;
    pagination.current = res.pageNum;
    pagination.pageSize = res.pageSize;
  } finally {
    loading.value = false;
  }
}

async function fetchOrgOptions() {
  treeLoading.value = true;
  try {
    const tree = await getOrgTree();
    orgTree.value = tree;
    orgSelectTree.value = tree;
    orgOptions.value = flattenOrgTree(tree);
    extraOrgTreeNodes.value = extraOrgTreeNodes.value.filter(
      (node) => !orgOptions.value.some((item) => item.value === node.value),
    );
  } finally {
    treeLoading.value = false;
  }
}

async function fetchOrgTree() {
  treeLoading.value = true;
  try {
    orgTree.value = await getOrgTree({
      keyword: treeKeyword.value || undefined,
    });
  } finally {
    treeLoading.value = false;
  }
}

function flattenOrgTree(nodes: OrgTreeItem[], level = 1): OrgOption[] {
  return nodes.flatMap((node) => [
    {
      label: `${'　'.repeat(Math.max(level - 1, 0))}${node.name}`,
      value: normalizeOrgValue(node.id),
      code: normalizeOrgValue(node.code) || undefined,
      rawName: node.name,
    },
    ...flattenOrgTree(node.children || [], level + 1),
  ]);
}

function mapOrgTreeToSelectData(nodes: OrgTreeItem[]): OrgTreeSelectNode[] {
  return nodes.map((node) => ({
    title: node.name,
    value: normalizeOrgValue(node.id),
    disabled: node.status === 0,
    children: node.children?.length ? mapOrgTreeToSelectData(node.children) : undefined,
  }));
}

function normalizeOrgValue(value?: string | number | null) {
  return value == null ? '' : String(value);
}

function resolveOrgSelectValue(orgId?: string | number | null, orgName?: string | null) {
  const normalizedOrgId = normalizeOrgValue(orgId);
  const directOption = orgOptions.value.find((item) => item.value === normalizedOrgId);
  if (directOption) {
    return directOption.value;
  }

  const codeOption = orgOptions.value.find((item) => item.code === normalizedOrgId);
  if (codeOption) {
    return codeOption.value;
  }

  const nameOption = orgOptions.value.find((item) => item.rawName === orgName);
  return nameOption?.value || normalizedOrgId;
}

function ensureOrgOption(value: string, orgName?: string | null) {
  if (!value) {
    return;
  }

  const existsInOrgOptions = orgOptions.value.some((item) => item.value === value);

  if (!existsInOrgOptions) {
    orgOptions.value.push({
      label: orgName || value,
      value,
      rawName: orgName || value,
    });
    if (!extraOrgTreeNodes.value.some((item) => item.value === value)) {
      extraOrgTreeNodes.value.push({
        title: orgName || value,
        value,
      });
    }
  }
}

function applyOrgFilter(org?: OrgTreeItem | null) {
  selectedOrgId.value = normalizeOrgValue(org?.id);
  selectedOrgName.value = org?.name || '全部部门';
  searchParams.orgName = org?.name || '';
  pagination.current = 1;
  fetchList();
}

function handleOrgSelect(value: (string | number)[], context: { node: unknown }) {
  if (!value.length) {
    applyOrgFilter(null);
    return;
  }

  const node = context.node as OrgTreeItem & { dataRef?: OrgTreeItem };
  applyOrgFilter(node.dataRef || node);
}

function handleSearchOrgTree() {
  fetchOrgTree();
}

async function handleRefreshOrgTree() {
  treeKeyword.value = '';
  selectedOrgId.value = '';
  selectedOrgName.value = '全部部门';
  searchParams.orgName = '';
  pagination.current = 1;
  await Promise.all([fetchOrgTree(), fetchList()]);
}

function handleSearch() {
  pagination.current = 1;
  fetchList();
}

function handleReset() {
  searchParams.username = '';
  searchParams.realName = '';
  searchParams.mobile = '';
  searchParams.orgName = '';
  searchParams.status = undefined;
  searchParams.createTimeRange = [];
  selectedOrgId.value = '';
  selectedOrgName.value = '全部部门';
  pagination.current = 1;
  fetchList();
}

function handlePageChange(pageInfo: { current?: number; pageSize?: number }) {
  pagination.current = pageInfo.current ?? pagination.current;
  pagination.pageSize = pageInfo.pageSize ?? pagination.pageSize;
  fetchList();
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
  });
}

function handleAdd() {
  drawerTitle.value = '新增用户';
  resetForm();
  drawerVisible.value = true;
}

async function handleEdit(row: UserPageItem) {
  drawerTitle.value = '编辑用户';
  if (!orgOptions.value.length) {
    await fetchOrgOptions();
  }
  const detail = await getUserDetail(row.id);
  const orgId = resolveOrgSelectValue(detail.orgId, detail.orgName);
  ensureOrgOption(orgId, detail.orgName);
  Object.assign(formData, {
    id: detail.id,
    username: detail.username,
    realName: detail.realName,
    jobNumber: detail.jobNumber || '',
    mobile: detail.mobile,
    email: detail.email || '',
    gender: detail.gender || 'MALE',
    orgId,
    position: detail.position || '',
    password: '',
    status: detail.status,
    remark: detail.remark || '',
  });
  drawerVisible.value = true;
}

async function handleDetail(row: UserPageItem) {
  detailData.value = await getUserDetail(row.id);
  detailDrawerVisible.value = true;
}

async function handleAssignRole(row: UserPageItem) {
  detailData.value = row;
  const [assignedRoles, rolePage] = await Promise.all([
    getUserRoleList(row.id),
    getRolePage({ pageNum: 1, pageSize: 100, status: 1 }),
  ]);
  selectedRoles.value = assignedRoles.map((item) => item.code);
  roleOptions.value = rolePage.list
    .filter((item) => item.status === 1)
    .map((item) => ({ label: `${item.name} (${item.code})`, value: item.code }));
  roleDrawerVisible.value = true;
}

function getMoreActionItems(): MenuProps['items'] {
  const items: NonNullable<MenuProps['items']> = [];

  if (hasPermission('system:user:role:assign')) {
    items.push({
      key: 'assignRole',
      label: '角色',
      icon: () => h(Users, { class: 'h-4 w-4' }),
    });
  }

  if (hasPermission('system:user:password:reset')) {
    items.push({
      key: 'resetPassword',
      label: '重置',
      icon: () => h(Key, { class: 'h-4 w-4' }),
    });
  }

  if (hasPermission('system:user:offline')) {
    items.push({
      key: 'forceOffline',
      label: '下线',
      icon: () => h(LogOut, { class: 'h-4 w-4' }),
    });
  }

  if (hasPermission('system:user:delete')) {
    items.push({
      key: 'delete',
      label: '删除',
      icon: () => h(Trash2, { class: 'h-4 w-4' }),
      danger: true,
    });
  }

  return items;
}

function handleMoreActionClick(row: UserPageItem, { key }: { key: string | number }) {
  const action = String(key);
  if (action === 'assignRole') {
    handleAssignRole(row);
  } else if (action === 'resetPassword') {
    handleResetPassword(row);
  } else if (action === 'forceOffline') {
    handleForceOffline(row);
  } else if (action === 'delete') {
    handleDelete(row);
  }
}

function getMoreMenu(row: UserPageItem): MenuProps {
  return {
    items: getMoreActionItems(),
    onClick: (event: { key: string | number }) => handleMoreActionClick(row, event),
  } as MenuProps;
}

async function handleSaveRoles() {
  if (!detailData.value.id) {
    return;
  }
  roleSubmitting.value = true;
  try {
    await assignUserRoles(detailData.value.id, selectedRoles.value);
    message.success('角色分配成功');
    roleDrawerVisible.value = false;
  } finally {
    roleSubmitting.value = false;
  }
}

async function handleResetPassword(row: UserPageItem) {
  const confirm = await confirmAction('确认重置该用户的密码吗?');
  if (!confirm) {
    return;
  }

  try {
    await resetUserPasswordRecord(row.id);
    message.success('密码已重置，用户下次登录需修改密码');
  } catch {}
}

async function handleForceOffline(row: UserPageItem) {
  const confirm = await confirmAction('确认强制该用户下线吗?');
  if (!confirm) {
    return;
  }

  try {
    await forceUserOffline(row.id);
    message.success('已强制下线');
  } catch {}
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
    };

    if (formData.id) {
      await updateUserRecord(formData.id, payload);
      message.success('修改成功');
    } else {
      await createUserRecord(payload);
      message.success('新增成功');
    }

    drawerVisible.value = false;
    fetchList();
  } finally {
    submitting.value = false;
  }
}

async function handleStatusChange(status: number, row: UserPageItem) {
  const previousStatus = row.status === 1 ? 0 : 1;
  try {
    await updateUserRecordStatus(row.id, status);
    message.success(status === 1 ? '启用成功' : '禁用成功');
    row.status = status;
  } catch {
    row.status = previousStatus;
  }
}

async function handleDelete(row: UserPageItem) {
  const confirm = await confirmAction('确认删除该用户吗？');
  if (!confirm) {
    return;
  }

  try {
    await deleteUserRecord(row.id);
    message.success('删除成功');
    if (userList.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1;
    }
    fetchList();
  } catch {}
}

function handleImport() {
  importInputRef.value?.click();
}

async function handleImportSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) {
    return;
  }

  importing.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);
    const result = await importUsers(formData);
    message.success(
      `导入完成：新增 ${result.importedCount}，更新 ${result.updatedCount}，跳过 ${result.skippedCount}`,
    );
    await fetchList();
  } finally {
    importing.value = false;
    if (importInputRef.value) {
      importInputRef.value.value = '';
    }
  }
}

async function handleExport() {
  exporting.value = true;
  try {
    const [startTime, endTime] = searchParams.createTimeRange;
    await exportUsers({
      username: searchParams.username || undefined,
      realName: searchParams.realName || undefined,
      mobile: searchParams.mobile || undefined,
      orgName: searchParams.orgName || undefined,
      status: searchParams.status,
      startTime: startTime || undefined,
      endTime: endTime || undefined,
    });
    message.success('导出成功');
  } finally {
    exporting.value = false;
  }
}

onMounted(async () => {
  await Promise.all([fetchList(), fetchOrgOptions()]);
});
</script>

<template>
  <div class="user-page">
    <input
      ref="importInputRef"
      type="file"
      class="hidden"
      accept=".csv,text/csv"
      @change="handleImportSelected"
    />

    <div class="user-workspace">
      <a-card variant="borderless" class="department-filter-card shadow-sm">
        <template #title>
          <div class="department-filter-header">
            <Building class="h-5 w-5 text-[#0052d9]" />
            <span>部门筛选</span>
          </div>
        </template>

        <div class="department-filter-body">
          <div class="department-search">
            <a-input
              v-model:value="treeKeyword"
              allow-clear
              placeholder="搜索部门"
              @pressEnter="handleSearchOrgTree"
            >
              <template #prefix>
                <Search class="h-4 w-4" />
              </template>
            </a-input>
            <a-button
              variant="outlined"
              class="department-refresh-button"
              :loading="treeLoading"
              @click="handleRefreshOrgTree"
            >
              <template #icon>
                <RefreshCcw class="h-4 w-4" />
              </template>
              刷新
            </a-button>
          </div>

          <a-tree
            block-node
            :tree-data="orgTree"
            :field-names="treeKeys"
            default-expand-all
            :selected-keys="selectedOrgId ? [selectedOrgId] : []"
            :loading="treeLoading"
            @select="handleOrgSelect"
          >
            <template #title="node">
              <div class="department-tree-node">
                <span>{{ node.name }}</span>
                <a-tag v-if="node.status === 0" color="orange" variant="filled" size="small">
                  禁用
                </a-tag>
              </div>
            </template>
          </a-tree>
        </div>
      </a-card>

      <div class="user-content flex flex-col gap-4">
        <a-card variant="borderless" class="shadow-sm">
          <a-row :gutter="[16, 12]" class="user-filter-layout" align="top">
            <a-col :xs="24" :xl="20">
              <a-form class="user-filter-form" :label-col="{ style: { width: '72px' } }">
                <a-row :gutter="[24, 18]" align="top">
                  <a-col :xs="24" :md="12" :lg="8">
                    <a-form-item label="用户名">
                      <a-input
                        v-model:value="searchParams.username"
                        allow-clear
                        placeholder="请输入用户名"
                        class="filter-control"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12" :lg="8">
                    <a-form-item label="真实姓名">
                      <a-input
                        v-model:value="searchParams.realName"
                        allow-clear
                        placeholder="请输入真实姓名"
                        class="filter-control"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12" :lg="8">
                    <a-form-item label="手机号">
                      <a-input
                        v-model:value="searchParams.mobile"
                        allow-clear
                        placeholder="请输入手机号"
                        class="filter-control"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12" :lg="8">
                    <a-form-item label="状态">
                      <a-select
                        v-model:value="searchParams.status"
                        allow-clear
                        placeholder="请选择状态"
                        class="filter-control filter-control--short"
                        :options="statusOptions"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12" :lg="8">
                    <a-form-item label="创建时间">
                      <a-range-picker
                        v-model="searchParams.createTimeRange"
                        allow-clear
                        :placeholder="['开始时间', '结束时间']"
                        class="filter-control filter-control--range"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>
              </a-form>
            </a-col>
            <a-col :xs="24" :xl="4" class="user-filter-actions-col">
              <div class="filter-actions">
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
              </div>
            </a-col>
          </a-row>
        </a-card>

        <a-card variant="borderless" class="user-table-card shadow-sm">
          <div class="table-toolbar">
            <div class="table-toolbar__actions">
              <a-button v-auth="'system:user:create'" type="primary" @click="handleAdd">
                <template #icon>
                  <Plus class="h-4 w-4" />
                </template>
                新增
              </a-button>
              <a-button
                v-auth="'system:user:import'"
                variant="outlined"
                :loading="importing"
                @click="handleImport"
              >
                <template #icon>
                  <Upload class="h-4 w-4" />
                </template>
                导入
              </a-button>
            </div>
            <div class="table-toolbar__actions table-toolbar__actions--right">
              <a-button
                v-auth="'system:user:export'"
                variant="outlined"
                size="small"
                :loading="exporting"
                @click="handleExport"
              >
                <template #icon>
                  <Download class="h-4 w-4" />
                </template>
                导出
              </a-button>
              <a-button variant="outlined" size="small" @click="fetchList">
                <template #icon>
                  <RefreshCcw class="h-4 w-4" />
                </template>
                刷新
              </a-button>
            </div>
          </div>
          <a-table
            row-key="id"
            :data-source="userList"
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
                  v-model:checked="record.status"
                  :checked-value="1"
                  :un-checked-value="0"
                  @change="
                    (val: number | string | boolean) => handleStatusChange(val as number, record)
                  "
                />
              </template>

              <template v-else-if="column.dataIndex === 'jobNumber'">
                {{ record.jobNumber || '-' }}
              </template>

              <template v-else-if="column.dataIndex === 'email'">
                {{ record.email || '-' }}
              </template>

              <template v-else-if="column.dataIndex === 'position'">
                {{ record.position || '-' }}
              </template>

              <template v-else-if="column.dataIndex === 'lastLoginTime'">
                {{ record.lastLoginTime || '-' }}
              </template>

              <template v-else-if="column.dataIndex === 'action'">
                <div class="row-actions">
                  <a-button
                    v-auth="'system:user:view'"
                    type="primary"
                    variant="text"
                    size="small"
                    @click="handleDetail(record)"
                  >
                    <template #icon>
                      <Eye class="h-3.5 w-3.5" />
                    </template>
                    详情
                  </a-button>
                  <a-button
                    v-auth="'system:user:update'"
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
                  <a-dropdown
                    v-if="hasMoreRowActions"
                    :trigger="['click']"
                    placement="bottomRight"
                    :menu="getMoreMenu(record)"
                  >
                    <a-button variant="text" size="small" class="row-more-button">
                      <template #icon>
                        <MoreHorizontal class="h-3.5 w-3.5" />
                      </template>
                      更多
                    </a-button>
                  </a-dropdown>
                </div>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>
  </div>

  <a-drawer v-model:open="drawerVisible" :title="drawerTitle" :size="600">
    <template #footer>
      <div class="flex justify-end gap-2">
        <a-button @click="drawerVisible = false">取消</a-button>
        <a-button type="primary" :loading="submitting" @click="handleSubmit">保存</a-button>
      </div>
    </template>

    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-align="right"
      :label-col="{ style: { width: '100px' } }"
    >
      <a-form-item label="用户名" name="username">
        <a-input
          v-model:value="formData.username"
          :disabled="!!formData.id"
          placeholder="请输入唯一登录名"
        />
      </a-form-item>
      <a-form-item label="真实姓名" name="realName">
        <a-input v-model:value="formData.realName" placeholder="请输入真实姓名" />
      </a-form-item>
      <a-form-item label="工号" name="jobNumber">
        <a-input v-model:value="formData.jobNumber" placeholder="请输入工号" />
      </a-form-item>
      <a-form-item label="手机号" name="mobile">
        <a-input v-model:value="formData.mobile" placeholder="请输入手机号" />
      </a-form-item>
      <a-form-item label="邮箱" name="email">
        <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
      </a-form-item>
      <a-form-item label="性别" name="gender">
        <a-radio-group v-model:value="formData.gender">
          <a-radio value="MALE"> 男 </a-radio>
          <a-radio value="FEMALE"> 女 </a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="所属部门" name="orgId">
        <a-tree-select
          v-model:value="formData.orgId"
          allow-clear
          show-search
          tree-default-expand-all
          tree-node-filter-prop="title"
          :tree-data="orgTreeSelectData"
          :styles="orgTreeSelectStyles"
          placeholder="请选择所属部门"
        />
      </a-form-item>
      <a-form-item label="岗位" name="position">
        <a-input v-model:value="formData.position" placeholder="请输入岗位" />
      </a-form-item>
      <a-form-item v-if="!formData.id" label="初始密码" name="password">
        <a-input
          v-model:value="formData.password"
          type="password"
          auto-complete="new-password"
          placeholder="请输入初始密码"
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
  </a-drawer>

  <a-drawer v-model:open="detailDrawerVisible" title="用户详情" :size="600" :footer="null">
    <a-descriptions :column="2">
      <a-descriptions-item label="用户名">
        {{ detailData.username }}
      </a-descriptions-item>
      <a-descriptions-item label="真实姓名">
        {{ detailData.realName }}
      </a-descriptions-item>
      <a-descriptions-item label="工号">
        {{ detailData.jobNumber || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="手机号">
        {{ detailData.mobile || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="邮箱">
        {{ detailData.email || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="所属部门">
        {{ detailData.orgName || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="岗位">
        {{ detailData.position || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="状态">
        <a-tag :color="detailData.status === 1 ? 'success' : 'warning'" variant="filled">
          {{ detailData.status === 1 ? '启用' : '禁用' }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="创建时间">
        {{ detailData.createTime || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="最后登录">
        {{ detailData.lastLoginTime || '-' }}
      </a-descriptions-item>
    </a-descriptions>
  </a-drawer>

  <a-drawer v-model:open="roleDrawerVisible" title="分配角色" :size="500">
    <template #footer>
      <div class="flex justify-end gap-2">
        <a-button @click="roleDrawerVisible = false">取消</a-button>
        <a-button type="primary" :loading="roleSubmitting" @click="handleSaveRoles">保存</a-button>
      </div>
    </template>

    <div
      class="mb-4 rounded-md border border-[#e7edf6] bg-[#f7faff] px-4 py-3 dark:border-gray-700 dark:bg-gray-700"
    >
      <p class="mb-2 text-sm text-[#86909c] dark:text-gray-400">
        当前用户: {{ detailData.realName }} ({{ detailData.username }})
      </p>
    </div>
    <a-checkbox-group v-model:value="selectedRoles">
      <div class="space-y-3">
        <a-checkbox v-for="item in roleOptions" :key="item.value" :value="item.value">
          {{ item.label }}
        </a-checkbox>
      </div>
    </a-checkbox-group>
  </a-drawer>
</template>

<style scoped>
.user-page {
  color: #1d2129;
}

.user-workspace {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.department-filter-card {
  width: 292px;
  flex-shrink: 0;
}

.department-filter-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}

.department-filter-body {
  display: flex;
  min-height: 520px;
  max-height: calc(100vh - 210px);
  flex-direction: column;
  gap: 14px;
}

.department-search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
}

.department-refresh-button {
  white-space: nowrap;
}

.department-tree-node {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 6px;
}

.department-tree-node span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-content {
  min-width: 0;
  flex: 1;
}

.user-table-card {
  min-width: 0;
  overflow: hidden;
}

.user-filter-layout {
  align-items: flex-start;
}

.user-filter-layout > :deep(.ant-col:not(.user-filter-actions-col)) {
  flex: 1 1 0 !important;
  max-width: calc(100% - 120px) !important;
}

.user-filter-actions-col {
  display: flex;
  flex: 0 0 104px !important;
  min-width: 104px;
  max-width: 104px !important;
  justify-content: flex-end;
}

.user-filter-form :deep(.ant-form-item) {
  margin-right: 0;
  margin-bottom: 0;
}

.user-filter-form :deep(.filter-control) {
  width: 220px !important;
}

.filter-control {
  width: 220px;
}

.filter-control--short {
  width: 220px;
}

.filter-control--range {
  width: 220px;
}

.filter-actions,
.row-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-actions {
  flex-direction: column;
  flex-wrap: nowrap;
  align-items: stretch;
  justify-content: flex-end;
  white-space: nowrap;
}

@media (min-width: 1600px) {
  .user-filter-layout > :deep(.ant-col:not(.user-filter-actions-col)) {
    max-width: calc(100% - 236px) !important;
  }

  .user-filter-actions-col {
    flex-basis: 220px !important;
    min-width: 220px;
    max-width: 220px !important;
  }

  .filter-actions {
    flex-direction: row;
    align-items: center;
  }
}

.row-actions {
  flex-wrap: nowrap;
}

.row-more-button {
  color: #4e5969;
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
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.user-page :deep(.ant-form-item) {
  margin-right: 12px;
  margin-bottom: 10px;
}

.user-table-card :deep(.ant-card-body),
.user-table-card :deep(.ant-table-wrapper) {
  min-width: 0;
  overflow: hidden;
}

.department-filter-card :deep(.ant-card-body) {
  padding-top: 14px;
}

.department-filter-card :deep(.ant-tree) {
  min-height: 0;
  overflow: auto;
  padding-right: 2px;
}

.department-filter-card :deep(.ant-tree-node-content-wrapper) {
  border-radius: 6px;
}

.department-filter-card :deep(.ant-tree-node-selected) {
  background: #eef4ff;
}

.user-page :deep(.ant-table-thead > tr > th),
.user-page :deep(.ant-table-tbody > tr > td) {
  padding-top: 9px;
  padding-bottom: 9px;
}

.user-page :deep(.ant-table-pagination) {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef2f7;
}

@media (max-width: 767px) {
  .user-workspace {
    flex-direction: column;
    align-items: stretch;
  }

  .department-filter-card {
    width: 100%;
  }

  .department-filter-body {
    min-height: auto;
    max-height: 320px;
  }

  .filter-control,
  .filter-control--short,
  .filter-control--range {
    width: 100%;
  }

  .user-filter-actions-col,
  .filter-actions {
    justify-content: flex-start;
  }

  .user-filter-layout > :deep(.ant-col:not(.user-filter-actions-col)),
  .user-filter-actions-col {
    flex: 0 0 100% !important;
    max-width: 100% !important;
  }

  .table-toolbar {
    align-items: flex-start;
  }
}
</style>
