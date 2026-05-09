# UPMS 前后端接口差异报告

生成时间：2026-05-01

## 范围与基准

- 后端契约基准：`upms-worktree` 当前源码中的 Controller、Request、Response、View。
- 前端扫描范围：`upms-web-worktree/src/api/`、`src/types/`、`src/views/`、`src/components/` 中与接口路径、字段、请求封装相关的代码。
- 本报告只记录差异，不修改前后端业务代码。后续实施对齐时，应以本报告替代过期的 `docs/API_COMPARISON.md` / `docs/API_ENDPOINTS.md` 口径。

## 修复进展

- 2026-05-01 已开始按本报告修复前端代码；下方差异清单保留为修复前基线。
- 已统一主要 API 路径为后端真实 `/admin/...`，由 `VITE_API_BASE_URL=/api` 和请求层拼成 `/api/admin/...`。
- 已去除 API 层对 `ApiResponse<T>` 返回泛型的误用，分页类型统一为 `{ list, total, pageNum, pageSize }`。
- 已修复认证、当前用户、通知铃铛、仪表盘、用户、角色、组织、权限、导航、字典、设置、会话、审计、文件、公告、个人中心等模块的主要路径和字段契约。
- 已删除或收敛一批后端无对应契约的旧 API 导出，例如 token 重置、首次登录改密旧函数、登录审计详情、组织成员增删、批量文件上传、会话详情/统计等。
- 当前静态验证：`npm run type-check` 通过，`npm run lint` 通过。

## 静态扫描摘要

- 后端接口：15 个 Controller，102 个 `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping`。
- 前端 API：14 个 `src/api/*.ts` 文件，147 个 `export function`。
- 页面实际调用：18 个 `src/views/**` 文件导入 `@/api/*`，另有 `src/components/layout/NotificationBell.vue` 直接导入 `@/utils/request`。
- 代理口径：`.env` 与 `.env.development` 均为 `VITE_API_BASE_URL=/api`；`vite.config.ts:56` 只代理 `/api`，并在转发前 `rewrite: path => path.replace('/api', '')`。

## 后端接口契约清单

