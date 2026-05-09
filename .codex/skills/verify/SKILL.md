---
name: verify
description: 验证 Andy Platform 项目代码质量，运行后端 Maven 测试和前端类型检查、代码检查，适用于用户要求验证、跑测试、检查代码质量或提交前检查时。
---

# 验证项目代码质量

用于验证根项目中的后端和前端代码质量。

## 工作流程

可并行执行后端和前端检查。

### 后端

在 `upms/` 目录执行：

```bash
mvn test
```

如果测试失败，报告失败的测试类、方法和关键错误信息。

### 前端

在 `upms-web/` 目录执行：

```bash
npm run type-check
npm run lint
```

如果当前环境明确使用 pnpm，可改用：

```bash
pnpm type-check
pnpm lint
```

## 注意事项

- 后端测试通常需要 MySQL 和 Redis 已启动。
- 前端静态检查不需要后端服务。
- 总结时列出通过项、失败项和建议修复路径。
