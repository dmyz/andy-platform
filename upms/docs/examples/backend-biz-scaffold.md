# Biz Scaffold

新增业务模块统一放在 `net.upms.biz.<domain>`。

## 顶层判断规则

优先判断是不是平台底座能力：

- 平台安全、异常、公共 API、Session → `net.upms.foundation.*`
- 平台主数据、组织、权限、配置 → `net.upms.system.*`
- 平台支撑能力，如文件、通知、审计 → `net.upms.support.*`
- 当前登录人工作台/个人中心聚合能力 → `net.upms.portal.*`
- 其余持续扩展的业务域 → `net.upms.biz.<domain>`

## 推荐结构

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

## 平台已有扩展预留示例

- `net.upms.support.notification.message`
- `net.upms.support.file.storage`
- `net.upms.support.file.preview`
- `net.upms.support.file.reference`
- `net.upms.system.iam.auth.credential`
- `net.upms.system.iam.auth.session`
- `net.upms.system.iam.auth.verification`
- `net.upms.biz.<domain>`

## 典型新增示例

- 审批中心 → `net.upms.biz.approval`
- 任务中心 → `net.upms.biz.task`
- 工单中心 → `net.upms.biz.ticket`
- 客户管理 → `net.upms.biz.customer`
