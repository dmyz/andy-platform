<script setup lang="ts">
import { Bell, Edit, KeyRound, Mail, Phone, Shield, User } from '@lucide/vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import { useUserStore } from '@/stores/user'
import { get, post } from '@/utils/request'

interface ProfileInfo {
  id: string
  username: string
  realName: string
  employeeNo?: string | null
  mobile: string
  email: string
  gender?: string | null
  avatar?: string | null
  orgName?: string | null
  positionName?: string | null
  remark?: string | null
}

interface LoginAuditItem {
  id: string
  loginTime: string
  loginType: string
  ip: string
  browser: string
  result: string
}

interface MessageItem {
  id: string
  title: string
  type: string
  publishTime: string
  read: boolean
  content: string
}

interface PageResult<T> {
  list: T[]
  total: number
}

const PHONE_REGEX = /^1[3-9]\d{9}$/
const EMAIL_REGEX = /^[\w.-]+@[a-z0-9.-]+\.[a-z]{2,6}$/i

const userStore = useUserStore()

const genderLabelMap: Record<string, string> = {
  MALE: '男',
  FEMALE: '女',
  男: '男',
  女: '女',
}

const loading = ref(false)
const profile = ref<ProfileInfo | null>(null)
const activeMenu = ref('profile')

const menuItems = [
  { key: 'profile', label: '个人资料', icon: User },
  { key: 'security', label: '账号安全', icon: Shield },
  { key: 'audit', label: '登录记录', icon: KeyRound },
  { key: 'messages', label: '我的消息', icon: Bell },
]

const loginAuditList = ref<LoginAuditItem[]>([])
const messageList = ref<MessageItem[]>([])
const loginAuditTotal = ref(0)
const messageTotal = ref(0)

const avatarDialogVisible = ref(false)
const mobileDialogVisible = ref(false)
const emailDialogVisible = ref(false)

const avatarSubmitting = ref(false)
const passwordSubmitting = ref(false)
const mobileSubmitting = ref(false)
const emailSubmitting = ref(false)
const mobileCodeSending = ref(false)
const emailCodeSending = ref(false)

const avatarForm = reactive({
  avatarUrl: '',
})

const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const mobileFormRef = ref()
const mobileForm = reactive({
  code: '',
  newMobile: '',
})

const emailFormRef = ref()
const emailForm = reactive({
  code: '',
  newEmail: '',
})

const passwordFormRules = {
  oldPassword: [{ required: true, message: '当前密码必填', type: 'error' as const }],
  newPassword: [{ required: true, message: '新密码必填', type: 'error' as const }],
  confirmPassword: [{ required: true, message: '确认密码必填', type: 'error' as const }],
}

const mobileFormRules = {
  code: [{ required: true, message: '验证码必填', type: 'error' as const }],
  newMobile: [{ required: true, message: '新手机号必填', type: 'error' as const }],
}

const emailFormRules = {
  code: [{ required: true, message: '验证码必填', type: 'error' as const }],
  newEmail: [{ required: true, message: '新邮箱必填', type: 'error' as const }],
}

const loginAuditColumns = [
  { colKey: 'loginTime', title: '登录时间', width: 180 },
  { colKey: 'loginType', title: '登录方式', width: 120 },
  { colKey: 'ip', title: 'IP', width: 140 },
  { colKey: 'browser', title: '浏览器信息', ellipsis: true },
  { colKey: 'result', title: '登录结果', width: 100 },
]

const messageColumns = [
  { colKey: 'title', title: '标题', ellipsis: true },
  { colKey: 'type', title: '类型', width: 120 },
  { colKey: 'publishTime', title: '发布时间', width: 180 },
  { colKey: 'read', title: '已读状态', width: 100 },
  { colKey: 'content', title: '内容摘要', ellipsis: true },
]

const maskedMobile = computed(() => {
  if (!profile.value?.mobile) {
    return '-'
  }
  return profile.value.mobile.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
})

const maskedEmail = computed(() => {
  if (!profile.value?.email) {
    return '-'
  }
  const [name, domain] = profile.value.email.split('@')
  if (!name || !domain || name.length < 2) {
    return profile.value.email
  }
  return `${name.slice(0, 2)}***@${domain}`
})

