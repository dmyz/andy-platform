---
name: new-component
description: 为 upms-web 创建符合项目规范的 Vue 3 组件模板，适用于用户要求新增页面组件、公共组件或布局组件时。
---

# 创建 Vue 组件

## 参数

- 组件名必须使用 PascalCase，例如 `UserList`、`DataTable`。
- 组件类型：
  - `page`：页面组件，放到 `src/views/`。
  - `common`：公共组件，放到 `src/components/`。
  - `layout`：布局组件，放到 `src/layouts/`。

## 生成规范

- 使用 `<script setup lang="ts">`。
- 使用 TypeScript 定义 props 和 emits。
- 优先使用 TDesign 组件。
- 优先使用 Tailwind CSS 原子类。
- 组件私有样式使用 `<style scoped>`。
- 避免 `any`。

## 流程

1. 确认组件名和组件类型。
2. 检查目标文件是否已存在；存在时先确认是否覆盖。
3. 创建组件文件。
4. 如果是页面组件，提醒补充路由配置和权限 `meta.permission`。
5. 创建后建议运行 `pnpm type-check`。
