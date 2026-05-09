---
name: xbatis-usage
description: 校验和维护 upms 后端分支的 XBatis 持久层用法，适用于检查 XBatis 使用是否规范、修复 XBatis 编译或测试问题、调整实体、Mapper、QueryChain 查询和分页逻辑，并防止 MyBatis-Flex 写法回退。
---

# XBatis 使用维护

当前分支持久层统一使用 XBatis。本技能用于维护和校验 XBatis 使用质量，确保后续持久层改动继续符合现有架构和 XBatis 约定，并防止重新引入 MyBatis-Flex 写法。

## 项目基线

- Spring Boot 使用 `4.0.4`。
- XBatis 使用 `xbatis-spring-boot-starter`。
- 版本线由 `xbatis-spring-boot-parent` 的 `1.10.1-spring-boot4` 管理。
- 公共实体基类位于 `src/main/java/net/junanw/upms/infrastructure/persistence/xbatis/`。
- 架构和模块边界以 `docs/架构与开发规范.md` 为准。

## 核心类型

- Mapper：`cn.xbatis.core.mybatis.mapper.MybatisMapper<T>`。
- 查询链：`cn.xbatis.core.sql.executor.chain.QueryChain`。
- 写入链：`InsertChain`、`UpdateChain`、`DeleteChain` 按已有模块写法使用。
- 实体注解：`cn.xbatis.db.annotations.Table`、`TableId`、`TableField`。
- 主键生成：`cn.xbatis.core.incrementer.Generators`、`cn.xbatis.db.IdAutoType`。
- 逻辑删除：`cn.xbatis.db.annotations.LogicDelete`。

## 使用范围

- 检查或修复实体映射、Mapper 继承、`QueryChain` 查询、分页、删除和批量操作。
- 修复 XBatis 相关编译错误、运行时 SQL 问题或测试失败。
- 新增持久层代码时确认其符合当前分支的 XBatis 模式。
- 回归检查是否重新引入 MyBatis-Flex 残留。

## ID 使用约定

- `Long` 类型实体主键统一使用 XBatis 生成器：

```java
@TableId(value = IdAutoType.GENERATOR, generator = Generators.nextId)
private Long id;
```

- 实体需要导入 `cn.xbatis.core.incrementer.Generators`、`cn.xbatis.db.IdAutoType` 和 `cn.xbatis.db.annotations.TableId`。
- 已标注上述 `@TableId` 的主键字段不需要、也不应该在 Service 或其他业务代码中手动设置 ID，例如不要调用 `entity.setId(IdGenerator.nextId())`。
- 插入后若后续逻辑需要使用新主键，应先执行 `mapper.save(entity)`，再读取 `entity.getId()`。
- `net.junanw.upms.infrastructure.shared.id.IdGenerator.nextId()` 是静态方法，仅用于确实需要显式生成业务编码或非实体主键 ID 的场景。

## 实体约定

- 类上使用 `cn.xbatis.db.annotations.Table`。
- 公共字段优先复用 `BaseEntity`、`AuditableEntity`、`SoftDeletableEntity` 等现有基类。
- 逻辑删除字段使用 `deleted`，语义保持 `0=false`、`1=true`。
- 审计字段和自动填充值保持与现有基类一致。
- 需要乐观锁时使用 `version` 字段并补齐对应 XBatis 注解。
- 不把 Entity 暴露给 `application.upms` Controller 返回值。

## Mapper 与查询约定

- Mapper 位于本模块 `mapper` 包。
- Mapper 继承 `cn.xbatis.core.mybatis.mapper.MybatisMapper<T>`。
- 不新增 repository 包。
- 不跨模块直接访问其他模块 Mapper 或 Entity。
- Service 内可以使用本模块 Mapper 和 XBatis `QueryChain`。
- `application.upms` 禁止直接依赖 Mapper、Entity 或 `QueryChain`。
- 动态条件优先使用 XBatis 条件能力，避免拼接 SQL 字符串。
- 分页返回结构与项目现有分页模型保持一致。

## 工作流程

1. 先阅读目标模块的实体、Mapper、Service 实现。
2. 对比同类模块，沿用已有 XBatis 注解、查询构造、分页和删除写法。
3. 修改实体、Mapper 或查询逻辑后，运行项目检查脚本：

```bash
bash .codex/skills/xbatis-usage/scripts/check-xbatis-usage.sh
```

4. 若改动会影响 `/admin/**` Controller 或应用层分包，同时使用 `upms-architecture` skill。
5. 优先运行相关单测；若同时使用 `upms-architecture` skill，再运行其架构检查脚本；最后按改动范围运行 `mvn test`。

## 禁止事项

- 不要按旧 MyBatis-Flex 模式新增依赖、注解、Wrapper 或 TableDef。
- 不要在 `application.upms` 中写 Mapper 调用、Entity 访问或 `QueryChain`。
- 不要通过直接改库替代 Flyway 迁移脚本。
- 不要为实体主键手动调用 `IdGenerator.nextId()`。
