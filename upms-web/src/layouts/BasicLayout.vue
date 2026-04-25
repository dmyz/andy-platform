<script setup lang="ts">
import { useLayoutStore } from '@/stores/layout'
import Header from './components/Header.vue'
import Sidebar from './components/Sidebar.vue'

const layoutStore = useLayoutStore()
</script>

<template>
  <div class="h-screen flex flex-col bg-[#f0f2f5] dark:bg-gray-900 overflow-hidden">
    <Header
      :show-logo="true"
      :show-sidebar-toggle="true"
    />

    <div class="flex flex-1 overflow-hidden">
      <Sidebar
        :collapsed="layoutStore.isSidebarCollapsed"
        :show-logo="false"
      />

      <main
        class="flex-1 overflow-y-auto custom-scrollbar transition-all duration-300"
      >
        <div class="p-6">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>