| 模块 | 后端 Controller | 真实路径 |
| --- | --- | --- |
| 认证 | `foundation/platform/auth/controller/AuthController.java:34` | `POST /admin/auth/login`、`POST /admin/auth/code/mobile/send`、`POST /admin/auth/code/email/send`、`GET /admin/auth/current-user`、`POST /admin/auth/logout`、`POST /admin/auth/password/change`、`POST /admin/auth/password/reset`、`GET /admin/auth/captcha` |
| 在线会话 | `foundation/platform/auth/controller/AuthSessionController.java:20` | `GET /admin/auth/session/page`、`POST /admin/auth/session/{id}/offline`、`POST /admin/auth/session/offline` |
| 用户 | `system/iam/user/controller/UserController.java:40` | `GET /admin/user/page`、`GET /admin/user/export`、`GET /admin/user/{id}`、`POST /admin/user`、`PUT /admin/user/{id}`、`DELETE /admin/user/{id}`、`PUT /admin/user/{id}/status`、`POST /admin/user/{id}/password/reset`、`POST /admin/user/{id}/offline`、`POST /admin/user/import`、`GET /admin/user/{id}/roles`、`POST /admin/user/{id}/roles` |
| 角色 | `system/iam/role/controller/RoleController.java:32` | `GET /admin/role/page`、`GET /admin/role/{id}`、`POST /admin/role`、`PUT /admin/role/{id}`、`DELETE /admin/role/{id}`、`PUT /admin/role/{id}/status`、`GET /admin/role/{id}/permissions`、`POST /admin/role/{id}/permissions`、`GET /admin/role/{id}/users`、`GET /admin/role/permission/catalog` |
| 组织 | `system/organization/controller/OrganizationController.java:29` | `GET /admin/org/tree`、`GET /admin/org/{id}`、`POST /admin/org`、`PUT /admin/org/{id}`、`DELETE /admin/org/{id}`、`PUT /admin/org/{id}/status`、`GET /admin/org/{id}/members` |
| 权限 | `system/iam/permission/controller/PermissionController.java:29` | `GET /admin/permission/page`、`GET /admin/permission/{id}`、`POST /admin/permission`、`PUT /admin/permission/{id}`、`DELETE /admin/permission/{id}`、`GET /admin/permission/current-user/codes` |
| 导航 | `system/iam/navigation/controller/NavigationController.java:29` | `GET /admin/navigation/tree`、`GET /admin/navigation/{id}`、`POST /admin/navigation`、`PUT /admin/navigation/{id}`、`DELETE /admin/navigation/{id}`、`GET /admin/navigation/{id}/permissions`、`POST /admin/navigation/{id}/permissions` |
| 字典 | `system/dictionary/controller/DictionaryController.java:31` | `GET /admin/dictionary/page`、`GET /admin/dictionary/{id}`、`POST /admin/dictionary`、`PUT /admin/dictionary/{id}`、`DELETE /admin/dictionary/{id}`、`GET /admin/dictionary/{id}/items`、`POST /admin/dictionary/{id}/items`、`PUT /admin/dictionary/item/{itemId}`、`DELETE /admin/dictionary/item/{itemId}`、`GET /admin/dictionary/code/{dictCode}` |
| 设置 | `system/setting/controller/SettingController.java:26` | `GET /admin/setting/page`、`GET /admin/setting/{id}`、`POST /admin/setting`、`PUT /admin/setting/{id}`、`DELETE /admin/setting/{id}`、`GET /admin/setting/key/{settingKey}` |
| 个人中心 | `portal/profile/controller/ProfileController.java:30` | `GET /admin/profile/me`、`PUT /admin/profile/me`、`POST /admin/profile/avatar`、`POST /admin/profile/mobile/code/send`、`POST /admin/profile/email/code/send`、`POST /admin/profile/mobile/change`、`POST /admin/profile/email/change`、`GET /admin/profile/login-audit/page`、`GET /admin/profile/messages/page` |
| 仪表盘 | `portal/workbench/controller/WorkbenchController.java:23` | `GET /admin/dashboard/summary`、`GET /admin/dashboard/recent-operations`、`GET /admin/dashboard/favorite-navigations`、`GET /admin/dashboard/announcements` |
| 公告 | `support/notification/announcement/controller/AnnouncementController.java:29` | `GET /admin/announcement/page`、`GET /admin/announcement/{id}`、`POST /admin/announcement`、`PUT /admin/announcement/{id}`、`DELETE /admin/announcement/{id}`、`POST /admin/announcement/{id}/publish`、`POST /admin/announcement/{id}/revoke`、`POST /admin/announcement/{id}/read`、`GET /admin/announcement/my/page` |
| 文件 | `support/file/controller/FileController.java:34` | `GET /admin/file/page`、`POST /admin/file/upload`、`GET /admin/file/{id}`、`GET /admin/file/{id}/download`、`GET /admin/file/{id}/preview`、`DELETE /admin/file/{id}` |
| 登录审计 | `support/audit/login/controller/LoginAuditController.java:28` | `GET /admin/login-audit/page`、`GET /admin/login-audit/export` |
| 操作审计 | `support/audit/operation/controller/OperationAuditController.java:30` | `GET /admin/operation-audit/page`、`GET /admin/operation-audit/{id}`、`GET /admin/operation-audit/export` |

## 全局差异

| 优先级 | 差异 | 来源 |
| --- | --- | --- |
| P0 | 前端 API 字面量应写后端真实路径 `/admin/...`，由请求层拼成 `/api/admin/...`。当前存在 `/user`、`/role`、`/org`、`/profile`、`/file`、`/announcement`、`/api/dashboard/*` 等路径，开发代理会转发成后端不存在的路径。 | 后端：所有 Controller 以 `/admin/...` 为根；前端：`src/api/user.ts:168`、`src/api/role.ts:144`、`src/api/organization.ts:141`、`src/api/profile.ts:55`、`src/api/file.ts:145`、`src/api/announcement.ts:52`、`src/api/dashboard.ts:6`；代理：`vite.config.ts:56` |
| P0 | `request.ts` 已自动解包 `{ code, message, data }`，API 泛型不应再写 `ApiResponse<T>`。否则页面拿到的运行时值是 `T`，类型却显示为 `ApiResponse<T>`。 | 后端统一响应：`foundation/shared/api/ApiResponse.java:11`；前端解包：`src/utils/request.ts:225`；误用：`src/api/user.ts:16`、`src/api/role.ts:36`、`src/api/organization.ts:34`、`src/api/profile.ts:56`、`src/api/session.ts:45`、`src/api/file.ts:32`、`src/api/navigation.ts:80` |
| P0 | 分页结构应为 `{ list, total, pageNum, pageSize }`，不是 `items/page/pageSize`。 | 后端：`foundation/shared/api/PageResponse.java:14`；前端旧类型：`src/types/responses/common.ts:12`；部分新 API 已局部使用 `list/pageNum`，但旧 `PageResponse<T>` 仍错误 |
| P0 | 文件下载应使用 `request.download` 或专门的下载方法；当前部分导出/下载仍用 `request.get(..., { responseType: 'blob' })`，`request.get` 的第二参数是 query params，不支持 axios 风格 `responseType`。 | 前端封装：`src/utils/request.ts`；误用：`src/api/audit.ts:98`、`src/api/audit.ts:108`、`src/api/file.ts:95` |
| P0 | 页面/组件不应直接绕过 `src/api/` 调 `@/utils/request`。 | 直接调用：`src/components/layout/NotificationBell.vue:5`、`src/components/layout/NotificationBell.vue:29`、`src/components/layout/NotificationBell.vue:53` |
| P1 | 后端状态字段在主数据模块多为 `Integer status`，前端旧实体仍有字符串状态枚举。 | 后端：`UserSaveRequest.java:44`、`RoleSaveRequest.java:27`、`PermissionSaveRequest.java:38`、`DictionaryTypeSaveRequest.java:23`；前端：`src/api/user.ts:50`、`src/types/entities/user.ts:16`、`src/types/entities/organization.ts:17` |
| P1 | 权限分配使用权限编码 `permissionCodes`，不是 `permissionIds`。 | 后端：`RolePermissionAssignRequest.java:14`、`NavigationPermissionAssignRequest.java:17`；前端错误：`src/api/role.ts:21`、`src/api/role.ts:77`；前端正确残留：`src/api/role.ts:205`、`src/api/navigation.ts:121` |
| P2 | 日期时间后端为 `LocalDateTime`，前端应统一按字符串处理。 | 后端示例：`UserPageItem.java:31`、`RolePageItem.java:30`；前端大多已用 `string`，需要保持一致 |

