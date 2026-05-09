<script setup lang="ts">
interface Props {
  title?: string;
  description?: string;
}

const props = defineProps<Props>();
</script>

<template>
  <div
    class="h-full flex flex-col bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-100 dark:border-gray-700"
  >
    <div
      v-if="props.title"
      class="px-6 py-4 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center"
    >
      <div>
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100">
          {{ props.title }}
        </h2>
        <p v-if="props.description" class="text-sm text-gray-500 mt-1">
          {{ props.description }}
        </p>
      </div>
      <div class="flex items-center gap-3">
        <slot name="extra" />
      </div>
    </div>

    <div class="p-6 flex-1 overflow-auto custom-scrollbar">
      <slot />
    </div>
  </div>
</template>
