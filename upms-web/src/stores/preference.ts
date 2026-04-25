/**
 * 主题偏好存储
 */
export const usePreferenceStore = defineStore('preference', () => {
  // 主题状态：'light' | 'dark'，默认从系统获取
  const theme = ref<'light' | 'dark'>('light')

  // 获取系统主题偏好
  const getSystemTheme = (): 'light' | 'dark' => {
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return 'dark'
    }
    return 'light'
  }

  /**
   * 应用主题到 DOM
   * 同时应用Tailwind的dark类和TDesign的主题属性
   */
  const applyTheme = () => {
    const html = document.documentElement

    if (theme.value === 'dark') {
      // Tailwind暗色模式
      html.classList.add('dark')
      // TDesign暗色主题
      html.setAttribute('theme-mode', 'dark')
    }
    else {
      // Tailwind亮色模式
      html.classList.remove('dark')
      // TDesign亮色主题
      html.removeAttribute('theme-mode')
    }
  }

  /**
   * 设置主题
   * @param newTheme 主题模式
   */
  const setTheme = (newTheme: 'light' | 'dark') => {
    theme.value = newTheme
    applyTheme()
  }

  /**
   * 切换主题（light <-> dark）
   */
  const toggleTheme = () => {
    const newTheme = theme.value === 'dark' ? 'light' : 'dark'
    setTheme(newTheme)
  }

  /**
   * 初始化主题（首次加载时根据系统偏好设置）
   */
  const initTheme = () => {
    // 如果本地没有保存的主题设置，则使用系统偏好
    const savedTheme = localStorage.getItem('theme_settings')
    if (!savedTheme) {
      theme.value = getSystemTheme()
    }
    applyTheme()

    // 监听系统主题变化
    if (window.matchMedia) {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      mediaQuery.addEventListener('change', (e) => {
        // 只有在用户没有手动设置过主题时，才跟随系统变化
        const savedTheme = localStorage.getItem('theme_settings')
        if (!savedTheme) {
          theme.value = e.matches ? 'dark' : 'light'
          applyTheme()
        }
      })
    }
  }

  // 监听主题变化
  watch(() => theme.value, applyTheme)

  return {
    theme,
    setTheme,
    toggleTheme,
    initTheme,
  }
}, {
  persist: {
    key: 'theme_settings',
    storage: localStorage,
    pick: ['theme'],
  },
})
