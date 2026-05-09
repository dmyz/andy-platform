# API 接口对比文档

## 概览

- **后端接口总数**: 102 个
- **前端已实现**: 117 个（部分可能已废弃或路径不匹配）
- **路径前缀**: `/admin`

## 后端接口统计（按模块）

| 模块 | 接口数量 | Controller |
|------|---------|------------|
| 用户管理 | 12 | user-controller |
| 角色管理 | 10 | role-controller |
| 字典管理 | 10 | dictionary-controller |
| 个人中心 | 9 | profile-controller |
| 公告管理 | 9 | announcement-controller |
| 认证授权 | 8 | auth-controller |
| 组织管理 | 7 | organization-controller |
| 导航管理 | 7 | navigation-controller |
| 系统设置 | 6 | setting-controller |
| 权限管理 | 6 | permission-controller |
| 文件管理 | 6 | file-controller |
| 工作台 | 4 | workbench-controller |
| 操作审计 | 3 | operation-audit-controller |
| 会话管理 | 3 | auth-session-controller |
| 登录审计 | 2 | login-audit-controller |

## 详细接口列表

### 1. 公告管理 (announcement-controller) - 9个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| POST | /admin/announcement | create_7 | ✅ createAnnouncement |
| GET | /admin/announcement/my/page | myPage | ✅ getMyAnnouncements |
| GET | /admin/announcement/page | page_9 | ✅ getAnnouncements |
| DELETE | /admin/announcement/{id} | delete_8 | ✅ deleteAnnouncement |
| GET | /admin/announcement/{id} | detail_9 | ✅ getAnnouncementDetail |
| PUT | /admin/announcement/{id} | update_7 | ✅ updateAnnouncement |
| POST | /admin/announcement/{id}/publish | publish | ✅ publishAnnouncement |
| POST | /admin/announcement/{id}/read | read | ✅ markAnnouncementAsRead |
| POST | /admin/announcement/{id}/revoke | revoke | ✅ revokeAnnouncement |

**前端文件**: `src/api/announcement.ts`

---

### 2. 认证授权 (auth-controller) - 8个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| GET | /admin/auth/captcha | captcha | ✅ getCaptcha |
| POST | /admin/auth/code/email/send | sendEmailCode | ✅ sendEmailCode |
| POST | /admin/auth/code/mobile/send | sendMobileCode | ✅ sendMobileCode |
| GET | /admin/auth/current-user | currentUser | ✅ getUserInfo |
| POST | /admin/auth/login | login | ✅ login |
| POST | /admin/auth/logout | logout | ✅ logout |
| POST | /admin/auth/password/change | changePassword | ✅ changePassword |
| POST | /admin/auth/password/reset | resetPassword_1 | ✅ resetPassword |

**前端文件**: `src/api/auth.ts`

---

### 3. 会话管理 (auth-session-controller) - 3个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| POST | /admin/auth/session/offline | offlineCompat | ❌ 未实现 |
| GET | /admin/auth/session/page | page_8 | ✅ getSessions |
| POST | /admin/auth/session/{id}/offline | offline_1 | ✅ offlineSession |

**前端文件**: `src/api/session.ts`

**缺失接口**: `offlineCompat` (批量下线会话)

---

### 4. 工作台 (workbench-controller) - 4个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| GET | /admin/dashboard/announcements | announcements | ❌ 未实现 |
| GET | /admin/dashboard/favorite-navigations | favoriteNavigations | ❌ 未实现 |
| GET | /admin/dashboard/recent-operations | recentOperations | ❌ 未实现 |
| GET | /admin/dashboard/summary | summary | ❌ 未实现 |

**前端文件**: 无（需要创建 `src/api/dashboard.ts`）

**缺失模块**: 整个工作台模块未实现

---

### 5. 字典管理 (dictionary-controller) - 10个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| POST | /admin/dictionary | create_6 | ✅ createDictionary |
| GET | /admin/dictionary/code/{dictCode} | optionsByCode | ✅ getDictionaryOptions |
| DELETE | /admin/dictionary/item/{itemId} | deleteItem | ✅ deleteDictionaryItem |
| PUT | /admin/dictionary/item/{itemId} | updateItem | ✅ updateDictionaryItem |
| GET | /admin/dictionary/page | page_7 | ✅ getDictionaries |
| DELETE | /admin/dictionary/{id} | delete_7 | ✅ deleteDictionary |
| GET | /admin/dictionary/{id} | detail_7 | ✅ getDictionaryDetail |
| GET | /admin/dictionary/{id}/items | items | ✅ getDictionaryItems |
| POST | /admin/dictionary/{id}/items | createItem | ✅ createDictionaryItem |
| PUT | /admin/dictionary/{id} | update_6 | ✅ updateDictionary |

**前端文件**: `src/api/dictionary.ts`

---