## P0：登录、当前用户、布局导航与代理口径

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 修改密码路径错误 | `POST /admin/auth/password/change`，`ChangePasswordRequest`，来源 `AuthController.java:106` | `changePassword` 调 `/admin/auth/change-password`，来源 `src/api/auth.ts:39`；`src/views/user/profile.vue:7` 使用该函数 | 改为 `/admin/auth/password/change` |
| 首次登录改密接口不存在 | 后端无 `/admin/auth/first-time-password` | `firstTimeChangePassword` 调 `/admin/auth/first-time-password`，来源 `src/api/auth.ts:46`；`src/views/login/first-time-password.vue:7` 使用 | 页面流程应复用 `/admin/auth/password/change` 或以后端新增接口为准 |
| 通用验证码接口不存在 | 后端只有 `/admin/auth/code/mobile/send`、`/admin/auth/code/email/send`，来源 `AuthController.java:73`、`AuthController.java:81` | `sendVerificationCode` 调 `/admin/auth/verification-code/send`，来源 `src/api/auth.ts:73` | 删除或改为按 `targetType` 分发到两个后端接口 |
| 忘记密码路径不存在 | 后端无 `/admin/auth/forgot-password` | `forgotPassword` 调 `/admin/auth/forgot-password`，来源 `src/api/auth.ts:94`；`src/views/login/forgot-password.vue:7` 使用 | 对齐为 `/admin/auth/password/reset` 的验证码重置流程，或由后端补契约 |
| token 重置路径不存在 | 后端只有 `POST /admin/auth/password/reset`，来源 `AuthController.java:116` | `resetPassword` 调 `/admin/auth/reset-password`，来源 `src/api/auth.ts:113` | 删除 token 版旧接口或后端补契约 |
| 登录响应字段需保持 `accessToken/tokenType/expiresIn` | `LoginTokenResponse` 字段见 `LoginTokenResponse.java:13` | `login`、`loginWithCode` 均调 `/admin/auth/login`，来源 `src/api/auth.ts:18`、`src/api/auth.ts:87` | 类型以 `LoginTokenResponse` 为准，避免旧 `token` 字段 |
| 当前用户响应需保持 `user/permissions/navigations` | `CurrentUserResponse` 字段见 `CurrentUserResponse.java:12`，`UserInfoView.avatar` 见 `UserInfoView.java:15` | `getCurrentUser` 路径正确，来源 `src/api/auth.ts:32`；布局使用 `userInfo.avatar`，来源 `src/components/layout/UserMenu.vue:57` | 继续按 `avatar` 字段，不引入 `avatarUrl` |
| 通知铃铛路径缺 `/admin` 且绕过 API 层 | 后端为 `GET /admin/announcement/my/page`、`POST /admin/announcement/{id}/read`，来源 `AnnouncementController.java:98`、`AnnouncementController.java:90` | `NotificationBell.vue` 直接 `get('/announcement/my/page')`、`post('/announcement/${id}/read')`，来源 `src/components/layout/NotificationBell.vue:29`、`:53` | 改走 `src/api/announcement.ts`，路径使用 `/admin/announcement/...` |
| 仪表盘首屏接口全不匹配 | 后端为 `/admin/dashboard/summary`、`recent-operations`、`favorite-navigations`、`announcements`，来源 `WorkbenchController.java:34`、`:41`、`:48`、`:55` | `src/api/dashboard.ts` 调 `/api/dashboard/stats`、`activities`、`notifications`、`quick-actions`，来源 `src/api/dashboard.ts:6`、`:13`、`:20`、`:27`；`src/views/dashboard/index.vue:16` 使用 | 重新映射到后端四个接口，并去掉路径字面量中的 `/api` |

