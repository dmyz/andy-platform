---
name: upms-architecture
description: 维护 upms 后端四层业务域架构和 application.upms 管理端聚合入口边界时使用，包括新增或调整 /admin/** Controller、调整应用层分包、审查跨层依赖、修复架构检查失败、更新架构文档和运行 .codex/skills/upms-architecture/scripts/check-architecture.sh。
---

# UPMS 架构边界维护

用于维护 `net.junanw.upms` 四层业务域架构，以及 `application.upms` 作为 UPMS 后台管理端聚合入口的规则。

## 先读文件

- 架构边界、模块归属、新增模块和验证规则：`docs/架构与开发规范.md`
- 新增或调整接口：目标模块同类 Controller、Service、Model。
- 持久层或 XBatis 用法调整：同时使用 `xbatis-usage` skill。

## 四层边界

- `core`：身份、组织、字典、设置、审计等稳定平台能力。
- `business`：文件、公告、消息及未来业务能力。
- `application`：端侧入口、请求适配、场景编排和视图组织。
- `infrastructure`：Web、安全配置、XBatis、公共 API、异常、工具、ID 等技术能力。

依赖方向：

- `application` 可以依赖 `core`、`business`、`infrastructure`。
- `business` 可以依赖 `core`、`infrastructure`，禁止依赖 `application`。
- `core` 可以依赖 `infrastructure`，原则上不依赖 `business`。
- `infrastructure` 不依赖业务层具体实现。
- 跨模块协作通过 Service，禁止跨模块直接依赖其他模块的 Mapper、Entity。

## application.upms 规则

`application.upms` 是 UPMS 后台管理端聚合入口：

- 所有 `/admin/**` Controller 必须放在 `src/main/java/net/junanw/upms/application/upms/**`。
- 禁止新增 `application/admin`、`application/personal`、`application/portal` 平铺入口包。
- `application.upms` 禁止直接依赖 Mapper、Entity、`cn.xbatis.core.sql.executor.chain.QueryChain`。
- 领域 Service、Entity、Mapper 保留在 `core` 或 `business`。
- `application.upms` 通过 `core` / `business` Service 聚合能力，只承载入口适配、场景编排、DTO/View 组装和兼容旧接口。

一级包白名单：

| 包 | 场景 |
| --- | --- |
| `auth` | 登录、退出、验证码、密码、当前用户认证上下文 |
| `identity` | 用户、角色、菜单、权限等身份权限管理入口 |
| `organization` | 部门、岗位等组织管理入口 |
| `dictionary` | 字典类型、字典项管理入口 |
| `setting` | 系统参数、配置项等设置入口 |
| `audit` | 登录日志、操作日志等审计入口 |
| `content` | 公告、消息、文件等内容和支撑能力管理入口 |
| `profile` | 管理端个人中心入口 |
| `workspace` | 管理端工作台、仪表盘入口 |

新增一级包前，先更新 `docs/架构与开发规范.md` 和 `.codex/skills/upms-architecture/scripts/check-architecture.sh` 白名单。

## 新增或调整管理端接口

1. 确认接口是否属于 `/admin/**`；是则放入 `application/upms/<场景>/controller`。
2. 纯 CRUD 管理入口优先让 Controller 调用领域 Service，不额外创建应用 Service。
3. 存在跨域聚合、当前登录人上下文、旧接口兼容、视图组装时，在同场景下新增 `service` 和 `model/*`。
4. 不把 Entity 暴露给 Controller 返回值；使用 request/response/view 模型。
5. 不在 `application.upms` 内写查询链或 Mapper 调用；需要领域能力时先补齐 `core` / `business` Service。
6. 调整包结构后同步修正 package、import、测试、文档和架构检查白名单。

## 验证

架构类改动至少运行：

```bash
bash .codex/skills/upms-architecture/scripts/check-architecture.sh
```

涉及 Java 代码时再运行：

```bash
mvn -q test-compile
```

影响业务行为、Mapper、SQL 或 Spring 上下文时运行相关测试；风险较高时运行：

```bash
mvn -q test
```