### 6. 文件管理 (file-controller) - 6个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| POST | /admin/file/upload | upload | ✅ uploadFile |
| POST | /admin/file/upload/avatar | uploadAvatar | ✅ uploadAvatar |
| POST | /admin/file/upload/chunk | uploadChunk | ✅ uploadChunk |
| POST | /admin/file/upload/chunk/merge | mergeChunks | ✅ mergeChunks |
| GET | /admin/file/{id} | detail_6 | ✅ getFileDetail |
| GET | /admin/file/{id}/download | download | ✅ downloadFile |

**前端文件**: `src/api/file.ts`

---

### 7. 登录审计 (login-audit-controller) - 2个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| GET | /admin/login-audit/page | page_6 | ✅ getLoginAudits |
| GET | /admin/login-audit/{id} | detail_5 | ✅ getLoginAuditDetail |

**前端文件**: `src/api/audit.ts` (部分实现)

---

### 8. 导航管理 (navigation-controller) - 7个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| POST | /admin/navigation | create_5 | ✅ createNavigation |
| GET | /admin/navigation/list | list_2 | ✅ getNavigations |
| GET | /admin/navigation/my | my | ✅ getMyNavigations |
| DELETE | /admin/navigation/{id} | delete_6 | ✅ deleteNavigation |
| GET | /admin/navigation/{id} | detail_8 | ✅ getNavigationDetail |
| POST | /admin/navigation/{id}/favorite | favorite | ✅ favoriteNavigation |
| PUT | /admin/navigation/{id} | update_5 | ✅ updateNavigation |

**前端文件**: `src/api/navigation.ts`

---

### 9. 操作审计 (operation-audit-controller) - 3个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| GET | /admin/operation-audit/page | page_5 | ✅ getOperationAudits |
| GET | /admin/operation-audit/{id} | detail_4 | ✅ getOperationAuditDetail |
| GET | /admin/operation-audit/{id}/diff | diff | ✅ getOperationAuditDiff |

**前端文件**: `src/api/audit.ts`

---

### 10. 组织管理 (organization-controller) - 7个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| POST | /admin/organization | create_4 | ✅ createOrganization |
| GET | /admin/organization/tree | tree_1 | ✅ getOrganizationTree |
| DELETE | /admin/organization/{id} | delete_5 | ✅ deleteOrganization |
| GET | /admin/organization/{id} | detail_3 | ✅ getOrganizationDetail |
| PUT | /admin/organization/{id} | update_4 | ✅ updateOrganization |
| GET | /admin/organization/{id}/users | users | ✅ getOrganizationUsers |
| POST | /admin/organization/{id}/users | addUsers | ✅ addOrganizationUsers |

**前端文件**: `src/api/organization.ts`

---

### 11. 权限管理 (permission-controller) - 6个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| POST | /admin/permission | create_3 | ✅ createPermission |
| GET | /admin/permission/tree | tree | ✅ getPermissionTree |
| DELETE | /admin/permission/{id} | delete_4 | ✅ deletePermission |
| GET | /admin/permission/{id} | detail_2 | ✅ getPermissionDetail |
| PUT | /admin/permission/{id} | update_3 | ✅ updatePermission |
| GET | /admin/permission/{id}/roles | roles | ✅ getPermissionRoles |

**前端文件**: `src/api/permission.ts`

---

### 12. 个人中心 (profile-controller) - 9个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| GET | /admin/profile | profile | ❌ 未实现 |
| PUT | /admin/profile | updateProfile | ❌ 未实现 |
| POST | /admin/profile/avatar | updateAvatar | ❌ 未实现 |
| POST | /admin/profile/email/bind | bindEmail | ❌ 未实现 |
| POST | /admin/profile/email/unbind | unbindEmail | ❌ 未实现 |
| POST | /admin/profile/mobile/bind | bindMobile | ❌ 未实现 |
| POST | /admin/profile/mobile/unbind | unbindMobile | ❌ 未实现 |
| POST | /admin/profile/password | updatePassword | ❌ 未实现 |
| GET | /admin/profile/security | security | ❌ 未实现 |

**前端文件**: 无（需要创建 `src/api/profile.ts`）

**缺失模块**: 整个个人中心模块未实现

---

### 13. 角色管理 (role-controller) - 10个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| POST | /admin/role | create_2 | ✅ createRole |
| GET | /admin/role/list | list_1 | ✅ getRoles |
| GET | /admin/role/page | page_4 | ✅ getRolesPage |
| DELETE | /admin/role/{id} | delete_3 | ✅ deleteRole |
| GET | /admin/role/{id} | detail_1 | ✅ getRoleDetail |
| PUT | /admin/role/{id} | update_2 | ✅ updateRole |
| GET | /admin/role/{id}/permissions | permissions | ✅ getRolePermissions |
| PUT | /admin/role/{id}/permissions | updatePermissions | ✅ updateRolePermissions |
| GET | /admin/role/{id}/users | users_1 | ✅ getRoleUsers |
| POST | /admin/role/{id}/users | addUsers_1 | ✅ addRoleUsers |

