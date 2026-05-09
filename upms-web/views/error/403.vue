<script setup lang="ts">
import { ShieldAlert } from '@lucide/vue';
import { useRouter } from 'vue-router';

const router = useRouter();

function goDashboard() {
  router.push('/dashboard');
}
</script>

<template>
  <div class="flex min-h-[70vh] items-center justify-center px-6">
    <div
      class="max-w-xl rounded-[28px] border border-[#e7e7e7] bg-white px-10 py-12 text-center shadow-sm"
    >
      <div
        class="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-[#fff1f0] text-[#e34d59]"
      >
        <ShieldAlert class="h-10 w-10" />
      </div>
      <div class="mt-6 text-4xl font-semibold tracking-tight text-[#1d2129]">403</div>
      <div class="mt-3 text-lg font-medium text-[#1d2129]">当前账号无权访问此页面</div>
      <p class="mt-3 text-sm leading-7 text-[#86909c]">
        页面已被静态路由注册，但当前用户不具备对应权限码。请联系管理员开通访问权限，或返回工作台继续处理其他任务。
      </p>
      <div class="mt-8 flex justify-center">
        <a-button type="primary" @click="goDashboard"> 返回 </a-button>
      </div>
    </div>
  </div>
</template>
