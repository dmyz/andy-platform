# 前端组件开发规范

## 组件分类

### 1. 页面组件（Page Components）
位置：`src/views/`

特点：
- 对应路由页面
- 包含完整的业务逻辑
- 可以使用布局组件
- 文件名使用 PascalCase

示例：
```
src/views/system/user/UserList.vue
src/views/system/user/UserDetail.vue
```

### 2. 布局组件（Layout Components）
位置：`src/layouts/`

特点：
- 定义页面整体布局
- 包含 Header、Sidebar、Footer 等
- 使用 `<router-view>` 渲染子页面

示例：
```
src/layouts/BasicLayout.vue
src/layouts/BlankLayout.vue
```

### 3. 公共组件（Common Components）
位置：`src/components/`

特点：
- 可复用的业务组件
- 独立的功能单元
- 通过 props 接收数据
- 通过 emit 触发事件

示例：
```
src/components/StatCard/index.vue
src/components/TrendChart/index.vue
```

## 组件开发模板

### 基础模板

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

// Props 定义
interface Props {
  title: string
  count?: number
}

const props = withDefaults(defineProps<Props>(), {
  count: 0,
})

// Emits 定义
interface Emits {
  (e: 'update', value: number): void
  (e: 'delete', id: string): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const loading = ref(false)
const data = ref<any[]>([])

// 计算属性
const total = computed(() => data.value.length)

// 方法
function handleUpdate() {
  emit('update', total.value)
}

// 生命周期
onMounted(() => {
  // 初始化逻辑
})
</script>

<template>
  <div class="component-wrapper">
    <h2>{{ title }}</h2>
    <p>Count: {{ count }}</p>
    <button @click="handleUpdate">Update</button>
  </div>
</template>

<style scoped>
.component-wrapper {
  padding: 16px;
}
</style>
```

### 表单组件模板

```vue
<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormRule } from 'tdesign-vue-next'

interface FormData {
  username: string
  email: string
  phone: string
}

const formRef = ref()
const formData = reactive<FormData>({
  username: '',
  email: '',
  phone: '',
})

