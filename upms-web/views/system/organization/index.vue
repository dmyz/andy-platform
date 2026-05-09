<script setup lang="ts">
import {
  Ban,
  Building,
  CheckCircle2,
  ChevronRight,
  Edit,
  FolderTree,
  Hash,
  Plus,
  RefreshCcw,
  Search,
  Trash2,
} from '@lucide/vue';
import { message } from 'antdv-next';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import {
  createOrg,
  deleteOrg,
  getOrg,
  getOrgTree,
  updateOrg,
  updateOrgStatus,
} from '@/api/organization';
import { confirmAction } from '@/utils/feedback';

interface OrgTreeItem {
  id: string;
  parentId?: string | null;
  name: string;
  code: string;
  status: number;
  sort: number;
  children?: OrgTreeItem[];
}

interface OrgDetail {
  id: string;
  parentId?: string | null;
  parentName?: string | null;
  name: string;
  code: string;
  leader?: string | null;
  level: number;
  sort: number;
  status: number;
  remark?: string | null;
}

interface OrgMetric {
  label: string;
  value: number | string;
  tone: 'blue' | 'green' | 'amber' | 'slate';
}

const treeLoading = ref(false);
const detailLoading = ref(false);
const submitting = ref(false);

const orgTree = ref<OrgTreeItem[]>([]);
const selectedOrgId = ref('');
const selectedOrg = ref<OrgDetail | null>(null);
const expandedKeys = ref<Array<string | number>>([]);

const treeKeyword = ref('');
const childPage = ref(1);
const childPageSize = 12;

const orgDrawerVisible = ref(false);
const orgDrawerTitle = ref('新增部门');
const orgParentMode = ref<'hidden' | 'locked' | 'select'>('hidden');
const orgLockedParentName = ref('');
const orgFormRef = ref();
const orgFormData = reactive({
  id: '',
  parentId: '',
  name: '',
  code: '',
  leader: '',
  sort: 1,
  status: 1,
  remark: '',
});

const orgFormRules = {
  name: [{ required: true, message: '部门名称必填', type: 'error' as const }],
  code: [{ required: true, message: '部门编码必填', type: 'error' as const }],
  sort: [{ required: true, message: '排序号必填', type: 'error' as const }],
};

const treeKeys = {
  key: 'id',
  title: 'name',
  children: 'children',
};

const flatOrgs = computed(() => flattenTree(orgTree.value));

const selectedNode = computed(() =>
  selectedOrgId.value ? findNode(selectedOrgId.value, orgTree.value) : null,
);

const childDepartments = computed(() =>
  (selectedNode.value?.children || []).slice().sort((a, b) => a.sort - b.sort),
);

const pagedChildDepartments = computed(() => {
  const start = (childPage.value - 1) * childPageSize;
  return childDepartments.value.slice(start, start + childPageSize);
});

const selectedPath = computed(() =>
  selectedOrgId.value ? findPath(selectedOrgId.value, orgTree.value) : [],
);

const orgMetrics = computed<OrgMetric[]>(() => {
  const all = flatOrgs.value;
  const disabledCount = all.filter((item) => item.status === 0).length;

  return [
    {
      label: '部门总数',
      value: all.length,
      tone: 'blue',
    },
    {
      label: '启用部门',
      value: all.length - disabledCount,
      tone: 'green',
    },
    {
      label: '禁用部门',
      value: disabledCount,
      tone: 'amber',
    },
    {
      label: '当前下级',
      value: childDepartments.value.length,
      tone: 'slate',
    },
  ];
});

const parentOptions = computed(() => {
  const excludedIds = orgFormData.id
    ? collectDescendantIds(orgFormData.id, orgTree.value)
    : new Set<string>();
  const options = [{ label: '作为根部门', value: '' }];
  flatOrgs.value.forEach((item) => {
    if (!excludedIds.has(item.id)) {
      options.push({
        label: `${'　'.repeat(Math.max(item.level - 1, 0))}${item.name}`,
        value: item.id,
      });
    }
  });
  return options;
});

