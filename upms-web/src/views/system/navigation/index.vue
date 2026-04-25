<script setup lang="ts">
import { Edit, ExternalLink, Folder, Plus, Trash2 } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { del, get, post, put } from '@/utils/request'

interface NavigationItem {
  id: string
  parentId?: string | null
  name: string
  type: 'GROUP' | 'PAGE' | 'LINK'
  routePath?: string | null
  componentPath?: string | null
  externalUrl?: string | null
  icon?: string | null
  sortOrder: number
  visible: boolean
  status: number
  children?: NavigationItem[]
}

interface NavigationDetail extends NavigationItem {
  parentName?: string | null
}

interface PermissionItem {
  code: string
  name: string
  type: string
  category: string
}

const loading = ref(false)
const submitting = ref(false)
const ruleLoading = ref(false)

const navigationTree = ref<NavigationItem[]>([])
const availableRules = ref<PermissionItem[]>([])
const selectedRules = ref<string[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增分组')
const ruleDialogVisible = ref(false)

const currentNav = ref<Partial<NavigationItem>>({})

const formRef = ref()
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
})

const formRules = {
  name: [{ required: true, message: '名称必填', type: 'error' as const }],
  type: [{ required: true, message: '导航类型必填', type: 'error' as const }],
}

const parentOptions = computed(() => {
  const excludedIds = formData.id ? collectDescendantIds(formData.id, navigationTree.value) : new Set<string>()
  const options = [{ label: '作为根节点', value: '' }]
  flattenTree(navigationTree.value).forEach((item) => {
    if (!excludedIds.has(item.id) && item.type !== 'LINK') {
      options.push({
        label: `${'　'.repeat(Math.max(item.level - 1, 0))}${item.name}`,
        value: item.id,
      })
    }
  })
  return options
})

const columns = [
  { colKey: 'name', title: '名称', width: 220, ellipsis: true },
  { colKey: 'type', title: '类型', width: 100 },
  { colKey: 'routePath', title: '路由路径', width: 200, ellipsis: true },
  { colKey: 'componentPath', title: '组件路径', width: 260, ellipsis: true },
  { colKey: 'icon', title: '图标', width: 120 },
  { colKey: 'sortOrder', title: '排序号', width: 90 },
  { colKey: 'visible', title: '显示状态', width: 100 },
  { colKey: 'status', title: '启用状态', width: 100 },
  { colKey: 'action', title: '操作', width: 280, fixed: 'right' as const },
]

async function fetchTree() {
  loading.value = true
  try {
    navigationTree.value = await get<NavigationItem[]>('/navigation/tree')
  }
  finally {
    loading.value = false
  }
}

async function fetchPermissionCatalog() {
  availableRules.value = await get<PermissionItem[]>('/role/permission/catalog')
}

function handleAddGroup(parent?: NavigationItem) {
  dialogTitle.value = parent ? `新增分组 - ${parent.name}` : '新增分组'
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
  })
  dialogVisible.value = true
}

function handleAddPage(parent?: NavigationItem) {
  dialogTitle.value = parent ? `新增页面 - ${parent.name}` : '新增页面'
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
  })
  dialogVisible.value = true
}

function handleAddLink(parent?: NavigationItem) {
  dialogTitle.value = parent ? `新增外链 - ${parent.name}` : '新增外链'
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
  })
  dialogVisible.value = true
}

async function handleEdit(row: NavigationItem) {
  const detail = await get<NavigationDetail>(`/navigation/${row.id}`)
  dialogTitle.value = '编辑导航'
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
  })
  dialogVisible.value = true
}

async function handleConfigRule(row: NavigationItem) {
  ruleLoading.value = true
  try {
    currentNav.value = row
    if (availableRules.value.length === 0) {
      await fetchPermissionCatalog()
    }
    const selected = await get<PermissionItem[]>(`/navigation/${row.id}/permissions`)
    selectedRules.value = selected.map(item => item.code)
    ruleDialogVisible.value = true
  }
  finally {
    ruleLoading.value = false
  }
}

async function handleDelete(row: NavigationItem) {
  const confirm = await MessagePlugin.question(`确认删除导航“${row.name}”吗?`)
  if (!confirm) {
    return
  }
  try {
    await del(`/navigation/${row.id}`)
    MessagePlugin.success('删除成功')
    fetchTree()
  }
  catch (error) {}
}

