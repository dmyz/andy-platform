<script setup lang="ts">
import type { TreeNodeModel, TreeNodeValue, TreeOptionData } from 'tdesign-vue-next'
import { Building, Edit, Plus, RefreshCcw, Search, Trash2, User } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { del, get, post, put } from '@/utils/request'

interface OrgTreeItem {
  id: string
  parentId?: string | null
  name: string
  code: string
  status: number
  sort: number
  children?: OrgTreeItem[]
}

interface OrgDetail {
  id: string
  parentId?: string | null
  parentName?: string | null
  name: string
  code: string
  leader?: string | null
  level: number
  sort: number
  status: number
  remark?: string | null
}

interface OrgMember {
  id: string
  username: string
  displayName: string
  mobile?: string
  email?: string
  positionName?: string
  status: number
}

const treeLoading = ref(false)
const detailLoading = ref(false)
const memberLoading = ref(false)
const submitting = ref(false)

const orgTree = ref<OrgTreeItem[]>([])
const selectedOrgId = ref('')
const selectedOrg = ref<OrgDetail | null>(null)
const memberList = ref<OrgMember[]>([])

const treeKeyword = ref('')

const memberSearchParams = reactive({
  displayName: '',
  status: undefined as number | undefined,
})

const orgDialogVisible = ref(false)
const orgDialogTitle = ref('新增组织')
const orgFormRef = ref()
const orgFormData = reactive({
  id: '',
  parentId: '',
  name: '',
  code: '',
  leader: '',
  sort: 1,
  status: 1,
  remark: '',
})

const orgFormRules = {
  name: [{ required: true, message: '组织名称必填', type: 'error' as const }],
  code: [{ required: true, message: '组织编码必填', type: 'error' as const }],
  sort: [{ required: true, message: '排序号必填', type: 'error' as const }],
}

const treeKeys = {
  value: 'id',
  label: 'name',
  children: 'children',
}

const memberColumns = [
  { colKey: 'username', title: '用户名', width: 120 },
  { colKey: 'displayName', title: '真实姓名', width: 120 },
  { colKey: 'mobile', title: '手机号', width: 140 },
  { colKey: 'email', title: '邮箱', ellipsis: true },
  { colKey: 'positionName', title: '岗位', width: 140 },
  { colKey: 'status', title: '状态', width: 100, cell: 'statusSlot' },
]

const parentOptions = computed(() => {
  const excludedIds = orgFormData.id ? collectDescendantIds(orgFormData.id, orgTree.value) : new Set<string>()
  const options = [{ label: '作为根组织', value: '' }]
  flattenTree(orgTree.value).forEach((item) => {
    if (!excludedIds.has(item.id)) {
      options.push({
        label: `${'　'.repeat(Math.max(item.level - 1, 0))}${item.name}`,
        value: item.id,
      })
    }
  })
  return options
})

async function fetchTree(preferredId?: string) {
  treeLoading.value = true
  try {
    const res = await get<OrgTreeItem[]>('/org/tree', {
      keyword: treeKeyword.value || undefined,
    })
    orgTree.value = res

    const availableIds = new Set(flattenTree(res).map(item => item.id))
    const nextId = preferredId && availableIds.has(preferredId)
      ? preferredId
      : availableIds.has(selectedOrgId.value)
          ? selectedOrgId.value
          : flattenTree(res)[0]?.id || ''

    if (!nextId) {
      selectedOrgId.value = ''
      selectedOrg.value = null
      memberList.value = []
      return
    }

    await selectOrg(nextId)
  }
  finally {
    treeLoading.value = false
  }
}

async function fetchOrgDetail(id: string) {
  detailLoading.value = true
  try {
    selectedOrg.value = await get<OrgDetail>(`/org/${id}`)
  }
  finally {
    detailLoading.value = false
  }
}

async function fetchMembers() {
  if (!selectedOrgId.value) {
    memberList.value = []
    return
  }
  memberLoading.value = true
  try {
    memberList.value = await get<OrgMember[]>(`/org/${selectedOrgId.value}/members`, {
      displayName: memberSearchParams.displayName || undefined,
      status: memberSearchParams.status,
    })
  }
  finally {
    memberLoading.value = false
  }
}

async function selectOrg(id: string) {
  selectedOrgId.value = id
  await fetchOrgDetail(id)
  await fetchMembers()
}

