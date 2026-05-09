-- V14: 为已有表补齐逻辑删除和审计字段
-- 这些表对应的实体均继承 BaseEntity，需要 deleted/creator_id/updater_id 列

ALTER TABLE `msg_announcement`
    ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

ALTER TABLE `org_membership`
    ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

ALTER TABLE `file_asset`
    ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    ADD COLUMN `creator_id` BIGINT NULL COMMENT '创建人ID',
    ADD COLUMN `updater_id` BIGINT NULL COMMENT '更新人ID';
