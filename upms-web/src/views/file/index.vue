<script setup lang="ts">
import { Download, Eye, Plus, RefreshCcw, Search, Trash2, Upload } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { del, download, get, post } from '@/utils/request'

interface FileItem {
  id: string
  fileName: string
  fileType: string
  fileSize: number
  uploaderName: string
  categoryCode?: string | null
  remark?: string | null
  uploadTime: string
}

const MAX_FILE_SIZE = 5 * 1024 * 1024
const ALLOWED_TYPES = ['image/png', 'image/jpeg', 'image/webp', 'application/pdf', 'text/plain']

const loading = ref(false)
const uploading = ref(false)
const uploadDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const total = ref(0)
const fileList = ref<FileItem[]>([])
const selectedFile = ref<File | null>(null)
const previewUrl = ref('')
const previewTitle = ref('')
const fileInputRef = ref<HTMLInputElement | null>(null)

const searchParams = reactive({
  fileName: '',
  fileType: '',
  uploaderName: '',
  uploadTimeRange: [] as string[],
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const uploadForm = reactive({
  categoryCode: 'document',
  remark: '',
})

const columns = [
  { colKey: 'fileName', title: '文件名', ellipsis: true },
  { colKey: 'fileType', title: '文件类型', width: 180 },
  { colKey: 'fileSize', title: '文件大小', width: 120 },
  { colKey: 'uploaderName', title: '上传人', width: 120 },
  { colKey: 'categoryCode', title: '分类编码', width: 140 },
  { colKey: 'remark', title: '备注', ellipsis: true },
  { colKey: 'uploadTime', title: '上传时间', width: 180 },
  { colKey: 'action', title: '操作', width: 180, fixed: 'right' as const },
]

function buildQueryParams() {
  const [startTime, endTime] = searchParams.uploadTimeRange
  return {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    fileName: searchParams.fileName || undefined,
    fileType: searchParams.fileType || undefined,
    uploaderName: searchParams.uploaderName || undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await get<{
      list: FileItem[]
      total: number
      pageNum: number
      pageSize: number
    }>('/file/page', buildQueryParams())
    fileList.value = res.list
    total.value = res.total
    pagination.current = res.pageNum
    pagination.pageSize = res.pageSize
  }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchList()
}

function handleReset() {
  searchParams.fileName = ''
  searchParams.fileType = ''
  searchParams.uploaderName = ''
  searchParams.uploadTimeRange = []
  handleSearch()
}

function handlePageChange(pageInfo: { current: number, pageSize: number }) {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  fetchList()
}

function formatFileSize(size: number) {
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(2)} MB`
}

function decodePreviewText(url: string) {
  const base64 = url.split(',')[1] || ''
  const binary = window.atob(base64)
  const bytes = Uint8Array.from(binary, char => char.charCodeAt(0))
  return new TextDecoder().decode(bytes)
}

function openUploadDialog() {
  selectedFile.value = null
  uploadForm.categoryCode = 'document'
  uploadForm.remark = ''
  uploadDialogVisible.value = true
}

function triggerFileSelect() {
  fileInputRef.value?.click()
}

function handleFileSelected(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }
  if (file.size > MAX_FILE_SIZE) {
    MessagePlugin.warning('文件大小不能超过 5MB')
    input.value = ''
    return
  }
  if (!ALLOWED_TYPES.includes(file.type)) {
    MessagePlugin.warning('仅支持 PNG、JPG、WEBP、PDF、TXT 文件')
    input.value = ''
    return
  }
  selectedFile.value = file
}

async function handleUpload() {
  if (!selectedFile.value) {
    MessagePlugin.warning('请先选择文件')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('categoryCode', uploadForm.categoryCode)
    formData.append('remark', uploadForm.remark)
    await post('/file/upload', formData)
    MessagePlugin.success('文件上传成功')
    uploadDialogVisible.value = false
    selectedFile.value = null
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
    fetchList()
  }
  finally {
    uploading.value = false
  }
}

async function handlePreview(row: FileItem) {
  const res = await get<{ previewUrl: string }>(`/file/${row.id}/preview`)
  previewTitle.value = row.fileName
  previewUrl.value = res.previewUrl
  previewDialogVisible.value = true
}

async function handleDownload(row: FileItem) {
  await download(`/file/${row.id}/download`, { filename: row.fileName })
  MessagePlugin.success('下载成功')
}

async function handleDelete(row: FileItem) {
  if (!window.confirm(`确认删除文件「${row.fileName}」吗？`)) {
    return
  }
  await del(`/file/${row.id}`)
  MessagePlugin.success('文件已删除')
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="文件管理" description="管理文件上传、预览、下载、删除，并维护分类和备注。">
    <input ref="fileInputRef" type="file" class="hidden" @change="handleFileSelected" />

    <div class="mb-4 rounded-2xl bg-white p-4 shadow-sm">
      <t-form layout="inline" label-width="80px">
        <t-form-item label="文件名">
          <t-input v-model="searchParams.fileName" clearable placeholder="请输入文件名" style="width: 220px" />
        </t-form-item>
        <t-form-item label="文件类型">
          <t-input v-model="searchParams.fileType" clearable placeholder="请输入 MIME 类型" style="width: 220px" />
        </t-form-item>
        <t-form-item label="上传人">
          <t-input v-model="searchParams.uploaderName" clearable placeholder="请输入上传人" style="width: 180px" />
        </t-form-item>
        <t-form-item label="上传时间">
          <t-date-range-picker
            v-model="searchParams.uploadTimeRange"
            clearable
            enable-time-picker
            format="YYYY-MM-DD HH:mm:ss"
            value-type="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择上传时间范围"
            style="width: 340px"
          />
        </t-form-item>
        <t-form-item>
          <div class="flex gap-2">
            <t-button theme="primary" @click="handleSearch">
              <template #icon>
                <Search class="h-4 w-4" />
              </template>
              查询
            </t-button>
            <t-button theme="default" variant="outline" @click="handleReset">
              <template #icon>
                <RefreshCcw class="h-4 w-4" />
              </template>
              重置
            </t-button>
            <t-button v-auth="'file:manage:upload'" theme="primary" @click="openUploadDialog">
              <template #icon>
                <Plus class="h-4 w-4" />
              </template>
              上传文件
            </t-button>
          </div>
        </t-form-item>
      </t-form>
    </div>

    <div class="rounded-2xl bg-white p-4 shadow-sm">
      <t-table
        row-key="id"
        :data="fileList"
        :columns="columns"
        :loading="loading"
        bordered
        hover
        :pagination="{ ...pagination, total }"
        @page-change="handlePageChange"
      >
        <template #fileSize="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
        <template #action="{ row }">
          <div class="flex items-center gap-1">
            <t-button v-auth="'file:manage:preview'" theme="primary" variant="text" size="small" @click="handlePreview(row)">
              <Eye class="h-4 w-4" />
            </t-button>
            <t-button v-auth="'file:manage:download'" theme="primary" variant="text" size="small" @click="handleDownload(row)">
              <Download class="h-4 w-4" />
            </t-button>
            <t-button v-auth="'file:manage:delete'" theme="danger" variant="text" size="small" @click="handleDelete(row)">
              <Trash2 class="h-4 w-4" />
            </t-button>
          </div>
        </template>
      </t-table>
    </div>

    <t-dialog v-model:visible="uploadDialogVisible" header="上传文件" width="640px" :on-confirm="handleUpload" :confirm-btn="{ loading: uploading }">
      <div class="space-y-4">
        <div class="rounded-xl border border-dashed border-[#d9e1f2] bg-[#f8fbff] p-4">
          <div class="flex items-center justify-between gap-4">
            <div>
              <div class="text-sm font-medium text-[#1d2129]">{{ selectedFile?.name || '未选择文件' }}</div>
              <div class="mt-1 text-xs text-[#86909c]">支持 PNG、JPG、WEBP、PDF、TXT，大小不超过 5MB</div>
            </div>
            <t-button theme="primary" variant="outline" @click="triggerFileSelect">
              <template #icon>
                <Upload class="h-4 w-4" />
              </template>
              选择文件
            </t-button>
          </div>
        </div>
        <t-form :data="uploadForm" label-width="100px">
          <t-form-item label="文件分类">
            <t-select v-model="uploadForm.categoryCode">
              <t-option value="document" label="文档" />
              <t-option value="image" label="图片" />
              <t-option value="security" label="安全资料" />
              <t-option value="other" label="其他" />
            </t-select>
          </t-form-item>
          <t-form-item label="备注">
            <t-textarea v-model="uploadForm.remark" :maxlength="200" autosize placeholder="请输入备注" />
          </t-form-item>
        </t-form>
      </div>
    </t-dialog>

    <t-dialog v-model:visible="previewDialogVisible" :header="previewTitle || '文件预览'" width="880px" :footer="false">
      <div class="max-h-[70vh] overflow-auto rounded-xl bg-[#f5f7fa] p-4">
        <img v-if="previewUrl.startsWith('data:image')" :src="previewUrl" class="mx-auto max-w-full rounded-lg" />
        <iframe v-else-if="previewUrl.startsWith('data:application/pdf')" :src="previewUrl" class="h-[65vh] w-full rounded-lg bg-white" />
        <pre v-else class="whitespace-pre-wrap break-words text-sm leading-7 text-[#1d2129]">{{ decodePreviewText(previewUrl) }}</pre>
      </div>
    </t-dialog>
  </PageContainer>
</template>
