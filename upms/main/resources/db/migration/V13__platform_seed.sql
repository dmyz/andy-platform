INSERT INTO `org_unit` (`id`, `parent_id`, `org_code`, `org_name`, `org_full_name`, `leader_user_id`, `level_no`, `sort_order`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (10100, NULL, 'HQ', '总部', '总部', 10001, 1, 1, 'ACTIVE', '平台总部', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10110, 10100, 'TECH', '技术中心', '总部/技术中心', 10002, 2, 1, 'ACTIVE', '负责平台研发', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10111, 10110, 'FE', '前端研发组', '总部/技术中心/前端研发组', 10003, 3, 1, 'ACTIVE', '负责管理端前端实现', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10112, 10110, 'BE', '后端研发组', '总部/技术中心/后端研发组', 10004, 3, 2, 'ACTIVE', '负责平台服务端能力', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10120, 10100, 'PRODUCT', '产品部', '总部/产品部', 10005, 2, 2, 'ACTIVE', '负责产品规划', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10130, 10100, 'OPERATION', '运营部', '总部/运营部', 10006, 2, 3, 'INACTIVE', '负责平台运营', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `parent_id` = VALUES(`parent_id`), `org_name` = VALUES(`org_name`), `org_full_name` = VALUES(`org_full_name`), `leader_user_id` = VALUES(`leader_user_id`), `level_no` = VALUES(`level_no`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `org_membership` (`id`, `user_id`, `org_id`, `position_name`, `is_primary`, `status`, `joined_at`, `left_at`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (10201, 10001, 10100, '平台管理员', 1, 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10202, 10002, 10110, '技术总监', 1, 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10203, 10003, 10111, '前端工程师', 1, 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10204, 10004, 10112, '后端工程师', 1, 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10205, 10005, 10120, '产品经理', 1, 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10206, 10006, 10130, '运营经理', 1, 'INACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `org_id` = VALUES(`org_id`), `position_name` = VALUES(`position_name`), `status` = VALUES(`status`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `iam_role` (`id`, `role_code`, `role_name`, `role_type`, `data_scope`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (10301, 'admin', '超级管理员', 'PLATFORM', 'ALL', 'ACTIVE', '平台最高权限账号', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10302, 'sys_admin', '系统管理员', 'PLATFORM', 'ALL', 'ACTIVE', '负责平台配置与权限维护', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10303, 'user', '普通用户', 'PLATFORM', 'SELF', 'ACTIVE', '授权范围内使用平台功能', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10304, 'operator', '运营人员', 'PLATFORM', 'SELF', 'ACTIVE', '负责公告与运营动作', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `role_type` = VALUES(`role_type`), `data_scope` = VALUES(`data_scope`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `iam_role_binding` (`id`, `subject_type`, `subject_id`, `role_id`, `source_type`, `status`, `effective_from`, `effective_to`, `creator_id`, `created_at`) VALUES
  (10401, 'USER', 10001, 10301, 'MANUAL', 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3)),
  (10402, 'USER', 10002, 10302, 'MANUAL', 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3)),
  (10403, 'USER', 10003, 10303, 'MANUAL', 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3)),
  (10404, 'USER', 10004, 10303, 'MANUAL', 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3)),
  (10405, 'USER', 10005, 10304, 'MANUAL', 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3)),
  (10406, 'USER', 10006, 10304, 'MANUAL', 'INACTIVE', CURRENT_TIMESTAMP(3), NULL, 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`), `status` = VALUES(`status`), `effective_to` = VALUES(`effective_to`);

INSERT INTO `iam_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `resource_type`, `action_code`, `module_code`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (10501, 'dashboard:view', '查看工作台', 'NAV_ACCESS', 'DASHBOARD', 'VIEW', 'dashboard', 'ACTIVE', '查看工作台', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10502, 'system:navigation', '系统管理导航', 'NAV_ACCESS', 'NAVIGATION', 'VIEW', 'system', 'ACTIVE', '系统管理导航入口', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10503, 'system:org:view', '查看组织', 'MENU', 'ORG', 'VIEW', 'org', 'ACTIVE', '查看组织列表', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10504, 'system:org:create', '新增组织', 'BUTTON', 'ORG', 'CREATE', 'org', 'ACTIVE', '新增组织', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10505, 'system:org:update', '编辑组织', 'BUTTON', 'ORG', 'UPDATE', 'org', 'ACTIVE', '编辑组织', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10506, 'system:org:delete', '删除组织', 'BUTTON', 'ORG', 'DELETE', 'org', 'ACTIVE', '删除组织', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10507, 'system:org:status', '变更组织状态', 'BUTTON', 'ORG', 'STATUS', 'org', 'ACTIVE', '变更组织状态', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10508, 'system:org:member:view', '查看组织成员', 'BUTTON', 'ORG', 'MEMBER_VIEW', 'org', 'ACTIVE', '查看组织成员', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10509, 'system:user:view', '查看用户', 'MENU', 'USER', 'VIEW', 'user', 'ACTIVE', '查看用户列表', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10510, 'system:user:create', '新增用户', 'BUTTON', 'USER', 'CREATE', 'user', 'ACTIVE', '新增用户', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10511, 'system:user:update', '编辑用户', 'BUTTON', 'USER', 'UPDATE', 'user', 'ACTIVE', '编辑用户', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10512, 'system:user:delete', '删除用户', 'BUTTON', 'USER', 'DELETE', 'user', 'ACTIVE', '删除用户', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10513, 'system:user:status', '变更用户状态', 'BUTTON', 'USER', 'STATUS', 'user', 'ACTIVE', '变更用户状态', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10514, 'system:user:reset-password', '重置用户密码', 'BUTTON', 'USER', 'RESET_PASSWORD', 'user', 'ACTIVE', '重置用户密码', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10515, 'system:user:offline', '强制下线用户', 'BUTTON', 'USER', 'OFFLINE', 'user', 'ACTIVE', '强制下线用户', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10516, 'system:user:assign-role', '分配用户角色', 'BUTTON', 'USER', 'ASSIGN_ROLE', 'user', 'ACTIVE', '分配用户角色', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10517, 'system:role:view', '查看角色', 'MENU', 'ROLE', 'VIEW', 'role', 'ACTIVE', '查看角色', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10518, 'system:role:create', '新增角色', 'BUTTON', 'ROLE', 'CREATE', 'role', 'ACTIVE', '新增角色', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10519, 'system:role:update', '编辑角色', 'BUTTON', 'ROLE', 'UPDATE', 'role', 'ACTIVE', '编辑角色', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10520, 'system:role:delete', '删除角色', 'BUTTON', 'ROLE', 'DELETE', 'role', 'ACTIVE', '删除角色', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10521, 'system:role:status', '变更角色状态', 'BUTTON', 'ROLE', 'STATUS', 'role', 'ACTIVE', '变更角色状态', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10522, 'system:role:assign-permission', '分配角色权限', 'BUTTON', 'ROLE', 'ASSIGN_PERMISSION', 'role', 'ACTIVE', '分配角色权限', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10523, 'system:navigation:view', '查看导航', 'MENU', 'NAVIGATION', 'VIEW', 'navigation', 'ACTIVE', '查看导航', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10524, 'system:navigation:create', '新增导航', 'BUTTON', 'NAVIGATION', 'CREATE', 'navigation', 'ACTIVE', '新增导航', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10525, 'system:navigation:update', '编辑导航', 'BUTTON', 'NAVIGATION', 'UPDATE', 'navigation', 'ACTIVE', '编辑导航', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10526, 'system:navigation:delete', '删除导航', 'BUTTON', 'NAVIGATION', 'DELETE', 'navigation', 'ACTIVE', '删除导航', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10527, 'system:navigation:assign-permission', '分配导航权限', 'BUTTON', 'NAVIGATION', 'ASSIGN_PERMISSION', 'navigation', 'ACTIVE', '分配导航权限', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10528, 'system:permission:view', '查看权限', 'MENU', 'PERMISSION', 'VIEW', 'permission', 'ACTIVE', '查看权限', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10529, 'system:permission:create', '新增权限', 'BUTTON', 'PERMISSION', 'CREATE', 'permission', 'ACTIVE', '新增权限', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10530, 'system:permission:update', '编辑权限', 'BUTTON', 'PERMISSION', 'UPDATE', 'permission', 'ACTIVE', '编辑权限', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10531, 'system:permission:delete', '删除权限', 'BUTTON', 'PERMISSION', 'DELETE', 'permission', 'ACTIVE', '删除权限', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10532, 'system:dictionary:view', '查看字典', 'MENU', 'DICTIONARY', 'VIEW', 'dictionary', 'ACTIVE', '查看字典', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10533, 'system:dictionary:create', '新增字典', 'BUTTON', 'DICTIONARY', 'CREATE', 'dictionary', 'ACTIVE', '新增字典', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10534, 'system:dictionary:update', '编辑字典', 'BUTTON', 'DICTIONARY', 'UPDATE', 'dictionary', 'ACTIVE', '编辑字典', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10535, 'system:dictionary:delete', '删除字典', 'BUTTON', 'DICTIONARY', 'DELETE', 'dictionary', 'ACTIVE', '删除字典', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10536, 'system:dictionary:item:manage', '维护字典项', 'BUTTON', 'DICTIONARY', 'ITEM_MANAGE', 'dictionary', 'ACTIVE', '维护字典项', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10537, 'system:setting:view', '查看系统配置', 'MENU', 'SETTING', 'VIEW', 'setting', 'ACTIVE', '查看系统配置', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10538, 'system:setting:create', '新增系统配置', 'BUTTON', 'SETTING', 'CREATE', 'setting', 'ACTIVE', '新增系统配置', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10539, 'system:setting:update', '编辑系统配置', 'BUTTON', 'SETTING', 'UPDATE', 'setting', 'ACTIVE', '编辑系统配置', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10540, 'system:setting:delete', '删除系统配置', 'BUTTON', 'SETTING', 'DELETE', 'setting', 'ACTIVE', '删除系统配置', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10541, 'announcement:navigation', '通知中心导航', 'NAV_ACCESS', 'NAVIGATION', 'VIEW', 'announcement', 'ACTIVE', '通知中心导航入口', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10542, 'announcement:manage:view', '查看公告管理', 'MENU', 'ANNOUNCEMENT', 'VIEW', 'announcement', 'ACTIVE', '查看公告管理', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10543, 'announcement:inbox:view', '查看消息中心', 'MENU', 'ANNOUNCEMENT', 'VIEW_INBOX', 'announcement', 'ACTIVE', '查看消息中心', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10544, 'file:navigation', '文件中心导航', 'NAV_ACCESS', 'NAVIGATION', 'VIEW', 'file', 'ACTIVE', '文件中心导航入口', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10545, 'file:manage:view', '查看文件管理', 'MENU', 'FILE', 'VIEW', 'file', 'ACTIVE', '查看文件管理', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10546, 'audit:navigation', '审计中心导航', 'NAV_ACCESS', 'NAVIGATION', 'VIEW', 'audit', 'ACTIVE', '审计中心导航入口', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10547, 'audit:login:view', '查看登录审计', 'MENU', 'AUDIT', 'LOGIN_VIEW', 'audit', 'ACTIVE', '查看登录审计', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10548, 'audit:operation:view', '查看操作审计', 'MENU', 'AUDIT', 'OPERATION_VIEW', 'audit', 'ACTIVE', '查看操作审计', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10549, 'auth:session:view', '查看在线会话', 'MENU', 'SESSION', 'VIEW', 'auth', 'ACTIVE', '查看在线会话', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10550, 'auth:session:offline', '强制下线会话', 'BUTTON', 'SESSION', 'OFFLINE', 'auth', 'ACTIVE', '强制下线会话', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10551, 'profile:view', '查看个人中心', 'MENU', 'PROFILE', 'VIEW', 'profile', 'ACTIVE', '查看个人中心', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10552, 'profile:update', '修改个人资料', 'BUTTON', 'PROFILE', 'UPDATE', 'profile', 'ACTIVE', '修改个人资料', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10553, 'profile:avatar:update', '修改头像', 'BUTTON', 'PROFILE', 'AVATAR_UPDATE', 'profile', 'ACTIVE', '修改头像', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10554, 'profile:mobile:update', '修改手机号', 'BUTTON', 'PROFILE', 'MOBILE_UPDATE', 'profile', 'ACTIVE', '修改手机号', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10555, 'profile:email:update', '修改邮箱', 'BUTTON', 'PROFILE', 'EMAIL_UPDATE', 'profile', 'ACTIVE', '修改邮箱', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10556, 'profile:password:update', '修改密码', 'BUTTON', 'PROFILE', 'PASSWORD_UPDATE', 'profile', 'ACTIVE', '修改密码', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `permission_type` = VALUES(`permission_type`), `resource_type` = VALUES(`resource_type`), `action_code` = VALUES(`action_code`), `module_code` = VALUES(`module_code`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `ui_navigation` (`id`, `parent_id`, `nav_code`, `nav_name`, `nav_type`, `route_path`, `component_path`, `icon`, `external_url`, `visible_flag`, `sort_order`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (11001, NULL, 'dashboard', '工作台', 'PAGE', '/dashboard', 'views/dashboard/index.vue', 'dashboard', NULL, 1, 1, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11002, NULL, 'system', '系统管理', 'GROUP', '/system', 'Layout', 'setting', NULL, 1, 2, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11021, 11002, 'system_user', '用户管理', 'PAGE', '/system/user', 'views/system/user/index.vue', 'user', NULL, 1, 1, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11022, 11002, 'system_role', '角色管理', 'PAGE', '/system/role', 'views/system/role/index.vue', 'usergroup-add', NULL, 1, 2, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11023, 11002, 'system_navigation', '导航管理', 'PAGE', '/system/navigation', 'views/system/navigation/index.vue', 'menu-unfold', NULL, 1, 3, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11024, 11002, 'system_org', '组织管理', 'PAGE', '/system/org', 'views/system/organization/index.vue', 'usergroup-add', NULL, 1, 4, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11025, 11002, 'system_permission', '权限定义', 'PAGE', '/system/permission', 'views/system/permission/index.vue', 'secured', NULL, 1, 5, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11026, 11002, 'system_dictionary', '字典管理', 'PAGE', '/system/dictionary', 'views/system/dictionary/index.vue', 'catalog', NULL, 1, 6, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11027, 11002, 'system_setting', '系统配置', 'PAGE', '/system/setting', 'views/system/setting/index.vue', 'setting', NULL, 1, 7, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11028, 11002, 'system_session', '在线会话', 'PAGE', '/system/session', 'views/system/session/index.vue', 'monitor', NULL, 1, 8, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11003, NULL, 'announcement', '通知中心', 'GROUP', '/announcement', 'Layout', 'bell', NULL, 1, 3, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11031, 11003, 'announcement_manage', '公告管理', 'PAGE', '/announcement/manage', 'views/announcement/index.vue', 'bell', NULL, 1, 1, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11032, 11003, 'announcement_inbox', '消息中心', 'PAGE', '/announcement/inbox', 'views/announcement/inbox.vue', 'bell', NULL, 1, 2, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11004, NULL, 'file', '文件中心', 'GROUP', '/file', 'Layout', 'file', NULL, 1, 4, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11041, 11004, 'file_manage', '文件管理', 'PAGE', '/file/manage', 'views/file/index.vue', 'file', NULL, 1, 1, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11005, NULL, 'audit', '审计中心', 'GROUP', '/audit', 'Layout', 'monitor', NULL, 1, 5, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11051, 11005, 'audit_login', '登录审计', 'PAGE', '/audit/login', 'views/audit/login/index.vue', 'monitor', NULL, 1, 1, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11052, 11005, 'audit_operation', '操作审计', 'PAGE', '/audit/operation', 'views/audit/operation/index.vue', 'monitor', NULL, 1, 2, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11006, NULL, 'profile', '个人中心', 'PAGE', '/profile', 'views/user/profile.vue', 'user', NULL, 1, 6, 'ACTIVE', NULL, 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `parent_id` = VALUES(`parent_id`), `nav_name` = VALUES(`nav_name`), `nav_type` = VALUES(`nav_type`), `route_path` = VALUES(`route_path`), `component_path` = VALUES(`component_path`), `icon` = VALUES(`icon`), `external_url` = VALUES(`external_url`), `visible_flag` = VALUES(`visible_flag`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `meta_dictionary` (`id`, `dict_code`, `dict_name`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (11201, 'user_status', '用户状态', 'ACTIVE', '用户启停状态', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11202, 'announcement_type', '公告类型', 'ACTIVE', '公告类型枚举', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11203, 'file_category', '文件分类', 'ACTIVE', '文件上传分类', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `meta_dictionary_item` (`id`, `dict_id`, `item_text`, `item_value`, `sort_order`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (11301, 11201, '启用', 'ENABLED', 1, 'ACTIVE', '可正常登录和使用', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11302, 11201, '禁用', 'DISABLED', 2, 'ACTIVE', '账号被停用', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11303, 11202, '系统通知', 'SYSTEM', 1, 'ACTIVE', '系统级通知', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11304, 11202, '运营公告', 'NOTICE', 2, 'ACTIVE', '运营类公告', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11305, 11202, '功能发布', 'FEATURE', 3, 'ACTIVE', '功能上新', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11306, 11203, '头像图片', 'AVATAR', 1, 'ACTIVE', '用户头像资源', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11307, 11203, '导入模板', 'IMPORT_TEMPLATE', 2, 'ACTIVE', '导入导出模板', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11308, 11203, '业务附件', 'ATTACHMENT', 3, 'ACTIVE', '业务上传附件', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `cfg_setting` (`id`, `setting_key`, `setting_name`, `setting_value`, `value_type`, `scope_type`, `scope_id`, `group_code`, `secret_flag`, `effective_mode`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (11401, 'platform.login.captcha.enabled', '登录验证码开关', 'false', 'BOOLEAN', 'GLOBAL', NULL, 'LOGIN', 0, 'REALTIME', 'ACTIVE', '默认关闭图片验证码', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11402, 'platform.file.max-size-mb', '文件上传大小限制', '10', 'NUMBER', 'GLOBAL', NULL, 'FILE', 0, 'REALTIME', 'ACTIVE', '单文件最大 10MB', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11403, 'platform.announcement.default-channel', '默认公告渠道', 'IN_APP', 'STRING', 'GLOBAL', NULL, 'ANNOUNCEMENT', 0, 'REALTIME', 'ACTIVE', '默认站内公告', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `setting_name` = VALUES(`setting_name`), `setting_value` = VALUES(`setting_value`), `value_type` = VALUES(`value_type`), `scope_type` = VALUES(`scope_type`), `scope_id` = VALUES(`scope_id`), `group_code` = VALUES(`group_code`), `secret_flag` = VALUES(`secret_flag`), `effective_mode` = VALUES(`effective_mode`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `iam_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `resource_type`, `action_code`, `module_code`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (10557, 'system:user:password:reset', '重置用户密码', 'BUTTON', 'USER', 'PASSWORD_RESET', 'user', 'ACTIVE', '重置用户密码', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10558, 'system:user:role:assign', '分配用户角色', 'BUTTON', 'USER', 'ROLE_ASSIGN', 'user', 'ACTIVE', '分配用户角色', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10559, 'system:user:import', '导入用户', 'BUTTON', 'USER', 'IMPORT', 'user', 'ACTIVE', '导入用户', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10560, 'system:user:export', '导出用户', 'BUTTON', 'USER', 'EXPORT', 'user', 'ACTIVE', '导出用户', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10561, 'system:role:permission:assign', '分配角色权限', 'BUTTON', 'ROLE', 'PERMISSION_ASSIGN', 'role', 'ACTIVE', '分配角色权限', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10562, 'system:role:user:view', '查看角色关联用户', 'BUTTON', 'ROLE', 'USER_VIEW', 'role', 'ACTIVE', '查看角色关联用户', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10563, 'system:navigation:access:assign', '分配导航访问权限', 'BUTTON', 'NAVIGATION', 'ACCESS_ASSIGN', 'navigation', 'ACTIVE', '分配导航访问权限', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10564, 'announcement:manage:create', '新增公告', 'BUTTON', 'ANNOUNCEMENT', 'CREATE', 'announcement', 'ACTIVE', '新增公告', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10565, 'announcement:manage:update', '编辑公告', 'BUTTON', 'ANNOUNCEMENT', 'UPDATE', 'announcement', 'ACTIVE', '编辑公告', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10566, 'announcement:manage:delete', '删除公告', 'BUTTON', 'ANNOUNCEMENT', 'DELETE', 'announcement', 'ACTIVE', '删除公告', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10567, 'announcement:manage:publish', '发布公告', 'BUTTON', 'ANNOUNCEMENT', 'PUBLISH', 'announcement', 'ACTIVE', '发布公告', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10568, 'announcement:manage:revoke', '撤回公告', 'BUTTON', 'ANNOUNCEMENT', 'REVOKE', 'announcement', 'ACTIVE', '撤回公告', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10569, 'announcement:inbox:read', '消息已读', 'BUTTON', 'ANNOUNCEMENT', 'READ', 'announcement', 'ACTIVE', '标记公告已读', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10570, 'file:manage:upload', '上传文件', 'BUTTON', 'FILE', 'UPLOAD', 'file', 'ACTIVE', '上传文件', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10571, 'file:manage:download', '下载文件', 'BUTTON', 'FILE', 'DOWNLOAD', 'file', 'ACTIVE', '下载文件', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10572, 'file:manage:preview', '预览文件', 'BUTTON', 'FILE', 'PREVIEW', 'file', 'ACTIVE', '预览文件', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10573, 'file:manage:delete', '删除文件', 'BUTTON', 'FILE', 'DELETE', 'file', 'ACTIVE', '删除文件', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10574, 'audit:login:export', '导出登录审计', 'BUTTON', 'AUDIT', 'LOGIN_EXPORT', 'audit', 'ACTIVE', '导出登录审计', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10575, 'audit:operation:export', '导出操作审计', 'BUTTON', 'AUDIT', 'OPERATION_EXPORT', 'audit', 'ACTIVE', '导出操作审计', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `permission_type` = VALUES(`permission_type`), `resource_type` = VALUES(`resource_type`), `action_code` = VALUES(`action_code`), `module_code` = VALUES(`module_code`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `iam_role_permission` (`id`, `role_id`, `permission_id`, `creator_id`, `created_at`)
SELECT 106000 + ROW_NUMBER() OVER (ORDER BY p.id), 10301, p.id, 10001, CURRENT_TIMESTAMP(3)
FROM `iam_permission` p
WHERE NOT EXISTS (
  SELECT 1 FROM `iam_role_permission` rp WHERE rp.role_id = 10301 AND rp.permission_id = p.id
);

INSERT INTO `iam_role_permission` (`id`, `role_id`, `permission_id`, `creator_id`, `created_at`)
SELECT 107000 + ROW_NUMBER() OVER (ORDER BY p.id), 10302, p.id, 10001, CURRENT_TIMESTAMP(3)
FROM `iam_permission` p
WHERE p.permission_code NOT IN ('announcement:inbox:read')
  AND NOT EXISTS (
    SELECT 1 FROM `iam_role_permission` rp WHERE rp.role_id = 10302 AND rp.permission_id = p.id
  );

INSERT INTO `iam_role_permission` (`id`, `role_id`, `permission_id`, `creator_id`, `created_at`)
SELECT 108000 + ROW_NUMBER() OVER (ORDER BY p.id), 10303, p.id, 10001, CURRENT_TIMESTAMP(3)
FROM `iam_permission` p
WHERE p.permission_code IN ('dashboard:view', 'announcement:inbox:view', 'announcement:inbox:read', 'profile:view', 'profile:update', 'profile:avatar:update', 'profile:mobile:update', 'profile:email:update', 'profile:password:update')
  AND NOT EXISTS (
    SELECT 1 FROM `iam_role_permission` rp WHERE rp.role_id = 10303 AND rp.permission_id = p.id
  );

INSERT INTO `iam_role_permission` (`id`, `role_id`, `permission_id`, `creator_id`, `created_at`)
SELECT 109000 + ROW_NUMBER() OVER (ORDER BY p.id), 10304, p.id, 10001, CURRENT_TIMESTAMP(3)
FROM `iam_permission` p
WHERE p.permission_code IN ('dashboard:view', 'announcement:navigation', 'announcement:manage:view', 'announcement:manage:create', 'announcement:manage:update', 'announcement:manage:delete', 'announcement:manage:publish', 'announcement:manage:revoke', 'announcement:inbox:view', 'announcement:inbox:read', 'file:navigation', 'file:manage:view', 'file:manage:upload', 'file:manage:download', 'file:manage:preview', 'profile:view', 'profile:update', 'profile:avatar:update', 'profile:mobile:update', 'profile:email:update', 'profile:password:update')
  AND NOT EXISTS (
    SELECT 1 FROM `iam_role_permission` rp WHERE rp.role_id = 10304 AND rp.permission_id = p.id
  );

INSERT INTO `ui_navigation_permission` (`id`, `navigation_id`, `permission_id`, `creator_id`, `created_at`)
SELECT 111000 + ROW_NUMBER() OVER (ORDER BY n.id, p.id), n.id, p.id, 10001, CURRENT_TIMESTAMP(3)
FROM `ui_navigation` n
JOIN `iam_permission` p ON (
  (n.nav_code = 'dashboard' AND p.permission_code = 'dashboard:view') OR
  (n.nav_code = 'system' AND p.permission_code = 'system:navigation') OR
  (n.nav_code = 'system_org' AND p.permission_code = 'system:org:view') OR
  (n.nav_code = 'system_user' AND p.permission_code = 'system:user:view') OR
  (n.nav_code = 'system_role' AND p.permission_code = 'system:role:view') OR
  (n.nav_code = 'system_navigation' AND p.permission_code = 'system:navigation:view') OR
  (n.nav_code = 'system_permission' AND p.permission_code = 'system:permission:view') OR
  (n.nav_code = 'system_dictionary' AND p.permission_code = 'system:dictionary:view') OR
  (n.nav_code = 'system_setting' AND p.permission_code = 'system:setting:view') OR
  (n.nav_code = 'system_session' AND p.permission_code = 'auth:session:view') OR
  (n.nav_code = 'announcement' AND p.permission_code = 'announcement:navigation') OR
  (n.nav_code = 'announcement_manage' AND p.permission_code = 'announcement:manage:view') OR
  (n.nav_code = 'announcement_inbox' AND p.permission_code = 'announcement:inbox:view') OR
  (n.nav_code = 'file' AND p.permission_code = 'file:navigation') OR
  (n.nav_code = 'file_manage' AND p.permission_code = 'file:manage:view') OR
  (n.nav_code = 'audit' AND p.permission_code = 'audit:navigation') OR
  (n.nav_code = 'audit_login' AND p.permission_code = 'audit:login:view') OR
  (n.nav_code = 'audit_operation' AND p.permission_code = 'audit:operation:view') OR
  (n.nav_code = 'profile' AND p.permission_code = 'profile:view')
)
WHERE NOT EXISTS (
  SELECT 1 FROM `ui_navigation_permission` np WHERE np.navigation_id = n.id AND np.permission_id = p.id
);
