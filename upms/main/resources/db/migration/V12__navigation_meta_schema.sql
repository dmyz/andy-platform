CREATE TABLE IF NOT EXISTS `ui_navigation` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `parent_id` BIGINT NULL COMMENT '父节点ID',
  `nav_code` VARCHAR(64) NOT NULL COMMENT '导航编码',
  `nav_name` VARCHAR(64) NOT NULL COMMENT '导航名称',
  `nav_type` VARCHAR(32) NOT NULL COMMENT '导航类型',
  `route_path` VARCHAR(255) NOT NULL COMMENT '路由路径',
  `component_path` VARCHAR(255) NOT NULL COMMENT '组件路径',
  `icon` VARCHAR(64) NULL COMMENT '图标',
  `external_url` VARCHAR(255) NULL COMMENT '外链地址',
  `visible_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater_id` BIGINT NULL COMMENT '更新人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ui_navigation_nav_code` (`nav_code`),
  KEY `idx_ui_navigation_parent_id` (`parent_id`),
  KEY `idx_ui_navigation_route_path` (`route_path`),
  KEY `idx_ui_navigation_status` (`status`),
  KEY `idx_ui_navigation_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导航节点表';

CREATE TABLE IF NOT EXISTS `ui_navigation_permission` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `navigation_id` BIGINT NOT NULL COMMENT '导航ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ui_navigation_permission_nav_permission` (`navigation_id`, `permission_id`),
  KEY `idx_ui_navigation_permission_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导航权限映射表';

CREATE TABLE IF NOT EXISTS `meta_dictionary` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `dict_code` VARCHAR(64) NOT NULL COMMENT '字典编码',
  `dict_name` VARCHAR(64) NOT NULL COMMENT '字典名称',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater_id` BIGINT NULL COMMENT '更新人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meta_dictionary_dict_code` (`dict_code`),
  KEY `idx_meta_dictionary_status` (`status`),
  KEY `idx_meta_dictionary_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典分类表';

CREATE TABLE IF NOT EXISTS `meta_dictionary_item` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `dict_id` BIGINT NOT NULL COMMENT '字典ID',
  `item_text` VARCHAR(64) NOT NULL COMMENT '字典项名称',
  `item_value` VARCHAR(64) NOT NULL COMMENT '字典项值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater_id` BIGINT NULL COMMENT '更新人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meta_dictionary_item_dict_value` (`dict_id`, `item_value`),
  KEY `idx_meta_dictionary_item_dict_id` (`dict_id`),
  KEY `idx_meta_dictionary_item_status` (`status`),
  KEY `idx_meta_dictionary_item_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典项表';

CREATE TABLE IF NOT EXISTS `cfg_setting` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `setting_key` VARCHAR(128) NOT NULL COMMENT '配置键',
  `setting_name` VARCHAR(128) NOT NULL COMMENT '配置名称',
  `setting_value` TEXT NOT NULL COMMENT '配置值',
  `value_type` VARCHAR(32) NOT NULL COMMENT '值类型',
  `scope_type` VARCHAR(32) NOT NULL COMMENT '作用域类型',
  `scope_id` VARCHAR(64) NULL COMMENT '作用域对象',
  `group_code` VARCHAR(64) NOT NULL COMMENT '配置分组',
  `secret_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否敏感',
  `effective_mode` VARCHAR(32) NOT NULL DEFAULT 'REALTIME' COMMENT '生效模式',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater_id` BIGINT NULL COMMENT '更新人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cfg_setting_setting_key` (`setting_key`),
  KEY `idx_cfg_setting_group_code` (`group_code`),
  KEY `idx_cfg_setting_status` (`status`),
  KEY `idx_cfg_setting_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS `file_binding` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `file_id` BIGINT NOT NULL COMMENT '文件ID',
  `owner_type` VARCHAR(64) NOT NULL COMMENT '归属对象类型',
  `owner_id` BIGINT NOT NULL COMMENT '归属对象ID',
  `usage_code` VARCHAR(64) NOT NULL COMMENT '用途编码',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `creator_id` BIGINT NULL COMMENT '创建人ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_binding_file_owner_usage` (`file_id`, `owner_type`, `owner_id`, `usage_code`),
  KEY `idx_file_binding_owner` (`owner_type`, `owner_id`),
  KEY `idx_file_binding_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件绑定表';
