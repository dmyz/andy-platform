<script setup lang="ts">
import { Minimize2, Square } from '@lucide/vue';

const isFullscreen = ref(false);

// 监听全屏状态变化
function handleFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement;
}

// 切换全屏
async function toggleFullscreen() {
  try {
    if (!document.fullscreenElement) {
      // 进入全屏
      await document.documentElement.requestFullscreen();
      isFullscreen.value = true;
    } else {
      // 退出全屏
      await document.exitFullscreen();
      isFullscreen.value = false;
    }
  } catch (error) {
    console.error('全屏切换失败:', error);
  }
}

// 监听全屏事件
onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange);
  // 初始化状态
  handleFullscreenChange();
});

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
});
</script>

<template>
  <div
    class="header-tool-button flex h-9 w-9 cursor-pointer items-center justify-center transition-colors"
    @click="toggleFullscreen"
  >
    <Square v-if="!isFullscreen" class="h-[18px] w-[18px] text-[#4e5969] dark:text-gray-300" />
    <Minimize2 v-else class="h-[18px] w-[18px] text-[#4e5969] dark:text-gray-300" />
  </div>
</template>

<style scoped>
.header-tool-button:hover {
  background: #f2f5f9;
}

.dark .header-tool-button:hover {
  background: rgba(255, 255, 255, 0.08);
}

.header-tool-button:focus-visible {
  outline: 2px solid rgba(0, 82, 217, 0.35);
  outline-offset: 2px;
}
</style>
