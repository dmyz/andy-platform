# AGENTS.md

本文件为 Codex 在前端开发分支 `upms-web` 工作时的项目约定。除非用户另有明确要求，所有沟通、计划、总结、提交说明、Markdown 文档和新增代码注释均使用中文。

## 分支定位

- 当前工作树：`/Users/dmyz/workspaces/andy-platform/upms-web-worktree`
- 当前分支：`upms-web`
- 稳定版本：主分支 `main` 的 `upms-web/` 目录
- 后端开发分支：`upms`
- 当前分支已有大量未提交改动，工作时必须保护已有变更，不要回退未确认的文件。

## 技术栈

- Vue 3.5
- TypeScript 5.9
- Vite 8
- TDesign Vue Next 1.19
- Vue Router 5
- Pinia 3
- Tailwind CSS 4
- ECharts 6
- Axios
- oxlint
- oxfmt
- pnpm

Node.js 版本要求：`^20.19.0 || >=22.12.0`。

## 常用命令

在本工作树根目录执行：

```bash
pnpm install
pnpm dev
pnpm type-check
pnpm lint
pnpm lint:fix
pnpm format
pnpm format:check
pnpm build
pnpm preview
```

完整验证流程按顺序执行：

```bash
pnpm type-check
pnpm lint
pnpm format:check
pnpm build
```

若格式检查失败，优先运行 `pnpm format`；若代码检查失败，可尝试 `pnpm lint:fix`。

## Codex Skills

从原 `.claude/skills` 迁移后的 Codex skills 位于 `.codex/skills/`：

- `api-interface-standard`：检查并修复 API 分层规范。
- `verify`：运行完整前端验证流程。
- `fix-types`：修复 TypeScript 类型错误。
- `new-component`：创建 Vue 组件模板。
- `commit-push-pr`：提交、推送并创建 PR。
- `dev`：启动前端开发服务器并进行 UI 验证。

## 项目结构

- `src/api/`：API 接口定义，按业务模块分文件。
- `src/views/`：页面视图。
- `src/layouts/`：布局组件。
- `src/components/`：公共组件。
- `src/stores/`：Pinia 状态。
- `src/router/`：静态路由配置。
- `src/composables/`：组合式函数。
- `src/directives/`：自定义指令。
- `src/utils/`：工具函数。
- `src/types/`：类型定义。

## 开发规范

- 使用 Vue 3 Composition API 和 `<script setup lang="ts">`。
- 组件文件使用 PascalCase。
- 优先使用 TDesign Vue Next 组件和现有公共组件。
- 优先使用 Tailwind CSS 原子类，组件私有样式使用 `<style scoped>`。
- 所有组件、函数、API 都需要类型定义。
- 避免使用 `any`，优先使用具体类型或 `unknown`。
- `tsconfig` 启用了 `noUncheckedIndexedAccess`，数组和对象访问需要安全检查。
- 路由路径使用 kebab-case。
- 需要权限控制的路由添加 `meta.permission`。
- `v-auth` 指令用于基于权限或角色的 DOM 元素移除。
- `usePermission()` 提供权限和角色检查方法。

## API 规范

- Vue 组件不应直接导入 `@/utils/request` 中的 `get`、`post`、`put`、`del`。
- 所有接口函数统一定义在 `src/api/`。
- 组件应导入并调用 `src/api/` 中的业务接口函数。
- API 响应遵循 `{ code: 0, message: string, data: T }` 结构。
- 类型按用途放入 `src/types/api/`、`src/types/entities/`、`src/types/requests/`、`src/types/responses/`。
- FormData 请求会自动切换到 XHR 以支持上传进度。

检查 API 规范时，先扫描 `.vue` 文件是否直接导入 `@/utils/request`，生成违规报告并说明需要新增或迁移的 API 函数；自动修复前应先确认修复范围。

## 开发环境

- 前端默认地址：`http://localhost:5173`
- 后端默认地址：`http://localhost:8004`
- 开发环境 `/api` 请求代理到 `VITE_API_TARGET`，通常为 `http://localhost:8004`。
- 代理会将 `/api` 重写为空字符串，后端 URL 不应再额外包含 `/api` 前缀。
- 默认测试账号：`admin / admin`、`user / user`。

UI 或前端交互修改完成后，应尽量：

- 启动 `pnpm dev`。
- 在浏览器打开 `http://localhost:5173`。
- 验证正常路径和边界情况。
- 检查控制台错误或警告。
- 说明是否完成浏览器验证；无法验证时说明原因。

## 类型错误修复

修复 TypeScript 错误时：

- 先运行 `pnpm type-check` 获取完整错误。
- 优先使用类型守卫、可选链 `?.` 和空值合并 `??`。
- 谨慎使用非空断言 `!`，只在确认值必定存在时使用。
- 缺失类型优先补到 `src/types/` 的对应分类。
- 修复后再次运行 `pnpm type-check`。

## 提交与 PR

- 提交前建议先跑完整验证流程。
- 提交信息格式建议为 `<type>: <description>`。
- 常用类型：`feat`、`fix`、`refactor`、`chore`、`docs`、`style`、`perf`。
- 不要直接推送到 `main`，除非用户明确要求。
- 避免提交 `.env`、密钥、凭据等敏感文件。

## 参考文档

- 分支说明：`CLAUDE.md`
- 前端架构：`docs/ARCHITECTURE.md`
- 组件规范：`docs/COMPONENT_GUIDE.md`
- 原型说明：`docs/prototype-description.md`
- API 端点：`docs/API_ENDPOINTS.md`
- API 对比：`docs/API_COMPARISON.md`

## 注意事项

- 文档名称必须中文
- 暂不适配移动端
- 优先使用antdv-next组件,无法适配时可以单独封装组件使用