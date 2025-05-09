SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app` (
  `app_id` bigint(20) NOT NULL COMMENT '应用ID',
  `tenant_id` varchar(255) DEFAULT NULL COMMENT '所属租户ID',
  `app_name` varchar(255) DEFAULT NULL COMMENT '应用名称',
  `intro` varchar(2000) DEFAULT NULL COMMENT '应用介绍',
  `logo` varchar(255) DEFAULT NULL COMMENT '应用Logo',
  `logo_background` varchar(255) DEFAULT NULL COMMENT 'Logo背景色',
  `type` varchar(20) DEFAULT NULL COMMENT '应用类型',
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用表';

-- ----------------------------
-- Table structure for model
-- ----------------------------
DROP TABLE IF EXISTS `model`;
CREATE TABLE `model` (
  `model_id` bigint(20) NOT NULL COMMENT '模型 ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '所属租户ID',
  `provider_id` bigint(20) DEFAULT NULL COMMENT '提供商ID',
  `model_name` varchar(255) DEFAULT NULL COMMENT '模型名称',
  `display_name` varchar(255) DEFAULT NULL COMMENT '显示名称',
  `model_type` varchar(20) DEFAULT NULL COMMENT '模型类型(枚举：chat:对话模型 reason:推理模型)',
  `context_length` int(11) DEFAULT NULL COMMENT '上下文长度',
  `max_tokens` int(11) DEFAULT NULL COMMENT '最大输出token',
  `input_price` decimal(10,2) DEFAULT NULL COMMENT '输入价格',
  `output_price` decimal(10,2) DEFAULT NULL COMMENT '输出价格',
  `status` varchar(20) DEFAULT NULL COMMENT '状态(枚举：active:激活 disabled:禁用)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for provider
-- ----------------------------
DROP TABLE IF EXISTS `provider`;
CREATE TABLE `provider` (
  `provider_id` bigint(20) NOT NULL COMMENT '提供商ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '所属租户ID',
  `provider_name` varchar(255) DEFAULT NULL COMMENT '提供商名称',
  `logo` varchar(255) DEFAULT NULL COMMENT 'Logo',
  `api_key` varchar(255) DEFAULT NULL COMMENT 'API密钥',
  `endpoint` varchar(255) DEFAULT NULL COMMENT '终端地址',
  `last_used_time` datetime DEFAULT NULL COMMENT '最后使用时间',
  `status` varchar(20) DEFAULT NULL COMMENT '状态(枚举：active:激活 disabled:禁用)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tenant
-- ----------------------------
DROP TABLE IF EXISTS `tenant`;
CREATE TABLE `tenant` (
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `tenant_name` varchar(255) DEFAULT NULL COMMENT '租户名称',
  `plan` varchar(255) DEFAULT NULL COMMENT '订阅套餐',
  `status` varchar(20) DEFAULT NULL COMMENT '状态（active: 活跃 closed:已关闭）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表（工作空间）';

-- ----------------------------
-- Table structure for tenant_user
-- ----------------------------
DROP TABLE IF EXISTS `tenant_user`;
CREATE TABLE `tenant_user` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role` varchar(20) DEFAULT NULL COMMENT '角色(owner: 所有者  admin: 管理员 normal: 普通成员)',
  `is_active` tinyint(1) DEFAULT '0' COMMENT '是否活跃租户',
  `inviter_id` bigint(20) DEFAULT NULL COMMENT '邀请人ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户成员表';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `email` varchar(255) NOT NULL COMMENT '邮箱',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `status` varchar(20) DEFAULT NULL COMMENT '状态(pending:待处理 uninitialized: 未初始化 active: 活跃 banned:已禁止 closed:已关闭)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

SET FOREIGN_KEY_CHECKS = 1;