## P1：用户、角色、组织、权限、导航

### 用户

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 资源路径错误，且同文件有两套 API | `/admin/user/page`、`/admin/user/{id}`、`POST /admin/user`、`PUT /admin/user/{id}`、`DELETE /admin/user/{id}`，来源 `UserController.java:50`、`:117`、`:124`、`:131`、`:138` | 旧 CRUD 用 `/admin/users`，来源 `src/api/user.ts:16`、`:23`、`:30`；实际页面用 `/user/page`、`/user/{id}`，来源 `src/api/user.ts:168`、`:181`；`src/views/system/user/index.vue:19` 使用 | 统一为 `/admin/user/...`，清理 `/admin/users` 和 `/user` 两套旧口径 |
| 用户状态旧枚举错误 | `UserStatusUpdateRequest.status` 为 `Integer`，来源 `UserController.java:146`、`UserStatusUpdateRequest.java:12` | `updateUserStatus` 接收 `'ACTIVE' | 'INACTIVE' | 'LOCKED'`，来源 `src/api/user.ts:50`；实际页面用数字，来源 `src/views/system/user/index.vue:331` | 保留数字状态，删除字符串枚举版旧函数 |
| 重置密码路径和请求体错误 | `POST /admin/user/{id}/password/reset` 无请求体，来源 `UserController.java:154` | 旧函数调 `/admin/users/{id}/reset-password` 且传 `{ newPassword }`，来源 `src/api/user.ts:57`；实际函数 `/user/{id}/password/reset` 缺 `/admin`，来源 `src/api/user.ts:229` | 改为 `POST /admin/user/{id}/password/reset`，不传新密码 |
| 用户角色接口路径需对齐 | `GET/POST /admin/user/{id}/roles`，请求体 `roleCodes`，来源 `UserController.java:177`、`:184`、`UserRoleAssignRequest.java:12` | 旧函数 `/admin/users/{id}/roles`，实际函数 `/user/{id}/roles`，来源 `src/api/user.ts:64`、`:215`、`:222` | 统一为 `/admin/user/{id}/roles` |
| 用户组织分配接口后端不存在 | 后端无 `/admin/user/{id}/organizations` | `assignOrganizations`、`getUserOrganizations` 调 `/admin/users/{id}/organizations`，来源 `src/api/user.ts:71`、`:85` | 删除前端旧函数，组织归属通过用户保存请求 `orgId` 管理 |
| 个人资料和头像不属于用户模块 | 后端个人中心为 `/admin/profile/me`、`/admin/profile/avatar` | `updateProfile`、`uploadAvatar` 调 `/admin/users/profile`、`/admin/users/avatar`，来源 `src/api/user.ts:92`、`:99` | 移到 `src/api/profile.ts` 并按个人中心契约实现 |

### 角色

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 资源路径错误，且同文件有两套 API | `/admin/role/page`、`/admin/role/{id}`、`POST /admin/role` 等，来源 `RoleController.java:42`、`:55`、`:62` | 旧函数用 `/admin/roles`，实际页面函数用 `/role`，来源 `src/api/role.ts:36`、`:144`、`:157`；`src/views/system/role/index.vue:17` 使用 | 统一为 `/admin/role/...` |
| `/admin/roles/all` 不存在 | 后端无 all/list 不分页接口；可用 `/admin/role/page?pageNum=1&pageSize=...` 或新增后端接口 | `getAllRoles` 调 `/admin/roles/all`，来源 `src/api/role.ts:42` | 删除旧函数或转为分页查询 |
| 创建/更新字段不完整 | `RoleSaveRequest` 需要 `name/code/dataScope/status/remark`，来源 `RoleSaveRequest.java:16` | 旧 `CreateRoleRequest` 有 `description`、`permissionIds`，缺 `dataScope/status`，来源 `src/api/role.ts:17` | 以 `RoleSaveRequest` 为准 |
| 权限分配方法和字段错误 | `POST /admin/role/{id}/permissions`，请求体 `{ permissionCodes }`，来源 `RoleController.java:99`、`RolePermissionAssignRequest.java:14` | 旧 `assignPermissions` 用 `/admin/roles/{id}/permissions` 和 `{ permissionIds }`，来源 `src/api/role.ts:77`；实际 `assignRolePermissions` 字段正确但路径 `/role`，来源 `src/api/role.ts:205` | 路径改 `/admin/role/{id}/permissions`，字段保持 `permissionCodes` |

