---
name: db-migrate
description: 为 UPMS 后端创建新的 Flyway 数据库迁移脚本，适用于用户要求新增数据库迁移、创建 migration、生成 Flyway 脚本或分配下一个迁移版本号时。
---

# 创建 Flyway 迁移脚本

用于在 `upms/src/main/resources/db/migration/` 下创建新的 Flyway SQL 脚本。

## 工作流程

1. 确认迁移描述。
   - 如果用户已给出描述，直接使用。
   - 如果未给出，询问这次迁移的目的，例如 `add_user_avatar`、`create_order_table`。
2. 读取 `upms/src/main/resources/db/migration/` 中的现有脚本。
3. 找到最大版本号，新版本号为最大版本号加一。
4. 生成文件名：`V{version}__{description}.sql`。
   - 版本号使用三位数字，例如 `V014`。
   - 描述使用下划线，不使用连字符。
5. 创建 SQL 文件，并写入基础头部：

```sql
-- 描述：<description>
-- 作者：<git user.name>
-- 日期：<YYYY-MM-DD>

-- 在此编写 SQL 语句
```

## 约束

- Flyway 脚本一旦执行，不要修改原脚本；后续变更创建新的迁移脚本。
- 不要直接修改数据库结构来替代迁移脚本。
- 创建后提醒用户补充 SQL 并在应用重启时验证迁移。
