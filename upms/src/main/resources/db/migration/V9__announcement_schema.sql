CREATE TABLE IF NOT EXISTS `msg_announcement` (
  `id` bigint NOT NULL,
  `message_code` varchar(64) NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `content_format` varchar(32) NOT NULL,
  `category_code` varchar(64) NOT NULL,
  `channel_type` varchar(32) NOT NULL,
  `publish_status` varchar(32) NOT NULL,
  `publish_time` datetime(3) DEFAULT NULL,
  `expire_time` datetime(3) DEFAULT NULL,
  `priority_level` int NOT NULL,
  `pin_flag` tinyint NOT NULL,
  `creator_id` bigint DEFAULT NULL,
  `created_at` datetime(3) NOT NULL,
  `updater_id` bigint DEFAULT NULL,
  `updated_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_msg_announcement_message_code` (`message_code`),
  KEY `idx_msg_announcement_publish_status` (`publish_status`),
  KEY `idx_msg_announcement_publish_time` (`publish_time`),
  KEY `idx_msg_announcement_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `msg_target` (
  `id` bigint NOT NULL,
  `announcement_id` bigint NOT NULL,
  `target_type` varchar(32) NOT NULL,
  `target_id` bigint DEFAULT NULL,
  `target_value` varchar(128) DEFAULT NULL,
  `creator_id` bigint DEFAULT NULL,
  `created_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_msg_target_announcement_target` (`announcement_id`, `target_type`, `target_id`, `target_value`),
  KEY `idx_msg_target_type_id` (`target_type`, `target_id`),
  KEY `idx_msg_target_type_value` (`target_type`, `target_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `msg_receipt` (
  `id` bigint NOT NULL,
  `announcement_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `delivery_status` varchar(32) NOT NULL,
  `read_flag` tinyint NOT NULL,
  `delivered_time` datetime(3) DEFAULT NULL,
  `read_time` datetime(3) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL,
  `updated_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_msg_receipt_announcement_user` (`announcement_id`, `user_id`),
  KEY `idx_msg_receipt_user_id` (`user_id`),
  KEY `idx_msg_receipt_read_flag` (`read_flag`),
  KEY `idx_msg_receipt_delivery_status` (`delivery_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
