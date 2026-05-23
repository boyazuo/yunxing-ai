-- Parent-Child 分块策略：文档表新增父块大小字段
ALTER TABLE `dataset_document`
    ADD COLUMN `parent_chunk_size` INT NOT NULL DEFAULT 1200
        COMMENT 'Parent 块最大长度（仅 parent_child 策略使用）'
        AFTER `overlap_length`;

-- 文档分段表新增父子块关联字段
ALTER TABLE `dataset_document_segment`
    ADD COLUMN `segment_type` TINYINT NOT NULL DEFAULT 0
        COMMENT '分段类型：0=普通（legacy），1=父块，2=子块'
        AFTER `position`,
    ADD COLUMN `parent_segment_id` BIGINT DEFAULT NULL
        COMMENT '父块 segment_id，仅 segment_type=2 时有值'
        AFTER `segment_type`,
    ADD INDEX `idx_parent_segment_id` (`parent_segment_id`);