### 组织

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 资源名应为 `/admin/org` | 后端为 `/admin/org/tree`、`/admin/org/{id}`、`POST /admin/org` 等，来源 `OrganizationController.java:29` | 旧函数用 `/admin/organizations`，实际页面函数用 `/org`，来源 `src/api/organization.ts:34`、`:141`；`src/views/system/organization/index.vue:15` 使用 | 统一为 `/admin/org/...` |
| 组织成员只支持查询 | 后端只有 `GET /admin/org/{id}/members`，查询参数 `displayName/status`，来源 `OrganizationController.java:83` | 前端旧函数有新增/移除成员 `/admin/organizations/{id}/members`、`/{userId}`，来源 `src/api/organization.ts:82`、`:89`；实际函数传 `includeSubOrgs`，来源 `src/api/organization.ts:155` | 删除新增/移除接口；查询参数改为后端支持的 `displayName/status` |
| 字段命名需统一 | `OrganizationSaveRequest` 使用 `parentId/name/code/leader/level/sort/status/remark`，来源 `OrganizationSaveRequest.java` | 旧 `CreateOrganizationRequest` 有 `type/sortOrder/description`，来源 `src/api/organization.ts:12` | 页面实际表单更接近后端，清理旧类型 |

### 权限

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 路径缺 `/admin` | `/admin/permission/page`、`/admin/permission/{id}`、`POST /admin/permission` 等，来源 `PermissionController.java:51`、`:77`、`:89` | 前端使用 `/permission/page`、`/permission/{id}`、`/permission`，来源 `src/api/permission.ts:49`、`:62`、`:69`；`src/views/system/permission/index.vue:13` 使用 | 统一为 `/admin/permission/...` |
| 权限列表接口不存在 | 后端无 `/admin/permission/list`，只有分页和当前用户权限编码 | `getPermissionList` 调 `/permission/list`，来源 `src/api/permission.ts:89` | 删除或改用分页；当前用户权限编码使用 `/admin/permission/current-user/codes` |
| 当前用户权限编码接口前端缺失 | `GET /admin/permission/current-user/codes`，来源 `PermissionController.java:126` | `src/api/permission.ts` 未实现 | 补充 `getCurrentUserPermissionCodes` |

### 导航

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 路径缺 `/admin` | `/admin/navigation/tree`、`/admin/navigation/{id}`、`POST /admin/navigation` 等，来源 `NavigationController.java:39`、`:46`、`:53` | `src/api/navigation.ts` 使用 `/navigation/...`，来源 `src/api/navigation.ts:79`、`:86`、`:93`；`src/views/system/navigation/index.vue:14` 使用 | 统一为 `/admin/navigation/...` |
| 导航旧文档接口不存在 | 后端无 `/admin/navigation/list`、`/my`、`/favorite`、`/order`、`/toggle` | 过期文档仍记录相关接口，来源 `docs/API_COMPARISON.md:145`、`:146`、`:149`，`docs/API_ENDPOINTS.md:140`、`:142`、`:151`、`:152` | 从文档和旧函数中清理 |
| 权限分配字段正确但路径错误 | 后端 `NavigationPermissionAssignRequest.permissionCodes`，来源 `NavigationPermissionAssignRequest.java:15` | 前端请求体正确，路径 `/navigation/{id}/permissions` 缺 `/admin`，来源 `src/api/navigation.ts:121` | 只修路径 |

## P2：字典、设置、会话、审计、文件、公告、个人中心、仪表盘

### 字典

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 路径缺 `/admin` | `/admin/dictionary/page`、`/admin/dictionary/{id}`、`/admin/dictionary/{id}/items` 等，来源 `DictionaryController.java:50`、`:74`、`:126` | `src/api/dictionary.ts` 使用 `/dictionary/...`，来源 `src/api/dictionary.ts:96`、`:109`、`:137`；`src/views/system/dictionary/index.vue:16` 使用 | 统一为 `/admin/dictionary/...` |
| 字典选项接口前端缺失 | `GET /admin/dictionary/code/{dictCode}`，来源 `DictionaryController.java:177` | `src/api/dictionary.ts` 未实现 | 补充 `getDictionaryOptionsByCode` |
| 查询参数封装目前在本文件是正确用法，但需要全局统一 | `request.get<T>(url, params?, options?)` 第二参数是 params | `getDictionaryPage` 和 `getDictionaryItems` 直接传 params，来源 `src/api/dictionary.ts:97`、`:137`；其他文件大量传 `{ params }` | 后续修复时统一改为直接传 params，而不是 axios 风格 `{ params }` |

