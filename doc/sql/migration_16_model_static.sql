-- 模型配置静态化改造迁移脚本
-- 执行前请备份数据库

-- 1. 删除 provider 表
DROP TABLE IF EXISTS `provider`;

-- 2. 删除 model 表
DROP TABLE IF EXISTS `model`;

-- 3. dataset 表移除 embedding_model_id 字段
ALTER TABLE `dataset` DROP COLUMN `embedding_model_id`;
