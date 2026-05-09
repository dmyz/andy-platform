---
name: start-watch-fix
description: 启动并监听 upms 后端开发服务，适用于用户要求启动项目、持续观察日志、发现启动或运行错误时交给 subagent 修复，并在修复完成后重启项目、验证健康检查和汇报结果。
---

# 启动监听与错误修复

## 基本目标

按项目约定启动 `upms` 后端服务，持续监听启动日志和端口状态。若发现编译失败、上下文启动失败、端口占用、数据库或 Redis 连接失败、运行期异常等问题，先提取关键错误，再把明确且可并行处理的修复任务交给 subagent；修复完成后重新启动项目并验证。

## 启动前检查

在 `upms-worktree/` 根目录执行：

```bash
git status --short
lsof -nP -iTCP:8004 -sTCP:LISTEN
redis-cli -h 127.0.0.1 -p 6379 ping
```

- 工作树已有未提交变更时，只围绕启动错误做最小必要处理，不回退用户改动。
- `8004` 已被占用时，先确认占用进程；不要擅自杀进程，除非用户明确要求或该进程是本轮启动留下的服务。
- Redis 不可用或 MySQL 配置明显缺失时，优先向用户报告环境前置条件，不把环境问题误判为代码问题。

## 启动与监听

优先使用可用的 Maven 命令启动：

```bash
mvn spring-boot:run
```

如果项目约定的 `MAVEN_HOME` 不存在或不可执行，使用 `which mvn` 找到系统 Maven，并说明实际使用的路径。

启动后持续监听日志，至少确认以下信号：

- 出现 `Started UpmsApplication`。
- Tomcat 监听 `8004`。
- `/actuator/health` 返回 `200`。
- `/v3/api-docs` 返回 `200`。
- 继续观察一小段日志，没有新的 `ERROR`、异常堆栈或失败重启。

常用验证命令：

```bash
curl -s -o /dev/null -w '%{http_code}' http://localhost:8004/actuator/health
curl -s -o /dev/null -w '%{http_code}' http://localhost:8004/v3/api-docs
lsof -nP -iTCP:8004 -sTCP:LISTEN
```

## 错误处理

发现错误时，先归类并提取最小可复现信息：

- 编译错误：记录类名、方法、行号、编译器错误。
- Spring 上下文错误：记录首个 `Caused by`、Bean 名称、依赖链和失败配置。
- 数据库迁移错误：记录 Flyway 版本、SQL 文件名、失败语句和数据库错误码。
- 连接错误：区分 MySQL、Redis、端口占用、网络或权限问题。
- 运行期接口错误：记录请求路径、状态码、异常类型和关键堆栈。

如果错误来自代码或配置，并且可以并行处理，启动 subagent：

- 使用 `worker` 处理明确修复任务，例如某个编译错误、Bean 注入失败、实体或 Mapper 用法错误。
- 告诉 subagent：它不独占代码库，必须保护已有改动，不要回退他人修改。
- 明确分配责任范围和输出要求：修改哪些文件、复现命令、验证命令、最终说明。
- 不把环境问题、凭据问题、端口占用等无法从代码修复的问题交给 subagent。

subagent 返回后，快速审查它的改动；如涉及架构边界或 XBatis 持久层，按需加载并执行对应 skill 的检查脚本。

## 修复后重启

修复完成后，停止本轮失败或旧的启动进程，再重新执行启动命令。不要保留多个 `spring-boot:run` 会话同时监听同一端口。

重启后重复健康检查：

```bash
curl -s -o /dev/null -w '%{http_code}' http://localhost:8004/actuator/health
curl -s -o /dev/null -w '%{http_code}' http://localhost:8004/v3/api-docs
```

如果仍失败，继续按“错误处理”流程归类；不要在没有新信息时重复同一个修复。

## 汇报要求

最终汇报使用中文，包含：

- 当前服务状态、监听端口和 PID。
- 健康检查与 OpenAPI 检查结果。
- 是否发现错误、是否启用 subagent。
- 如启用 subagent，说明修复摘要、涉及文件和重启验证结果。
- 如未能启动，说明阻塞原因、已确认的环境状态和下一步需要用户处理的事项。
