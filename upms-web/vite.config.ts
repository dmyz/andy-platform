import path from 'node:path'
import { cwd } from 'node:process'

import tailwindcss from '@tailwindcss/vite'
import { AntdvNextResolver } from '@antdv-next/auto-import-resolver'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { defineConfig, loadEnv } from 'vite'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, cwd(), '')

  const plugins = [
    vue(),
    tailwindcss(),
    vueDevTools(),
    AutoImport({
      imports: [
        'vue',
        'vue-router',
        'pinia',
      ],
      dts: true,
    }),
    Components({
      dirs: ['src/components'],
      extensions: ['ui/**', 'layout/**'],
      dts: true,
      resolvers: [
        AntdvNextResolver(),
      ],
    })
  ]

  return {
    plugins,
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      fs: {
        // 仅在开发环境禁用严格模式
        strict: env.NODE_ENV === 'production',
      },
      proxy: {
        '/api': {
          target: env.VITE_API_TARGET,
          changeOrigin: true,
          rewrite: path => path.replace('/api', ''),
        },
      },
    },
    build: {},
  }
})