### 设置

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 路径缺 `/admin` | `/admin/setting/page`、`/admin/setting/{id}`、`/admin/setting/key/{settingKey}`，来源 `SettingController.java:36`、`:57`、`:86` | `src/api/setting.ts` 使用 `/setting/...`，来源 `src/api/setting.ts:54`、`:66`、`:73`；`src/views/system/setting/index.vue:12` 使用 | 统一为 `/admin/setting/...` |
| 按 key 查询接口前端缺失 | `GET /admin/setting/key/{settingKey}`，来源 `SettingController.java:86` | `src/api/setting.ts` 未实现 | 补充 `getSettingByKey` |

### 会话

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 路径缺 `/admin` | `GET /admin/auth/session/page`、`POST /admin/auth/session/{id}/offline`、`POST /admin/auth/session/offline`，来源 `AuthSessionController.java:29`、`:51`、`:58` | `getSessionPage` 用 `/auth/session/page`，`offlineSession` 用 `/auth/session/offline`，来源 `src/api/session.ts:37`、`:58`；`src/views/system/session/index.vue:6` 使用 | 统一为 `/admin/auth/session/...` |
| 大量 `/admin/sessions/*` 后端不存在 | 后端没有 current-user、detail、batch、others、user、refresh、stats | 前端实现 `getCurrentUserSessions`、`getSession`、`terminateSessions`、`terminateOtherSessions`、`terminateUserSessions`、`refreshSession`、`getOnlineUserStats`，来源 `src/api/session.ts:44` 至 `:93` | 删除旧函数或由后端明确新增 |
| 离线兼容接口请求字段需确认 | 后端 `POST /admin/auth/session/offline` 接收 query `id` 或 body map，来源 `AuthSessionController.java:58` | 前端传 `{ sessionKey }`，来源 `src/api/session.ts:58` | 改为 `{ id }` 或路径版 `/{id}/offline` |

### 审计

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 登录审计路径缺 `/admin` | `GET /admin/login-audit/page`、`GET /admin/login-audit/export`，来源 `LoginAuditController.java:39`、`:62` | `/login-audit/page`、`/login-audit/export`，来源 `src/api/audit.ts:67`、`:97`；`src/views/audit/login/index.vue:6` 使用 | 统一为 `/admin/login-audit/...` |
| 登录审计详情接口不存在 | 后端无 `GET /admin/login-audit/{id}` | `getLoginAuditDetail` 调 `/admin/audit/login/{id}`，来源 `src/api/audit.ts:74` | 删除或后端补接口 |
| 操作审计详情路径错误 | `GET /admin/operation-audit/{id}`，来源 `OperationAuditController.java:66` | `getOperationAuditDetail` 调 `/audit/operation/{id}`，来源 `src/api/audit.ts:90`；`src/views/audit/operation/index.vue:6` 使用 | 改为 `/admin/operation-audit/{id}` |
| 导出方法不匹配请求封装 | 后端导出为 `GET /admin/login-audit/export`、`GET /admin/operation-audit/export` | 前端用 `request.get(..., { responseType: 'blob' })`，来源 `src/api/audit.ts:97`、`:107` | 改用 `request.download` |

### 文件

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 资源路径应为 `/admin/file` 单数 | `GET /admin/file/page`、`POST /admin/file/upload`、`GET /admin/file/{id}`，来源 `FileController.java:45`、`:60`、`:67` | 旧函数用 `/admin/files`，实际函数用 `/file`，来源 `src/api/file.ts:32`、`:146`、`:158`；`src/views/file/index.vue:12` 使用 | 统一为 `/admin/file/...` |
| 批量上传后端不存在 | 后端只有单文件上传 `POST /admin/file/upload` | `uploadFiles` 调 `/admin/files/batch-upload`，来源 `src/api/file.ts:65` | 删除或后端补接口 |
| 头像上传、分片上传旧文档不可信 | 后端无 `/admin/file/upload/avatar`、`/upload/chunk`、`/upload/chunk/merge` | 旧文档记录这些接口，来源 `docs/API_COMPARISON.md:119` 至 `:121` | 从文档和旧 API 清理 |
| 上传请求头不应手动写 multipart | `request.ts` 对 FormData 会让浏览器设置 boundary | `uploadFile` 手动设置 `'Content-Type': 'multipart/form-data'`，来源 `src/api/file.ts:49` | 删除该 header |

