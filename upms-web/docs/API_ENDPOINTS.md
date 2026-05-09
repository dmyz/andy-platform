# API 接口统计

本文档统计了项目中所有的 API 接口定义。

## 路径说明

- **前端请求路径**: `/admin/*`（例如 `/admin/auth/login`）
- **Vite 代理配置**: 开发环境下，`/admin` 前缀会被重写为空字符串
- **实际后端路径**: 去掉 `/admin` 前缀后的路径（例如 `/auth/login`）
- **完整请求流程**:
  - 前端发起: `/admin/auth/login`
  - 代理重写后: `http://localhost:8004/auth/login`

## 接口概览

| 模块 | 接口数量 | 文件路径 |
|------|---------|----------|
| 认证授权 | 14 | `src/api/auth.ts` |
| 用户管理 | 14 | `src/api/user.ts` |
| 角色管理 | 8 | `src/api/role.ts` |
| 权限管理 | 7 | `src/api/permission.ts` |
| 导航管理 | 10 | `src/api/navigation.ts` |
| 组织管理 | 9 | `src/api/organization.ts` |
| 字典管理 | 14 | `src/api/dictionary.ts` |
| 系统配置 | 10 | `src/api/setting.ts` |
| 会话管理 | 9 | `src/api/session.ts` |
| 文件管理 | 7 | `src/api/file.ts` |
| 审计日志 | 6 | `src/api/audit.ts` |
| 公告管理 | 9 | `src/api/announcement.ts` |
| **总计** | **117** | - |

---

## 1. 认证授权 (auth.ts) - 14 个接口

### 登录相关
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `login` | POST | `/admin/auth/login` | 用户登录（支持密码/手机/邮箱） |
| `loginWithCode` | POST | `/admin/auth/login` | 验证码登录 |
| `logout` | POST | `/admin/auth/logout` | 用户登出 |
| `getCurrentUser` | GET | `/admin/auth/current-user` | 获取当前用户信息 |

### 密码管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `changePassword` | POST | `/admin/auth/change-password` | 修改当前用户密码 |
| `firstTimeChangePassword` | POST | `/admin/auth/first-time-password` | 首次登录修改密码 |
| `forgotPassword` | POST | `/admin/auth/forgot-password` | 忘记密码 - 发送重置链接 |
| `resetPassword` | POST | `/admin/auth/reset-password` | 重置密码（通过 token） |
| `resetPasswordWithCode` | POST | `/admin/auth/password/reset` | 重置密码（通过验证码） |

### 验证码
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `sendMobileVerificationCode` | POST | `/admin/auth/code/mobile/send` | 发送手机验证码 |
| `sendEmailVerificationCode` | POST | `/admin/auth/code/email/send` | 发送邮箱验证码 |
| `sendVerificationCode` | POST | `/admin/auth/verification-code/send` | 发送验证码（通用） |
| `getCaptcha` | GET | `/admin/auth/captcha` | 获取图形验证码 |

---

## 2. 用户管理 (user.ts) - 14 个接口

### 用户 CRUD
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getUserList` | GET | `/api/users` | 获取用户列表（分页） |
| `getUserDetail` | GET | `/api/users/:id` | 获取用户详情 |
| `createUser` | POST | `/api/users` | 创建用户 |
| `updateUser` | PUT | `/api/users/:id` | 更新用户信息 |
| `deleteUser` | DELETE | `/api/users/:id` | 删除用户 |

### 用户状态管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `updateUserStatus` | PUT | `/api/users/:id/status` | 更新用户状态（激活/禁用/锁定） |
| `resetUserPassword` | POST | `/api/users/:id/reset-password` | 重置用户密码 |

### 用户关联管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `assignRoles` | POST | `/api/users/:id/roles` | 分配角色 |
| `getUserRoles` | GET | `/api/users/:id/roles` | 获取用户角色 |
| `assignOrganizations` | POST | `/api/users/:id/organizations` | 分配组织 |
| `getUserOrganizations` | GET | `/api/users/:id/organizations` | 获取用户组织 |

### 个人中心
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `updateProfile` | PUT | `/api/users/profile` | 更新个人资料 |
| `uploadAvatar` | POST | `/api/users/avatar` | 上传头像 |

---

## 3. 角色管理 (role.ts) - 8 个接口

### 角色 CRUD
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getRoleList` | GET | `/api/roles` | 获取角色列表（分页） |
| `getAllRoles` | GET | `/api/roles/all` | 获取所有角色（不分页） |
| `getRoleDetail` | GET | `/api/roles/:id` | 获取角色详情 |
| `createRole` | POST | `/api/roles` | 创建角色 |
| `updateRole` | PUT | `/api/roles/:id` | 更新角色 |
| `deleteRole` | DELETE | `/api/roles/:id` | 删除角色 |

### 角色权限管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `assignPermissions` | POST | `/api/roles/:id/permissions` | 分配权限 |
| `getRolePermissions` | GET | `/api/roles/:id/permissions` | 获取角色权限 |