async function fetchTree(preferredId?: string) {
  treeLoading.value = true;
  try {
    const res = await getOrgTree({
      keyword: treeKeyword.value || undefined,
    });
    orgTree.value = res;
    expandedKeys.value = flattenTree(res).map((item) => item.id);

    const availableIds = new Set(flattenTree(res).map((item) => item.id));
    const nextId =
      preferredId && availableIds.has(preferredId)
        ? preferredId
        : availableIds.has(selectedOrgId.value)
          ? selectedOrgId.value
          : flattenTree(res)[0]?.id || '';

    if (!nextId) {
      selectedOrgId.value = '';
      selectedOrg.value = null;
      return;
    }

    await selectOrg(nextId);
  } finally {
    treeLoading.value = false;
  }
}

async function fetchOrgDetail(id: string) {
  detailLoading.value = true;
  try {
    selectedOrg.value = await getOrg(id);
  } finally {
    detailLoading.value = false;
  }
}

async function selectOrg(id: string) {
  if (id !== selectedOrgId.value) {
    childPage.value = 1;
  }
  selectedOrgId.value = id;
  await fetchOrgDetail(id);
}

function handleTreeSelect(_value: (string | number)[], context: { node: unknown }) {
  const node = context.node as OrgTreeItem & { dataRef?: OrgTreeItem };
  const nextId = String((node.dataRef || node).id);
  if (nextId === selectedOrgId.value) {
    return;
  }
  selectOrg(nextId);
}

function handleSearchTree() {
  fetchTree();
}

async function handleResetTree() {
  treeKeyword.value = '';
  await fetchTree();
}

async function handleRefreshTree() {
  await fetchTree(selectedOrgId.value);
}

function handleAddOrg(parent?: OrgTreeItem | OrgDetail | null) {
  orgDrawerTitle.value = parent ? `新增下级部门 - ${parent.name}` : '新增部门';
  orgParentMode.value = parent ? 'locked' : 'hidden';
  orgLockedParentName.value = parent?.name || '';
  Object.assign(orgFormData, {
    id: '',
    parentId: parent?.id || '',
    name: '',
    code: '',
    leader: '',
    sort: 1,
    status: 1,
    remark: '',
  });
  orgDrawerVisible.value = true;
}

function handleEditOrg(org?: OrgDetail | null) {
  if (!org) {
    return;
  }
  orgDrawerTitle.value = '编辑部门';
  orgParentMode.value = 'select';
  orgLockedParentName.value = '';
  Object.assign(orgFormData, {
    id: org.id,
    parentId: org.parentId || '',
    name: org.name,
    code: org.code,
    leader: org.leader || '',
    sort: org.sort,
    status: org.status,
    remark: org.remark || '',
  });
  orgDrawerVisible.value = true;
}

async function handleDeleteOrg(org?: OrgDetail | null) {
  if (!org) {
    return;
  }
  const confirm = await confirmAction(`确认删除部门“${org.name}”吗?`);
  if (!confirm) {
    return;
  }

  try {
    await deleteOrg(org.id);
    message.success('删除成功');
    treeKeyword.value = '';
    await fetchTree();
  } catch {}
}

async function handleToggleOrgStatus(org?: OrgDetail | null) {
  if (!org) {
    return;
  }

  const nextStatus = org.status === 1 ? 0 : 1;
  try {
    await updateOrgStatus(org.id, nextStatus);
    message.success(nextStatus === 1 ? '已启用' : '已禁用');
    await fetchTree(org.id);
  } catch {}
}

async function handleSubmitOrg() {
  try {
    await orgFormRef.value?.validate();
  } catch {
    return;
  }

  submitting.value = true;
  try {
    const payload = {
      parentId: orgFormData.parentId || null,
      name: orgFormData.name,
      code: orgFormData.code,
      leader: orgFormData.leader || null,
      sort: orgFormData.sort,
      status: orgFormData.status,
      remark: orgFormData.remark || null,
    };

    const res = orgFormData.id
      ? await updateOrg(orgFormData.id, payload)
      : await createOrg(payload);

    message.success(orgFormData.id ? '修改成功' : '新增成功');
    orgDrawerVisible.value = false;
    treeKeyword.value = '';
    await fetchTree(res.id);
  } finally {
    submitting.value = false;
  }
}

function getMetricClass(tone: OrgMetric['tone']) {
  return `org-metric org-metric--${tone}`;
}

function flattenTree(nodes: OrgTreeItem[], level = 1): Array<OrgTreeItem & { level: number }> {
  return nodes.flatMap((node) => [
    { ...node, level },
    ...flattenTree(node.children || [], level + 1),
  ]);
}

