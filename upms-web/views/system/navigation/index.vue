<script setup lang="ts">
import { Edit, ExternalLink, Folder, Plus, RefreshCcw, ShieldCheck, Trash2 } from '@lucide/vue';
import { message } from 'antdv-next';
import { confirmAction } from '@/utils/feedback';
import { computed, onMounted, reactive, ref } from 'vue';
import {
  getNavigationTree as fetchNavigationTreeApi,
  getNavigation as fetchNavigationApi,
  createNavigation as createNavigationApi,
  updateNavigation as updateNavigationApi,
  deleteNavigation as deleteNavigationApi,
  getNavigationPermissions,
  assignNavigationPermissions,
} from '@/api/navigation';
import { getPermissionCatalog } from '@/api/role';

interface NavigationItem {
  id: string;
  parentId?: string | null;
  name: string;
  type: 'GROUP' | 'PAGE' | 'LINK';
  routePath?: string | null;
  componentPath?: string | null;
  externalUrl?: string | null;
  icon?: string | null;
  sortOrder: number;
  visible: boolean;
  status: number;
  children?: NavigationItem[];
}

interface NavigationDetail extends NavigationItem {
  parentName?: string | null;
}

interface PermissionItem {
  code: string;
  name: string;
  type: string;
  category: string;
  moduleName?: string;
}

const loading = ref(false);
const submitting = ref(false);
const ruleLoading = ref(false);

const navigationTree = ref<NavigationItem[]>([]);
const availableRules = ref<PermissionItem[]>([]);
const selectedRules = ref<string[]>([]);

const dialogVisible = ref(false);
const dialogTitle = ref('新增分组');
const ruleDialogVisible = ref(false);

const currentNav = ref<Partial<NavigationItem>>({});

const formRef = ref();
const formData = reactive({
  id: '',
  parentId: '',
  name: '',
  type: 'PAGE',
  routePath: '',
  componentPath: '',
  externalUrl: '',
  icon: '',
  sortOrder: 1,
  visible: true,
  status: 1,
});

const formRules = {
  name: [{ required: true, message: '名称必填', type: 'error' as const }],
  type: [{ required: true, message: '导航类型必填', type: 'error' as const }],
};

const parentOptions = computed(() => {
  const excludedIds = formData.id
    ? collectDescendantIds(formData.id, navigationTree.value)
    : new Set<string>();
  const options = [{ label: '作为根节点', value: '' }];
  flattenTree(navigationTree.value).forEach((item) => {
    if (!excludedIds.has(item.id) && item.type !== 'LINK') {
      options.push({
        label: `${'　'.repeat(Math.max(item.level - 1, 0))}${item.name}`,
        value: item.id,
      });
    }
  });
  return options;
});

const columns = [
  { dataIndex: 'name', title: '名称', width: 220, ellipsis: true },
  { dataIndex: 'type', title: '类型', width: 100 },
  { dataIndex: 'routePath', title: '路由路径', width: 200, ellipsis: true },
  { dataIndex: 'componentPath', title: '组件路径', width: 260, ellipsis: true },
  { dataIndex: 'icon', title: '图标', width: 120 },
  { dataIndex: 'sortOrder', title: '排序号', width: 90 },
  { dataIndex: 'visible', title: '显示状态', width: 100 },
  { dataIndex: 'status', title: '启用状态', width: 100 },
  { dataIndex: 'action', title: '操作', width: 320, fixed: 'right' as const },
];

const tableScroll = { x: 1460 };

async function fetchTree() {
  loading.value = true;
  try {
    const res = await fetchNavigationTreeApi();
    navigationTree.value = res;
  } finally {
    loading.value = false;
  }
}

async function fetchPermissionCatalog() {
  const res = await getPermissionCatalog();
  availableRules.value = res;
}

function handleAddGroup(parent?: NavigationItem) {
  dialogTitle.value = parent ? `新增分组 - ${parent.name}` : '新增分组';
  Object.assign(formData, {
    id: '',
    parentId: parent?.id || '',
    name: '',
    type: 'GROUP',
    routePath: '',
    componentPath: '',
    externalUrl: '',
    icon: '',
    sortOrder: 1,
    visible: true,
    status: 1,
  });
  dialogVisible.value = true;
}

