<script setup lang="ts">
import { Edit, KeyRound, Mail, Phone, Shield, User } from '@lucide/vue';
import { message } from 'antdv-next';
import { computed, onMounted, reactive, ref } from 'vue';
import { useUserStore } from '@/stores/user';
import { changePassword } from '@/api/auth';
import {
  getProfile,
  getLoginAuditList,
  updateAvatar,
  sendMobileCode as sendProfileMobileCode,
  sendEmailCode as sendProfileEmailCode,
  changeMobile,
  changeEmail,
} from '@/api/profile';

interface ProfileInfo {
  id: string;
  username: string;
  realName: string;
  employeeNo?: string | null;
  mobile: string;
  email: string;
  gender?: string | null;
  avatar?: string | null;
  orgName?: string | null;
  positionName?: string | null;
  remark?: string | null;
}

interface LoginAuditItem {
  id: string;
  loginTime: string;
  loginType: string;
  ip: string;
  browser: string;
  result: string;
}

const PHONE_REGEX = /^1[3-9]\d{9}$/;
const EMAIL_REGEX = /^[\w.-]+@[a-z0-9.-]+\.[a-z]{2,6}$/i;

const userStore = useUserStore();

const genderLabelMap: Record<string, string> = {
  MALE: '男',
  FEMALE: '女',
  男: '男',
  女: '女',
};

const loading = ref(false);
const loginAuditLoading = ref(false);
const profile = ref<ProfileInfo | null>(null);
const activeMenu = ref('profile');

const menuItems = [
  { key: 'profile', label: '个人资料', icon: User },
  { key: 'security', label: '账号安全', icon: Shield },
  { key: 'audit', label: '登录记录', icon: KeyRound },
];

const loginAuditList = ref<LoginAuditItem[]>([]);
const loginAuditTotal = ref(0);
const loginAuditPagination = reactive({
  current: 1,
  pageSize: 10,
  showQuickJumper: true,
});

const avatarDialogVisible = ref(false);
const mobileDialogVisible = ref(false);
const emailDialogVisible = ref(false);

const avatarSubmitting = ref(false);
const passwordSubmitting = ref(false);
const mobileSubmitting = ref(false);
const emailSubmitting = ref(false);
const mobileCodeSending = ref(false);
const emailCodeSending = ref(false);

const avatarForm = reactive({
  avatarUrl: '',
});

const passwordFormRef = ref();
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const mobileFormRef = ref();
const mobileForm = reactive({
  code: '',
  newMobile: '',
});

const emailFormRef = ref();
const emailForm = reactive({
  code: '',
  newEmail: '',
});

const passwordFormRules = {
  oldPassword: [{ required: true, message: '当前密码必填', type: 'error' as const }],
  newPassword: [{ required: true, message: '新密码必填', type: 'error' as const }],
  confirmPassword: [{ required: true, message: '确认密码必填', type: 'error' as const }],
};

const mobileFormRules = {
  code: [{ required: true, message: '验证码必填', type: 'error' as const }],
  newMobile: [{ required: true, message: '新手机号必填', type: 'error' as const }],
};

const emailFormRules = {
  code: [{ required: true, message: '验证码必填', type: 'error' as const }],
  newEmail: [{ required: true, message: '新邮箱必填', type: 'error' as const }],
};

const loginAuditColumns = [
  { dataIndex: 'loginTime', title: '登录时间', width: 180 },
  { dataIndex: 'loginType', title: '登录方式', width: 120 },
  { dataIndex: 'ip', title: 'IP', width: 140 },
  { dataIndex: 'browser', title: '浏览器信息', ellipsis: true },
  { dataIndex: 'result', title: '登录结果', width: 100 },
];

const loginAuditTableScroll = { x: 760 };

const maskedMobile = computed(() => {
  if (!profile.value?.mobile) {
    return '-';
  }
  return profile.value.mobile.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
});

const maskedEmail = computed(() => {
  if (!profile.value?.email) {
    return '-';
  }
  const [name, domain] = profile.value.email.split('@');
  if (!name || !domain || name.length < 2) {
    return profile.value.email;
  }
  return `${name.slice(0, 2)}***@${domain}`;
});

const displayGender = computed(() => {
  const gender = profile.value?.gender;
  if (!gender) {
    return '-';
  }
  return genderLabelMap[gender] || gender;
});

const hasBoundMobile = computed(() => Boolean(profile.value?.mobile));
const hasBoundEmail = computed(() => Boolean(profile.value?.email));