function collectDescendantIds(id: string, nodes: OrgTreeItem[]): Set<string> {
  const result = new Set<string>();
  const target = findNode(id, nodes);
  if (!target) {
    return result;
  }
  walkNode(target, (node) => {
    result.add(node.id);
  });
  return result;
}

function findNode(id: string, nodes: OrgTreeItem[]): OrgTreeItem | null {
  for (const node of nodes) {
    if (node.id === id) {
      return node;
    }
    const child = findNode(id, node.children || []);
    if (child) {
      return child;
    }
  }
  return null;
}

function findPath(id: string, nodes: OrgTreeItem[], path: OrgTreeItem[] = []): OrgTreeItem[] {
  for (const node of nodes) {
    const nextPath = [...path, node];
    if (node.id === id) {
      return nextPath;
    }
    const childPath = findPath(id, node.children || [], nextPath);
    if (childPath.length) {
      return childPath;
    }
  }
  return [];
}

function walkNode(node: OrgTreeItem, visitor: (node: OrgTreeItem) => void) {
  visitor(node);
  (node.children || []).forEach((child) => walkNode(child, visitor));
}

onMounted(() => {
  fetchTree();
});

watch(childDepartments, (items) => {
  const maxPage = Math.max(1, Math.ceil(items.length / childPageSize));
  if (childPage.value > maxPage) {
    childPage.value = maxPage;
  }
});
</script>

