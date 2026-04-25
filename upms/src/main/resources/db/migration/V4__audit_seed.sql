INSERT INTO `audit_login_event` (
  `id`, `user_id`, `account_identifier`, `auth_type`, `event_type`, `success_flag`,
  `reason_code`, `ip`, `user_agent`, `session_key`, `trace_id`, `request_id`, `event_time`
) VALUES (
  13001, 10001, "admin", "PASSWORD", "LOGIN_SUCCESS", 1,
  NULL, "127.0.0.1", "Chrome 135 / macOS", NULL, NULL, NULL, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 1 HOUR)
) ON DUPLICATE KEY UPDATE
  `user_id` = VALUES(`user_id`),
  `account_identifier` = VALUES(`account_identifier`),
  `auth_type` = VALUES(`auth_type`),
  `event_type` = VALUES(`event_type`),
  `success_flag` = VALUES(`success_flag`),
  `reason_code` = VALUES(`reason_code`),
  `ip` = VALUES(`ip`),
  `user_agent` = VALUES(`user_agent`),
  `session_key` = VALUES(`session_key`),
  `trace_id` = VALUES(`trace_id`),
  `request_id` = VALUES(`request_id`),
  `event_time` = VALUES(`event_time`);

INSERT INTO `audit_login_event` (
  `id`, `user_id`, `account_identifier`, `auth_type`, `event_type`, `success_flag`,
  `reason_code`, `ip`, `user_agent`, `session_key`, `trace_id`, `request_id`, `event_time`
) VALUES (
  13002, NULL, "wangwu", "PASSWORD", "LOGIN_FAIL", 0,
  "用户名或密码错误", "10.10.1.32", "Chrome 134 / Windows", NULL, NULL, NULL, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 8 HOUR)
) ON DUPLICATE KEY UPDATE
  `user_id` = VALUES(`user_id`),
  `account_identifier` = VALUES(`account_identifier`),
  `auth_type` = VALUES(`auth_type`),
  `event_type` = VALUES(`event_type`),
  `success_flag` = VALUES(`success_flag`),
  `reason_code` = VALUES(`reason_code`),
  `ip` = VALUES(`ip`),
  `user_agent` = VALUES(`user_agent`),
  `session_key` = VALUES(`session_key`),
  `trace_id` = VALUES(`trace_id`),
  `request_id` = VALUES(`request_id`),
  `event_time` = VALUES(`event_time`);
