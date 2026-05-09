---
name: start-dev
description: 启动 Andy Platform 完整开发环境，包括后端 Spring Boot 服务和前端 Vite 开发服务器，适用于用户要求启动项目、启动开发环境或本地联调时。
---

# 启动开发环境

用于启动后端和前端本地开发服务。

## 前置检查

- MySQL 数据库 `upms` 已创建。
- Redis 运行在 `127.0.0.1:6379`。
- `DB_PASSWORD` 已设置，或使用项目默认值。
- 前端依赖已安装；如未安装，先运行 `npm install` 或 `pnpm install`。

## 启动后端

在 `upms/` 目录执行：

```bash
mvn spring-boot:run
```

后端默认地址：`http://localhost:8004`。

## 启动前端

在 `upms-web/` 目录执行：

```bash
npm run dev
```

如当前环境明确使用 pnpm，可执行：

```bash
pnpm dev
```

前端默认地址：`http://localhost:5173`。

## 输出信息

启动完成后告知用户：

- 前端地址：`http://localhost:5173`
- 后端 API：`http://localhost:8004`
- Swagger UI：`http://localhost:8004/swagger-ui.html`
- 默认测试账号：`admin / admin`
