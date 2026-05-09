<script setup lang="ts">
import { Edit, Plus, RefreshCcw, Search, Send, Trash2, Undo2 } from '@lucide/vue';
import { message } from 'antdv-next';
import { onMounted, reactive, ref } from 'vue';
import {
  getAnnouncementPage,
  getAnnouncementDetail,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  publishAnnouncement,
  revokeAnnouncement,
} from '@/api/announcement';

interface AnnouncementItem {
  id: string;
  title: string;
  type: string;
  status: string;
  top: boolean;
  targetType: string;
  targetValue?: string | null;
  creatorName: string;
  publishTime?: string | null;
  updateTime: string;
}

interface AnnouncementDetail extends AnnouncementItem {
  content: string;
}

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const total = ref(0);
const announcementList = ref<AnnouncementItem[]>([]);

const searchParams = reactive({
  title: '',
  type: '',
  status: '',
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const formRef = ref();
const formData = reactive({
  id: '',
  title: '',
  content: '',
  type: 'NOTICE',
  targetType: 'ALL',
  targetValue: '',
  top: false,
});

const formRules = {
  title: [{ required: true, message: '公告标题必填', type: 'error' as const }],
  content: [{ required: true, message: '公告内容必填', type: 'error' as const }],
  type: [{ required: true, message: '公告类型必填', type: 'error' as const }],
  targetType: [{ required: true, message: '投放范围必填', type: 'error' as const }],
};

const announcementTypeOptions = [
  { label: '系统通知', value: 'SYSTEM' },
  { label: '业务通知', value: 'NOTICE' },
  { label: '功能发布', value: 'FEATURE' },
  { label: '安全提醒', value: 'SECURITY' },
];

const announcementStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已撤回', value: 'REVOKED' },
];

const targetTypeOptions = [
  { label: '全部用户', value: 'ALL' },
  { label: '组织', value: 'ORG' },
  { label: '角色', value: 'ROLE' },
  { label: '用户', value: 'USER' },
];

const columns = [
  { dataIndex: 'title', title: '公告标题', ellipsis: true },
  { dataIndex: 'type', title: '类型', width: 120 },
  { dataIndex: 'status', title: '状态', width: 110 },
  { dataIndex: 'targetType', title: '投放范围', width: 120 },
  { dataIndex: 'targetValue', title: '目标值', ellipsis: true },
  { dataIndex: 'top', title: '置顶', width: 80 },
  { dataIndex: 'publishTime', title: '发布时间', width: 180 },
  { dataIndex: 'action', title: '操作', width: 280, fixed: 'right' as const },
];

const tableScroll = { x: 1320 };

async function fetchList() {
  loading.value = true;
  try {
    const res = await getAnnouncementPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      title: searchParams.title || undefined,
      type: searchParams.type || undefined,
      status: searchParams.status || undefined,
    });
    announcementList.value = res.list;
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
  searchParams.title = '';
  searchParams.type = '';
  searchParams.status = '';
  handleSearch();
}

function handlePageChange(pageInfo: { current?: number; pageSize?: number }) {
  pagination.current = pageInfo.current ?? pagination.current;
  pagination.pageSize = pageInfo.pageSize ?? pagination.pageSize;
  fetchList();
}

function resetForm() {
  Object.assign(formData, {
    id: '',
    title: '',
    content: '',
    type: 'NOTICE',
    targetType: 'ALL',
    targetValue: '',
    top: false,
  });
}

function openCreateDialog() {
  resetForm();
  dialogVisible.value = true;
}

