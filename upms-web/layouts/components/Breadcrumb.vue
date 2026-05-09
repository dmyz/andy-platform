<script setup lang="ts">
import { ChevronRight } from '@lucide/vue';

const route = useRoute();

const breadcrumbList = computed(() => {
  const matched = route.matched.filter((item) => item.meta?.title);
  return matched.map((item) => ({
    title: (item.meta?.title as string) || (item.name as string),
    path: item.path,
  }));
});
</script>

<template>
  <div class="flex items-center gap-1 text-sm">
    <router-link to="/" class="text-[#86909c] hover:text-[#0052d9]"> 首页 </router-link>
    <template v-for="(item, index) in breadcrumbList" :key="index">
      <ChevronRight class="w-4 h-4 text-[#86909c]" />
      <router-link
        :to="item.path"
        class="text-[#86909c] hover:text-[#0052d9]"
        :class="{ 'text-[#1d2129]': index === breadcrumbList.length - 1 }"
      >
        {{ item.title }}
      </router-link>
    </template>
  </div>
</template>
