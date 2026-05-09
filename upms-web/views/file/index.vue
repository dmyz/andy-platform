<script setup lang="ts">
import { Download, Eye, Plus, RefreshCcw, Search, Trash2, Upload } from '@lucide/vue';
import { message } from 'antdv-next';
import { onMounted, reactive, ref } from 'vue';
import {
  getFilePage,
  uploadFileRecord,
  getFilePreview,
  downloadFileRecord,
  deleteFileRecord,
} from '@/api/file';

interface FileItem {
  id: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploaderName: string;
  categoryCode?: string | null;
  remark?: string | null;
  uploadTime: string;
}

const MAX_FILE_SIZE = 5 * 1024 * 1024;
const ALLOWED_TYPES = ['image/png', 'image/jpeg', 'image/webp', 'application/pdf', 'text/plain'];

const loading = ref(false);
const uploading = ref(false);
const uploadDialogVisible = ref(false);
const previewDialogVisible = ref(false);
const total = ref(0);
const fileList = ref<FileItem[]>([]);
const selectedFile = ref<File | null>(null);
const previewUrl = ref('');
const previewTitle = ref('');
const fileInputRef = ref<HTMLInputElement | null>(null);

const searchParams = reactive({
  fileName: '',
  fileType: '',
  uploaderName: '',
  uploadTimeRange: [] as string[],
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const uploadForm = reactive({
  categoryCode: 'document',
  remark: '',
});

const fileCategoryOptions = [
  { label: '文档', value: 'document' },
  { label: '图片', value: 'image' },
  { label: '安全资料', value: 'security' },
  { label: '其他', value: 'other' },
];

const columns = [
  { dataIndex: 'fileName', title: '文件名', ellipsis: true },
  { dataIndex: 'fileType', title: '文件类型', width: 180 },
  { dataIndex: 'fileSize', title: '文件大小', width: 120 },
  { dataIndex: 'uploaderName', title: '上传人', width: 120 },
  { dataIndex: 'categoryCode', title: '分类编码', width: 140 },
  { dataIndex: 'remark', title: '备注', ellipsis: true },
  { dataIndex: 'uploadTime', title: '上传时间', width: 180 },
  { dataIndex: 'action', title: '操作', width: 260, fixed: 'right' as const },
];

const tableScroll = { x: 1320 };

function buildQueryParams() {
  const [startTime, endTime] = searchParams.uploadTimeRange;
  return {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    fileName: searchParams.fileName || undefined,
    fileType: searchParams.fileType || undefined,
    uploaderName: searchParams.uploaderName || undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
  };
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await getFilePage(buildQueryParams());
    fileList.value = res.list;
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
  searchParams.fileName = '';
  searchParams.fileType = '';
  searchParams.uploaderName = '';
  searchParams.uploadTimeRange = [];
  handleSearch();
}

function handlePageChange(pageInfo: { current?: number; pageSize?: number }) {
  pagination.current = pageInfo.current ?? pagination.current;
  pagination.pageSize = pageInfo.pageSize ?? pagination.pageSize;
  fetchList();
}

function formatFileSize(size: number) {
  if (size < 1024) {
    return `${size} B`;
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }
  return `${(size / 1024 / 1024).toFixed(2)} MB`;
}

function decodePreviewText(url: string) {
  const base64 = url.split(',')[1] || '';
  const binary = window.atob(base64);
  const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
  return new TextDecoder().decode(bytes);
}

function openUploadDialog() {
  selectedFile.value = null;
  uploadForm.categoryCode = 'document';
  uploadForm.remark = '';
  uploadDialogVisible.value = true;
}

function triggerFileSelect() {
  fileInputRef.value?.click();
}

function handleFileSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) {
    return;
  }
  if (file.size > MAX_FILE_SIZE) {
    message.warning('文件大小不能超过 5MB');
    input.value = '';
    return;
  }
  if (!ALLOWED_TYPES.includes(file.type)) {
    message.warning('仅支持 PNG、JPG、WEBP、PDF、TXT 文件');
    input.value = '';
    return;
  }
  selectedFile.value = file;
}

async function handleUpload() {
  if (!selectedFile.value) {
    message.warning('请先选择文件');
    return;
  }
  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value);
    formData.append('categoryCode', uploadForm.categoryCode);
    formData.append('remark', uploadForm.remark);
    await uploadFileRecord(formData);
    message.success('文件上传成功');
    uploadDialogVisible.value = false;
    selectedFile.value = null;
    if (fileInputRef.value) {
      fileInputRef.value.value = '';
    }
    fetchList();
  } finally {
    uploading.value = false;
  }
}

async function handlePreview(row: FileItem) {
  const res = await getFilePreview(row.id);
  previewTitle.value = row.fileName;
  previewUrl.value = res.previewUrl;
  previewDialogVisible.value = true;
}

async function handleDownload(row: FileItem) {
  await downloadFileRecord(row.id, row.fileName);
  message.success('下载成功');
}

async function handleDelete(row: FileItem) {
  if (!window.confirm(`确认删除文件「${row.fileName}」吗？`)) {
    return;
  }
  await deleteFileRecord(row.id);
  message.success('文件已删除');
  fetchList();
}

