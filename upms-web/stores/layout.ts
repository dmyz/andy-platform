/**
 * 布局状态管理
 * 管理侧边栏折叠状态等布局相关状态
 */
export const useLayoutStore = defineStore(
  'layout',
  () => {
    // 侧边栏折叠状态
    const isSidebarCollapsed = ref(false);

    /**
     * 切换侧边栏折叠状态
     */
    const toggleSidebar = () => {
      isSidebarCollapsed.value = !isSidebarCollapsed.value;
    };

    /**
     * 设置侧边栏折叠状态
     * @param collapsed 折叠状态
     */
    const setSidebarCollapsed = (collapsed: boolean) => {
      isSidebarCollapsed.value = collapsed;
    };

    return {
      isSidebarCollapsed,
      toggleSidebar,
      setSidebarCollapsed,
    };
  },
  {
    persist: {
      key: 'layout_settings',
      storage: localStorage,
      pick: ['isSidebarCollapsed'],
    },
  },
);