function handleAddPage(parent?: NavigationItem) {
  dialogTitle.value = parent ? `新增页面 - ${parent.name}` : '新增页面';
  Object.assign(formData, {
    id: '',
    parentId: parent?.id || '',
    name: '',
    type: 'PAGE',
    routePath: '',
    componentPath: '',
    externalUrl: '',
    icon: '',
    sortOrder: 1,
    visible: true,
    status: 1,
  });
  dialogVisible.value = true;
}

function handleAddLink(parent?: NavigationItem) {
  dialogTitle.value = parent ? `新增外链 - ${parent.name}` : '新增外链';
  Object.assign(formData, {
    id: '',
    parentId: parent?.id || '',
    name: '',
    type: 'LINK',
    routePath: '',
    componentPath: '',
    externalUrl: '',
    icon: '',
    sortOrder: 1,
    visible: true,
    status: 1,
  });
  dialogVisible.value = true;
}

async function handleEdit(row: NavigationItem) {
  const res = await fetchNavigationApi(row.id);
  const detail = res;
  dialogTitle.value = '编辑导航';
  Object.assign(formData, {
    id: detail.id,
    parentId: detail.parentId || '',
    name: detail.name,
    type: detail.type,
    routePath: detail.routePath || '',
    componentPath: detail.componentPath || '',
    externalUrl: detail.externalUrl || '',
    icon: detail.icon || '',
    sortOrder: detail.sortOrder,
    visible: detail.visible,
    status: detail.status,
  });
  dialogVisible.value = true;
}

async function handleConfigRule(row: NavigationItem) {
  ruleLoading.value = true;
  try {
    currentNav.value = row;
    if (availableRules.value.length === 0) {
      await fetchPermissionCatalog();
    }
    const res = await getNavigationPermissions(row.id);
    const selected = res;
    selectedRules.value = selected.map((item: PermissionItem) => item.code);
    ruleDialogVisible.value = true;
  } finally {
    ruleLoading.value = false;
  }
}

async function handleDelete(row: NavigationItem) {
  const confirm = await confirmAction(`确认删除导航”${row.name}”吗?`);
  if (!confirm) {
    return;
  }
  try {
    await deleteNavigationApi(row.id);
    message.success('删除成功');
    fetchTree();
  } catch (error) {}
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
      parentId: formData.parentId || null,
      name: formData.name,
      type: formData.type as 'GROUP' | 'PAGE' | 'LINK',
      routePath: formData.routePath || null,
      componentPath: formData.componentPath || null,
      externalUrl: formData.externalUrl || null,
      icon: formData.icon || null,
      sortOrder: formData.sortOrder,
      visible: formData.visible,
      status: formData.status,
    };

    if (formData.id) {
      await updateNavigationApi(formData.id, payload);
      message.success('修改成功');
    } else {
      await createNavigationApi(payload);
      message.success('新增成功');
    }
    dialogVisible.value = false;
    fetchTree();
  } finally {
    submitting.value = false;
  }
}

async function handleInlineUpdate(
  row: NavigationItem,
  data: Partial<Pick<NavigationItem, 'visible' | 'status'>>,
) {
  const previousVisible = row.visible;
  const previousStatus = row.status;
  Object.assign(row, data);
  try {
    await updateNavigationApi(row.id, {
      parentId: row.parentId || null,
      name: row.name,
      type: row.type,
      routePath: row.routePath || null,
      componentPath: row.componentPath || null,
      externalUrl: row.externalUrl || null,
      icon: row.icon || null,
      sortOrder: row.sortOrder,
      visible: row.visible,
      status: row.status,
    });
    message.success('状态更新成功');
    await fetchTree();
  } catch {
    row.visible = previousVisible;
    row.status = previousStatus;
  }
}

