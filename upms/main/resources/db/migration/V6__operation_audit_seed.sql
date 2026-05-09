INSERT INTO `audit_operation_event` (
  `id`, `actor_user_id`, `module_code`, `action_code`, `target_type`, `target_id`,
  `request_method`, `request_uri`, `request_summary`, `response_summary`, `result_status`,
  `duration_ms`, `ip`, `trace_id`, `request_id`, `event_time`
) VALUES (
  14001, 10001, 'user', 'CREATE', 'USER', 10002,
  'POST', '/admin/user', '{"username":"test01"}', '{"code":0,"message":"success"}', 'SUCCESS',
  126, '127.0.0.1', NULL, NULL, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 12 MINUTE)
) ON DUPLICATE KEY UPDATE
  `actor_user_id` = VALUES(`actor_user_id`),
  `module_code` = VALUES(`module_code`),
  `action_code` = VALUES(`action_code`),
  `target_type` = VALUES(`target_type`),
  `target_id` = VALUES(`target_id`),
  `request_method` = VALUES(`request_method`),
  `request_uri` = VALUES(`request_uri`),
  `request_summary` = VALUES(`request_summary`),
  `response_summary` = VALUES(`response_summary`),
  `result_status` = VALUES(`result_status`),
  `duration_ms` = VALUES(`duration_ms`),
  `ip` = VALUES(`ip`),
  `trace_id` = VALUES(`trace_id`),
  `request_id` = VALUES(`request_id`),
  `event_time` = VALUES(`event_time`);

INSERT INTO `audit_operation_event` (
  `id`, `actor_user_id`, `module_code`, `action_code`, `target_type`, `target_id`,
  `request_method`, `request_uri`, `request_summary`, `response_summary`, `result_status`,
  `duration_ms`, `ip`, `trace_id`, `request_id`, `event_time`
) VALUES (
  14002, 10001, 'navigation', 'DELETE', 'NAVIGATION', 21,
  'DELETE', '/admin/navigation/21', '{}', '{"code":403,"message":"当前角色无删除权限"}', 'FAIL',
  96, '127.0.0.1', NULL, NULL, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 2 DAY)
) ON DUPLICATE KEY UPDATE
  `actor_user_id` = VALUES(`actor_user_id`),
  `module_code` = VALUES(`module_code`),
  `action_code` = VALUES(`action_code`),
  `target_type` = VALUES(`target_type`),
  `target_id` = VALUES(`target_id`),
  `request_method` = VALUES(`request_method`),
  `request_uri` = VALUES(`request_uri`),
  `request_summary` = VALUES(`request_summary`),
  `response_summary` = VALUES(`response_summary`),
  `result_status` = VALUES(`result_status`),
  `duration_ms` = VALUES(`duration_ms`),
  `ip` = VALUES(`ip`),
  `trace_id` = VALUES(`trace_id`),
  `request_id` = VALUES(`request_id`),
  `event_time` = VALUES(`event_time`);