async function fetchProfile() {
  loading.value = true;
  try {
    profile.value = await getProfile();
  } finally {
    loading.value = false;
  }
}

async function fetchLoginAudit() {
  loginAuditLoading.value = true;
  try {
    const res = await getLoginAuditList({
      pageNum: loginAuditPagination.current,
      pageSize: loginAuditPagination.pageSize,
    });
    loginAuditList.value = res.list;
    loginAuditTotal.value = res.total;
    loginAuditPagination.current = res.pageNum;
    loginAuditPagination.pageSize = res.pageSize;
  } finally {
    loginAuditLoading.value = false;
  }
}

function handleLoginAuditPageChange(pageInfo: { current?: number; pageSize?: number }) {
  loginAuditPagination.current = pageInfo.current ?? loginAuditPagination.current;
  loginAuditPagination.pageSize = pageInfo.pageSize ?? loginAuditPagination.pageSize;
  fetchLoginAudit();
}

function openAvatarDialog() {
  avatarForm.avatarUrl = profile.value?.avatar || '';
  avatarDialogVisible.value = true;
}

function openMobileDialog() {
  mobileForm.code = '';
  mobileForm.newMobile = '';
  mobileDialogVisible.value = true;
}

function openEmailDialog() {
  emailForm.code = '';
  emailForm.newEmail = '';
  emailDialogVisible.value = true;
}

async function handleUpdateAvatar() {
  if (!avatarForm.avatarUrl.trim()) {
    message.warning('请输入头像地址');
    return;
  }
  avatarSubmitting.value = true;
  try {
    await updateAvatar({ avatarUrl: avatarForm.avatarUrl.trim() });
    message.success('头像更新成功');
    avatarDialogVisible.value = false;
    await Promise.all([fetchProfile(), userStore.getUserInfo()]);
  } finally {
    avatarSubmitting.value = false;
  }
}

async function handleChangePassword() {
  try {
    await passwordFormRef.value?.validate();
  } catch {
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.warning('两次输入的新密码不一致');
    return;
  }
  passwordSubmitting.value = true;
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    });
    await userStore.getUserInfo();
    message.success('密码修改成功');
    Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' });
  } finally {
    passwordSubmitting.value = false;
  }
}

async function sendMobileCode() {
  if (!hasBoundMobile.value) {
    message.warning('当前账号未绑定手机号');
    return;
  }
  mobileCodeSending.value = true;
  try {
    await sendProfileMobileCode();
    message.success('验证码已发送到当前绑定手机号');
  } finally {
    mobileCodeSending.value = false;
  }
}

async function sendEmailCode() {
  if (!hasBoundEmail.value) {
    message.warning('当前账号未绑定邮箱');
    return;
  }
  emailCodeSending.value = true;
  try {
    await sendProfileEmailCode();
    message.success('验证码已发送到当前绑定邮箱');
  } finally {
    emailCodeSending.value = false;
  }
}

async function handleChangeMobile() {
  try {
    await mobileFormRef.value?.validate();
  } catch {
    return;
  }
  if (!PHONE_REGEX.test(mobileForm.newMobile)) {
    message.warning('请输入正确的手机号');
    return;
  }
  if (mobileForm.newMobile === profile.value?.mobile) {
    message.warning('新手机号不能与当前手机号相同');
    return;
  }
  mobileSubmitting.value = true;
  try {
    await changeMobile(mobileForm.code, mobileForm.newMobile);
    message.success('手机号修改成功');
    mobileDialogVisible.value = false;
    await Promise.all([fetchProfile(), userStore.getUserInfo()]);
  } finally {
    mobileSubmitting.value = false;
  }
}

async function handleChangeEmail() {
  try {
    await emailFormRef.value?.validate();
  } catch {
    return;
  }
  if (!EMAIL_REGEX.test(emailForm.newEmail)) {
    message.warning('请输入正确的邮箱');
    return;
  }
  if (emailForm.newEmail === profile.value?.email) {
    message.warning('新邮箱不能与当前邮箱相同');
    return;
  }
  emailSubmitting.value = true;
  try {
    await changeEmail(emailForm.code, emailForm.newEmail);
    message.success('邮箱修改成功');
    emailDialogVisible.value = false;
    await Promise.all([fetchProfile(), userStore.getUserInfo()]);
  } finally {
    emailSubmitting.value = false;
  }
}

onMounted(async () => {
  await Promise.all([fetchProfile(), fetchLoginAudit()]);
});
</script>

