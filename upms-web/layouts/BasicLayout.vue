<script setup lang="ts">
import { useLayoutStore } from '@/stores/layout';
import Header from './components/Header.vue';
import Sidebar from './components/Sidebar.vue';

const layoutStore = useLayoutStore();

const isNarrowScreen = ref(false);
let narrowScreenQuery: MediaQueryList | undefined;

function syncNarrowScreen() {
  isNarrowScreen.value = narrowScreenQuery?.matches ?? false;
}

onMounted(() => {
  if (!window.matchMedia) {
    return;
  }
  narrowScreenQuery = window.matchMedia('(max-width: 767px)');
  syncNarrowScreen();
  narrowScreenQuery.addEventListener('change', syncNarrowScreen);
});

onBeforeUnmount(() => {
  narrowScreenQuery?.removeEventListener('change', syncNarrowScreen);
});

const sidebarCollapsed = computed(() => isNarrowScreen.value || layoutStore.isSidebarCollapsed);
</script>

<template>
  <div class="app-shell h-screen flex flex-col overflow-hidden text-[#1d2129] dark:text-gray-100">
    <Header :show-logo="true" :show-sidebar-toggle="true" />

    <div class="flex min-h-0 flex-1 overflow-hidden">
      <Sidebar :collapsed="sidebarCollapsed" :show-logo="false" />

      <main class="app-main custom-scrollbar min-w-0 flex-1 overflow-y-auto">
        <div class="mx-auto w-full max-w-[1680px] px-4 py-4 md:px-6 md:py-5">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  background: #f3f5f8;
}

.app-main {
  background: #f6f8fb;
}

.dark .app-shell {
  background: #0f172a;
}

.dark .app-main {
  background: #111827;
}
</style>
