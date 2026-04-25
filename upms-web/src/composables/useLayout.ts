import { ref } from 'vue'

export type LayoutType = 'basic' | 'mixed' | 'topnav' | 'blank'

const currentLayout = ref<LayoutType>('basic')

export function useLayout() {
  const setLayout = (layout: LayoutType) => {
    currentLayout.value = layout
  }

  const getLayout = () => currentLayout.value

  return {
    currentLayout,
    setLayout,
    getLayout,
  }
}
