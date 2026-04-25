INSERT INTO `msg_announcement` (
  `id`, `message_code`, `title`, `content`, `content_format`, `category_code`, `channel_type`,
  `publish_status`, `publish_time`, `expire_time`, `priority_level`, `pin_flag`,
  `creator_id`, `created_at`, `updater_id`, `updated_at`
) VALUES (
  16001, 'MSG-16001', '系统升级通知', '今晚 23:00 至 23:30 进行平台升级维护，请提前保存数据。', 'TEXT', 'SYSTEM', 'IN_APP',
  'PUBLISHED', DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 6 HOUR), NULL, 100, 1,
  10001, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 6 HOUR), 10001, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 6 HOUR)
) ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `content` = VALUES(`content`),
  `publish_status` = VALUES(`publish_status`),
  `publish_time` = VALUES(`publish_time`),
  `pin_flag` = VALUES(`pin_flag`),
  `updated_at` = VALUES(`updated_at`);

INSERT INTO `msg_announcement` (
  `id`, `message_code`, `title`, `content`, `content_format`, `category_code`, `channel_type`,
  `publish_status`, `publish_time`, `expire_time`, `priority_level`, `pin_flag`,
  `creator_id`, `created_at`, `updater_id`, `updated_at`
) VALUES (
  16002, 'MSG-16002', '权限模型优化上线', '导航访问规则与动作权限已解耦，角色授权后即时生效。', 'TEXT', 'FEATURE', 'IN_APP',
  'PUBLISHED', DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 1 DAY), NULL, 50, 0,
  10001, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 1 DAY), 10001, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 1 DAY)
) ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `content` = VALUES(`content`),
  `publish_status` = VALUES(`publish_status`),
  `publish_time` = VALUES(`publish_time`),
  `pin_flag` = VALUES(`pin_flag`),
  `updated_at` = VALUES(`updated_at`);

INSERT INTO `msg_target` (
  `id`, `announcement_id`, `target_type`, `target_id`, `target_value`, `creator_id`, `created_at`
) VALUES
  (16101, 16001, 'ALL', NULL, NULL, 10001, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 6 HOUR)),
  (16102, 16002, 'ROLE', NULL, 'admin', 10001, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE
  `target_value` = VALUES(`target_value`),
  `created_at` = VALUES(`created_at`);

INSERT INTO `msg_receipt` (
  `id`, `announcement_id`, `user_id`, `delivery_status`, `read_flag`, `delivered_time`, `read_time`, `created_at`, `updated_at`
) VALUES (
  16201, 16002, 10001, 'DELIVERED', 1, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 1 DAY), DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 12 HOUR), DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 1 DAY), DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 12 HOUR)
) ON DUPLICATE KEY UPDATE
  `read_flag` = VALUES(`read_flag`),
  `read_time` = VALUES(`read_time`),
  `updated_at` = VALUES(`updated_at`);