<template>
  <div class="org-page">
    <section class="org-metrics">
      <div v-for="item in orgMetrics" :key="item.label" :class="getMetricClass(item.tone)">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </section>

    <section class="org-workspace">
      <aside class="org-tree-panel">
        <header class="org-panel-header">
          <div>
            <h2>组织结构</h2>
            <span>{{ flatOrgs.length }} 个部门</span>
          </div>
          <div class="org-panel-actions">
            <a-button variant="outlined" :loading="treeLoading" @click="handleRefreshTree">
              <template #icon>
                <RefreshCcw class="h-4 w-4" />
              </template>
              刷新
            </a-button>
            <a-button v-auth="'system:org:create'" type="primary" @click="handleAddOrg()">
              <template #icon>
                <Plus class="h-4 w-4" />
              </template>
              新增
            </a-button>
          </div>
        </header>

        <div class="org-tree-search">
          <a-input
            v-model:value="treeKeyword"
            allow-clear
            placeholder="搜索部门名称或编码"
            @pressEnter="handleSearchTree"
          >
            <template #prefix>
              <Search class="h-4 w-4" />
            </template>
          </a-input>
          <a-button type="primary" @click="handleSearchTree">查询</a-button>
          <a-button variant="outlined" @click="handleResetTree">重置</a-button>
        </div>

        <a-tree
          v-model:expanded-keys="expandedKeys"
          block-node
          :tree-data="orgTree"
          :field-names="treeKeys"
          :selected-keys="selectedOrgId ? [selectedOrgId] : []"
          :loading="treeLoading"
          class="org-tree"
          @select="handleTreeSelect"
        >
          <template #title="node">
            <div class="org-tree-node">
              <div class="org-tree-node__main">
                <span class="org-tree-node__name">{{ node.name }}</span>
                <small>{{ node.code }}</small>
              </div>
              <a-tag v-if="node.status === 0" color="orange" variant="filled" size="small">
                禁用
              </a-tag>
            </div>
          </template>
        </a-tree>
      </aside>

      <main class="org-detail-panel">
        <template v-if="selectedOrg">
          <div class="org-detail-shell" :class="{ 'is-loading': detailLoading }">
            <header class="org-detail-header">
              <div class="org-detail-title">
                <span class="org-avatar">
                  <Building class="h-6 w-6" />
                </span>
                <div>
                  <div class="org-path">
                    <template v-for="(item, index) in selectedPath" :key="item.id">
                      <span>{{ item.name }}</span>
                      <ChevronRight v-if="index < selectedPath.length - 1" class="h-3.5 w-3.5" />
                    </template>
                  </div>
                  <h2>{{ selectedOrg.name }}</h2>
                  <p>{{ selectedOrg.code }}</p>
                </div>
              </div>
            </header>

            <div class="org-detail-grid">
              <div class="org-info-board">
                <div class="org-section-title">
                  <Hash class="h-4 w-4" />
                  基础资料
                </div>
                <dl class="org-info-list">
                  <div>
                    <dt>部门名称</dt>
                    <dd>{{ selectedOrg.name }}</dd>
                  </div>
                  <div>
                    <dt>部门编码</dt>
                    <dd>{{ selectedOrg.code }}</dd>
                  </div>
                  <div>
                    <dt>上级部门</dt>
                    <dd>{{ selectedOrg.parentName || '无' }}</dd>
                  </div>
                  <div>
                    <dt>负责人</dt>
                    <dd>{{ selectedOrg.leader || '-' }}</dd>
                  </div>
                  <div>
                    <dt>层级</dt>
                    <dd>{{ selectedOrg.level }}</dd>
                  </div>
                  <div>
                    <dt>排序号</dt>
                    <dd>{{ selectedOrg.sort }}</dd>
                  </div>
                  <div>
                    <dt>状态</dt>
                    <dd>
                      <a-tag
                        :color="selectedOrg.status === 1 ? 'success' : 'warning'"
                        variant="filled"
                      >
                        {{ selectedOrg.status === 1 ? '启用' : '禁用' }}
                      </a-tag>
                    </dd>
                  </div>
                  <div class="org-info-list__wide">
                    <dt>备注</dt>
                    <dd>{{ selectedOrg.remark || '-' }}</dd>
                  </div>
                </dl>
              </div>

              <aside class="org-action-board">
                <div class="org-section-title">
                  <Edit class="h-4 w-4" />
                  管理动作
                </div>
                <div class="org-action-list">
                  <a-button
                    v-auth="'system:org:create'"
                    type="primary"
                    block
                    @click="handleAddOrg(selectedOrg)"
                  >
                    <template #icon>
                      <Plus class="h-4 w-4" />
                    </template>
                    下级
                  </a-button>
                  <a-button type="primary" block @click="handleEditOrg(selectedOrg)">
                    <template #icon>
                      <Edit class="h-4 w-4" />
                    </template>
                    编辑
                  </a-button>
                  <a-button
                    v-auth="'system:org:status'"
                    block
                    :color="selectedOrg.status === 1 ? 'orange' : 'green'"
                    variant="outlined"
                    @click="handleToggleOrgStatus(selectedOrg)"
                  >
                    <template #icon>
                      <Ban v-if="selectedOrg.status === 1" class="h-4 w-4" />
                      <CheckCircle2 v-else class="h-4 w-4" />
                    </template>
                    {{ selectedOrg.status === 1 ? '禁用' : '启用' }}
                  </a-button>
                  <a-button
                    v-auth="'system:org:delete'"
                    block
                    danger
                    variant="outlined"
                    @click="handleDeleteOrg(selectedOrg)"
                  >
                    <template #icon>
                      <Trash2 class="h-4 w-4" />
                    </template>
                    删除
                  </a-button>
                </div>
              </aside>
            </div>

            <section class="org-children-board">
              <div class="org-section-title org-section-title--between">
                <span class="org-section-heading">
                  <FolderTree class="h-4 w-4" />
                  直属下级
                </span>
                <small v-if="childDepartments.length">共 {{ childDepartments.length }} 个</small>
              </div>
              <div v-if="childDepartments.length" class="org-child-list">
                <button
                  v-for="item in pagedChildDepartments"
                  :key="item.id"
                  class="org-child-item"
                  type="button"
                  @click="selectOrg(item.id)"
                >
                  <span class="org-child-icon">
                    <Building class="h-4 w-4" />
                  </span>
                  <span class="org-child-name">{{ item.name }}</span>
                  <small>{{ item.code }}</small>
                  <a-tag
                    :color="item.status === 1 ? 'success' : 'warning'"
                    variant="filled"
                    size="small"
                  >
                    {{ item.status === 1 ? '启用' : '禁用' }}
                  </a-tag>
                </button>
              </div>
              <div v-if="childDepartments.length > childPageSize" class="org-child-pagination">
                <span>每页渲染 {{ childPageSize }} 个</span>
                <a-pagination
                  v-model:current="childPage"
                  :page-size="childPageSize"
                  :total="childDepartments.length"
                  size="small"
                  :show-size-changer="false"
                />
              </div>
              <div v-if="!childDepartments.length" class="org-empty-mini">暂无直属下级</div>
            </section>
          </div>
        </template>

        <div v-else class="org-empty-state">
          <Building class="h-14 w-14" />
          <p>暂无组织数据</p>
          <a-button v-auth="'system:org:create'" type="primary" @click="handleAddOrg()">
            <template #icon>
              <Plus class="h-4 w-4" />
            </template>
            新增
          </a-button>
        </div>
      </main>
    </section>

    <a-drawer v-model:open="orgDrawerVisible" :title="orgDrawerTitle" :size="620">
      <template #footer>
        <div class="flex justify-end gap-2">
          <a-button @click="orgDrawerVisible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmitOrg">保存</a-button>
        </div>
      </template>

      <a-form
        ref="orgFormRef"
        :model="orgFormData"
        :rules="orgFormRules"
        label-align="right"
        :label-col="{ style: { width: '100px' } }"
      >
        <a-form-item v-if="orgParentMode === 'locked'" label="上级部门">
          <a-input :value="orgLockedParentName" disabled />
        </a-form-item>
        <a-form-item v-else-if="orgParentMode === 'select'" label="上级部门" name="parentId">
          <a-select
            v-model:value="orgFormData.parentId"
            allow-clear
            placeholder="请选择上级部门"
            :options="parentOptions"
          />
        </a-form-item>
        <a-form-item label="部门名称" name="name">
          <a-input v-model:value="orgFormData.name" placeholder="请输入部门名称" />
        </a-form-item>
        <a-form-item label="部门编码" name="code">
          <a-input v-model:value="orgFormData.code" placeholder="请输入部门编码" />
        </a-form-item>
        <a-form-item label="负责人" name="leader">
          <a-input v-model:value="orgFormData.leader" placeholder="请输入负责人" />
        </a-form-item>
        <a-form-item label="排序号" name="sort">
          <a-input-number v-model:value="orgFormData.sort" class="org-sort-input" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="orgFormData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="orgFormData.remark"
            :maxlength="200"
            placeholder="请输入备注"
          />
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<style scoped>
.org-page {
  color: #1d2129;
}

