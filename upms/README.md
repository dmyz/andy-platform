# ja-upms worktree

- 架构说明：`docs/ARCHITECTURE.md`

## 当前状态

- 根包统一为 `net.upms`
- 顶层分组固定为 `foundation / system / support / portal / biz`
- 默认通过 `application-dev.yaml` 连接本地 MySQL 与 Redis
- 平台基础域数据全部走数据库持久化，不再依赖运行时 bootstrap 假数据
- 不再维护独立的 `bootstrap` profile，默认仅支持数据库/JPA 运行路径
- 启动时自动初始化认证、组织、角色、权限、导航、字典、系统配置、公告、文件、审计等基础表与示例数据

## 本地启动

### 前置条件

- MySQL 已创建数据库 `upms`
- Redis 运行在 `127.0.0.1:6379`

### 启动命令

```bash
mvn spring-boot:run
```

## 初始化脚本位置

- `src/main/resources/db/mysql/001_auth_schema.sql`
- `src/main/resources/db/mysql/002_auth_seed.sql`
- `src/main/resources/db/mysql/003_audit_schema.sql`
- `src/main/resources/db/mysql/004_audit_seed.sql`
- `src/main/resources/db/mysql/005_operation_audit_schema.sql`
- `src/main/resources/db/mysql/006_operation_audit_seed.sql`
- `src/main/resources/db/mysql/007_file_schema.sql`
- `src/main/resources/db/mysql/008_file_seed.sql`
- `src/main/resources/db/mysql/009_announcement_schema.sql`
- `src/main/resources/db/mysql/010_announcement_seed.sql`
- `src/main/resources/db/mysql/011_org_rbac_schema.sql`
- `src/main/resources/db/mysql/012_navigation_meta_schema.sql`
- `src/main/resources/db/mysql/013_platform_seed.sql`

## 默认示例账号

- `admin / admin`
- `lisi / lisi`
- `wangwu / wangwu`
- `zhaoliu / zhaoliu`
- `sunqi / sunqi`

说明：`zhouba` 为禁用示例账号，默认不可登录。