---

## 4. 权限管理 (permission.ts) - 7 个接口

### 权限 CRUD
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getPermissionTree` | GET | `/api/permissions/tree` | 获取权限树 |
| `getPermissionList` | GET | `/api/permissions` | 获取权限列表 |
| `getPermissionDetail` | GET | `/api/permissions/:id` | 获取权限详情 |
| `createPermission` | POST | `/api/permissions` | 创建权限 |
| `updatePermission` | PUT | `/api/permissions/:id` | 更新权限 |
| `deletePermission` | DELETE | `/api/permissions/:id` | 删除权限 |

### 当前用户权限
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getCurrentUserPermissions` | GET | `/api/permissions/current` | 获取当前用户权限 |

---

## 5. 导航管理 (navigation.ts) - 10 个接口

### 导航 CRUD
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getNavigations` | GET | `/api/navigations` | 获取导航列表（分页） |
| `getNavigationTree` | GET | `/api/navigations/tree` | 获取导航树 |
| `getUserNavigations` | GET | `/api/navigations/user` | 获取用户导航 |
| `getNavigation` | GET | `/api/navigations/:id` | 获取导航详情 |
| `createNavigation` | POST | `/api/navigations` | 创建导航 |
| `updateNavigation` | PUT | `/api/navigations/:id` | 更新导航 |
| `deleteNavigation` | DELETE | `/api/navigations/:id` | 删除导航 |

### 导航状态管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `updateNavigationsOrder` | PUT | `/api/navigations/order` | 更新导航排序 |
| `toggleNavigation` | PUT | `/api/navigations/:id/toggle` | 启用/禁用导航 |

---

## 6. 组织管理 (organization.ts) - 9 个接口

### 组织 CRUD
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getOrganizationTree` | GET | `/api/organizations/tree` | 获取组织树 |
| `getOrganizationList` | GET | `/api/organizations` | 获取组织列表 |
| `getOrganizationDetail` | GET | `/api/organizations/:id` | 获取组织详情 |
| `createOrganization` | POST | `/api/organizations` | 创建组织 |
| `updateOrganization` | PUT | `/api/organizations/:id` | 更新组织 |
| `deleteOrganization` | DELETE | `/api/organizations/:id` | 删除组织 |

### 组织成员管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getOrganizationMembers` | GET | `/api/organizations/:id/members` | 获取组织成员 |
| `addOrganizationMembers` | POST | `/api/organizations/:id/members` | 添加组织成员 |
| `removeOrganizationMember` | DELETE | `/api/organizations/:id/members/:userId` | 移除组织成员 |

---

## 7. 字典管理 (dictionary.ts) - 14 个接口

### 字典类型管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getDictionaryTypes` | GET | `/api/dictionaries/types` | 获取字典类型列表（分页） |
| `getDictionaryType` | GET | `/api/dictionaries/types/:id` | 获取字典类型详情 |
| `createDictionaryType` | POST | `/api/dictionaries/types` | 创建字典类型 |
| `updateDictionaryType` | PUT | `/api/dictionaries/types/:id` | 更新字典类型 |
| `deleteDictionaryType` | DELETE | `/api/dictionaries/types/:id` | 删除字典类型 |

### 字典项管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getDictionaryItems` | GET | `/api/dictionaries/items` | 获取字典项列表（分页） |
| `getDictionaryItemsByType` | GET | `/api/dictionaries/types/:typeCode/items` | 根据类型获取字典项 |
| `getDictionaryItem` | GET | `/api/dictionaries/items/:id` | 获取字典项详情 |
| `createDictionaryItem` | POST | `/api/dictionaries/items` | 创建字典项 |
| `updateDictionaryItem` | PUT | `/api/dictionaries/items/:id` | 更新字典项 |
| `deleteDictionaryItem` | DELETE | `/api/dictionaries/items/:id` | 删除字典项 |

### 字典项状态管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `updateDictionaryItemsOrder` | PUT | `/api/dictionaries/items/order` | 更新字典项排序 |
| `toggleDictionaryItem` | PUT | `/api/dictionaries/items/:id/toggle` | 启用/禁用字典项 |

---

## 8. 系统配置 (setting.ts) - 10 个接口

### 配置 CRUD
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getSystemSettings` | GET | `/api/settings` | 获取系统配置列表（分页） |
| `getSystemSetting` | GET | `/api/settings/:key` | 获取系统配置详情 |
| `updateSystemSetting` | PUT | `/api/settings/:key` | 更新系统配置 |
| `batchUpdateSystemSettings` | PUT | `/api/settings/batch` | 批量更新系统配置 |
| `resetSystemSetting` | POST | `/api/settings/:key/reset` | 重置系统配置 |

### 配置查询
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getPublicSettings` | GET | `/api/settings/public` | 获取公开配置 |
| `getSettingsByCategory` | GET | `/api/settings/category/:category` | 根据分类获取配置 |

