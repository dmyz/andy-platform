# Backend Architecture

## 顶层分组

后端代码以 `net.junanw.upms` 为根包，并按职责分为 5 层：

- `net.junanw.upms.foundation.*`：技术基础设施与公共能力
- `net.junanw.upms.system.*`：平台主数据、权限与系统配置
- `net.junanw.upms.support.*`：平台支撑能力
- `net.junanw.upms.portal.*`：当前登录用户视角模块
- `net.junanw.upms.biz.<domain>`：未来业务扩展域

当前顶层源码树如下：

```text
net.junanw.upms
├── foundation
├── system
├── support
├── portal
└── biz
```

## 当前模块归类

### `foundation`

- `foundation.shared`：公共响应、异常、JPA 基类、ID、通用支持类
- `foundation.platform.web`：平台通用 HTTP 端点
- `foundation.platform.security.config`：安全配置、权限守卫、异常处理器
- `foundation.platform.security.auth`：登录认证安全组件
- `foundation.platform.security.session`：基于会话事件的认证会话监听与销毁处理
- `foundation.platform.security.audit`：请求级操作审计拦截能力
- `foundation.platform.session.config`：Session 配置、Session ID 解析与 `HttpSessionEventPublisher`

### `system`

- `system.iam`：账号、认证、用户、角色、权限、导航
- `system.organization`：组织与组织成员关系
- `system.dictionary`：字典类型与字典项
- `system.setting`：系统配置项

### `support`

- `support.file`：文件能力
- `support.notification.announcement`：公告能力
- `support.notification.message`：消息中心预留位
- `support.audit.login`：登录审计
- `support.audit.operation`：操作审计

### `portal`

- `portal.workbench`：工作台
- `portal.profile`：个人中心

说明：`portal` 只承载“当前登录人视角”的聚合能力，不承载平台底座主数据。

### `biz`

- `biz.<domain>`：审批、任务、工单等未来业务域

## 模块内约定

每个业务模块默认采用以下结构：

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

按需可扩展：

- `job`
- `listener`
- `scheduler`
- `export`
- `security`

## 依赖约束

- `controller` 只依赖本模块或其他模块的 `service`
- 禁止直接依赖其他模块的 `repository`
- 禁止直接依赖其他模块的 `entity`
- 技术公共能力进入 `foundation`
- 跨模块业务协作优先通过 `service`

## 扩展建议

新增业务统一进入 `net.junanw.upms.biz.<domain>`。

新增平台支撑能力时，优先挂在现有支撑域下扩展，例如：

- `support.notification.message`
- `support.file.storage`
- `support.file.preview`
- `support.file.reference`
- `system.iam.auth.credential`
- `system.iam.auth.session`
- `system.iam.auth.verification`

## 会话链路

- 登录成功：`foundation.platform.security.auth` 完成认证并调用 `system.iam.auth.session.service.SessionRegistryService#register`
- 显式登出：认证接口先调用 `offline(...)`，再执行 `session.invalidate()`
- 会话销毁/超时：`foundation.platform.session.config.SessionConfig` 注册 `HttpSessionEventPublisher`，由 `foundation.platform.security.session.AuthenticatedSessionListener` 监听 `HttpSessionDestroyedEvent` 后调用 `offline(...)`
- 会话台账幂等更新：`system.iam.auth.session.service.SessionRegistryServiceImpl` 对重复下线请求直接忽略，避免显式登出和销毁事件重复记账
