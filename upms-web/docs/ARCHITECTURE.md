# 前端架构文档

## 项目概述

Andy Platform 前端是基于 Vue 3 + Vite 7 + TDesign Vue Next 开发的企业级中台管理系统。

## 技术栈

| 技术             | 版本   | 用途       |
| ---------------- | ------ | ---------- |
| Vue              | 3.5    | 前端框架   |
| Vite             | 7.x    | 构建工具   |
| TypeScript       | 5.x    | 类型安全   |
| TDesign Vue Next | 1.18+  | UI 组件库  |
| Vue Router       | 5.x    | 路由管理   |
| Pinia            | 3.x    | 状态管理   |
| ECharts          | 6.x    | 数据可视化 |
| Tailwind CSS     | 4.x    | 原子化 CSS |

## 目录结构

```
src/
├── api/          # API 接口定义（按业务模块分组）
│   ├── auth.ts           # 认证接口
│   ├── user.ts           # 用户管理
│   ├── role.ts           # 角色管理
│   ├── permission.ts     # 权限管理
│   ├── navigation.ts     # 导航菜单
│   ├── organization.ts   # 组织管理
│   ├── dictionary.ts     # 字典管理
│   ├── setting.ts        # 系统配置
│   ├── session.ts        # 会话管理
│   ├── audit.ts          # 审计日志
│   ├── file.ts           # 文件管理
│   └── announcement.ts   # 公告管理
├── views/        # 页面视图
│   ├── dashboard/        # 数据仪表盘
│   ├── login/            # 登录页
│   ├── system/           # 系统管理
│   │   ├── user/         # 用户管理
│   │   ├── role/         # 角色管理
│   │   ├── permission/   # 权限管理
│   │   ├── navigation/   # 导航管理
│   │   ├── organization/ # 组织管理
│   │   ├── dictionary/   # 字典管理
│   │   ├── setting/      # 系统配置
│   │   └── session/      # 会话管理
│   ├── audit/            # 审计日志
│   ├── file/             # 文件管理
│   ├── announcement/     # 公告管理
│   ├── user/             # 个人中心
│   └── error/            # 错误页面
├── layouts/      # 布局组件
├── components/   # 公共组件
├── stores/       # Pinia 状态管理
├── router/       # 路由配置
├── composables/  # 组合式函数
├── directives/   # 自定义指令
├── utils/        # 工具函数
└── types/        # TypeScript 类型定义
```

## 核心功能模块

### 1. 数据中枢
- 数据仪表盘：核心指标展示、趋势图表、业务排名

### 2. 系统管理
- 用户管理：用户 CRUD、角色分配、密码重置、强制下线
- 角色管理：角色 CRUD、权限授权、关联用户查看
- 权限管理：权限定义、权限分类
- 导航管理：菜单树管理、访问规则配置
- 组织管理：组织树管理、成员管理
- 字典管理：字典类型与字典项管理
- 系统配置：系统参数配置
- 会话管理：在线会话查看、强制下线

### 3. 支撑功能
- 审计日志：登录审计、操作审计
- 文件管理：文件上传、预览、下载
- 公告管理：公告发布、定向推送

### 4. 个人中心
- 基本信息：个人资料编辑、头像修改
- 安全设置：密码修改、手机/邮箱绑定
- 登录记录：登录历史查看
- 我的消息：消息通知查看

## 开发规范

### 组件规范
- 使用 Vue 3 组合式 API（Composition API）
- 组件文件使用 PascalCase 命名（如 `UserList.vue`）
- 组件内部使用 `<script setup>` 语法
- 优先使用 TDesign 组件，避免重复造轮子

### API 调用规范
- 所有 API 接口统一在 `src/api/` 目录定义
- 使用 TypeScript 定义请求和响应类型
- 统一使用 `utils/request.ts` 封装的 axios 实例
- 错误处理统一在请求拦截器中处理

### 状态管理规范
- 使用 Pinia 进行状态管理
- Store 按功能模块划分（user、app、permission 等）
- 需要持久化的状态使用 `pinia-plugin-persistedstate`

### 路由规范
- 路由配置统一在 `src/router/routes.ts`
- 路由路径使用 kebab-case（如 `/user-management`）
- 需要权限控制的路由添加 `meta.permission` 字段

### 样式规范
- 优先使用 Tailwind CSS 原子类
- 组件私有样式使用 `<style scoped>`
- 全局样式定义在 `src/assets/styles/`
- 单行代码不超过 160 字符

### TypeScript 规范
- 所有组件、函数、API 都需要类型定义
- 类型定义统一放在 `src/types/` 目录
- 避免使用 `any`，优先使用 `unknown` 或具体类型

## 开发工具

### 自动导入
项目配置了 `unplugin-auto-import` 和 `unplugin-vue-components`：
- Vue API（ref、reactive、computed 等）自动导入
- Vue Router API（useRouter、useRoute 等）自动导入
- Pinia API（defineStore、storeToRefs 等）自动导入
- TDesign 组件自动导入

### 开发服务器
- 默认端口：5173
- API 代理：`/api` → `http://localhost:8004`
- 热更新：支持

## 构建优化

- 使用 Vite 7 的快速构建能力
- 组件按需加载
- 路由懒加载
- 图片资源优化
- 代码分割

## 浏览器兼容性

支持现代浏览器最新 2 个版本：
- Chrome
- Edge
- Safari
- Firefox

## 性能要求

| 指标       | 目标值   |
| ---------- | -------- |
| 首屏加载   | < 2s     |
| 路由切换   | < 300ms  |
| 接口响应   | < 500ms  |
| 大数据列表 | 虚拟滚动 |
| 图表渲染   | < 1s     |

## 安全规范

- 所有接口需要身份认证（JWT Token）
- 敏感操作需要二次确认
- 防止 XSS 和 CSRF 攻击
- 不在前端存储敏感信息
- 路由权限控制

## 测试规范

- 单元测试：使用 Vitest
- E2E 测试：使用 Playwright（可选）
- 测试覆盖率目标：> 80%
