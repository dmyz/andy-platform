---
name: dev
description: 启动 upms-web 前端 Vite 开发服务器并用于浏览器验证，适用于用户要求启动前端、运行开发服务器或测试 UI 变更时。
---

# 启动前端开发服务器

## 前置条件

- Node.js 满足 `^20.19.0 || >=22.12.0`。
- 依赖已安装；首次运行先执行 `pnpm install`。
- 后端服务通常需要运行在 `http://localhost:8004`。

## 启动

在 `upms-web-worktree/` 根目录执行：

```bash
pnpm dev
```

默认访问地址：

```text
http://localhost:5173
```

## 验证 UI

UI 修改后应检查：

- 页面是否正常加载。
- 修改功能的正常路径和边界情况。
- 浏览器控制台是否有错误或警告。
- API 代理是否正常指向后端。

默认测试账号：

- `admin / admin`
- `user / user`
