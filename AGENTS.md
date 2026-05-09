# AGENTS.md

本文件为 Codex 在本仓库工作时的项目约定。除非用户另有明确要求，所有沟通、计划、总结、提交说明、Markdown 文档和新增代码注释均使用中文。

## 项目概览

Andy Platform 是企业级统一权限管理系统（UPMS），采用前后端分离架构。

- 后端：`upms/`，Spring Boot 4.0.4 + Java 21，根包 `net.junanw.upms`。
- 前端：`upms-web/`，Vue 3 + Vite + TypeScript + TDesign Vue Next。
- 开发分支工作树：
  - 后端开发分支：`upms-worktree/`
  - 前端开发分支：`upms-web-worktree/`
  - 根目录主分支代码作为稳定版本。

## 通用规范

- 必须保护用户已有改动，不要回退未确认的变更。
- 禁止硬编码密钥、密码、令牌等敏感信息。
- 新增或修改函数/方法时添加必要的文档注释；代码注释使用中文。
- 异常处理避免直接暴露堆栈信息。
- 建议单行代码不超过 160 个字符。
- 修改文档时同步相关说明，尤其是功能开发完成后的分支文档与主分支文档。

## 后端约定

后端采用 DDD 分层，顶层包固定为：

- `foundation`：技术基础设施与公共能力。
- `system`：平台主数据、权限与系统配置。
- `support`：审计、文件、通知等平台支撑能力。
- `portal`：当前登录用户视角能力。
- `biz`：未来业务扩展域。

模块内常用结构：

- `controller`
- `service`
- `repository`
- `entity`
- `model/request`
- `model/response`
- `model/view`
- `convert`
- `enums`
- `support`

依赖约束：

- `controller` 只依赖本模块或其他模块的 `service`。
- 禁止跨模块直接依赖其他模块的 `repository` 或 `entity`。
- 跨模块业务协作优先通过 `service` 接口。
- 数据库结构变更通过 Flyway 脚本管理，路径为 `upms/src/main/resources/db/migration/`。

常用命令在 `upms/` 目录执行：

```bash
mvn test
mvn spring-boot:run
mvn clean package
mvn clean package -DskipTests
```

后端启动前通常需要 MySQL `upms` 库和 Redis `127.0.0.1:6379`。数据库密码通过 `DB_PASSWORD` 配置，默认值见项目配置。

## 前端约定

前端主要目录：

- `src/api/`：接口定义，按业务模块分文件。
- `src/views/`：页面视图。
- `src/layouts/`：布局组件。
- `src/components/`：公共组件。
- `src/stores/`：Pinia 状态。
- `src/router/`：路由配置。
- `src/composables/`：组合式函数。
- `src/directives/`：自定义指令。
- `src/utils/`：工具函数。
- `src/types/`：类型定义。

开发约定：

- 使用 Vue 3 Composition API 和 `<script setup>`。
- 组件文件使用 PascalCase。
- 优先使用 TDesign Vue Next 组件和现有公共组件。
- API 统一放在 `src/api/`，类型放在 `src/types/`。
- 避免使用 `any`，优先使用具体类型或 `unknown`。
- 路由路径使用 kebab-case。
- 前端 UI 修改完成后，应尽量启动开发服务器并在浏览器验证页面、交互和控制台错误。

常用命令在 `upms-web/` 目录执行：

```bash
npm install
npm run dev
npm run type-check
npm run lint
npm run lint:fix
npm run build
npm run preview
```

仓库包含 `pnpm-lock.yaml`，如当前分支或用户环境明确使用 pnpm，可按等价脚本执行 `pnpm install`、`pnpm dev`、`pnpm build` 等。

## 常见工作流

对应的 Codex skills 已放在 `.codex/skills/`，可作为可复用流程维护：

- `db-migrate`：创建 Flyway 迁移脚本。
- `verify`：验证后端和前端代码质量。
- `start-dev`：启动完整开发环境。

### 验证代码质量

需要完整验证时，可并行执行后端和前端检查：

- 后端在 `upms/` 目录执行 `mvn test`。
- 前端在 `upms-web/` 目录执行 `npm run type-check` 和 `npm run lint`；如当前环境明确使用 pnpm，可执行 `pnpm type-check` 和 `pnpm lint`。
- 后端测试通常依赖 MySQL 和 Redis；前端静态检查不依赖后端服务。
- 汇总结果时说明通过项、失败项和需要修复的具体问题。

### 启动开发环境

需要启动完整开发环境时：

- 确认 MySQL 数据库 `upms` 已创建。
- 确认 Redis 运行在 `127.0.0.1:6379`。
- 确认 `DB_PASSWORD` 已设置，或使用项目默认值。
- 在 `upms/` 目录启动后端：`mvn spring-boot:run`，默认端口 `8004`。
- 在 `upms-web/` 目录启动前端：`npm run dev` 或 `pnpm dev`，默认端口 `5173`。
- 前端地址为 `http://localhost:5173`，后端 API 为 `http://localhost:8004`，Swagger UI 为 `http://localhost:8004/swagger-ui.html`。
- 默认测试账号为 `admin / admin`。

### 创建 Flyway 迁移脚本

需要新增数据库迁移时：

- 读取 `upms/src/main/resources/db/migration/` 下的现有脚本，找到最大版本号。
- 新脚本版本号为最大版本号加一，文件名格式为 `V{version}__{description}.sql`。
- 版本号使用三位数字，例如 `V014__add_user_avatar.sql`。
- 描述使用下划线分隔，不使用连字符。
- 新文件中写入简短头部信息，包括描述、作者和日期。
- Flyway 脚本一旦执行就不要修改；后续变更应继续创建新的迁移脚本。

## 参考文档

- 后端架构：`upms/docs/ARCHITECTURE.md`
- 后端模块模板：`upms/docs/backend-module-template.md`
- 后端业务脚手架示例：`upms/docs/examples/backend-biz-scaffold.md`
- 前端需求规划：`upms-web/README.md`
- 前端原型说明：`upms-web/prototype-description.md`
- 后端开发分支说明：`upms-worktree/AGENTS.md`
- 前端开发分支说明：`upms-web-worktree/AGENTS.md`