function handleTreeActive(
  _value: TreeNodeValue[],
  context: { node: TreeNodeModel<TreeOptionData>; e?: MouseEvent; trigger: 'node-click' | 'setItem' },
) {
  const nextId = String((context.node.data as OrgTreeItem).id)
  if (nextId === selectedOrgId.value) {
    return
  }
  selectOrg(nextId)
}

function handleSearchTree() {
  fetchTree()
}

function handleResetTree() {
  treeKeyword.value = ''
  fetchTree()
}

function handleAddOrg(parent?: OrgTreeItem | OrgDetail | null) {
  orgDialogTitle.value = parent ? `新增下级组织 - ${parent.name}` : '新增组织'
  Object.assign(orgFormData, {
    id: '',
    parentId: parent?.id || '',
    name: '',
    code: '',
    leader: '',
    sort: 1,
    status: 1,
    remark: '',
  })
  orgDialogVisible.value = true
}

function handleEditOrg(org?: OrgDetail | null) {
  if (!org) {
    return
  }
  orgDialogTitle.value = '编辑组织'
  Object.assign(orgFormData, {
    id: org.id,
    parentId: org.parentId || '',
    name: org.name,
    code: org.code,
    leader: org.leader || '',
    sort: org.sort,
    status: org.status,
    remark: org.remark || '',
  })
  orgDialogVisible.value = true
}

async function openEditByNode(node: OrgTreeItem) {
  await selectOrg(node.id)
  handleEditOrg(selectedOrg.value)
}

async function handleDeleteOrg(org?: OrgDetail | null) {
  if (!org) {
    return
  }
  const confirm = await MessagePlugin.question(`确认删除组织“${org.name}”吗?`)
  if (!confirm) {
    return
  }

  try {
    await del(`/org/${org.id}`)
    MessagePlugin.success('删除成功')
    treeKeyword.value = ''
    await fetchTree()
  }
  catch (error) {}
}

async function openDeleteByNode(node: OrgTreeItem) {
  await selectOrg(node.id)
  await handleDeleteOrg(selectedOrg.value)
}

async function handleToggleOrgStatus(org?: OrgDetail | null) {
  if (!org) {
    return
  }

  const nextStatus = org.status === 1 ? 0 : 1
  try {
    await put(`/org/${org.id}/status`, { status: nextStatus })
    MessagePlugin.success(nextStatus === 1 ? '已启用' : '已禁用')
    await fetchTree(org.id)
  }
  catch (error) {}
}

async function handleSubmitOrg() {
  const validateResult = await orgFormRef.value?.validate()
  if (validateResult !== true) {
    return
  }

  submitting.value = true
  try {
    const payload = {
      parentId: orgFormData.parentId || null,
      name: orgFormData.name,
      code: orgFormData.code,
      leader: orgFormData.leader || null,
      sort: orgFormData.sort,
      status: orgFormData.status,
      remark: orgFormData.remark || null,
    }

    const res = orgFormData.id
      ? await put<OrgDetail>(`/org/${orgFormData.id}`, payload)
      : await post<OrgDetail>('/org', payload)

    MessagePlugin.success(orgFormData.id ? '修改成功' : '新增成功')
    orgDialogVisible.value = false
    treeKeyword.value = ''
    await fetchTree(res.id)
  }
  finally {
    submitting.value = false
  }
}

function handleSearchMember() {
  fetchMembers()
}

function handleResetMember() {
  memberSearchParams.displayName = ''
  memberSearchParams.status = undefined
  fetchMembers()
}

function flattenTree(nodes: OrgTreeItem[], level = 1): Array<OrgTreeItem & { level: number }> {
  return nodes.flatMap(node => [
    { ...node, level },
    ...flattenTree(node.children || [], level + 1),
  ])
}

function collectDescendantIds(id: string, nodes: OrgTreeItem[]): Set<string> {
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

function findNode(id: string, nodes: OrgTreeItem[]): OrgTreeItem | null {
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    const child = findNode(id, node.children || [])
    if (child) {
      return child
    }
  }
  return null
}

function walkNode(node: OrgTreeItem, visitor: (node: OrgTreeItem) => void) {
  visitor(node)
  ;(node.children || []).forEach(child => walkNode(child, visitor))
}