<template>
  <div>
    <div class="flex flex-col gap-6 xl:flex-row">
      <div class="w-full shrink-0 xl:w-56">
        <a-card variant="borderless" class="profile-menu-card shadow-sm">
          <div
            v-for="item in menuItems"
            :key="item.key"
            class="flex cursor-pointer items-center gap-3 rounded-md px-4 py-3 text-sm font-medium transition-colors"
            :class="
              activeMenu === item.key
                ? 'bg-[#0052d9] text-white'
                : 'text-[#4e5969] hover:bg-[#f5f5f5]'
            "
            @click="activeMenu = item.key"
          >
            <component :is="item.icon" class="h-5 w-5" />
            <span>{{ item.label }}</span>
          </div>
        </a-card>
      </div>

      <div class="min-w-0 flex-1 space-y-6">
        <div v-show="activeMenu === 'profile'" class="flex flex-col gap-6">
          <a-card variant="borderless" class="table-card shadow-sm">
            <h2 class="mb-6 text-lg font-medium text-[#1d2129]">头像设置</h2>
            <div class="flex flex-col gap-6 sm:flex-row sm:items-start">
              <div class="relative w-fit">
                <a-avatar :image="profile?.avatar || undefined" size="96px" class="shadow-sm">
                  <User class="h-10 w-10 text-[#86909c]" />
                </a-avatar>
                <button
                  type="button"
                  class="absolute -bottom-1 -right-1 flex h-9 w-9 items-center justify-center rounded-full bg-[#0052d9] text-white shadow-md transition hover:bg-[#0040b0]"
                  @click="openAvatarDialog"
                >
                  <Edit class="h-4 w-4" />
                </button>
              </div>
              <div class="flex-1 pt-1">
                <p class="mb-1 text-sm font-medium text-[#1d2129]">点击修改头像地址</p>
                <p class="text-xs text-[#86909c]">提交可访问的图片地址后会立即刷新用户头像。</p>
                <div class="mt-4">
                  <a-button type="primary" variant="outlined" @click="openAvatarDialog">
                    修改
                  </a-button>
                </div>
              </div>
            </div>
          </a-card>

          <a-card variant="borderless" class="shadow-sm">
            <div class="mb-6 flex flex-col gap-2">
              <h2 class="text-lg font-medium text-[#1d2129]">基本信息</h2>
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
          </a-card>
        </div>

        <div v-show="activeMenu === 'security'" class="flex flex-col gap-6">
          <a-card variant="borderless" class="shadow-sm">
            <h2 class="mb-6 text-lg font-medium text-[#1d2129]">登录密码</h2>
            <a-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordFormRules"
              :label-col="{ style: { width: '92px' } }"
            >
              <input
                class="visually-hidden"
                type="text"
                autocomplete="username"
                :value="profile?.username || ''"
                tabindex="-1"
                aria-hidden="true"
              />
              <a-form-item label="当前密码" name="oldPassword">
                <a-input
                  v-model:value="passwordForm.oldPassword"
                  type="password"
                  auto-complete="current-password"
                  placeholder="请输入当前密码"
                />
              </a-form-item>
              <a-form-item label="新密码" name="newPassword">
                <a-input
                  v-model:value="passwordForm.newPassword"
                  type="password"
                  auto-complete="new-password"
                  placeholder="请输入新密码"
                />
              </a-form-item>
              <a-form-item label="确认密码" name="confirmPassword">
                <a-input
                  v-model:value="passwordForm.confirmPassword"
                  type="password"
                  auto-complete="new-password"
                  placeholder="请再次输入新密码"
                />
              </a-form-item>
              <a-button
                v-auth="'profile:password:update'"
                type="primary"
                :loading="passwordSubmitting"
                @click="handleChangePassword"
              >
                保存
              </a-button>
            </a-form>
          </a-card>

          <div class="grid gap-6 lg:grid-cols-2">
            <a-card variant="borderless" class="shadow-sm">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <h2 class="text-lg font-medium text-[#1d2129]">手机绑定</h2>
                  <p class="mt-2 text-sm text-[#86909c]">当前绑定手机号：{{ maskedMobile }}</p>
                </div>
                <Phone class="h-5 w-5 text-[#0052d9]" />
              </div>
              <div class="mt-6">
                <a-button
                  v-auth="'profile:mobile:update'"
                  type="primary"
                  variant="outlined"
                  :disabled="!hasBoundMobile"
                  @click="openMobileDialog"
                >
                  修改
                </a-button>
              </div>
            </a-card>

            <a-card variant="borderless" class="shadow-sm">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <h2 class="text-lg font-medium text-[#1d2129]">邮箱绑定</h2>
                  <p class="mt-2 text-sm text-[#86909c]">当前绑定邮箱：{{ maskedEmail }}</p>
                </div>
                <Mail class="h-5 w-5 text-[#0052d9]" />
              </div>
              <div class="mt-6">
                <a-button
                  v-auth="'profile:email:update'"
                  type="primary"
                  variant="outlined"
                  :disabled="!hasBoundEmail"
                  @click="openEmailDialog"
                >
                  修改
                </a-button>
              </div>
            </a-card>
          </div>
        </div>

        <div v-show="activeMenu === 'audit'" class="flex flex-col gap-6">
          <a-card variant="borderless" class="shadow-sm">
            <div class="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h2 class="text-lg font-medium text-[#1d2129]">登录记录</h2>
                <p class="mt-1 text-sm text-[#86909c]">
                  展示最近登录时间、登录方式、IP、浏览器信息和登录结果。
                </p>
              </div>
              <div class="rounded-full bg-[#f5f8ff] px-4 py-2 text-sm font-medium text-[#0052d9]">
                共 {{ loginAuditTotal }} 条
              </div>
            </div>
            <a-table
              row-key="id"
              :data-source="loginAuditList"
              :columns="loginAuditColumns"
              :loading="loginAuditLoading"
              :pagination="{ ...loginAuditPagination, total: loginAuditTotal }"
              :scroll="loginAuditTableScroll"
              hover
              @change="handleLoginAuditPageChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'result'">
                  <a-tag
                    :color="record.result === 'SUCCESS' ? 'success' : 'danger'"
                    variant="filled"
                    size="small"
                  >
                    {{ record.result === 'SUCCESS' ? '成功' : '失败' }}
                  </a-tag>
                </template>
              </template>
            </a-table>
          </a-card>
        </div>
      </div>
    </div>
    <a-modal
      v-model:open="avatarDialogVisible"
      title="更新头像地址"
      width="560px"
      :confirm-loading="avatarSubmitting"
      @ok="handleUpdateAvatar"
    >
      <a-input v-model:value="avatarForm.avatarUrl" placeholder="请输入可访问的头像图片地址" />
    </a-modal>

    <a-modal
      v-model:open="mobileDialogVisible"
      title="修改手机号"
      width="560px"
      :confirm-loading="mobileSubmitting"
      @ok="handleChangeMobile"
    >
      <a-form
        ref="mobileFormRef"
        :model="mobileForm"
        :rules="mobileFormRules"
        :label-col="{ style: { width: '100px' } }"
      >
        <a-form-item label="当前手机号">
          <div class="flex w-full items-center justify-between gap-3">
            <a-input :value="maskedMobile" disabled />
            <a-button
              type="primary"
              variant="outlined"
              :loading="mobileCodeSending"
              :disabled="!hasBoundMobile"
              @click="sendMobileCode"
            >
              发送
            </a-button>
          </div>
        </a-form-item>
        <a-form-item label="验证码" name="code">
          <a-input v-model:value="mobileForm.code" placeholder="请输入验证码" />
        </a-form-item>
        <a-form-item label="新手机号" name="newMobile">
          <a-input v-model:value="mobileForm.newMobile" placeholder="请输入新手机号" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="emailDialogVisible"
      title="修改邮箱"
      width="560px"
      :confirm-loading="emailSubmitting"
      @ok="handleChangeEmail"
    >
      <a-form
        ref="emailFormRef"
        :model="emailForm"
        :rules="emailFormRules"
        :label-col="{ style: { width: '100px' } }"
      >
        <a-form-item label="当前邮箱">
          <div class="flex w-full items-center justify-between gap-3">
            <a-input :value="maskedEmail" disabled />
            <a-button
              type="primary"
              variant="outlined"
              :loading="emailCodeSending"
              :disabled="!hasBoundEmail"
              @click="sendEmailCode"
            >
              发送
            </a-button>
          </div>
        </a-form-item>
        <a-form-item label="验证码" name="code">
          <a-input v-model:value="emailForm.code" placeholder="请输入验证码" />
        </a-form-item>
        <a-form-item label="新邮箱" name="newEmail">
          <a-input v-model:value="emailForm.newEmail" placeholder="请输入新邮箱" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.profile-menu-card :deep(.ant-card-body) {
  padding: 8px;
}
</style>
