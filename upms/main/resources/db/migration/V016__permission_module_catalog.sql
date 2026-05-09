-- 描述：permission_module_catalog
-- 作者：宇宙星星
-- 日期：2026-05-06

CREATE TABLE IF NOT EXISTS `iam_permission_module` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `module_code` VARCHAR(64) NOT NULL COMMENT '模块编码',
  `module_name` VARCHAR(128) NOT NULL COMMENT '模块名称',
  `parent_code` VARCHAR(64) NULL COMMENT '父模块编码',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater_id` BIGINT NULL COMMENT '更新人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iam_permission_module_code` (`module_code`),
  KEY `idx_iam_permission_module_parent_code` (`parent_code`),
  KEY `idx_iam_permission_module_status` (`status`),
  KEY `idx_iam_permission_module_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限模块目录表';

INSERT INTO `iam_permission_module` (`id`, `module_code`, `module_name`, `parent_code`, `sort_order`, `status`, `remark`, `deleted`, `creator_id`, `created_at`, `updater_id`, `updated_at`) VALUES
  (10601, 'dashboard', '工作台', NULL, 10, 'ACTIVE', '工作台权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10602, 'system', '系统管理', NULL, 20, 'ACTIVE', '系统管理权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10603, 'org', '组织管理', 'system', 30, 'ACTIVE', '组织权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10604, 'user', '用户管理', 'system', 40, 'ACTIVE', '用户权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10605, 'role', '角色管理', 'system', 50, 'ACTIVE', '角色权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10606, 'navigation', '导航管理', 'system', 60, 'ACTIVE', '导航权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10607, 'permission', '权限定义', 'system', 70, 'ACTIVE', '权限定义分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10608, 'dictionary', '字典管理', 'system', 80, 'ACTIVE', '字典权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10609, 'setting', '系统配置', 'system', 90, 'ACTIVE', '系统配置权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10610, 'announcement', '公告管理', NULL, 100, 'ACTIVE', '公告权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10611, 'file', '文件管理', NULL, 110, 'ACTIVE', '文件权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10612, 'audit', '审计中心', NULL, 120, 'ACTIVE', '审计权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10613, 'auth', '认证管理', NULL, 130, 'ACTIVE', '认证权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3)),
  (10614, 'profile', '个人中心', NULL, 140, 'ACTIVE', '个人中心权限分组', 0, 10001, CURRENT_TIMESTAMP(3), 10001, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE `module_name` = VALUES(`module_name`), `parent_code` = VALUES(`parent_code`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `deleted` = VALUES(`deleted`), `updater_id` = VALUES(`updater_id`), `updated_at` = VALUES(`updated_at`);