async function openEditDialog(row: AnnouncementItem) {
  const detail = await getAnnouncementDetail(row.id);
  Object.assign(formData, {
    id: detail.id,
    title: detail.title,
    content: detail.content,
    type: detail.type,
    targetType: detail.targetType,
    targetValue: detail.targetValue || '',
    top: detail.top,
  });
  dialogVisible.value = true;
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  if (formData.targetType !== 'ALL' && !formData.targetValue.trim()) {
    message.warning('非全部投放时目标值不能为空');
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      title: formData.title,
      content: formData.content,
      type: formData.type,
      targetType: formData.targetType,
      targetValue: formData.targetType === 'ALL' ? null : formData.targetValue.trim(),
      top: formData.top,
    };
    if (formData.id) {
      await updateAnnouncement(formData.id, payload);
      message.success('公告修改成功');
    } else {
      await createAnnouncement(payload);
      message.success('公告新增成功');
    }
    dialogVisible.value = false;
    fetchList();
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(row: AnnouncementItem) {
  if (!window.confirm(`确认删除公告「${row.title}」吗？`)) {
    return;
  }
  await deleteAnnouncement(row.id);
  message.success('公告已删除');
  fetchList();
}

async function handlePublish(row: AnnouncementItem) {
  await publishAnnouncement(row.id);
  message.success('公告已发布');
  fetchList();
}

async function handleRevoke(row: AnnouncementItem) {
  await revokeAnnouncement(row.id);
  message.success('公告已撤回');
  fetchList();
}

onMounted(() => {
  fetchList();
});
</script>

<template>
  <div class="announcement-page flex flex-col gap-4">
    <a-card variant="borderless" class="shadow-sm">
      <a-row :gutter="[16, 12]" class="filter-layout" align="top">
        <a-col :xs="24" :xl="20">
          <a-form class="filter-form-grid" :label-col="{ style: { width: '72px' } }">
            <a-row :gutter="[24, 18]" align="top">
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="公告标题">
                  <a-input
                    v-model:value="searchParams.title"
                    allow-clear
                    placeholder="请输入公告标题"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="公告类型">
                  <a-select
                    v-model:value="searchParams.type"
                    allow-clear
                    placeholder="请选择类型"
                    class="filter-control"
                    :options="announcementTypeOptions"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="公告状态">
                  <a-select
                    v-model:value="searchParams.status"
                    allow-clear
                    placeholder="请选择状态"
                    class="filter-control"
                    :options="announcementStatusOptions"
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
          <a-button v-auth="'announcement:manage:create'" type="primary" @click="openCreateDialog">
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
        :data-source="announcementList"
        :columns="columns"
        :loading="loading"
        bordered
        hover
        stripe
        size="small"
        :pagination="{ ...pagination, total }"
        :scroll="tableScroll"
        @change="handlePageChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'type'">
            {{
              record.type === 'SYSTEM'
                ? '系统通知'
                : record.type === 'NOTICE'
                  ? '业务通知'
                  : record.type === 'FEATURE'
                    ? '功能发布'
                    : '安全提醒'
            }}
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <a-tag
              :color="
                record.status === 'PUBLISHED'
                  ? 'success'
                  : record.status === 'REVOKED'
                    ? 'warning'
                    : 'default'
              "
              variant="filled"
            >
              {{
                record.status === 'PUBLISHED'
                  ? '已发布'
                  : record.status === 'REVOKED'
                    ? '已撤回'
                    : '草稿'
              }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'targetType'">
            {{
              record.targetType === 'ALL'
                ? '全部用户'
                : record.targetType === 'ORG'
                  ? '组织'
                  : record.targetType === 'ROLE'
                    ? '角色'
                    : '用户'
            }}
          </template>
          <template v-else-if="column.dataIndex === 'targetValue'">
            {{ record.targetValue || '-' }}
          </template>
          <template v-else-if="column.dataIndex === 'top'">
            <a-tag :color="record.top ? 'primary' : 'default'" variant="filled" size="small">
              {{ record.top ? '置顶' : '普通' }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'publishTime'">
            {{ record.publishTime || '-' }}
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <div class="row-actions">
              <a-button
                v-auth="'announcement:manage:update'"
                type="primary"
                size="small"
                @click="openEditDialog(record)"
              >
                <template #icon>
                  <Edit class="h-3.5 w-3.5" />
                </template>
                编辑
              </a-button>
              <a-button
                v-if="record.status !== 'PUBLISHED'"
                v-auth="'announcement:manage:publish'"
                type="primary"
                size="small"
                @click="handlePublish(record)"
              >
                <template #icon>
                  <Send class="h-3.5 w-3.5" />
                </template>
                发布
              </a-button>
              <a-button
                v-if="record.status === 'PUBLISHED'"
                v-auth="'announcement:manage:revoke'"
                color="orange"
                variant="outlined"
                size="small"
                @click="handleRevoke(record)"
              >
                <template #icon>
                  <Undo2 class="h-3.5 w-3.5" />
                </template>
                撤回
              </a-button>
              <a-button
                v-auth="'announcement:manage:delete'"
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
    :title="formData.id ? '编辑公告' : '新增公告'"
    width="720px"
    :confirm-loading="submitting"
    @ok="handleSubmit"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ style: { width: '100px' } }"
    >
      <a-form-item label="公告标题" name="title">
        <a-input v-model:value="formData.title" :maxlength="60" />
      </a-form-item>
      <a-form-item label="公告类型" name="type">
        <a-select v-model:value="formData.type" :options="announcementTypeOptions" />
      </a-form-item>
      <a-form-item label="投放范围" name="targetType">
        <a-select v-model:value="formData.targetType" :options="targetTypeOptions" />
      </a-form-item>
      <a-form-item label="目标值">
        <a-input
          v-model:value="formData.targetValue"
          :disabled="formData.targetType === 'ALL'"
          placeholder="组织名 / 角色编码 / 用户名"
        />
      </a-form-item>
      <a-form-item label="是否置顶">
        <a-switch v-model:checked="formData.top" />
      </a-form-item>
      <a-form-item label="公告内容" name="content">
        <a-textarea v-model:value="formData.content" :maxlength="1000" auto-size />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<style scoped>
.announcement-page {
  color: #1d2129;
}

.filter-control {
  width: 160px;
}

.filter-control--title {
  width: 240px;
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
  padding-bottom: 14px;
}

.table-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.announcement-page :deep(.ant-form-item) {
  margin-right: 12px;
  margin-bottom: 10px;
}

.announcement-page :deep(.ant-table-thead > tr > th),
.announcement-page :deep(.ant-table-tbody > tr > td) {
  padding-top: 9px;
  padding-bottom: 9px;
}

.announcement-page :deep(.ant-table-pagination) {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef2f7;
}
</style>
