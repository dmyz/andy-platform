# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Andy Platform 是一个企业级统一权限管理系统（UPMS - Unified Permission Management System），采用前后端分离架构：
- **后端**：Spring Boot 4.0.4 + Java 21，基于 DDD 分层架构
- **前端**：Vue 3 + Vite 7 + TDesign Vue Next + TypeScript

**核心能力**：
- 统一身份认证与授权（IAM）
- 组织架构与 RBAC 权限管理
- 审计日志与操作追踪
- 文件管理与公告通知
- 可扩展的业务域支持

## 文档路径

项目文档位于各子项目的 `docs/` 目录：

### 后端文档
- **架构文档**：[upms/docs/ARCHITECTURE.md](upms/docs/ARCHITECTURE.md) - 详细的后端分层架构、模块职责、依赖约束
- **模块开发模板**：[upms/docs/backend-module-template.md](upms/docs/backend-module-template.md) - 新增业务模块的标准结构和命名规范
- **业务脚手架示例**：[upms/docs/examples/backend-biz-scaffold.md](upms/docs/examples/backend-biz-scaffold.md) - 业务模块开发示例

### 前端文档
- **架构文档**：[upms-web/docs/ARCHITECTURE.md](upms-web/docs/ARCHITECTURE.md) - 前端技术栈、目录结构、开发规范
- **组件开发规范**：[upms-web/docs/COMPONENT_GUIDE.md](upms-web/docs/COMPONENT_GUIDE.md) - 组件分类、开发规范、自动导入配置
- **页面原型说明**：[upms-web/docs/prototype-description.md](upms-web/docs/prototype-description.md) - 页面原型与交互设计
- **需求文档**：[upms-web/README.md](upms-web/README.md) - 前端功能模块详细设计与开发计划

### 开发分支文档
- **后端开发分支**：[upms-worktree/CLAUDE.md](upms-worktree/CLAUDE.md) - 后端开发分支指导文档
- **前端开发分支**：[upms-web-worktree/CLAUDE.md](upms-web-worktree/CLAUDE.md) - 前端开发分支指导文档

## 项目分支说明

项目采用 Git Worktree 管理多个开发分支：

- **main**（主分支）：稳定版本，包含合并后的前后端代码
  - 位置：项目根目录 `/Users/dmyz/workspaces/andy-platform`
  - 用途：生产就绪代码，所有功能开发完成后合并到此分支
  
- **upms**（后端开发分支）：后端独立开发分支
  - 位置：`upms-worktree/` 目录（Git Worktree）
  - 用途：后端功能开发、测试，完成后合并到 main
  
- **upms-web**（前端开发分支）：前端独立开发分支
  - 位置：`upms-web-worktree/` 目录（Git Worktree）
  - 用途：前端功能开发、测试，完成后合并到 main

**开发流程**：
1. 后端开发在 `upms-worktree/` 目录的 `upms` 分支进行
2. 前端开发在 `upms-web-worktree/` 目录的 `upms-web` 分支进行
3. 功能完成后，分别合并到 `main` 分支
4. `main` 分支的 `upms/` 和 `upms-web/` 目录是稳定版本代码

**开发流程**：
1. 先分支git提交后再询问用户是否提交主分支;

### 文档更新说明

1. 每次分支开发完都要更新本项目的文档
2. 先分支文档更新完再更新主分支文档，确保文档之中同步一致

## 架构说明

### 后端架构（upms/）

采用 DDD（领域驱动设计）分层架构，根包为 `net.junanw.upms`，顶层分组固定为五层：