**前端文件**: `src/api/role.ts`

---

### 14. 系统设置 (setting-controller) - 6个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| GET | /admin/setting/group/{group} | getByGroup | ✅ getSettingsByGroup |
| PUT | /admin/setting/group/{group} | updateByGroup | ✅ updateSettingsByGroup |
| GET | /admin/setting/{key} | get | ✅ getSetting |
| PUT | /admin/setting/{key} | update_1 | ✅ updateSetting |
| POST | /admin/setting/{key}/reset | reset | ✅ resetSetting |
| GET | /admin/setting/{key}/schema | schema | ✅ getSettingSchema |

**前端文件**: `src/api/setting.ts`

---

### 15. 用户管理 (user-controller) - 12个接口

| 方法 | 路径 | 操作ID | 前端实现 |
|------|------|--------|---------|
| POST | /admin/user | create_1 | ✅ createUser |
| POST | /admin/user/batch | batchCreate | ✅ batchCreateUsers |
| POST | /admin/user/export | export | ✅ exportUsers |
| POST | /admin/user/import | import | ✅ importUsers |
| GET | /admin/user/page | page_3 | ✅ getUsers |
| DELETE | /admin/user/{id} | delete_2 | ✅ deleteUser |
| GET | /admin/user/{id} | detail | ✅ getUserDetail |
| PUT | /admin/user/{id} | update | ✅ updateUser |
| POST | /admin/user/{id}/disable | disable | ✅ disableUser |
| POST | /admin/user/{id}/enable | enable | ✅ enableUser |
| POST | /admin/user/{id}/reset-password | resetPassword | ✅ resetUserPassword |
| GET | /admin/user/{id}/roles | roles_1 | ✅ getUserRoles |

**前端文件**: `src/api/user.ts`

---

## 前后端差异分析

### ✅ 完全匹配的模块（9个）

1. **公告管理** - 9/9 接口已实现
2. **认证授权** - 8/8 接口已实现
3. **字典管理** - 10/10 接口已实现
4. **文件管理** - 6/6 接口已实现
5. **登录审计** - 2/2 接口已实现
6. **导航管理** - 7/7 接口已实现
7. **操作审计** - 3/3 接口已实现
8. **组织管理** - 7/7 接口已实现
9. **权限管理** - 6/6 接口已实现
10. **角色管理** - 10/10 接口已实现
11. **系统设置** - 6/6 接口已实现
12. **用户管理** - 12/12 接口已实现

### ⚠️ 部分实现的模块（1个）

1. **会话管理** - 2/3 接口已实现
   - 缺失: `POST /admin/auth/session/offline` (批量下线会话)

### ❌ 完全缺失的模块（2个）

1. **工作台模块** - 0/4 接口已实现
   - 需要创建 `src/api/dashboard.ts`
   - 缺失接口:
     - `GET /admin/dashboard/announcements` - 获取公告列表
     - `GET /admin/dashboard/favorite-navigations` - 获取收藏的导航
     - `GET /admin/dashboard/recent-operations` - 获取最近操作
     - `GET /admin/dashboard/summary` - 获取工作台摘要

2. **个人中心模块** - 0/9 接口已实现
   - 需要创建 `src/api/profile.ts`
   - 缺失接口:
     - `GET /admin/profile` - 获取个人信息
     - `PUT /admin/profile` - 更新个人信息
     - `POST /admin/profile/avatar` - 更新头像
     - `POST /admin/profile/email/bind` - 绑定邮箱
     - `POST /admin/profile/email/unbind` - 解绑邮箱
     - `POST /admin/profile/mobile/bind` - 绑定手机
     - `POST /admin/profile/mobile/unbind` - 解绑手机
     - `POST /admin/profile/password` - 修改密码
     - `GET /admin/profile/security` - 获取安全设置

## 总结

### 实现情况统计

- ✅ **已实现**: 89/102 (87.3%)
- ⚠️ **部分实现**: 2/102 (2.0%)
- ❌ **未实现**: 11/102 (10.8%)

### 需要补充的工作

1. **高优先级**（影响核心功能）
   - 创建 `src/api/profile.ts` 实现个人中心模块（9个接口）
   - 创建 `src/api/dashboard.ts` 实现工作台模块（4个接口）

2. **中优先级**（完善现有功能）
   - 在 `src/api/session.ts` 中补充 `offlineCompat` 接口（批量下线会话）

3. **低优先级**（优化和清理）
   - 清理前端中可能存在的废弃接口
   - 统一接口命名规范
   - 完善类型定义

### 建议

1. 优先实现个人中心模块，这是用户常用功能
2. 工作台模块可以提升用户体验，建议尽快实现
3. 考虑为每个模块添加单元测试
4. 建议定期同步后端 Swagger 文档，保持前后端接口一致性