onMounted(() => {
  fetchTree()
})
</script>

<template>
  <PageContainer title="组织管理" description="管理组织树、详情信息和组织成员。">
    <div class="flex h-[calc(100vh-200px)] gap-6">
      <t-card :bordered="false" class="w-80 shrink-0 shadow-sm">
        <template #header>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Building class="h-5 w-5 text-[#0052d9]" />
              <span class="font-semibold">组织树</span>
            </div>
            <t-button v-auth="'system:org:create'" theme="primary" variant="text" size="small" @click="handleAddOrg(selectedOrg)">
              <template #icon>
                <Plus class="h-4 w-4" />
              </template>
              新增
            </t-button>
          </div>
        </template>

        <div class="mb-4 flex gap-2">
          <t-input v-model="treeKeyword" clearable placeholder="搜索组织名称或编码" @enter="handleSearchTree">
            <template #prefix-icon>
              <Search class="h-4 w-4" />
            </template>
          </t-input>
          <t-button theme="primary" @click="handleSearchTree">
            查询
          </t-button>
          <t-button variant="outline" @click="handleResetTree">
            重置
          </t-button>
        </div>

        <t-tree
          activable
          hover
          :data="orgTree"
          :keys="treeKeys"
          :expand-all="true"
          :actived="selectedOrgId ? [selectedOrgId] : []"
          :loading="treeLoading"
          @active="handleTreeActive"
        >
          <template #label="{ node }">
            <div class="flex items-center gap-2">
              <span>{{ node.data.name }}</span>
              <t-tag v-if="node.data.status === 0" theme="warning" variant="light" size="small">
                禁用
              </t-tag>
            </div>
          </template>

          <template #operations="{ node }">
            <div class="flex gap-1">
              <t-button
                v-auth="'system:org:create'"
                theme="primary"
                variant="text"
                size="small"
                @click.stop="handleAddOrg(node.data)"
              >
                <Plus class="h-3.5 w-3.5" />
              </t-button>
              <t-button
                v-auth="'system:org:update'"
                theme="primary"
                variant="text"
                size="small"
                @click.stop="openEditByNode(node.data)"
              >
                <Edit class="h-3.5 w-3.5" />
              </t-button>
              <t-button
                v-auth="'system:org:delete'"
                theme="danger"
                variant="text"
                size="small"
                @click.stop="openDeleteByNode(node.data)"
              >
                <Trash2 class="h-3.5 w-3.5" />
              </t-button>
            </div>
          </template>
        </t-tree>
      </t-card>

      <div class="flex flex-1 flex-col gap-4">
        <t-card v-if="selectedOrg" :bordered="false" class="shadow-sm" :loading="detailLoading">
          <template #header>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <Building class="h-5 w-5 text-[#0052d9]" />
                <span class="font-semibold">组织详情</span>
              </div>
              <div class="flex gap-2">
                <t-button v-auth="'system:org:update'" theme="primary" variant="outline" size="small" @click="handleEditOrg(selectedOrg)">
                  编辑
                </t-button>
                <t-button
                  v-auth="'system:org:status'"
                  :theme="selectedOrg.status === 1 ? 'warning' : 'success'"
                  variant="outline"
                  size="small"
                  @click="handleToggleOrgStatus(selectedOrg)"
                >
                  {{ selectedOrg.status === 1 ? '禁用' : '启用' }}
                </t-button>
                <t-button v-auth="'system:org:delete'" theme="danger" variant="outline" size="small" @click="handleDeleteOrg(selectedOrg)">
                  删除
                </t-button>
              </div>
            </div>
          </template>

          <t-descriptions :column="2">
            <t-descriptions-item label="组织名称">
              {{ selectedOrg.name }}
            </t-descriptions-item>
            <t-descriptions-item label="组织编码">
              {{ selectedOrg.code }}
            </t-descriptions-item>
            <t-descriptions-item label="上级组织">
              {{ selectedOrg.parentName || '无' }}
            </t-descriptions-item>
            <t-descriptions-item label="负责人">
              {{ selectedOrg.leader || '-' }}
            </t-descriptions-item>
            <t-descriptions-item label="层级">
              {{ selectedOrg.level }}
            </t-descriptions-item>
            <t-descriptions-item label="排序号">
              {{ selectedOrg.sort }}
            </t-descriptions-item>
            <t-descriptions-item label="状态">
              <t-tag :theme="selectedOrg.status === 1 ? 'success' : 'warning'" variant="light">
                {{ selectedOrg.status === 1 ? '启用' : '禁用' }}
              </t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="备注">
              {{ selectedOrg.remark || '-' }}
            </t-descriptions-item>
          </t-descriptions>
        </t-card>

        <t-card v-if="selectedOrg" :bordered="false" class="flex-1 shadow-sm" :loading="memberLoading">
          <template #header>
            <div class="flex items-center gap-2">
              <User class="h-5 w-5 text-[#00a870]" />
              <span class="font-semibold">组织成员</span>
            </div>
          </template>

          <div class="mb-4">
            <t-form layout="inline" label-width="80px">
              <t-form-item label="用户姓名">
                <t-input
                  v-model="memberSearchParams.displayName"
                  clearable
                  placeholder="请输入用户姓名"
                  style="width: 220px"
                  @enter="handleSearchMember"
                />
              </t-form-item>
              <t-form-item label="状态">
                <t-select v-model="memberSearchParams.status" clearable placeholder="请选择状态" style="width: 160px">
                  <t-option :value="1" label="启用" />
                  <t-option :value="0" label="禁用" />
                </t-select>
              </t-form-item>
              <t-form-item>
                <div class="flex gap-2">
                  <t-button v-auth="'system:org:member:view'" theme="primary" size="small" @click="handleSearchMember">
                    <template #icon>
                      <Search class="h-4 w-4" />
                    </template>
                    查询
                  </t-button>
                  <t-button theme="default" variant="outline" size="small" @click="handleResetMember">
                    <template #icon>
                      <RefreshCcw class="h-4 w-4" />
                    </template>
                    重置
                  </t-button>
                </div>
              </t-form-item>
            </t-form>
          </div>

          <t-table
            row-key="id"
            :data="memberList"
            :columns="memberColumns"
            :loading="memberLoading"
            :pagination="{ disabled: true }"
            hover
          >
            <template #mobile="{ row }">
              {{ row.mobile || '-' }}
            </template>

            <template #email="{ row }">
              {{ row.email || '-' }}
            </template>

            <template #positionName="{ row }">
              {{ row.positionName || '-' }}
            </template>

            <template #statusSlot="{ row }">
              <t-tag :theme="row.status === 1 ? 'success' : 'warning'" variant="light" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </t-tag>
            </template>
          </t-table>
        </t-card>

        <t-card v-else :bordered="false" class="flex flex-1 items-center justify-center shadow-sm">
          <div class="text-center text-[#86909c]">
            <Building class="mx-auto mb-4 h-16 w-16 opacity-30" />
            <p class="text-lg">
              请从左侧选择一个组织
            </p>
          </div>
        </t-card>
      </div>
    </div>

    <t-dialog
      v-model:visible="orgDialogVisible"
      :header="orgDialogTitle"
      width="640px"
      :confirm-btn="{ loading: submitting }"
      :on-confirm="handleSubmitOrg"
    >
      <t-form ref="orgFormRef" :data="orgFormData" :rules="orgFormRules" label-align="right" label-width="100px">
        <t-form-item label="上级组织" name="parentId">
          <t-select v-model="orgFormData.parentId" clearable placeholder="请选择上级组织">
            <t-option v-for="item in parentOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>
        <t-form-item label="组织名称" name="name">
          <t-input v-model="orgFormData.name" placeholder="请输入组织名称" />
        </t-form-item>
        <t-form-item label="组织编码" name="code">
          <t-input v-model="orgFormData.code" placeholder="请输入组织编码" />
        </t-form-item>
        <t-form-item label="负责人" name="leader">
          <t-input v-model="orgFormData.leader" placeholder="请输入负责人" />
        </t-form-item>
        <t-form-item label="排序号" name="sort">
          <t-input-number v-model="orgFormData.sort" theme="column" />
        </t-form-item>
        <t-form-item label="状态" name="status">
          <t-radio-group v-model="orgFormData.status">
            <t-radio :value="1">
              启用
            </t-radio>
            <t-radio :value="0">
              禁用
            </t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="备注" name="remark">
          <t-textarea v-model="orgFormData.remark" :maxlength="200" placeholder="请输入备注" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </PageContainer>
</template>
