import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import AntdvNext from 'antdv-next';

import { createApp } from 'vue';
import App from './App.vue';
import { setupAuthDirective } from './directives/auth';
import router from './router';
import { usePreferenceStore } from './stores/preference';

import 'antdv-next/dist/reset.css';

const app = createApp(App);
const pinia = createPinia();

// 先注册持久化插件
pinia.use(piniaPluginPersistedstate);
app.use(pinia);

// 然后初始化主题（在 pinia 插件注册之后）
usePreferenceStore().initTheme();

// 注册 Antdv Next 组件库
app.use(AntdvNext);

// 注册权限指令
setupAuthDirective(app);

// 注册路由
app.use(router);

app.mount('#app');