async function handleSubmit() {
  const validateResult = await formRef.value?.validate()
  if (validateResult !== true) {
    return
  }
  submitting.value = true
  try {
    const payload = {
      parentId: formData.parentId || null,
      name: formData.name,
      type: formData.type,
      routePath: formData.routePath || null,
      componentPath: formData.componentPath || null,
      externalUrl: formData.externalUrl || null,
      icon: formData.icon || null,
      sortOrder: formData.sortOrder,
      visible: formData.visible,
      status: formData.status,
    }

    if (formData.id) {
      await put(`/navigation/${formData.id}`, payload)
      MessagePlugin.success('修改成功')
    }
    else {
      await post('/navigation', payload)
      MessagePlugin.success('新增成功')
    }
    dialogVisible.value = false
    fetchTree()
  }
  finally {
    submitting.value = false
  }
}

async function handleSubmitRule() {
  if (!currentNav.value.id) {
    return
  }
  try {
    await post(`/navigation/${currentNav.value.id}/permissions`, {
      permissionCodes: selectedRules.value,
    })
    MessagePlugin.success('访问规则配置成功')
    ruleDialogVisible.value = false
  }
  catch (error) {}
}

function flattenTree(nodes: NavigationItem[], level = 1): Array<NavigationItem & { level: number }> {
  return nodes.flatMap(node => [
    { ...node, level },
    ...flattenTree(node.children || [], level + 1),
  ])
}

function collectDescendantIds(id: string, nodes: NavigationItem[]): Set<string> {
  const result = new Set<string>()
  const target = findNode(id, nodes)
  if (!target) {
    return result
  }
  walkNode(target, (node) => {
    result.add(node.id)
  })
  return result
}

function findNode(id: string, nodes: NavigationItem[]): NavigationItem | null {
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    const found = findNode(id, node.children || [])
    if (found) {
      return found
    }
  }
  return null
}

function walkNode(node: NavigationItem, visitor: (node: NavigationItem) => void) {
  visitor(node)
  ;(node.children || []).forEach(child => walkNode(child, visitor))
}

onMounted(async () => {
  await Promise.all([fetchTree(), fetchPermissionCatalog()])
})
</script>

