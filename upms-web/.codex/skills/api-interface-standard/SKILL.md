---
name: api-interface-standard
description: 检查并修复 Vue 前端项目中违反 API 接口分层规范的代码，适用于用户要求检查 API 规范、修复 API 调用、重构接口、规范化 API 或将 Vue 文件中的直接 HTTP 调用迁移到 API 层时。
---

# API 接口规范检查与修复

确保 Vue 组件不直接调用请求工具，所有接口统一通过 `src/api/` 暴露。

## 核心规范

- `.vue` 文件不应直接导入 `@/utils/request` 中的 `get`、`post`、`put`、`del`。
- API 函数统一定义在 `src/api/`，按业务模块分文件。
- 组件导入并调用 API 函数，不直接拼接请求。
- 类型定义放在 `src/types/api/`、`src/types/entities/`、`src/types/requests/`、`src/types/responses/`。

## 扫描流程

1. 扫描违规导入：

```bash
rg "from ['\"]@/utils/request['\"]" src -g '*.vue'
```

2. 对每个违规文件识别：
   - HTTP 方法。
   - 请求路径。
   - 请求参数。
   - 响应类型。
   - 所属业务模块。
3. 生成 Markdown 报告，列出违规文件、API 调用清单、建议新增的 API 函数和类型定义。
4. 自动修复前先确认修复范围。

## 修复流程

- 在 `src/api/{module}.ts` 创建或补充接口函数。
- 避免重复创建已有函数。
- 在需要时补充 `src/types/` 类型。
- 修改 Vue 文件，移除 `@/utils/request` 导入，替换为 API 函数。
- 修复后运行 `pnpm type-check` 和 `pnpm lint`。

## 命名建议

- 列表：`get{Module}List`
- 详情：`get{Module}` 或 `get{Module}ById`
- 创建：`create{Module}`
- 更新：`update{Module}`
- 删除：`delete{Module}`
- 其他动作：`enable{Module}`、`disable{Module}` 等动词前缀。
