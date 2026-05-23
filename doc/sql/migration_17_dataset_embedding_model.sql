-- 知识库记录向量模型标识，用于检测模型更换后的数据漂移
-- 执行前请备份数据库

ALTER TABLE `dataset`
  ADD COLUMN `embedding_model` varchar(100) NOT NULL DEFAULT '' COMMENT '向量化使用的模型标识，格式 provider:model' AFTER `status`;