### 公告

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 路径缺 `/admin` | `/admin/announcement/page`、`/{id}`、`publish/revoke/read`，来源 `AnnouncementController.java:40` 至 `:90` | `src/api/announcement.ts` 使用 `/announcement/...`，来源 `src/api/announcement.ts:52`、`:65`、`:93`；`src/views/announcement/index.vue:14` 使用 | 统一为 `/admin/announcement/...` |
| 收件箱路径错误 | `GET /admin/announcement/my/page`，来源 `AnnouncementController.java:98` | `getInboxAnnouncementPage` 调 `/announcement/inbox`，来源 `src/api/announcement.ts:106`；`src/views/announcement/inbox.vue:6` 使用 | 改为 `/admin/announcement/my/page` |
| 状态字段类型不一致 | 后端 `AnnouncementPageItem.status` 是 `String`，来源 `AnnouncementPageItem.java:26` | 前端 `AnnouncementPageItem.status` 为 `number`，来源 `src/api/announcement.ts:9`，页面搜索也按字符串，来源 `src/views/announcement/index.vue:20` | 以字符串状态为准，页面筛选和类型统一 |

### 个人中心

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 路径缺 `/admin` | `/admin/profile/me`、`/admin/profile/avatar`、`/admin/profile/mobile/change` 等，来源 `ProfileController.java:41` 至 `:100` | `src/api/profile.ts` 使用 `/profile/...`，来源 `src/api/profile.ts:55` 至 `:104`；`src/views/user/profile.vue:17` 使用 | 统一为 `/admin/profile/...` |
| 头像返回类型错误 | `POST /admin/profile/avatar` 返回 `ProfileMeView`，来源 `ProfileController.java:55`、`ProfileMeView.java:21` | `updateAvatar` 返回 `ApiResponse<void>`，且函数参数签名是 `avatarUrl: string`，来源 `src/api/profile.ts:76`；页面实际传 `{ avatarUrl }`，来源 `src/views/user/profile.vue:230` | 函数签名改为接收 `{ avatarUrl: string }` 或字符串但调用一致，返回 `ProfileMeView` |
| 字段名需对齐 | 后端 `ProfileMeView` 使用 `realName/employeeNo/avatar/positionName`，来源 `ProfileMeView.java:21` | 前端 `ProfileInfo` 使用 `displayName/avatarUrl/roleName/lastLoginTime`，来源 `src/api/profile.ts:7` | 以 `ProfileMeView` 字段为准 |
| 验证码发送返回类型错误 | 后端 `sendMobileChangeCode`、`sendEmailChangeCode` 返回 `ProfileVerificationCodeSendView`，来源 `ProfileController.java:62`、`:69` | 前端返回 `ApiResponse<void>`，来源 `src/api/profile.ts:83`、`:90` | 改为具体验证码发送返回类型 |

### 仪表盘

| 差异 | 后端契约 | 前端现状 | 建议 |
| --- | --- | --- | --- |
| 路径与模块语义全不匹配 | `GET /admin/dashboard/summary`、`recent-operations`、`favorite-navigations`、`announcements`，来源 `WorkbenchController.java:34` 至 `:55` | `src/api/dashboard.ts` 使用 `/api/dashboard/stats`、`activities`、`notifications`、`quick-actions`，来源 `src/api/dashboard.ts:6` 至 `:27`；`src/views/dashboard/index.vue:16` 使用 | 以四个后端接口重建 API 类型和页面映射 |
| 路径字面量不应包含 `/api` | 请求层会自动拼 `BASE_URL=/api` | `src/api/dashboard.ts` 直接写 `/api/dashboard/*` | 改为 `/admin/dashboard/*` |

## P3：过期文档、旧 API 函数和未使用类型清理建议

