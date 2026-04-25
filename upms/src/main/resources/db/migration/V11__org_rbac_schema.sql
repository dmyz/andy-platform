CREATE TABLE IF NOT EXISTS `org_unit` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `parent_id` BIGINT NULL COMMENT '父组织ID',
  `org_code` VARCHAR(64) NOT NULL COMMENT '组织编码',
  `org_name` VARCHAR(128) NOT NULL COMMENT '组织名称',
  `org_full_name` VARCHAR(255) NOT NULL COMMENT '组织全称路径',
  `leader_user_id` BIGINT NULL COMMENT '负责人用户ID',
  `level_no` INT NOT NULL DEFAULT 1 COMMENT '层级',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater_id` BIGINT NULL COMMENT '更新人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_unit_org_code` (`org_code`),
  KEY `idx_org_unit_parent_id` (`parent_id`),
  KEY `idx_org_unit_status` (`status`),
  KEY `idx_org_unit_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织单元表';

CREATE TABLE IF NOT EXISTS `org_membership` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `org_id` BIGINT NOT NULL COMMENT '组织ID',
  `position_name` VARCHAR(64) NULL COMMENT '岗位名称',
  `is_primary` TINYINT NOT NULL DEFAULT 1 COMMENT '是否主组织关系',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `joined_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '加入时间',
  `left_at` DATETIME(3) NULL COMMENT '离开时间',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater_id` BIGINT NULL COMMENT '更新人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_membership_user_org_primary` (`user_id`, `org_id`, `is_primary`),
  KEY `idx_org_membership_org_id` (`org_id`),
  KEY `idx_org_membership_user_id` (`user_id`),
  KEY `idx_org_membership_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织成员关系表';

CREATE TABLE IF NOT EXISTS `iam_role` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
  `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `role_type` VARCHAR(32) NOT NULL DEFAULT 'PLATFORM' COMMENT '角色类型',
  `data_scope` VARCHAR(32) NOT NULL DEFAULT 'SELF' COMMENT '数据权限范围',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater_id` BIGINT NULL COMMENT '更新人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iam_role_role_code` (`role_code`),
  KEY `idx_iam_role_status` (`status`),
  KEY `idx_iam_role_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `iam_role_binding` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `subject_type` VARCHAR(32) NOT NULL COMMENT '主体类型',
  `subject_id` BIGINT NOT NULL COMMENT '主体ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `source_type` VARCHAR(32) NOT NULL DEFAULT 'MANUAL' COMMENT '来源类型',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `effective_from` DATETIME(3) NULL COMMENT '生效开始时间',
  `effective_to` DATETIME(3) NULL COMMENT '生效结束时间',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iam_role_binding_subject_role` (`subject_type`, `subject_id`, `role_id`),
  KEY `idx_iam_role_binding_role_id` (`role_id`),
  KEY `idx_iam_role_binding_subject` (`subject_type`, `subject_id`),
  KEY `idx_iam_role_binding_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色分配表';

CREATE TABLE IF NOT EXISTS `iam_permission` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `permission_code` VARCHAR(128) NOT NULL COMMENT '权限编码',
  `permission_name` VARCHAR(128) NOT NULL COMMENT '权限名称',
  `permission_type` VARCHAR(32) NOT NULL COMMENT '权限类型',
  `resource_type` VARCHAR(32) NOT NULL COMMENT '资源类型',
  `action_code` VARCHAR(64) NOT NULL COMMENT '动作编码',
  `module_code` VARCHAR(64) NOT NULL COMMENT '模块编码',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater_id` BIGINT NULL COMMENT '更新人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iam_permission_permission_code` (`permission_code`),
  KEY `idx_iam_permission_module_code` (`module_code`),
  KEY `idx_iam_permission_status` (`status`),
  KEY `idx_iam_permission_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限定义表';

CREATE TABLE IF NOT EXISTS `iam_role_permission` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iam_role_permission_role_permission` (`role_id`, `permission_id`),
  KEY `idx_iam_role_permission_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限表';