const displayGender = computed(() => {
  const gender = profile.value?.gender
  if (!gender) {
    return '-'
  }
  return genderLabelMap[gender] || gender
})

const hasBoundMobile = computed(() => Boolean(profile.value?.mobile))
const hasBoundEmail = computed(() => Boolean(profile.value?.email))

async function fetchProfile() {
  loading.value = true
  try {
    profile.value = await get<ProfileInfo>('/profile/me')
  }
  finally {
    loading.value = false
  }
}

async function fetchLoginAudit() {
  const res = await get<PageResult<LoginAuditItem>>('/profile/login-audit/page', { pageNum: 1, pageSize: 10 })
  loginAuditList.value = res.list
  loginAuditTotal.value = res.total
}

async function fetchMessages() {
  const res = await get<PageResult<MessageItem>>('/profile/messages/page', { pageNum: 1, pageSize: 10 })
  messageList.value = res.list
  messageTotal.value = res.total
}

function openAvatarDialog() {
  avatarForm.avatarUrl = profile.value?.avatar || ''
  avatarDialogVisible.value = true
}

function openMobileDialog() {
  mobileForm.code = ''
  mobileForm.newMobile = ''
  mobileDialogVisible.value = true
}

function openEmailDialog() {
  emailForm.code = ''
  emailForm.newEmail = ''
  emailDialogVisible.value = true
}

async function handleUpdateAvatar() {
  if (!avatarForm.avatarUrl.trim()) {
    MessagePlugin.warning('请输入头像地址')
    return
  }
  avatarSubmitting.value = true
  try {
    await post('/profile/avatar', { avatarUrl: avatarForm.avatarUrl.trim() })
    MessagePlugin.success('头像更新成功')
    avatarDialogVisible.value = false
    await Promise.all([fetchProfile(), userStore.getUserInfo()])
  }
  finally {
    avatarSubmitting.value = false
  }
}

async function handleChangePassword() {
  const validateResult = await passwordFormRef.value?.validate()
  if (validateResult !== true) {
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    MessagePlugin.warning('两次输入的新密码不一致')
    return
  }
  passwordSubmitting.value = true
  try {
    await post('/auth/password/change', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    await userStore.getUserInfo()
    MessagePlugin.success('密码修改成功')
    Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  }
  finally {
    passwordSubmitting.value = false
  }
}

async function sendMobileCode() {
  if (!hasBoundMobile.value) {
    MessagePlugin.warning('当前账号未绑定手机号')
    return
  }
  mobileCodeSending.value = true
  try {
    await post('/profile/mobile/code/send')
    MessagePlugin.success('验证码已发送到当前绑定手机号')
  }
  finally {
    mobileCodeSending.value = false
  }
}

async function sendEmailCode() {
  if (!hasBoundEmail.value) {
    MessagePlugin.warning('当前账号未绑定邮箱')
    return
  }
  emailCodeSending.value = true
  try {
    await post('/profile/email/code/send')
    MessagePlugin.success('验证码已发送到当前绑定邮箱')
  }
  finally {
    emailCodeSending.value = false
  }
}

async function handleChangeMobile() {
  const validateResult = await mobileFormRef.value?.validate()
  if (validateResult !== true) {
    return
  }
  if (!PHONE_REGEX.test(mobileForm.newMobile)) {
    MessagePlugin.warning('请输入正确的手机号')
    return
  }
  if (mobileForm.newMobile === profile.value?.mobile) {
    MessagePlugin.warning('新手机号不能与当前手机号相同')
    return
  }
  mobileSubmitting.value = true
  try {
    await post('/profile/mobile/change', {
      code: mobileForm.code,
      newMobile: mobileForm.newMobile,
    })
    MessagePlugin.success('手机号修改成功')
    mobileDialogVisible.value = false
    await Promise.all([fetchProfile(), userStore.getUserInfo()])
  }
  finally {
    mobileSubmitting.value = false
  }
}

async function handleChangeEmail() {
  const validateResult = await emailFormRef.value?.validate()
  if (validateResult !== true) {
    return
  }
  if (!EMAIL_REGEX.test(emailForm.newEmail)) {
    MessagePlugin.warning('请输入正确的邮箱')
    return
  }
  if (emailForm.newEmail === profile.value?.email) {
    MessagePlugin.warning('新邮箱不能与当前邮箱相同')
    return
  }
  emailSubmitting.value = true
  try {
    await post('/profile/email/change', {
      code: emailForm.code,
      newEmail: emailForm.newEmail,
    })
    MessagePlugin.success('邮箱修改成功')
    emailDialogVisible.value = false
    await Promise.all([fetchProfile(), userStore.getUserInfo()])
  }
  finally {
    emailSubmitting.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchProfile(), fetchLoginAudit(), fetchMessages()])
})
</script>

