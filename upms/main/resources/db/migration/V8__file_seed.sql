INSERT INTO `file_asset` (
  `id`, `file_code`, `original_name`, `storage_name`, `category_code`, `file_ext`,
  `mime_type`, `size_bytes`, `storage_provider`, `bucket_name`, `object_key`, `checksum`,
  `visibility`, `uploader_user_id`, `status`, `remark`, `content_blob`, `created_at`, `updated_at`
) VALUES (
  15001, 'FILE-15001', 'platform-overview.txt', 'platform-overview.txt', 'document', 'txt',
  'text/plain', 24, 'DATABASE', 'db', 'FILE-15001', 'seed-platform-overview',
  'PRIVATE', 10001, 'ACTIVE', '平台说明文档', _binary '平台初始化说明内容', DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 2 DAY)
) ON DUPLICATE KEY UPDATE
  `original_name` = VALUES(`original_name`),
  `storage_name` = VALUES(`storage_name`),
  `category_code` = VALUES(`category_code`),
  `file_ext` = VALUES(`file_ext`),
  `mime_type` = VALUES(`mime_type`),
  `size_bytes` = VALUES(`size_bytes`),
  `storage_provider` = VALUES(`storage_provider`),
  `bucket_name` = VALUES(`bucket_name`),
  `object_key` = VALUES(`object_key`),
  `checksum` = VALUES(`checksum`),
  `visibility` = VALUES(`visibility`),
  `uploader_user_id` = VALUES(`uploader_user_id`),
  `status` = VALUES(`status`),
  `remark` = VALUES(`remark`),
  `content_blob` = VALUES(`content_blob`),
  `updated_at` = VALUES(`updated_at`);

INSERT INTO `file_asset` (
  `id`, `file_code`, `original_name`, `storage_name`, `category_code`, `file_ext`,
  `mime_type`, `size_bytes`, `storage_provider`, `bucket_name`, `object_key`, `checksum`,
  `visibility`, `uploader_user_id`, `status`, `remark`, `content_blob`, `created_at`, `updated_at`
) VALUES (
  15002, 'FILE-15002', 'security-checklist.txt', 'security-checklist.txt', 'security', 'txt',
  'text/plain', 46, 'DATABASE', 'db', 'FILE-15002', 'seed-security-checklist',
  'PRIVATE', 10001, 'ACTIVE', '安全检查清单', _binary '1. 修改默认密码
2. 检查权限
3. 清理无效账号', DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 5 HOUR), DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 5 HOUR)
) ON DUPLICATE KEY UPDATE
  `original_name` = VALUES(`original_name`),
  `storage_name` = VALUES(`storage_name`),
  `category_code` = VALUES(`category_code`),
  `file_ext` = VALUES(`file_ext`),
  `mime_type` = VALUES(`mime_type`),
  `size_bytes` = VALUES(`size_bytes`),
  `storage_provider` = VALUES(`storage_provider`),
  `bucket_name` = VALUES(`bucket_name`),
  `object_key` = VALUES(`object_key`),
  `checksum` = VALUES(`checksum`),
  `visibility` = VALUES(`visibility`),
  `uploader_user_id` = VALUES(`uploader_user_id`),
  `status` = VALUES(`status`),
  `remark` = VALUES(`remark`),
  `content_blob` = VALUES(`content_blob`),
  `updated_at` = VALUES(`updated_at`);
