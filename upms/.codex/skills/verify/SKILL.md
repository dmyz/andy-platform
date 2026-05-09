---
name: verify
description: 验证 upms 后端分支代码质量，运行 Maven 测试或指定测试，适用于用户要求验证、跑测试、检查后端代码质量或提交前检查时。
---

# 验证后端代码质量

## 常用验证

检查四层架构和 `application.upms` 管理端入口边界：

```bash
bash .codex/skills/upms-architecture/scripts/check-architecture.sh
```

在 `upms-worktree/` 根目录执行：

```bash
mvn test
```

运行指定测试：

```bash
mvn test -Dtest=ClassName#methodName
```

只验证编译：

```bash
mvn test-compile
```

## 前置条件

- MySQL 数据库 `upms` 已创建。
- Redis 运行在 `127.0.0.1:6379`。
- `DB_PASSWORD` 已设置，或使用项目默认值。

## 失败处理

- 报告失败测试类、方法和关键堆栈。
- 区分编译失败、上下文启动失败、数据库连接失败和断言失败。
- 修复后优先重跑失败的单个测试，再跑相关测试集。
