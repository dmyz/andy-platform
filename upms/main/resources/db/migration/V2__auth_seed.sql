INSERT INTO `iam_user` (
  `id`, `user_code`, `display_name`, `gender`, `employee_no`, `avatar_file_id`,
  `user_type`, `source_type`, `status`, `password_reset_required`,
  `last_login_time`, `last_login_ip`, `remark`, `deleted`,
  `creator_id`, `created_at`, `updater_id`, `updated_at`
) VALUES (
  10001, 'ADMIN', '超级管理员', 'UNKNOWN', 'ADMIN001', NULL,
  'ADMIN', 'LOCAL', 'ACTIVE', 0,
  NULL, NULL, 'dbdev 初始化管理员', 0,
  10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)
) ON DUPLICATE KEY UPDATE
  `display_name` = VALUES(`display_name`),
  `status` = VALUES(`status`),
  `deleted` = VALUES(`deleted`),
  `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `iam_account` (
  `id`, `user_id`, `account_type`, `identifier`, `normalized_identifier`,
  `is_login_enabled`, `is_primary`, `verified_flag`, `status`,
  `last_used_time`, `creator_id`, `created_at`, `updater_id`, `updated_at`
) VALUES (
  11001, 10001, 'USERNAME', 'admin', 'admin',
  1, 1, 1, 'ACTIVE',
  NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)
) ON DUPLICATE KEY UPDATE
  `identifier` = VALUES(`identifier`),
  `normalized_identifier` = VALUES(`normalized_identifier`),
  `is_login_enabled` = VALUES(`is_login_enabled`),
  `status` = VALUES(`status`),
  `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `iam_account` (
  `id`, `user_id`, `account_type`, `identifier`, `normalized_identifier`,
  `is_login_enabled`, `is_primary`, `verified_flag`, `status`,
  `last_used_time`, `creator_id`, `created_at`, `updater_id`, `updated_at`
) VALUES (
  11002, 10001, 'MOBILE', '13800138000', '13800138000',
  1, 0, 1, 'ACTIVE',
  NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)
) ON DUPLICATE KEY UPDATE
  `identifier` = VALUES(`identifier`),
  `normalized_identifier` = VALUES(`normalized_identifier`),
  `is_login_enabled` = VALUES(`is_login_enabled`),
  `status` = VALUES(`status`),
  `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `iam_account` (
  `id`, `user_id`, `account_type`, `identifier`, `normalized_identifier`,
  `is_login_enabled`, `is_primary`, `verified_flag`, `status`,
  `last_used_time`, `creator_id`, `created_at`, `updater_id`, `updated_at`
) VALUES (
  11003, 10001, 'EMAIL', 'admin@example.com', 'admin@example.com',
  1, 0, 1, 'ACTIVE',
  NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)
) ON DUPLICATE KEY UPDATE
  `identifier` = VALUES(`identifier`),
  `normalized_identifier` = VALUES(`normalized_identifier`),
  `is_login_enabled` = VALUES(`is_login_enabled`),
  `status` = VALUES(`status`),
  `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `auth_password_credential` (
  `id`, `user_id`, `password_hash`, `password_algo`, `password_changed_time`,
  `temporary_flag`, `failed_count`, `locked_until`, `created_at`, `updated_at`
) VALUES (
  12001, 10001, '{bcrypt}$2y$10$i6NMBMb8AiON6iCxrhExLek2hEuKoVoG0llb5/Hkv.Kr0hYYo5ItC', 'BCRYPT',
  CURRENT_TIMESTAMP(3), 0, 0, NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)
) ON DUPLICATE KEY UPDATE
  `password_hash` = VALUES(`password_hash`),
  `password_algo` = VALUES(`password_algo`),
  `failed_count` = 0,
  `locked_until` = NULL,
  `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `iam_user` (
  `id`, `user_code`, `display_name`, `gender`, `employee_no`, `avatar_file_id`,
  `user_type`, `source_type`, `status`, `password_reset_required`,
  `last_login_time`, `last_login_ip`, `remark`, `deleted`,
  `creator_id`, `created_at`, `updater_id`, `updated_at`
) VALUES
  (10002, 'LISI', '李四', 'MALE', 'A0002', NULL, 'STAFF', 'LOCAL', 'ACTIVE', 0, NULL, NULL, '系统管理员角色示例账号', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10003, 'WANGWU', '王五', 'MALE', 'A0003', NULL, 'STAFF', 'LOCAL', 'ACTIVE', 0, NULL, NULL, '普通用户示例账号', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10004, 'ZHAOLIU', '赵六', 'MALE', 'A0004', NULL, 'STAFF', 'LOCAL', 'ACTIVE', 0, NULL, NULL, '普通用户示例账号', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10005, 'SUNQI', '孙七', 'FEMALE', 'A0005', NULL, 'STAFF', 'LOCAL', 'ACTIVE', 0, NULL, NULL, '运营角色示例账号', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10006, 'ZHOUBA', '周八', 'FEMALE', 'A0006', NULL, 'STAFF', 'LOCAL', 'INACTIVE', 0, NULL, NULL, '禁用状态示例账号', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
  `display_name` = VALUES(`display_name`),
  `status` = VALUES(`status`),
  `remark` = VALUES(`remark`),
  `deleted` = VALUES(`deleted`),
  `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `iam_account` (
  `id`, `user_id`, `account_type`, `identifier`, `normalized_identifier`,
  `is_login_enabled`, `is_primary`, `verified_flag`, `status`,
  `last_used_time`, `creator_id`, `created_at`, `updater_id`, `updated_at`
) VALUES
  (11011, 10002, 'USERNAME', 'lisi', 'lisi', 1, 1, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11012, 10002, 'MOBILE', '13800138001', '13800138001', 1, 0, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11013, 10002, 'EMAIL', 'lisi@example.com', 'lisi@example.com', 1, 0, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11021, 10003, 'USERNAME', 'wangwu', 'wangwu', 1, 1, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11022, 10003, 'MOBILE', '13800138002', '13800138002', 1, 0, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11023, 10003, 'EMAIL', 'wangwu@example.com', 'wangwu@example.com', 1, 0, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11031, 10004, 'USERNAME', 'zhaoliu', 'zhaoliu', 1, 1, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11032, 10004, 'MOBILE', '13800138003', '13800138003', 1, 0, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11033, 10004, 'EMAIL', 'zhaoliu@example.com', 'zhaoliu@example.com', 1, 0, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11041, 10005, 'USERNAME', 'sunqi', 'sunqi', 1, 1, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11042, 10005, 'MOBILE', '13800138004', '13800138004', 1, 0, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11043, 10005, 'EMAIL', 'sunqi@example.com', 'sunqi@example.com', 1, 0, 1, 'ACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11051, 10006, 'USERNAME', 'zhouba', 'zhouba', 0, 1, 1, 'INACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11052, 10006, 'MOBILE', '13800138005', '13800138005', 0, 0, 1, 'INACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (11053, 10006, 'EMAIL', 'zhouba@example.com', 'zhouba@example.com', 0, 0, 1, 'INACTIVE', NULL, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
  `identifier` = VALUES(`identifier`),
  `normalized_identifier` = VALUES(`normalized_identifier`),
  `is_login_enabled` = VALUES(`is_login_enabled`),
  `status` = VALUES(`status`),
  `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `auth_password_credential` (
  `id`, `user_id`, `password_hash`, `password_algo`, `password_changed_time`,
  `temporary_flag`, `failed_count`, `locked_until`, `created_at`, `updated_at`
) VALUES
  (12002, 10002, '{noop}lisi', 'NOOP', CURRENT_TIMESTAMP(3), 0, 0, NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
  (12003, 10003, '{noop}wangwu', 'NOOP', CURRENT_TIMESTAMP(3), 0, 0, NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
  (12004, 10004, '{noop}zhaoliu', 'NOOP', CURRENT_TIMESTAMP(3), 0, 0, NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
  (12005, 10005, '{noop}sunqi', 'NOOP', CURRENT_TIMESTAMP(3), 0, 0, NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
  (12006, 10006, '{noop}zhouba', 'NOOP', CURRENT_TIMESTAMP(3), 0, 0, NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
  `password_hash` = VALUES(`password_hash`),
  `password_algo` = VALUES(`password_algo`),
  `failed_count` = 0,
  `locked_until` = NULL,
  `updated_at` = CURRENT_TIMESTAMP(3);