- **foundation/**：技术基础设施与公共能力
  - `platform.auth`：认证授权核心（账号、密码、验证码、会话、认证流程）
  - `platform.web`：平台通用 HTTP 端点、审计拦截
  - `shared`：公共响应、异常、MyBatis-Flex 基类、ID、安全工具、通用工具类
- **system/**：平台主数据、权限与系统配置
  - `iam`：账号、认证、用户、角色、权限、导航
  - `organization`：组织与组织成员关系
  - `dictionary`：字典类型与字典项
  - `setting`：系统配置项
- **support/**：平台支撑能力
  - `audit.login`：登录审计
  - `audit.operation`：操作审计
  - `file`：文件能力
  - `notification.announcement`：公告能力
  - `notification.message`：消息中心（预留）
- **portal/**：当前登录用户视角模块
  - `workbench`：工作台
  - `profile`：个人中心
- **biz/**：业务扩展域（未来业务模块统一放这里）
  - `biz.<domain>`：审批、任务、工单等业务域

**详细架构说明**：参见 [upms/docs/ARCHITECTURE.md](upms/docs/ARCHITECTURE.md)

**模块内标准结构**：
```
controller/       # 控制器
service/          # 服务接口与实现
repository/       # 数据仓储
entity/           # 实体类
model/
  ├── request/    # 请求对象
  ├── response/   # 响应对象
  └── view/       # 视图对象
convert/          # 对象转换器（MapStruct）
enums/            # 枚举类
support/          # 模块内支持类
```

**依赖约束**：
- `controller` 只依赖本模块或其他模块的 `service`
- 禁止直接依赖其他模块的 `repository` 或 `entity`
- 跨模块业务协作通过 `service` 接口

### 前端架构（upms-web/）

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

## 开发命令

### 后端（upms/）

```bash
# 启动后端服务（默认端口 8004）
mvn spring-boot:run

# 运行测试
mvn test

# 构建
mvn clean package

# 跳过测试构建
mvn clean package -DskipTests
```

**前置条件**：
- MySQL 数据库已创建 `upms` 库
- Redis 运行在 `127.0.0.1:6379`
- 数据库密码通过环境变量 `DB_PASSWORD` 配置（默认 `admin888`）

### 前端（upms-web/）

```bash
# 安装依赖
npm install

# 启动开发服务器（默认端口 5173，代理到后端 8004）
npm run dev

# 类型检查
npm run type-check

# 代码检查
npm run lint

# 代码格式化
npm run format

# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

## 技术栈关键点

### 后端

- **Spring Boot 4.0.4**：使用虚拟线程（Virtual Threads）
- **MyBatis-Flex 1.11.6**：数据库 ORM，支持逻辑删除、乐观锁
- **Sa-Token**：认证授权框架，JWT Bearer Token 模式
- **Redisson 4.1.0**：Redis 客户端，使用 Kryo5 序列化
- **Flyway**：数据库迁移工具，脚本位于 `src/main/resources/db/migration/`
- **SpringDoc OpenAPI**：API 文档，访问 `/swagger-ui.html`
- **MapStruct 1.6.3**：对象映射

### 前端

- **Vue 3.5 + TypeScript 5**：组合式 API（Composition API）
- **Vite 7**：构建工具，支持热更新
- **TDesign Vue Next 1.18+**：腾讯 UI 组件库
- **Pinia 3**：状态管理（支持持久化 pinia-plugin-persistedstate）
- **Vue Router 5**：路由管理
- **ECharts 6**：数据可视化
- **Tailwind CSS 4**：原子化 CSS
- **Axios**：HTTP 客户端
- **unplugin-auto-import**：自动导入 Vue/Router/Pinia API
- **unplugin-vue-components**：自动导入 TDesign 组件

## 数据库初始化

系统启动时通过 Flyway 自动执行数据库迁移脚本，初始化以下模块：
- 认证授权（auth）
- 审计日志（audit、operation_audit）
- 文件管理（file）
- 公告管理（announcement）
- 组织架构与 RBAC（org_rbac）
- 导航菜单（navigation_meta）
- 平台种子数据（platform_seed）

**默认测试账号**：
- `admin / admin`（管理员）
- `lisi / lisi`
- `wangwu / wangwu`
- `zhaoliu / zhaoliu`
- `sunqi / sunqi`
- `zhouba`（禁用账号，不可登录）

## 开发注意事项

### 后端

1. **分层规范**：严格遵循 DDD 分层，controller → application → domain → infrastructure
2. **包命名**：新功能必须归属到五大顶层分组之一（foundation/system/support/portal/biz）
3. **数据库变更**：通过 Flyway 迁移脚本管理，不要直接修改数据库
4. **逻辑删除**：实体类使用 `deleted` 字段（0=未删除，1=已删除）
5. **乐观锁**：实体类使用 `version` 字段
6. **主键策略**：使用雪花算法（Snowflake）
7. **测试**：集成测试使用 `application-test.yaml` 配置

### 前端

1. **组件规范**：使用 Vue 3 组合式 API（`<script setup>`），组件文件使用 PascalCase 命名
2. **API 代理**：开发环境通过 `/api` 代理到 `http://localhost:8004`
3. **环境变量**：使用 `.env.development` 配置开发环境（`VITE_API_BASE_URL`）
4. **组件自动导入**：Vue/Router/Pinia API 和 TDesign 组件自动导入，无需手动 import
5. **路径别名**：使用 `@/` 指向 `src/` 目录
6. **TypeScript 规范**：所有组件、函数、API 都需要类型定义，避免使用 `any`
7. **样式规范**：优先使用 Tailwind CSS 原子类，组件私有样式使用 `<style scoped>`
8. **状态管理**：按功能模块划分 Store（user、app、permission 等）
9. **路由规范**：路由路径使用 kebab-case，需要权限控制的路由添加 `meta.permission` 字段

## API 文档

后端启动后访问：
- Swagger UI：http://localhost:8004/swagger-ui.html
- OpenAPI JSON：http://localhost:8004/v3/api-docs

## 常见任务

### 添加新的业务模块

**后端**：
1. 确定模块归属：
   - 平台能力（认证、权限、组织、字典、配置）→ `system.*`
   - 支撑能力（审计、文件、通知）→ `support.*`
   - 业务功能 → `biz.<domain>`（推荐）
2. 创建标准包结构：`controller/` → `service/` → `repository/` → `entity/` → `model/`
3. 遵循依赖约束：controller 只依赖 service，禁止跨模块直接访问 repository/entity
4. 参考模板：[upms/docs/backend-module-template.md](upms/docs/backend-module-template.md)

**前端**：
1. 创建对应的 `api/`（接口定义）、`views/`（页面）、`types/`（类型定义）
2. 如需状态管理，在 `stores/` 创建对应 store
3. 在 `router/routes.ts` 添加路由配置

**数据库**：
- 如需新表，在 `upms/src/main/resources/db/migration/` 创建 Flyway 脚本
- 命名格式：`V{version}__{description}.sql`（例如：`V014__add_user_avatar.sql`）

### 运行单个测试

```bash
# 后端
mvn test -Dtest=ClassName#methodName

# 前端（如果有测试）
npm test
```

### 修改数据库结构

1. 在 `upms/src/main/resources/db/migration/` 创建新的 SQL 脚本
2. 命名格式：`V{version}__{description}.sql`（例如：`V014__add_user_avatar.sql`）
3. 重启应用，Flyway 自动执行

## 项目状态

- 平台基础域数据全部走数据库持久化
- 不再维护独立的 `bootstrap` profile
- 默认仅支持数据库运行路径
- 启动时自动初始化基础表与示例数据

## 浏览器兼容性

前端支持现代浏览器最新 2 个版本：
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
