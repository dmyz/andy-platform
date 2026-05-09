---
name: commit-push-pr
description: 为 upms-web 执行提交、推送和创建 PR 的工作流，适用于用户明确要求提交代码、推送分支或创建 Pull Request 时。
---

# 提交、推送并创建 PR

## 工作流程

1. 检查工作区状态：

```bash
git status
git diff
```

2. 分析变更并生成提交信息。
   - 格式：`<type>: <description>`
   - 常用类型：`feat`、`fix`、`refactor`、`chore`、`docs`、`style`、`perf`
3. 提交前建议运行前端验证：

```bash
pnpm type-check
pnpm lint
pnpm format:check
pnpm build
```

4. 添加相关文件并提交。
5. 推送当前分支。
6. 如用户要求，使用 `gh pr create` 创建 PR。

## 约束

- 不要直接推送到 `main`，除非用户明确要求。
- 如果当前在 `main`，先创建功能分支。
- 避免提交 `.env`、密钥、凭据等敏感文件。
- 不要回退用户已有改动。
