---
name: start-dev
description: 启动 upms 后端 Spring Boot 开发服务，适用于用户要求启动后端、运行 API 服务、打开 Swagger 或本地联调时。
---

# 启动后端开发服务

## 前置检查

- MySQL 数据库 `upms` 已创建。
- Redis 运行在 `127.0.0.1:6379`。
- `DB_PASSWORD` 已设置，或使用项目默认值。
- 端口 `8004` 未被其他进程占用。

## 启动命令

在 `upms-worktree/` 根目录执行：

```bash
mvn spring-boot:run
```

## 启动后地址

- 后端 API：`http://localhost:8004`
- Swagger UI：`http://localhost:8004/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8004/v3/api-docs`

默认测试账号：

- `admin / admin`
- `lisi / lisi`
- `wangwu / wangwu`
- `zhaoliu / zhaoliu`
- `sunqi / sunqi`
- `zhouba` 为禁用账号。