async function handleSubmitRule() {
  if (!currentNav.value.id) {
    return;
  }
  try {
    await assignNavigationPermissions(currentNav.value.id, {
      permissionCodes: selectedRules.value,
    });
    message.success('访问规则配置成功');
    ruleDialogVisible.value = false;
  } catch (error) {}
}

function flattenTree(
  nodes: NavigationItem[],
  level = 1,
): Array<NavigationItem & { level: number }> {
  return nodes.flatMap((node) => [
    { ...node, level },
    ...flattenTree(node.children || [], level + 1),
  ]);
}

function collectDescendantIds(id: string, nodes: NavigationItem[]): Set<string> {
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

function findNode(id: string, nodes: NavigationItem[]): NavigationItem | null {
  for (const node of nodes) {
    if (node.id === id) {
      return node;
    }
    const found = findNode(id, node.children || []);
    if (found) {
      return found;
    }
  }
  return null;
}

function walkNode(node: NavigationItem, visitor: (node: NavigationItem) => void) {
  visitor(node);
  (node.children || []).forEach((child) => walkNode(child, visitor));
}

onMounted(async () => {
  await Promise.all([fetchTree(), fetchPermissionCatalog()]);
});
</script>

<template>
  <div>
    <a-card variant="borderless" class="table-card rounded-lg shadow-sm">
      <div class="table-toolbar">
        <div class="table-toolbar__actions">
          <a-button
            v-auth="'system:navigation:create'"
            type="primary"
            variant="outlined"
            @click="handleAddGroup()"
          >
            <template #icon>
              <Folder class="h-4 w-4" />
            </template>
            分组
          </a-button>
          <a-button
            v-auth="'system:navigation:create'"
            type="primary"
            variant="outlined"
            @click="handleAddPage()"
          >
            <template #icon>
              <Plus class="h-4 w-4" />
            </template>
            页面
          </a-button>
          <a-button
            v-auth="'system:navigation:create'"
            type="primary"
            variant="outlined"
            @click="handleAddLink()"
          >
            <template #icon>
              <ExternalLink class="h-4 w-4" />
            </template>
            外链
          </a-button>
        </div>
        <a-button variant="outlined" size="small" @click="fetchTree">
          <template #icon>
            <RefreshCcw class="h-4 w-4" />
          </template>
          刷新
        </a-button>
      </div>
      <a-table
        row-key="id"
        :data-source="navigationTree"
        :columns="columns"
        :loading="loading"
        :pagination="{ disabled: true }"
        :scroll="tableScroll"
        bordered
        hover
        tree
        :tree-expand-and-fold-icon="true"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'name'">
            <div class="flex items-center gap-2">
              <Folder v-if="record.type === 'GROUP'" class="h-4 w-4 text-[#ed7b2f]" />
              <ExternalLink v-else-if="record.type === 'LINK'" class="h-4 w-4 text-[#0052d9]" />
              <span class="font-medium">{{ record.name }}</span>
            </div>
          </template>

          <template v-else-if="column.dataIndex === 'type'">
            <a-tag
              :color="
                record.type === 'GROUP' ? 'warning' : record.type === 'PAGE' ? 'primary' : 'success'
              "
              variant="filled"
              size="small"
            >
              {{ record.type === 'GROUP' ? '分组' : record.type === 'PAGE' ? '页面' : '外链' }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'routePath'">
            {{ record.routePath || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'componentPath'">
            {{ record.componentPath || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'icon'">
            {{ record.icon || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'visible'">
            <a-switch
              :checked="record.visible"
              @change="(val: number | string | boolean) => handleInlineUpdate(record, { visible: !!val })"
            />
          </template>

          <template v-else-if="column.dataIndex === 'status'">
            <a-switch
              :checked="record.status"
              :checked-value="1"
              :un-checked-value="0"
              @change="
                (val: number | string | boolean) =>
                  handleInlineUpdate(record, { status: val as number })
              "
            />
          </template>

          <template v-else-if="column.dataIndex === 'action'">
            <div class="row-actions">
              <a-button
                v-if="record.type === 'GROUP'"
                v-auth="'system:navigation:create'"
                type="primary"
                size="small"
                @click="handleAddPage(record)"
              >
                <template #icon>
                  <Plus class="h-3.5 w-3.5" />
                </template>
                下级
              </a-button>
              <a-button
                v-auth="'system:navigation:update'"
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
                v-if="record.type !== 'GROUP'"
                v-auth="'system:navigation:access:assign'"
                variant="outlined"
                size="small"
                @click="handleConfigRule(record)"
              >
                <template #icon>
                  <ShieldCheck class="h-3.5 w-3.5" />
                </template>
                规则
              </a-button>
              <a-button
                v-auth="'system:navigation:delete'"
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
        <a-form-item label="上级节点" name="parentId">
          <a-select
            v-model:value="formData.parentId"
            allow-clear
            placeholder="请选择上级节点"
            :options="parentOptions"
          />
        </a-form-item>
        <a-form-item label="导航类型" name="type">
          <a-radio-group v-model:value="formData.type">
            <a-radio value="GROUP"> 分组 </a-radio>
            <a-radio value="PAGE"> 页面 </a-radio>
            <a-radio value="LINK"> 外链 </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item v-if="formData.type !== 'GROUP'" label="路由路径" name="routePath">
          <a-input v-model:value="formData.routePath" placeholder="请输入路由路径" />
        </a-form-item>
        <a-form-item v-if="formData.type === 'PAGE'" label="组件路径" name="componentPath">
          <a-input v-model:value="formData.componentPath" placeholder="请输入组件路径" />
        </a-form-item>
        <a-form-item v-if="formData.type === 'LINK'" label="外链地址" name="externalUrl">
          <a-input v-model:value="formData.externalUrl" placeholder="请输入外链地址" />
        </a-form-item>
        <a-form-item label="图标" name="icon">
          <a-input v-model:value="formData.icon" placeholder="请输入图标名称" />
        </a-form-item>
        <a-form-item label="排序号" name="sortOrder">
          <a-input-number v-model:value="formData.sortOrder" />
        </a-form-item>
        <a-form-item label="是否显示" name="visible">
          <a-switch v-model:checked="formData.visible" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1"> 启用 </a-radio>
            <a-radio :value="0"> 禁用 </a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="ruleDialogVisible"
      :title="`访问规则 - ${currentNav.name || ''}`"
      width="700px"
      :confirm-loading="ruleLoading"
      @ok="handleSubmitRule"
    >
      <div class="flex gap-6">
        <div class="flex-1">
          <h4 class="mb-3 text-sm font-semibold">可选权限</h4>
          <a-checkbox-group v-model:value="selectedRules" class="space-y-2">
            <div
              v-for="rule in availableRules"
              :key="rule.code"
              class="flex items-center gap-3 rounded border border-[#e7e7e7] p-3 hover:border-[#0052d9] dark:border-gray-700"
            >
              <a-checkbox :value="rule.code" />
              <div class="flex-1">
                <div class="text-sm font-medium">{{ rule.name }}</div>
                <div class="text-xs text-[#86909c] dark:text-gray-400">
                  {{ rule.code }} · {{ rule.moduleName || rule.category }}
                </div>
              </div>
              <a-tag size="small" variant="filled">
                {{ rule.type }}
              </a-tag>
            </div>
          </a-checkbox-group>
        </div>

        <div class="w-64 shrink-0 border-l border-[#e7e7e7] pl-6 dark:border-gray-700">
          <h4 class="mb-3 text-sm font-semibold">已绑定权限</h4>
          <div class="mb-4">
            <div class="text-2xl font-bold text-[#0052d9]">{{ selectedRules.length }}</div>
            <div class="text-xs text-[#86909c] dark:text-gray-400">个权限</div>
          </div>
          <div class="space-y-2">
            <div
              v-for="ruleCode in selectedRules"
              :key="ruleCode"
              class="rounded bg-[#f5f5f5] p-2 text-sm dark:bg-gray-700"
            >
              {{ availableRules.find((item) => item.code === ruleCode)?.name || ruleCode }}
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.table-toolbar__actions,
.row-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.row-actions {
  flex-wrap: nowrap;
}
</style>