| 差异 | 来源 | 建议 |
| --- | --- | --- |
| `docs/API_ENDPOINTS.md` 的代理说明错误，称前端请求 `/admin/*` 且 Vite 代理 `/admin` 重写为空；当前真实配置是 `VITE_API_BASE_URL=/api`，Vite 只代理 `/api` 并去掉 `/api`。 | 错误文档：`docs/API_ENDPOINTS.md:7` 至 `:11`；真实配置：`.env:4`、`.env.development:4`、`vite.config.ts:56` | 用本报告口径替换；文档示例应为 API 函数写 `/admin/auth/login`，实际请求 `/api/admin/auth/login`，代理后到后端 `/admin/auth/login` |
| `docs/API_ENDPOINTS.md` 记录了大量 `/api/users`、`/api/roles`、`/api/navigations`、`/api/settings`、`/api/sessions`、`/api/files` 等旧资源路径。 | `docs/API_ENDPOINTS.md:68` 至 `:310` | 删除或标记为历史，不作为契约源 |
| `docs/API_COMPARISON.md` 混入了不存在的后端接口，如 file chunk、navigation my/list/favorite、operation diff、profile bind/unbind/security、role users add、permission tree/roles。 | `docs/API_COMPARISON.md:118` 至 `:121`、`:145` 至 `:150`、`:162`、`:203` 至 `:210`、`:230` 至 `:232`、`:188` 至 `:193` | 用本报告替代，后续实施时只按后端 Controller 当前源码 |
| `README.md` 仍包含模板化 `/api/dashboard/*`、CRM 和通用后台接口表。 | `README.md:514` 至 `:538` | 与 UPMS 当前接口无关，后续文档清理 |
| `src/types/responses/common.ts` 保留旧 `ApiResponse<T = any>` 和错误分页结构。 | `src/types/responses/common.ts:4`、`:12` | API 层不再使用 `ApiResponse<T>` 作为返回泛型；分页类型改为后端 `PageResponse<T>` |
| `src/api/user.ts`、`src/api/role.ts`、`src/api/organization.ts`、`src/api/file.ts` 同时保留“旧接口函数”和“实际项目使用的 API 函数”。 | `src/api/user.ts:15` 与 `:168`、`src/api/role.ts:35` 与 `:144`、`src/api/organization.ts:33` 与 `:141`、`src/api/file.ts:31` 与 `:145` | 实施对齐时先按页面实际使用函数修正，再删除未使用旧函数 |

## 前端缺失的后端接口汇总

- `GET /admin/permission/current-user/codes`：前端 `src/api/permission.ts` 未实现。
- `GET /admin/dictionary/code/{dictCode}`：前端 `src/api/dictionary.ts` 未实现。
- `GET /admin/setting/key/{settingKey}`：前端 `src/api/setting.ts` 未实现。
- `PUT /admin/profile/me`：前端 `src/api/profile.ts` 未实现更新个人资料。
- `GET /admin/dashboard/summary`、`GET /admin/dashboard/recent-operations`、`GET /admin/dashboard/favorite-navigations`、`GET /admin/dashboard/announcements`：前端 `src/api/dashboard.ts` 当前实现的是另一套旧 dashboard 接口。

## 前端存在但后端无对应契约的接口汇总

- 认证：`/admin/auth/change-password`、`/admin/auth/first-time-password`、`/admin/auth/verification-code/send`、`/admin/auth/forgot-password`、`/admin/auth/reset-password`。
- 用户：`/admin/users*`、`/user*`、`/admin/users/{id}/organizations`、`/admin/users/profile`、`/admin/users/avatar`。
- 角色：`/admin/roles*`、`/role*`、`/admin/roles/all`。
- 组织：`/admin/organizations*`、`/org*`，以及组织成员新增/移除接口。
- 权限：`/permission*`、`/permission/list`。
- 导航：`/navigation*`，以及旧文档中的 list/my/favorite/order/toggle。
- 字典：`/dictionary*`。
- 设置：`/setting*`。
- 会话：`/auth/session*` 缺 `/admin`，以及 `/admin/sessions/current-user`、`/{id}`、`/batch`、`/others`、`/user/{userId}`、`/{id}/refresh`、`/stats`。
- 审计：`/login-audit*`、`/operation-audit*` 缺 `/admin`，`/admin/audit/login/{id}`、`/audit/operation/{id}`。
- 文件：`/admin/files*`、`/file*`、`/admin/files/batch-upload`。
- 公告：`/announcement*`、`/announcement/inbox`。
- 个人中心：`/profile*` 缺 `/admin`。
- 仪表盘：`/api/dashboard/stats`、`/api/dashboard/activities`、`/api/dashboard/notifications`、`/api/dashboard/quick-actions`。

## 建议实施顺序

1. P0：先统一 `request` 泛型、代理路径口径、认证当前用户、通知铃铛、仪表盘首屏，避免登录和基础布局阻塞联调。
2. P1：修正系统主数据模块路径和字段：用户、角色、组织、权限、导航。
3. P2：修正支撑模块：字典、设置、会话、审计、文件、公告、个人中心、仪表盘。
4. P3：清理过期文档、旧 API 函数、未使用类型和 axios 风格参数写法。

## 本轮未执行项

- 未运行格式化、`pnpm type-check`、`pnpm lint`、后端测试或开发服务器。
- 未修改任何前端 API、类型、页面或后端 Controller/DTO。
- 本报告基于静态扫描；后续实际修复后需要按模块执行类型检查、lint，并启动前后端验证登录、布局导航和核心页面。