const rules: Record<keyof FormData, FormRule[]> = {
  username: [
    { required: true, message: '请输入用户名', type: 'error' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', type: 'error' },
  ],
  email: [
    { required: true, message: '请输入邮箱', type: 'error' },
    { email: true, message: '请输入正确的邮箱格式', type: 'error' },
  ],
  phone: [
    { required: true, message: '请输入手机号', type: 'error' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', type: 'error' },
  ],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (valid === true) {
    // 提交逻辑
    console.log('提交数据：', formData)
  }
}

function handleReset() {
  formRef.value?.reset()
}
</script>

<template>
  <t-form
    ref="formRef"
    :data="formData"
    :rules="rules"
    label-width="100px"
  >
    <t-form-item label="用户名" name="username">
      <t-input v-model="formData.username" placeholder="请输入用户名" />
    </t-form-item>

    <t-form-item label="邮箱" name="email">
      <t-input v-model="formData.email" placeholder="请输入邮箱" />
    </t-form-item>

    <t-form-item label="手机号" name="phone">
      <t-input v-model="formData.phone" placeholder="请输入手机号" />
    </t-form-item>

    <t-form-item>
      <t-space>
        <t-button theme="primary" @click="handleSubmit">提交</t-button>
        <t-button theme="default" @click="handleReset">重置</t-button>
      </t-space>
    </t-form-item>
  </t-form>
</template>
```

### 列表页面模板

```vue
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { PageInfo, PrimaryTableCol } from 'tdesign-vue-next'
import { getUserList } from '@/api/user'

// 查询条件
const searchForm = reactive({
  username: '',
  status: '',
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页信息
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 表格列定义
const columns: PrimaryTableCol[] = [
  { colKey: 'username', title: '用户名', width: 150 },
  { colKey: 'realName', title: '真实姓名', width: 150 },
  { colKey: 'phone', title: '手机号', width: 150 },
  { colKey: 'email', title: '邮箱', width: 200 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'createTime', title: '创建时间', width: 180 },
  { colKey: 'operation', title: '操作', width: 200, fixed: 'right' },
]

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await getUserList({
      ...searchForm,
      page: pagination.current,
      size: pagination.pageSize,
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.current = 1
  loadData()
}

// 重置
function handleReset() {
  Object.assign(searchForm, {
    username: '',
    status: '',
  })
  handleSearch()
}

// 分页变化
function handlePageChange(pageInfo: PageInfo) {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  loadData()
}

// 编辑
function handleEdit(row: any) {
  console.log('编辑：', row)
}

// 删除
function handleDelete(row: any) {
  console.log('删除：', row)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page-container">
    <!-- 查询区 -->
    <t-card class="search-card">
      <t-form :data="searchForm" layout="inline">
        <t-form-item label="用户名">
          <t-input v-model="searchForm.username" placeholder="请输入用户名" />
        </t-form-item>
        <t-form-item label="状态">
          <t-select v-model="searchForm.status" placeholder="请选择状态">
            <t-option value="1" label="启用" />
            <t-option value="0" label="禁用" />
          </t-select>
        </t-form-item>
        <t-form-item>
          <t-space>
            <t-button theme="primary" @click="handleSearch">查询</t-button>
            <t-button theme="default" @click="handleReset">重置</t-button>
          </t-space>
        </t-form-item>
      </t-form>
    </t-card>

    <!-- 表格区 -->
    <t-card class="table-card">
      <t-table
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @page-change="handlePageChange"
      >
        <template #operation="{ row }">
          <t-space>
            <t-link theme="primary" @click="handleEdit(row)">编辑</t-link>
            <t-link theme="danger" @click="handleDelete(row)">删除</t-link>
          </t-space>
        </template>
      </t-table>
    </t-card>
  </div>
</template>

<style scoped>
.page-container {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}
</style>
```

## 组件命名规范

### 文件命名
- 组件文件使用 PascalCase：`UserList.vue`
- 组件目录使用 PascalCase：`UserList/index.vue`
- 工具文件使用 kebab-case：`format-date.ts`

### 组件名称
- 单文件组件名使用 PascalCase
- 组件注册使用 PascalCase
- 模板中使用 kebab-case 或 PascalCase

```vue
<!-- 推荐 -->
<UserList />
<user-list />

<!-- 不推荐 -->
<userList />
```

## Props 规范

### Props 定义
```typescript
// 使用 TypeScript 接口定义
interface Props {
  // 必填属性
  title: string
  // 可选属性
  count?: number
  // 带默认值的属性
  size?: 'small' | 'medium' | 'large'
  // 对象类型
  user?: {
    id: string
    name: string
  }
  // 数组类型
  items?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  count: 0,
  size: 'medium',
  items: () => [],
})
```

### Props 命名
- 使用 camelCase
- 布尔类型使用 is/has/should 前缀
- 事件处理函数使用 on 前缀

```typescript
interface Props {
  userName: string
  isVisible: boolean
  hasPermission: boolean
  onClose?: () => void
}
```

## Emits 规范

```typescript
// 定义事件类型
interface Emits {
  // 无参数事件
  (e: 'close'): void
  // 单参数事件
  (e: 'update', value: string): void
  // 多参数事件
  (e: 'change', id: string, value: number): void
}

const emit = defineEmits<Emits>()

// 触发事件
emit('close')
emit('update', 'new value')
emit('change', '123', 100)
```

## 组合式函数（Composables）

位置：`src/composables/`

命名：使用 `use` 前缀

```typescript
// src/composables/useTable.ts
import { ref, reactive } from 'vue'

export function useTable() {
  const loading = ref(false)
  const data = ref([])
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  async function loadData() {
    loading.value = true
    try {
      // 加载数据逻辑
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    data,
    pagination,
    loadData,
  }
}
```

## 样式规范

### 使用 Tailwind CSS
```vue
<template>
  <div class="flex items-center justify-between p-4 bg-white rounded-lg shadow">
    <h2 class="text-lg font-semibold">标题</h2>
    <button class="px-4 py-2 text-white bg-blue-500 rounded hover:bg-blue-600">
      按钮
    </button>
  </div>
</template>
```

### 使用 Scoped 样式
```vue
<style scoped>
/* 组件私有样式 */
.custom-class {
  color: red;
}

/* 深度选择器 */
:deep(.t-button) {
  margin-right: 8px;
}
</style>
```

## 性能优化

### 1. 使用 v-show 代替 v-if（频繁切换）
```vue
<!-- 推荐：频繁切换 -->
<div v-show="isVisible">内容</div>

<!-- 推荐：条件渲染 -->
<div v-if="hasPermission">内容</div>
```

### 2. 使用计算属性缓存
```typescript
// 推荐
const filteredList = computed(() => {
  return list.value.filter(item => item.status === 'active')
})

// 不推荐
function getFilteredList() {
  return list.value.filter(item => item.status === 'active')
}
```

### 3. 列表使用 key
```vue
<div v-for="item in list" :key="item.id">
  {{ item.name }}
</div>
```

### 4. 大列表虚拟滚动
```vue
<t-table
  :data="largeData"
  :virtual-scroll="{ threshold: 100 }"
/>
```