<template>
  <PageContainer title="导航管理" description="管理系统导航菜单，配置路由和访问权限。">
    <template #extra>
      <div class="flex gap-2">
        <t-button v-auth="'system:navigation:create'" theme="primary" variant="outline" @click="handleAddGroup()">
          <template #icon>
            <Folder class="h-4 w-4" />
          </template>
          新增分组
        </t-button>
        <t-button v-auth="'system:navigation:create'" theme="primary" variant="outline" @click="handleAddPage()">
          <template #icon>
            <Plus class="h-4 w-4" />
          </template>
          新增页面
        </t-button>
        <t-button v-auth="'system:navigation:create'" theme="primary" variant="outline" @click="handleAddLink()">
          <template #icon>
            <ExternalLink class="h-4 w-4" />
          </template>
          新增外链
        </t-button>
      </div>
    </template>

    <t-card :bordered="false" class="rounded-lg shadow-sm">
      <t-table
        row-key="id"
        :data="navigationTree"
        :columns="columns"
        :loading="loading"
        :pagination="{ disabled: true }"
        hover
        tree
        :tree-expand-and-fold-icon="true"
      >
        <template #name="{ row }">
          <div class="flex items-center gap-2">
            <Folder v-if="row.type === 'GROUP'" class="h-4 w-4 text-[#ed7b2f]" />
            <ExternalLink v-else-if="row.type === 'LINK'" class="h-4 w-4 text-[#0052d9]" />
            <span class="font-medium">{{ row.name }}</span>
          </div>
        </template>

        <template #type="{ row }">
          <t-tag
            :theme="row.type === 'GROUP' ? 'warning' : row.type === 'PAGE' ? 'primary' : 'success'"
            variant="light"
            size="small"
          >
            {{ row.type === 'GROUP' ? '分组' : row.type === 'PAGE' ? '页面' : '外链' }}
          </t-tag>
        </template>

        <template #routePath="{ row }">
          {{ row.routePath || '-' }}
        </template>

        <template #componentPath="{ row }">
          {{ row.componentPath || '-' }}
        </template>

        <template #icon="{ row }">
          {{ row.icon || '-' }}
        </template>

        <template #visible="{ row }">
          <t-tag :theme="row.visible ? 'success' : 'default'" variant="light" size="small">
            {{ row.visible ? '显示' : '隐藏' }}
          </t-tag>
        </template>

        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'warning'" variant="light" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </t-tag>
        </template>

        <template #action="{ row }">
          <div class="flex gap-2">
            <t-button
              v-if="row.type === 'GROUP'"
              v-auth="'system:navigation:create'"
              theme="primary"
              variant="text"
              size="small"
              @click="handleAddPage(row)"
            >
              <template #icon>
                <Plus class="h-3.5 w-3.5" />
              </template>
              新增下级
            </t-button>
            <t-button v-auth="'system:navigation:update'" theme="primary" variant="text" size="small" @click="handleEdit(row)">
              <template #icon>
                <Edit class="h-3.5 w-3.5" />
              </template>
              编辑
            </t-button>
            <t-button
              v-if="row.type !== 'GROUP'"
              v-auth="'system:navigation:access:assign'"
              theme="success"
              variant="text"
              size="small"
              @click="handleConfigRule(row)"
            >
              访问规则
            </t-button>
            <t-button v-auth="'system:navigation:delete'" theme="danger" variant="text" size="small" @click="handleDelete(row)">
              <template #icon>
                <Trash2 class="h-3.5 w-3.5" />
              </template>
              删除
            </t-button>
          </div>
        </template>
      </t-table>
    </t-card>

    <t-dialog
      v-model:visible="dialogVisible"
      :header="dialogTitle"
      width="640px"
      :on-confirm="handleSubmit"
      :confirm-btn="{ loading: submitting }"
    >
      <t-form ref="formRef" :data="formData" :rules="formRules" label-align="right" label-width="100px">
        <t-form-item label="上级节点" name="parentId">
          <t-select v-model="formData.parentId" clearable placeholder="请选择上级节点">
            <t-option v-for="item in parentOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>
        <t-form-item label="导航类型" name="type">
          <t-radio-group v-model="formData.type">
            <t-radio value="GROUP">
              分组
            </t-radio>
            <t-radio value="PAGE">
              页面
            </t-radio>
            <t-radio value="LINK">
              外链
            </t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="名称" name="name">
          <t-input v-model="formData.name" placeholder="请输入名称" />
        </t-form-item>
        <t-form-item v-if="formData.type !== 'GROUP'" label="路由路径" name="routePath">
          <t-input v-model="formData.routePath" placeholder="请输入路由路径" />
        </t-form-item>
        <t-form-item v-if="formData.type === 'PAGE'" label="组件路径" name="componentPath">
          <t-input v-model="formData.componentPath" placeholder="请输入组件路径" />
        </t-form-item>
        <t-form-item v-if="formData.type === 'LINK'" label="外链地址" name="externalUrl">
          <t-input v-model="formData.externalUrl" placeholder="请输入外链地址" />
        </t-form-item>
        <t-form-item label="图标" name="icon">
          <t-input v-model="formData.icon" placeholder="请输入图标名称" />
        </t-form-item>
        <t-form-item label="排序号" name="sortOrder">
          <t-input-number v-model="formData.sortOrder" theme="column" />
        </t-form-item>
        <t-form-item label="是否显示" name="visible">
          <t-switch v-model="formData.visible" />
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
      </t-form>
    </t-dialog>

    <t-dialog
      v-model:visible="ruleDialogVisible"
      :header="`访问规则 - ${currentNav.name || ''}`"
      width="700px"
      :on-confirm="handleSubmitRule"
      :confirm-btn="{ loading: ruleLoading }"
    >
      <div class="flex gap-6">
        <div class="flex-1">
          <h4 class="mb-3 text-sm font-semibold">可选权限</h4>
          <t-checkbox-group v-model="selectedRules" class="space-y-2">
            <div
              v-for="rule in availableRules"
              :key="rule.code"
              class="flex items-center gap-3 rounded border border-[#e7e7e7] p-3 hover:border-[#0052d9] dark:border-gray-700"
            >
              <t-checkbox :value="rule.code" />
              <div class="flex-1">
                <div class="text-sm font-medium">{{ rule.name }}</div>
                <div class="text-xs text-[#86909c] dark:text-gray-400">{{ rule.code }}</div>
              </div>
            </div>
          </t-checkbox-group>
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
              {{ availableRules.find(item => item.code === ruleCode)?.name || ruleCode }}
            </div>
          </div>
        </div>
      </div>
    </t-dialog>
  </PageContainer>
</template>