### 配置导入导出
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `exportSystemSettings` | GET | `/api/settings/export` | 导出系统配置 |
| `importSystemSettings` | POST | `/api/settings/import` | 导入系统配置 |

---

## 9. 会话管理 (session.ts) - 9 个接口

### 会话查询
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getSessions` | GET | `/api/sessions` | 获取会话列表（分页） |
| `getCurrentUserSessions` | GET | `/api/sessions/current-user` | 获取当前用户会话 |
| `getSession` | GET | `/api/sessions/:id` | 获取会话详情 |
| `getOnlineUserStats` | GET | `/api/sessions/stats` | 获取在线用户统计 |

### 会话管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `terminateSession` | DELETE | `/api/sessions/:id` | 终止会话 |
| `terminateSessions` | DELETE | `/api/sessions/batch` | 批量终止会话 |
| `terminateOtherSessions` | DELETE | `/api/sessions/others` | 终止其他会话 |
| `terminateUserSessions` | DELETE | `/api/sessions/user/:userId` | 终止用户所有会话 |
| `refreshSession` | POST | `/api/sessions/:id/refresh` | 刷新会话 |

---

## 10. 文件管理 (file.ts) - 7 个接口

### 文件 CRUD
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getFileList` | GET | `/api/files` | 获取文件列表（分页） |
| `getFileDetail` | GET | `/api/files/:id` | 获取文件详情 |
| `uploadFile` | POST | `/api/files/upload` | 上传单个文件 |
| `uploadFiles` | POST | `/api/files/batch-upload` | 批量上传文件 |
| `deleteFile` | DELETE | `/api/files/:id` | 删除文件 |

### 文件操作
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `downloadFile` | GET | `/api/files/:id/download` | 下载文件 |
| `getFilePreviewUrl` | GET | `/api/files/:id/preview` | 获取文件预览 URL |

---

## 11. 审计日志 (audit.ts) - 6 个接口

### 登录审计
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getLoginAuditLogs` | GET | `/api/audit/login` | 获取登录审计日志（分页） |
| `getLoginAuditDetail` | GET | `/api/audit/login/:id` | 获取登录审计详情 |
| `exportLoginAuditLogs` | GET | `/api/audit/login/export` | 导出登录审计日志 |

### 操作审计
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getOperationAuditLogs` | GET | `/api/audit/operation` | 获取操作审计日志（分页） |
| `getOperationAuditDetail` | GET | `/api/audit/operation/:id` | 获取操作审计详情 |
| `exportOperationAuditLogs` | GET | `/api/audit/operation/export` | 导出操作审计日志 |

---

## 12. 公告管理 (announcement.ts) - 9 个接口

### 公告 CRUD
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getAnnouncementList` | GET | `/api/announcements` | 获取公告列表（分页） |
| `getAnnouncementDetail` | GET | `/api/announcements/:id` | 获取公告详情 |
| `createAnnouncement` | POST | `/api/announcements` | 创建公告 |
| `updateAnnouncement` | PUT | `/api/announcements/:id` | 更新公告 |
| `deleteAnnouncement` | DELETE | `/api/announcements/:id` | 删除公告 |

### 公告状态管理
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `publishAnnouncement` | POST | `/api/announcements/:id/publish` | 发布公告 |
| `archiveAnnouncement` | POST | `/api/announcements/:id/archive` | 归档公告 |

### 用户公告
| 接口名称 | HTTP 方法 | 路径 | 说明 |
|---------|----------|------|------|
| `getInboxAnnouncements` | GET | `/api/announcements/inbox` | 获取收件箱公告 |
| `markAnnouncementAsRead` | POST | `/api/announcements/:id/read` | 标记公告为已读 |

---

## API 路径前缀说明

项目中使用了两种 API 路径前缀：

1. **`/admin/auth/*`** - 认证授权相关接口
2. **`/api/*`** - 其他业务接口

开发环境下，所有请求会通过 Vite 代理转发到后端服务（默认 `http://localhost:8004`）。

---

## 接口返回格式

所有接口遵循统一的响应格式：

```typescript
interface ApiResponse<T> {
  code: number;      // 0 表示成功，非 0 表示失败
  message: string;   // 响应消息
  data: T;          // 响应数据
}
```

**注意**：`request` 工具会自动解包响应，API 函数直接返回 `data` 字段的内容。

---

## 分页响应格式

分页接口返回格式：

```typescript
interface PageResponse<T> {
  items: T[];        // 数据列表
  total: number;     // 总记录数
  page: number;      // 当前页码
  pageSize: number;  // 每页大小
}
```

---

## 文件上传说明

文件上传接口使用 `FormData` 格式，支持上传进度回调：

```typescript
uploadFile(file: File, onProgress?: (percent: number) => void)
```

---

## 文件下载说明

文件下载接口使用 `XMLHttpRequest`，支持下载进度回调和自动触发浏览器下载。

---

**生成时间**: 2026-04-30
**接口总数**: 117 个