onMounted(() => {
  fetchList();
});
</script>

<template>
  <div class="file-page flex flex-col gap-4">
    <input ref="fileInputRef" type="file" class="hidden" @change="handleFileSelected" />

    <a-card variant="borderless" class="shadow-sm">
      <a-row :gutter="[16, 12]" class="filter-layout" align="top">
        <a-col :xs="24" :xl="20">
          <a-form class="filter-form-grid" :label-col="{ style: { width: '72px' } }">
            <a-row :gutter="[24, 18]" align="top">
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="文件名">
                  <a-input
                    v-model:value="searchParams.fileName"
                    allow-clear
                    placeholder="请输入文件名"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="文件类型">
                  <a-input
                    v-model:value="searchParams.fileType"
                    allow-clear
                    placeholder="请输入 MIME 类型"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="上传人">
                  <a-input
                    v-model:value="searchParams.uploaderName"
                    allow-clear
                    placeholder="请输入上传人"
                    class="filter-control"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12" :lg="8" :xxl="6">
                <a-form-item label="上传时间">
                  <a-range-picker
                    v-model:value="searchParams.uploadTimeRange"
                    allow-clear
                    enable-time-picker
                    format="YYYY-MM-DD HH:mm:ss"
                    value-type="YYYY-MM-DD HH:mm:ss"
                    :placeholder="['开始时间', '结束时间']"
                    class="filter-control"
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
          <a-button v-auth="'file:manage:upload'" type="primary" @click="openUploadDialog">
            <template #icon>
              <Upload class="h-4 w-4" />
            </template>
            上传
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
        :data-source="fileList"
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
          <template v-if="column.dataIndex === 'fileSize'">
            {{ formatFileSize(record.fileSize) }}
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <div class="row-actions">
              <a-button
                v-auth="'file:manage:preview'"
                type="primary"
                size="small"
                @click="handlePreview(record)"
              >
                <template #icon>
                  <Eye class="h-3.5 w-3.5" />
                </template>
                预览
              </a-button>
              <a-button
                v-auth="'file:manage:download'"
                variant="outlined"
                size="small"
                @click="handleDownload(record)"
              >
                <template #icon>
                  <Download class="h-3.5 w-3.5" />
                </template>
                下载
              </a-button>
              <a-button
                v-auth="'file:manage:delete'"
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
    v-model:open="uploadDialogVisible"
    title="上传文件"
    width="640px"
    :confirm-loading="uploading"
    @ok="handleUpload"
  >
    <div class="space-y-4">
      <div class="rounded-lg border border-dashed border-[#b8c7e6] bg-[#f7faff] p-4">
        <div class="flex items-center justify-between gap-4">
          <div>
            <div class="text-sm font-medium text-[#1d2129]">
              {{ selectedFile?.name || '未选择文件' }}
            </div>
            <div class="mt-1 text-xs text-[#86909c]">
              支持 PNG、JPG、WEBP、PDF、TXT，大小不超过 5MB
            </div>
          </div>
          <a-button type="primary" variant="outlined" @click="triggerFileSelect">
            <template #icon>
              <Upload class="h-4 w-4" />
            </template>
            选择
          </a-button>
        </div>
      </div>
      <a-form :model="uploadForm" :label-col="{ style: { width: '100px' } }">
        <a-form-item label="文件分类">
          <a-select v-model:value="uploadForm.categoryCode" :options="fileCategoryOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea
            v-model:value="uploadForm.remark"
            :maxlength="200"
            auto-size
            placeholder="请输入备注"
          />
        </a-form-item>
      </a-form>
    </div>
  </a-modal>

  <a-modal
    v-model:open="previewDialogVisible"
    :title="previewTitle || '文件预览'"
    width="880px"
    :footer="null"
  >
    <div class="max-h-[70vh] overflow-auto rounded-xl bg-[#f5f7fa] p-4">
      <img
        v-if="previewUrl.startsWith('data:image')"
        :src="previewUrl"
        class="mx-auto max-w-full rounded-lg"
      />
      <iframe
        v-else-if="previewUrl.startsWith('data:application/pdf')"
        :src="previewUrl"
        class="h-[65vh] w-full rounded-lg bg-white"
      />
      <pre v-else class="whitespace-pre-wrap break-words text-sm leading-7 text-[#1d2129]">{{
        decodePreviewText(previewUrl)
      }}</pre>
    </div>
  </a-modal>
</template>

<style scoped>
.file-page {
  color: #1d2129;
}

.filter-control {
  width: 216px;
}

.filter-control--short {
  width: 170px;
}

.filter-control--range {
  width: 330px;
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

.file-page :deep(.ant-form-item) {
  margin-right: 12px;
  margin-bottom: 10px;
}

.file-page :deep(.ant-table-thead > tr > th),
.file-page :deep(.ant-table-tbody > tr > td) {
  padding-top: 9px;
  padding-bottom: 9px;
}

.file-page :deep(.ant-table-pagination) {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef2f7;
}
</style>
