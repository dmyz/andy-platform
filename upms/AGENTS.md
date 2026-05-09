# AGENTS.md

本文件为 Codex 在后端开发分支 `upms` 工作时的项目约定。除非用户另有明确要求，所有沟通、计划、总结、提交说明、Markdown 文档和新增代码注释均使用中文。

## 分支定位

- 当前工作树：`/Users/dmyz/workspaces/andy-platform/upms-worktree`
- 当前分支：`upms`
- 稳定版本：主分支 `main` 的 `upms/` 目录
- 开发流程：在本分支开发、验证、提交后，再合并到主分支。
- 当前分支已有大量未提交改动，工作时必须保护已有变更，不要回退未确认的文件。

## 项目基线

- Java 21
- Spring Boot 4.0.4
- Sa-Token 1.45.0
- Redisson 4.1.0
- XBatis 1.10.1-spring-boot4
- Flyway
- MapStruct 1.6.3
- Lombok 1.18.36

项目架构、模块归属、依赖边界、新增模块规范和持久层约束统一以 `docs/架构与开发规范.md` 为准。修改架构、`application.upms` 分包、`/admin/**` Controller 或持久层代码前，先阅读该文档。

## 关键约束

- 后端根包为 `net.junanw.upms`，顶层分组固定为 `core`、`business`、`application`、`infrastructure`。
- `application.upms` 是 UPMS 后台管理端聚合入口，所有 `/admin/**` Controller 必须放在 `src/main/java/net/junanw/upms/application/upms/**`。
- `application.upms` 禁止直接依赖 Mapper、Entity 或 `QueryChain`，跨域协作必须通过 `core` / `business` Service。
- 当前持久层统一使用 XBatis，公共能力位于 `infrastructure.persistence.xbatis`；不要新增 MyBatis-Flex 依赖、注解、Wrapper 或 TableDef。
- 数据库结构变更通过 Flyway 脚本管理，不要直接修改数据库。
- 必须保护已有未提交变更，不要回退未确认的文件。

## 开发规范

- 必须为新增或修改的函数、方法添加必要文档注释。
- 生产项目 Markdown 文档文件名使用中文；`README.md` 和 `AGENTS.md` 是约定入口文件，保留英文名。
- 项目根目录不存放 `scripts/`；检查脚本放在对应 `.codex/skills/*/scripts/` 目录。
- 禁止硬编码密钥、密码、令牌等敏感信息。
- 必须处理异常，避免直接暴露堆栈信息。
- 建议单行代码不超过 160 个字符。
- 逻辑删除字段使用 `deleted`，通常 `0` 表示未删除、`1` 表示已删除。
- 乐观锁字段使用 `version`。
- 主键策略使用雪花算法；实体主键优先交给 XBatis 生成器。

## 常用命令

在本工作树根目录执行：

```bash
mvn test
mvn test -Dtest=ClassName#methodName
mvn spring-boot:run
mvn clean package
mvn clean package -DskipTests
bash .codex/skills/upms-architecture/scripts/check-architecture.sh
bash .codex/skills/xbatis-usage/scripts/check-xbatis-usage.sh
```

## Codex Skills

后端分支专用 Codex skills 位于 `.codex/skills/`：

- `db-migrate`：创建新的 Flyway 数据库迁移脚本。
- `verify`：运行 Maven 编译和测试验证。
- `start-dev`：启动后端 Spring Boot 开发服务。
- `xbatis-usage`：校验和维护 XBatis 持久层用法，防止 MyBatis-Flex 写法回退。
- `upms-architecture`：维护四层业务域架构和 `application.upms` 管理端聚合入口边界。

## 本地前置条件

- MySQL 数据库 `upms` 已创建。
- Redis 运行在 `127.0.0.1:6379`。
- 数据库密码通过 `DB_PASSWORD` 配置，项目配置中有默认值。

后端启动后：

- Swagger UI：`http://localhost:8004/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8004/v3/api-docs`

默认测试账号：

- `admin / admin`
- `lisi / lisi`
- `wangwu / wangwu`
- `zhaoliu / zhaoliu`
- `sunqi / sunqi`
- `zhouba / zhouba` 为禁用账号。

## Flyway 迁移

Flyway 数据库迁移目录是 `src/main/resources/db/migration/`，必须保留。

新增数据库迁移脚本时：

- 路径：`src/main/resources/db/migration/`
- 文件名格式：`V{version}__{description}.sql`
- 版本号按现有最大版本加一，建议三位数字，例如 `V015__add_user_avatar.sql`。
- 描述使用下划线分隔，不使用连字符。
- Flyway 脚本一旦执行，不要修改原脚本；后续变更创建新脚本。
