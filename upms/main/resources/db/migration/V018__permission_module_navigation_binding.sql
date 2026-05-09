-- 描述：permission_module_navigation_binding
-- 作者：宇宙星星
-- 日期：2026-05-06

INSERT INTO `iam_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `resource_type`, `action_code`, `module_code`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (10576, 'system:permission-module:view', '查看权限模块', 'MENU', 'PERMISSION_MODULE', 'VIEW', 'permission', 'ACTIVE', '查看权限模块列表', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10577, 'system:permission-module:create', '新增权限模块', 'BUTTON', 'PERMISSION_MODULE', 'CREATE', 'permission', 'ACTIVE', '新增权限模块', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10578, 'system:permission-module:update', '编辑权限模块', 'BUTTON', 'PERMISSION_MODULE', 'UPDATE', 'permission', 'ACTIVE', '编辑权限模块', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10579, 'system:permission-module:delete', '删除权限模块', 'BUTTON', 'PERMISSION_MODULE', 'DELETE', 'permission', 'ACTIVE', '删除权限模块', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `permission_type` = VALUES(`permission_type`), `resource_type` = VALUES(`resource_type`), `action_code` = VALUES(`action_code`), `module_code` = VALUES(`module_code`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `iam_role_permission` (`id`, `role_id`, `permission_id`, `creator_id`, `created_at`)
SELECT 118000 + ROW_NUMBER() OVER (ORDER BY p.id), 10301, p.id, 10001, CURRENT_TIMESTAMP(3)
FROM `iam_permission` p
WHERE p.permission_code IN ('system:permission-module:view', 'system:permission-module:create', 'system:permission-module:update', 'system:permission-module:delete')
  AND NOT EXISTS (
    SELECT 1 FROM `iam_role_permission` rp WHERE rp.role_id = 10301 AND rp.permission_id = p.id
  );

INSERT INTO `iam_role_permission` (`id`, `role_id`, `permission_id`, `creator_id`, `created_at`)
SELECT 119000 + ROW_NUMBER() OVER (ORDER BY p.id), 10302, p.id, 10001, CURRENT_TIMESTAMP(3)
FROM `iam_permission` p
WHERE p.permission_code IN ('system:permission-module:view', 'system:permission-module:create', 'system:permission-module:update', 'system:permission-module:delete')
  AND NOT EXISTS (
    SELECT 1 FROM `iam_role_permission` rp WHERE rp.role_id = 10302 AND rp.permission_id = p.id
  );

UPDATE `ui_navigation`
SET `sort_order` = 9,
    `updater_id` = 10001,
    `updated_at` = CURRENT_TIMESTAMP(3)
WHERE `nav_code` = 'system_session';

UPDATE `ui_navigation`
SET `sort_order` = 8,
    `updater_id` = 10001,
    `updated_at` = CURRENT_TIMESTAMP(3)
WHERE `nav_code` = 'system_setting';

UPDATE `ui_navigation`
SET `sort_order` = 7,
    `updater_id` = 10001,
    `updated_at` = CURRENT_TIMESTAMP(3)
WHERE `nav_code` = 'system_dictionary';

INSERT INTO `ui_navigation` (`id`, `parent_id`, `nav_code`, `nav_name`, `nav_type`, `route_path`, `component_path`, `icon`, `external_url`, `visible_flag`, `sort_order`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (11029, 11002, 'system_permission_module', '权限模块', 'PAGE', '/system/permission-module', 'views/system/permission-module/index.vue', 'secured', NULL, 1, 6, 'ACTIVE', '权限模块目录维护', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `parent_id` = VALUES(`parent_id`), `nav_name` = VALUES(`nav_name`), `nav_type` = VALUES(`nav_type`), `route_path` = VALUES(`route_path`), `component_path` = VALUES(`component_path`), `icon` = VALUES(`icon`), `external_url` = VALUES(`external_url`), `visible_flag` = VALUES(`visible_flag`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);

INSERT INTO `ui_navigation_permission` (`id`, `navigation_id`, `permission_id`, `creator_id`, `created_at`)
SELECT 111020, n.id, p.id, 10001, CURRENT_TIMESTAMP(3)
FROM `ui_navigation` n
JOIN `iam_permission` p ON p.permission_code = 'system:permission-module:view'
WHERE n.nav_code = 'system_permission_module'
  AND NOT EXISTS (
    SELECT 1 FROM `ui_navigation_permission` np WHERE np.navigation_id = n.id AND np.permission_id = p.id
  );
