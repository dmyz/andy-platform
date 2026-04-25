# Backend Business Module Template

## 顶层分组

- `net.upms.foundation.*`：技术基础设施与公共能力
- `net.upms.system.*`：平台主数据与权限体系
- `net.upms.support.*`：平台支撑能力
- `net.upms.portal.*`：当前登录用户视角模块
- `net.upms.biz.<domain>`：新增业务域

## 当前归类约定

- `foundation.platform`
- `foundation.shared`
- `system.iam`
- `system.organization`
- `system.dictionary`
- `system.setting`
- `support.file`
- `support.notification`
- `support.audit`
- `portal.workbench`
- `portal.profile`

## 新增业务默认归位

新增业务模块统一放在 `net.upms.biz.<domain>`，不要直接平铺到 `net.upms` 顶层。

只有以下场景继续使用平台顶层分组，而不是 `biz`：

- 账号、认证、权限、导航等平台身份能力 → `system.iam`
- 组织、字典、系统配置等平台主数据 → `system.*`
- 文件、通知、审计等平台支撑能力 → `support.*`
- 当前登录人视角的聚合页面与接口 → `portal.*`
- 安全、会话、Web、异常、公共 API/JPA 基类 → `foundation.*`

## 业务模块标准结构

- `controller`
- `service`
- `repository`
- `entity`
- `model/request`
- `model/response`
- `model/view`
- `convert`
- `enums`
- `support`

扩展目录按需增加：

- `job`
- `listener`
- `scheduler`
- `export`
- `security`

## 命名规范

- 服务接口使用 `*Service`
- 服务实现使用 `*ServiceImpl`
- 实体使用业务名 `*Entity`
- 仓储使用业务名 `*Repository`
- 请求对象使用 `*Request`
- 响应对象使用 `*Response`
- 详情/列表视图使用 `*DetailView`、`*PageItem`

## 依赖约束

- `controller` 只依赖本模块或其他模块的 `service`
- 禁止直接依赖其他模块的 `repository`
- 禁止直接依赖其他模块的 `entity`
- 通用技术能力放 `net.upms.foundation.shared`
- 全局配置放 `net.upms.foundation.platform`
- 会话、安全、审计等基础设施优先复用 `foundation.platform`