.org-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.org-metric {
  min-height: 82px;
  border: 1px solid #e6edf5;
  border-radius: 8px;
  background: #fff;
  padding: 14px 16px;
  box-shadow: 0 6px 18px rgb(23 35 61 / 5%);
}

.org-metric span {
  display: block;
  color: #6b778c;
  font-size: 13px;
}

.org-metric strong {
  display: block;
  margin-top: 8px;
  font-size: 28px;
  line-height: 1;
}

.org-metric--blue strong {
  color: #1265d8;
}

.org-metric--green strong {
  color: #159a72;
}

.org-metric--amber strong {
  color: #d9822b;
}

.org-metric--slate strong {
  color: #41536b;
}

.org-workspace {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.org-tree-panel,
.org-detail-panel {
  border: 1px solid #e6edf5;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgb(23 35 61 / 6%);
}

.org-tree-panel {
  display: flex;
  min-height: calc(100vh - 220px);
  max-height: calc(100vh - 150px);
  flex-direction: column;
  overflow: hidden;
}

.org-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 18px 12px;
  border-bottom: 1px solid #eef2f7;
}

.org-panel-header h2,
.org-detail-header h2 {
  margin: 0;
  color: #17202a;
  font-size: 16px;
  font-weight: 700;
}

.org-panel-header span {
  display: block;
  margin-top: 2px;
  color: #7a869a;
  font-size: 12px;
}

.org-panel-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;
}

.org-panel-actions :deep(.ant-btn),
.org-tree-search :deep(.ant-btn) {
  height: 32px;
  padding-inline: 14px;
}

.org-panel-actions :deep(.ant-btn) {
  min-width: 88px;
}

.org-tree-search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 8px;
  padding: 14px 18px;
  border-bottom: 1px solid #eef2f7;
}

.org-tree-search :deep(.ant-btn) {
  min-width: 64px;
}

.org-tree {
  min-height: 0;
  overflow: auto;
  padding: 10px 12px 18px;
}

.org-tree-node {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.org-tree-node__main {
  min-width: 0;
}

.org-tree-node__name {
  display: block;
  overflow: hidden;
  color: #1d2129;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.org-tree-node small {
  display: block;
  overflow: hidden;
  color: #9aa5b5;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.org-detail-panel {
  min-height: calc(100vh - 220px);
  overflow: hidden;
}

.org-detail-shell {
  padding: 22px;
}

.org-detail-shell.is-loading {
  opacity: 0.65;
  pointer-events: none;
}

.org-detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eef2f7;
}

.org-detail-title {
  display: flex;
  min-width: 0;
  align-items: flex-start;
  gap: 14px;
}