<template>
  <PageContainer title="个人中心" description="查看个人资料、维护账号安全，并查看登录记录和站内消息。">
    <div class="flex flex-col gap-6 xl:flex-row">
      <div class="w-full shrink-0 xl:w-56">
        <div class="rounded-lg border border-[#e7e7e7] bg-white p-2 shadow-sm">
          <div
            v-for="item in menuItems"
            :key="item.key"
            class="flex cursor-pointer items-center gap-3 rounded-md px-4 py-3 text-sm font-medium transition-colors"
            :class="activeMenu === item.key ? 'bg-[#0052d9] text-white' : 'text-[#4e5969] hover:bg-[#f5f5f5]'"
            @click="activeMenu = item.key"
          >
            <component :is="item.icon" class="h-5 w-5" />
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>

      <div class="min-w-0 flex-1 space-y-6">
        <div v-show="activeMenu === 'profile'" class="space-y-6">
          <div class="rounded-lg border border-[#e7e7e7] bg-white p-6 shadow-sm">
            <h2 class="mb-6 text-lg font-medium text-[#1d2129]">
              头像设置
            </h2>
            <div class="flex flex-col gap-6 sm:flex-row sm:items-start">
              <div class="relative w-fit">
                <t-avatar :image="profile?.avatar || undefined" size="96px" class="shadow-sm">
                  <User class="h-10 w-10 text-[#86909c]" />
                </t-avatar>
                <button
                  type="button"
                  class="absolute -bottom-1 -right-1 flex h-9 w-9 items-center justify-center rounded-full bg-[#0052d9] text-white shadow-md transition hover:bg-[#0040b0]"
                  @click="openAvatarDialog"
                >
                  <Edit class="h-4 w-4" />
                </button>
              </div>
              <div class="flex-1 pt-1">
                <p class="mb-1 text-sm font-medium text-[#1d2129]">
                  点击修改头像地址
                </p>
                <p class="text-xs text-[#86909c]">
                  当前版本对接 `/profile/avatar` 接口，提交可访问的图片地址后会立即刷新用户头像。
                </p>
                <div class="mt-4">
                  <t-button theme="primary" variant="outline" @click="openAvatarDialog">
                    修改头像
                  </t-button>
                </div>
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-[#e7e7e7] bg-white p-6 shadow-sm">
            <div class="mb-6 flex flex-col gap-2">
              <h2 class="text-lg font-medium text-[#1d2129]">
                基本信息
              </h2>
              <p class="text-sm text-[#86909c]">
                个人资料仅展示当前信息，不支持在个人中心直接编辑；如需更新，请联系管理员维护基础档案。
              </p>
            </div>
            <div class="grid gap-6 md:grid-cols-2">
              <div>
                <label class="mb-2 flex items-center gap-2 text-sm text-[#86909c]">
                  <User class="h-4 w-4" />
                  真实姓名
                </label>
                <p class="text-sm text-[#1d2129]">{{ profile?.realName || '-' }}</p>
              </div>
              <div>
                <label class="mb-2 flex items-center gap-2 text-sm text-[#86909c]">
                  <User class="h-4 w-4" />
                  用户名
                </label>
                <p class="text-sm text-[#1d2129]">{{ profile?.username || '-' }}</p>
              </div>
              <div>
                <label class="mb-2 flex items-center gap-2 text-sm text-[#86909c]">
                  <KeyRound class="h-4 w-4" />
                  工号
                </label>
                <p class="text-sm text-[#1d2129]">{{ profile?.employeeNo || '-' }}</p>
              </div>
              <div>
                <label class="mb-2 flex items-center gap-2 text-sm text-[#86909c]">
                  <Phone class="h-4 w-4" />
                  手机号
                </label>
                <p class="text-sm text-[#1d2129]">{{ maskedMobile }}</p>
              </div>
              <div>
                <label class="mb-2 flex items-center gap-2 text-sm text-[#86909c]">
                  <Mail class="h-4 w-4" />
                  邮箱
                </label>
                <p class="text-sm text-[#1d2129]">{{ maskedEmail }}</p>
              </div>
              <div>
                <label class="mb-2 flex items-center gap-2 text-sm text-[#86909c]">
                  <Shield class="h-4 w-4" />
                  性别
                </label>
                <p class="text-sm text-[#1d2129]">{{ displayGender }}</p>
              </div>
              <div>
                <label class="mb-2 flex items-center gap-2 text-sm text-[#86909c]">
                  <Shield class="h-4 w-4" />
                  所属组织
                </label>
                <p class="text-sm text-[#1d2129]">{{ profile?.orgName || '-' }}</p>
              </div>
              <div>
                <label class="mb-2 flex items-center gap-2 text-sm text-[#86909c]">
                  <Shield class="h-4 w-4" />
                  岗位
                </label>
                <p class="text-sm text-[#1d2129]">{{ profile?.positionName || '-' }}</p>
              </div>
              <div class="md:col-span-2">
                <label class="mb-2 flex items-center gap-2 text-sm text-[#86909c]">
                  <Edit class="h-4 w-4" />
                  备注
                </label>
                <p class="text-sm leading-7 text-[#1d2129]">{{ profile?.remark || '-' }}</p>
              </div>
            </div>
          </div>
        </div>

        <div v-show="activeMenu === 'security'" class="space-y-6">
          <div class="rounded-lg border border-[#e7e7e7] bg-white p-6 shadow-sm">
            <h2 class="mb-6 text-lg font-medium text-[#1d2129]">
              登录密码
            </h2>
            <t-form ref="passwordFormRef" :data="passwordForm" :rules="passwordFormRules" label-width="92px">
              <t-form-item label="当前密码" name="oldPassword">
                <t-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
              </t-form-item>
              <t-form-item label="新密码" name="newPassword">
                <t-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
              </t-form-item>
              <t-form-item label="确认密码" name="confirmPassword">
                <t-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
              </t-form-item>
              <t-button v-auth="'profile:password:update'" theme="primary" :loading="passwordSubmitting" @click="handleChangePassword">
                保存密码
              </t-button>
            </t-form>
          </div>

          <div class="grid gap-6 lg:grid-cols-2">
            <div class="rounded-lg border border-[#e7e7e7] bg-white p-6 shadow-sm">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <h2 class="text-lg font-medium text-[#1d2129]">
                    手机绑定
                  </h2>
                  <p class="mt-2 text-sm text-[#86909c]">
                    当前绑定手机号：{{ maskedMobile }}
                  </p>
                </div>
                <Phone class="h-5 w-5 text-[#0052d9]" />
              </div>
              <div class="mt-6">
                <t-button v-auth="'profile:mobile:update'" theme="primary" variant="outline" :disabled="!hasBoundMobile" @click="openMobileDialog">
                  修改手机号
                </t-button>
              </div>
            </div>

            <div class="rounded-lg border border-[#e7e7e7] bg-white p-6 shadow-sm">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <h2 class="text-lg font-medium text-[#1d2129]">
                    邮箱绑定
                  </h2>
                  <p class="mt-2 text-sm text-[#86909c]">
                    当前绑定邮箱：{{ maskedEmail }}
                  </p>
                </div>
                <Mail class="h-5 w-5 text-[#0052d9]" />
              </div>
              <div class="mt-6">
                <t-button v-auth="'profile:email:update'" theme="primary" variant="outline" :disabled="!hasBoundEmail" @click="openEmailDialog">
                  修改邮箱
                </t-button>
              </div>
            </div>
          </div>
        </div>

        <div v-show="activeMenu === 'audit'" class="space-y-6">
          <div class="rounded-lg border border-[#e7e7e7] bg-white p-6 shadow-sm">
            <div class="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h2 class="text-lg font-medium text-[#1d2129]">
                  登录记录
                </h2>
                <p class="mt-1 text-sm text-[#86909c]">
                  展示最近登录时间、登录方式、IP、浏览器信息和登录结果。
                </p>
              </div>
              <div class="rounded-full bg-[#f5f8ff] px-4 py-2 text-sm font-medium text-[#0052d9]">
                共 {{ loginAuditTotal }} 条
              </div>
            </div>
            <t-table row-key="id" :data="loginAuditList" :columns="loginAuditColumns" :loading="loading" :pagination="{ disabled: true }" hover>
              <template #result="{ row }">
                <t-tag :theme="row.result === 'SUCCESS' ? 'success' : 'danger'" variant="light" size="small">
                  {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
                </t-tag>
              </template>
            </t-table>
          </div>
        </div>

        <div v-show="activeMenu === 'messages'" class="space-y-6">
          <div class="rounded-lg border border-[#e7e7e7] bg-white p-6 shadow-sm">
            <div class="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <div class="flex items-center gap-2 text-lg font-medium text-[#1d2129]">
                  <Bell class="h-4 w-4 text-[#ed7b2f]" />
                  我的消息
                </div>
                <p class="mt-1 text-sm text-[#86909c]">
                  展示标题、类型、发布时间、已读状态和消息摘要。
                </p>
              </div>
              <div class="rounded-full bg-[#fff7e8] px-4 py-2 text-sm font-medium text-[#ed7b2f]">
                共 {{ messageTotal }} 条
              </div>
            </div>
            <t-table row-key="id" :data="messageList" :columns="messageColumns" :loading="loading" :pagination="{ disabled: true }" hover>
              <template #type="{ row }">
                <t-tag theme="primary" variant="light" size="small">
                  {{ row.type }}
                </t-tag>
              </template>
              <template #read="{ row }">
                <t-tag :theme="row.read ? 'success' : 'warning'" variant="light" size="small">
                  {{ row.read ? '已读' : '未读' }}
                </t-tag>
              </template>
              <template #content="{ row }">
                <div class="line-clamp-2 text-sm text-[#4e5969]">
                  {{ row.content }}
                </div>
              </template>
            </t-table>
          </div>
        </div>
      </div>
    </div>
    <t-dialog v-model:visible="avatarDialogVisible" header="更新头像地址" width="560px" :on-confirm="handleUpdateAvatar" :confirm-btn="{ loading: avatarSubmitting }">
      <t-input v-model="avatarForm.avatarUrl" placeholder="请输入可访问的头像图片地址" />
    </t-dialog>

    <t-dialog v-model:visible="mobileDialogVisible" header="修改手机号" width="560px" :on-confirm="handleChangeMobile" :confirm-btn="{ loading: mobileSubmitting }">
      <t-form ref="mobileFormRef" :data="mobileForm" :rules="mobileFormRules" label-width="100px">
        <t-form-item label="当前手机号">
          <div class="flex w-full items-center justify-between gap-3">
            <t-input :value="maskedMobile" disabled />
            <t-button theme="primary" variant="outline" :loading="mobileCodeSending" :disabled="!hasBoundMobile" @click="sendMobileCode">
              发送验证码
            </t-button>
          </div>
        </t-form-item>
        <t-form-item label="验证码" name="code">
          <t-input v-model="mobileForm.code" placeholder="请输入验证码" />
        </t-form-item>
        <t-form-item label="新手机号" name="newMobile">
          <t-input v-model="mobileForm.newMobile" placeholder="请输入新手机号" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog v-model:visible="emailDialogVisible" header="修改邮箱" width="560px" :on-confirm="handleChangeEmail" :confirm-btn="{ loading: emailSubmitting }">
      <t-form ref="emailFormRef" :data="emailForm" :rules="emailFormRules" label-width="100px">
        <t-form-item label="当前邮箱">
          <div class="flex w-full items-center justify-between gap-3">
            <t-input :value="maskedEmail" disabled />
            <t-button theme="primary" variant="outline" :loading="emailCodeSending" :disabled="!hasBoundEmail" @click="sendEmailCode">
              发送验证码
            </t-button>
          </div>
        </t-form-item>
        <t-form-item label="验证码" name="code">
          <t-input v-model="emailForm.code" placeholder="请输入验证码" />
        </t-form-item>
        <t-form-item label="新邮箱" name="newEmail">
          <t-input v-model="emailForm.newEmail" placeholder="请输入新邮箱" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </PageContainer>
</template>
