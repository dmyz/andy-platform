# ja-upms

统一权限管理系统（Unified Permission Management System）后端服务。

## 项目概述

ja-upms 是一个基于 Spring Boot 4 的企业级权限管理平台，提供认证、授权、组织架构、角色权限、审计日志、文件、公告和消息等能力。

## 技术栈

- Java 21
- Spring Boot 4.0.4
- Sa-Token 1.45.0
- Redisson 4.1.0
- XBatis 1.10.1-spring-boot4
- Flyway
- MapStruct 1.6.3
- Lombok 1.18.36
- SpringDoc 2.6.0

## 项目结构

项目采用四层业务域架构，根包 `net.junanw.upms` 只允许以下顶层分组：

```text
net.junanw.upms/
├── core/             # 核心平台域：身份、组织、字典、设置、审计
├── business/         # 可扩展业务域：文件、公告、消息及未来业务能力
├── application/      # 应用层：端侧入口、请求适配、场景编排和视图组织
└── infrastructure/   # 基础设施：Web、安全、XBatis、公共 API、异常、工具、ID
```

`application.upms` 是 UPMS 后台管理端聚合入口，所有 `/admin/**` Controller 必须位于该包下。架构和开发规范见 [docs/架构与开发规范.md](docs/架构与开发规范.md)。

## 核心功能

- 认证与授权：登录、会话、权限控制、验证码、密码和当前用户能力。
- 组织与身份：用户、组织、角色、权限和导航菜单管理。
- 审计日志：登录审计、操作审计和审计追踪查询。
- 系统管理：字典管理和系统配置管理。
- 内容业务：公告、消息和文件管理。
- UPMS 应用场景：后台认证入口、个人中心、工作台和仪表盘。

## 本地开发

### 前置条件

1. JDK 21 或更高版本。
2. Maven 3.6+。
3. MySQL 8.0+，数据库：

   ```sql
   CREATE DATABASE upms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

4. Redis 6.0+，默认连接 `127.0.0.1:6379`。

### 配置说明

开发环境默认使用 `application-dev.yaml`：

- 数据库连接：`jdbc:mysql://127.0.0.1:3306/upms`
- 数据库用户：`root`
- 数据库密码：通过环境变量 `DB_PASSWORD` 配置，默认值 `admin888`
- Redis 地址：`redis://127.0.0.1:6379`

### 启动步骤

```bash
export DB_PASSWORD=your_password
mvn spring-boot:run
```

服务地址：

- 应用端口：`http://localhost:8004`
- Swagger UI：`http://localhost:8004/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8004/v3/api-docs`

### 测试账号

- `admin / admin`：超级管理员
- `lisi / lisi`：普通用户
- `wangwu / wangwu`：普通用户
- `zhaoliu / zhaoliu`：普通用户
- `sunqi / sunqi`：普通用户
- `zhouba / zhouba`：禁用账号

## 数据库版本管理

项目使用 Flyway 管理数据库结构和基础数据版本，启动时会自动执行迁移脚本。

Flyway 数据库迁移目录：

```text
src/main/resources/db/migration/
```

新增脚本命名格式：

```text
V{version}__{description}.sql
```

版本号按现有最大版本递增，建议三位数字，例如 `V015__add_user_avatar.sql`。Flyway 脚本一旦执行，不要修改原脚本；后续变更创建新脚本。

## 常用命令

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 运行指定测试
mvn test -Dtest=ClassName#methodName

# 启动应用
mvn spring-boot:run

# 打包应用
mvn clean package

# 打包（跳过测试）
mvn clean package -DskipTests

# 检查架构规则
bash .codex/skills/upms-architecture/scripts/check-architecture.sh

# 检查 XBatis 使用约定
bash .codex/skills/xbatis-usage/scripts/check-xbatis-usage.sh
```

## 项目文档

- [架构与开发规范](docs/架构与开发规范.md)
- [AI Agent 工作约定](AGENTS.md)