.org-avatar {
  display: inline-flex;
  width: 52px;
  height: 52px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #eef6ff;
  color: #1265d8;
}

.org-path {
  display: flex;
  max-width: 100%;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
  color: #6b778c;
  font-size: 12px;
}

.org-detail-header h2 {
  font-size: 22px;
}

.org-detail-header p {
  margin: 4px 0 0;
  color: #7a869a;
  font-size: 13px;
}

.org-detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 16px;
  margin-top: 18px;
}

.org-info-board,
.org-action-board,
.org-children-board {
  border: 1px solid #e7edf6;
  border-radius: 8px;
  background: #fbfdff;
  padding: 16px;
}

.org-section-title {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 14px;
  color: #24364b;
  font-size: 14px;
  font-weight: 700;
}

.org-section-title--between {
  justify-content: space-between;
  gap: 16px;
}

.org-section-heading {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.org-section-title small {
  color: #7a869a;
  font-size: 12px;
  font-weight: 600;
}

.org-action-list {
  display: grid;
  gap: 10px;
}

.org-action-list :deep(.ant-btn) {
  justify-content: flex-start;
  height: 36px;
}

.org-info-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 22px;
  margin: 0;
}

.org-info-list div {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr);
  align-items: start;
  gap: 12px;
  min-width: 0;
}

.org-info-list dt {
  color: #7a869a;
  font-size: 12px;
  font-weight: 600;
  line-height: 24px;
  white-space: nowrap;
}

.org-info-list dd {
  overflow-wrap: anywhere;
  margin: 0;
  color: #17202a;
  font-size: 14px;
  font-weight: 600;
  line-height: 24px;
}

.org-info-list__wide {
  grid-column: 1 / -1;
}

.org-child-item {
  border: 0;
  background: transparent;
  cursor: pointer;
  text-align: left;
}

.org-children-board {
  margin-top: 16px;
}

.org-child-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 10px;
}

.org-child-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #e7edf6;
}

.org-child-pagination span {
  flex-shrink: 0;
  color: #7a869a;
  font-size: 12px;
}

.org-child-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  min-height: 54px;
  border: 1px solid #e7edf6;
  border-radius: 8px;
  background: #fff;
  padding: 10px;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.org-child-item:hover {
  border-color: #9fc4f5;
  box-shadow: 0 8px 20px rgb(18 101 216 / 10%);
}

.org-child-icon {
  display: inline-flex;
  width: 32px;
  height: 32px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #eef6ff;
  color: #1265d8;
}

.org-child-name {
  overflow: hidden;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.org-child-item small {
  overflow: hidden;
  grid-column: 2;
  color: #7a869a;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.org-child-item :deep(.ant-tag) {
  grid-row: 1 / span 2;
  grid-column: 3;
}

.org-empty-mini,
.org-empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #86909c;
}

.org-empty-mini {
  min-height: 86px;
  border: 1px dashed #d8e1ec;
  border-radius: 8px;
  background: #fff;
}

.org-empty-state {
  min-height: calc(100vh - 220px);
  flex-direction: column;
  gap: 14px;
}

.org-empty-state svg {
  color: #a9b7c8;
}

.org-page :deep(.ant-tree-node-content-wrapper) {
  min-width: 0;
  border-radius: 8px;
}

.org-page :deep(.ant-tree-node-selected) {
  background: #eef6ff;
}

.org-page :deep(.ant-tree-title) {
  min-width: 0;
  flex: 1;
}

.org-sort-input {
  width: 100%;
}

@media (max-width: 1180px) {
  .org-workspace {
    grid-template-columns: 320px minmax(0, 1fr);
  }
}

@media (max-width: 900px) {
  .org-detail-header {
    flex-direction: column;
  }

  .org-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .org-workspace {
    grid-template-columns: 1fr;
  }

  .org-tree-panel,
  .org-detail-panel {
    min-height: auto;
    max-height: none;
  }

  .org-tree {
    max-height: 360px;
  }
}

@media (max-width: 560px) {
  .org-metrics,
  .org-info-list {
    grid-template-columns: 1fr;
  }

  .org-tree-search {
    grid-template-columns: 1fr;
  }

  .org-detail-shell {
    padding: 16px;
  }

  .org-panel-actions {
    width: 100%;
  }

  .org-panel-actions :deep(.ant-btn) {
    flex: 1;
  }
}
</style>
