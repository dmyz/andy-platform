---
name: fix-types
description: 运行并修复 upms-web 的 TypeScript 类型错误，适用于用户要求修复类型、处理 vue-tsc 错误或解决 noUncheckedIndexedAccess 相关问题时。
---

# 修复 TypeScript 类型错误

## 工作流程

1. 执行类型检查：

```bash
pnpm type-check
```

2. 归类错误：
   - `noUncheckedIndexedAccess` 导致的可能 `undefined`。
   - 缺失类型定义。
   - 类型不匹配。
   - 隐式 `any`。
   - 未使用变量或导入。
3. 修复可自动处理的问题。
4. 再次运行 `pnpm type-check` 验证。

## 修复原则

- 优先使用类型守卫、可选链 `?.` 和空值合并 `??`。
- 谨慎使用非空断言 `!`，只在确认值必定存在时使用。
- 避免使用 `any`，优先使用具体类型或 `unknown`。
- 缺失类型优先补到 `src/types/` 的对应分类。
