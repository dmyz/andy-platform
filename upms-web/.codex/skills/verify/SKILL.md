---
name: verify
description: 运行 upms-web 前端完整验证流程，包括类型检查、代码检查、格式检查和构建，适用于用户要求验证前端、提交前检查或确认代码质量时。
---

# 验证前端代码质量

按顺序执行以下命令，任何一步失败都停止并报告关键错误。

```bash
pnpm type-check
pnpm lint
pnpm format:check
pnpm build
```

## 失败处理

- 类型检查失败：定位具体类型错误并修复。
- 代码检查失败：可尝试 `pnpm lint:fix`。
- 格式检查失败：运行 `pnpm format`。
- 构建失败：报告失败模块和关键错误。

验证完成后说明各步骤结果。
