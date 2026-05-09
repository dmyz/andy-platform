<script setup lang="ts">
import type { ConfigProviderProps, TableProps, TourStepItem, UploadFile } from 'antdv-next';

import zhCN from 'antdv-next/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import { useRoute } from 'vue-router';

const route = useRoute();

const configProvider = reactive<ConfigProviderProps>({
  locale: zhCN,
  componentSize: 'middle',
  theme: {},
});

const locale = ref<ConfigProviderProps['locale']>(zhCN);
const isAuthRoute = computed(() =>
  ['/login', '/forgot-password', '/first-time-password'].includes(route.path),
);

watchEffect(() => {
  document.body.classList.toggle('auth-route', isAuthRoute.value);
});

dayjs.locale('zh-cn');
</script>

<template>
  <a-config-provider :locale="configProvider.locale" :componentSize="configProvider.componentSize">
    <a-app>
      <RouterView />
    </a-app>
  </a-config-provider>
</template>

<style>
.table-card {
  min-width: 0;
  overflow: hidden;
}

.table-card .ant-card-body,
.table-card .ant-table-wrapper {
  min-width: 0;
  overflow: hidden;
}

.table-card .ant-table-cell {
  word-break: break-word;
}

.filter-actions,
.table-toolbar__actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-layout {
  align-items: flex-start;
}

.filter-layout > .ant-col:not(.filter-actions-col) {
  flex: 1 1 0 !important;
  max-width: calc(100% - 120px) !important;
}

.filter-actions-col {
  display: flex;
  flex: 0 0 104px !important;
  min-width: 104px;
  max-width: 104px !important;
  justify-content: flex-end;
}

body:not(.auth-route) .filter-layout .filter-actions {
  flex-direction: column;
  flex-wrap: nowrap !important;
  align-items: stretch;
  white-space: nowrap;
}

@media (min-width: 1600px) {
  .filter-layout > .ant-col:not(.filter-actions-col) {
    max-width: calc(100% - 236px) !important;
  }

  .filter-actions-col {
    flex-basis: 220px !important;
    min-width: 220px;
    max-width: 220px !important;
  }

  body:not(.auth-route) .filter-layout .filter-actions {
    flex-direction: row;
    align-items: center;
  }
}

.filter-form-grid .ant-form-item {
  margin-right: 0;
  margin-bottom: 0;
}

.filter-form-grid .filter-control {
  width: 220px !important;
  max-width: 100%;
}

body:not(.auth-route) .ant-form-inline {
  column-gap: 20px;
  row-gap: 14px;
}

body:not(.auth-route) .ant-form-inline .ant-form-item:has(.filter-actions) {
  margin-left: auto;
  margin-right: 0;
}

body:not(.auth-route)
  .ant-form-inline
  .ant-form-item:has(.filter-actions)
  .ant-form-item-control-input-content {
  display: flex;
  justify-content: flex-end;
}

body:not(.auth-route) .ant-form-inline .filter-actions {
  justify-content: flex-end;
}

.table-toolbar__actions--right {
  justify-content: flex-end;
  margin-left: auto;
}

body:not(.auth-route) .ant-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  white-space: nowrap;
}

body:not(.auth-route) .ant-btn-sm {
  gap: 4px;
}

body:not(.auth-route) .table-toolbar .ant-btn,
body:not(.auth-route) .filter-actions .ant-btn {
  min-width: 72px;
}

.row-actions {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

body:not(.auth-route) .row-actions .ant-btn {
  min-width: 28px;
  padding-inline: 6px;
}

body:not(.auth-route) .row-actions .ant-btn-sm:not([shape='square']) {
  width: auto !important;
  min-width: 44px !important;
}

body:not(.auth-route) .row-actions .ant-btn[shape='square'],
body:not(.auth-route) .row-actions .ant-btn-square,
body:not(.auth-route) .row-actions .ant-btn-icon-only {
  width: 28px;
  min-width: 28px;
  padding-inline: 0;
}

@media (max-width: 767px) {
  .ant-card,
  .ant-card-body,
  .ant-form,
  .ant-form-item,
  .ant-form-item-row,
  .ant-form-item-control,
  .ant-form-item-control-input,
  .ant-form-item-control-input-content {
    min-width: 0;
  }

  .ant-form-inline {
    align-items: stretch;
  }

  .ant-form-inline .ant-form-item {
    display: flex;
    width: 100%;
    margin-right: 0 !important;
  }

  body:not(.auth-route) .ant-form-inline .ant-form-item:has(.filter-actions) {
    margin-left: 0;
  }

  .ant-form-inline .ant-form-item-row {
    width: 100%;
  }

  .ant-form-inline .ant-form-item-control {
    flex: 1;
  }

  .ant-form-inline .ant-input-affix-wrapper,
  .ant-form-inline .ant-select,
  .ant-form-inline .ant-picker,
  .ant-form-inline .filter-control {
    width: 100% !important;
    max-width: 100%;
  }

  body:not(.auth-route) .ant-form-inline .filter-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .filter-actions-col {
    flex: 0 0 100% !important;
    max-width: 100% !important;
    justify-content: flex-start;
  }

  .filter-layout > .ant-col:not(.filter-actions-col) {
    flex: 0 0 100% !important;
    max-width: 100% !important;
  }

  .filter-form-grid .filter-control {
    width: 100% !important;
  }

  .filter-actions,
  .row-actions,
  .table-toolbar,
  .table-toolbar__actions {
    flex-wrap: wrap;
    white-space: normal !important;
  }
}
</style>
