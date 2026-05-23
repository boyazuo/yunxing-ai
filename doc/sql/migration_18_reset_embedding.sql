-- 嵌入模型切换迁移脚本：zhipuai:embedding-3(dim=2048) → ollama:qwen3-embedding:4b(dim=2560)
-- 执行前请备份数据库，并在 Qdrant 中手动删除所有 tenant_*_dataset_* 集合
-- 执行后重启后端服务，文档处理服务会自动重新向量化所有文档

-- 1. 清空知识库的向量模型标识（触发模型兼容性检测）
UPDATE `dataset` SET `embedding_model` = '' WHERE `deleted` = 0;

-- 2. 清空文档分段的向量 ID（确保重处理时不引用旧向量）
UPDATE `dataset_document_segment` SET `vector_id` = NULL WHERE `deleted` = 0;

-- 3. 将已完成的文档重置为待处理状态（触发重新向量化）
UPDATE `dataset_document`
SET `status` = 'waiting'
WHERE `deleted` = 0 AND `status` = 'completed';
