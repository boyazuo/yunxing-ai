SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app` (
  `app_id` bigint(20) NOT NULL COMMENT '应用ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '所属租户ID',
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
-- Table structure for app_config
-- ----------------------------
DROP TABLE IF EXISTS `app_config`;
CREATE TABLE `app_config` (
  `config_id` bigint(20) NOT NULL COMMENT 'ID',
  `app_id` bigint(20) DEFAULT NULL COMMENT '应用ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '所属租户ID',
  `sys_prompt` text COMMENT '系统提示词',
  `models` text COMMENT 'AI模型配置',
  `variables` text COMMENT '变量配置',
  `datasets` text COMMENT '知识库配置',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用配置表';

-- ----------------------------
-- Table structure for conversation
-- ----------------------------
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `app_id` bigint(20) DEFAULT NULL COMMENT '应用 ID',
  `title` varchar(1000) DEFAULT NULL COMMENT '会话标题',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- ----------------------------
-- Table structure for dataset
-- ----------------------------
DROP TABLE IF EXISTS `dataset`;
CREATE TABLE `dataset` (
  `dataset_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '所属租户ID',
  `dataset_name` varchar(255) DEFAULT NULL COMMENT '知识库名称',
  `dataset_desc` varchar(1000) DEFAULT NULL COMMENT '知识库描述',
  `embedding_model_id` bigint(20) DEFAULT NULL COMMENT '嵌入模型 ID',
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dataset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- ----------------------------
-- Table structure for dataset_document
-- ----------------------------
DROP TABLE IF EXISTS `dataset_document`;
CREATE TABLE `dataset_document` (
  `document_id` bigint(20) NOT NULL COMMENT '文档ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '所属租户ID',
  `dataset_id` bigint(20) DEFAULT NULL COMMENT '知识库ID',
  `file_id` bigint(20) DEFAULT NULL COMMENT '文件 ID',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名称',
  `file_size` int(11) DEFAULT NULL COMMENT '文件大小',
  `segment_method` varchar(20) DEFAULT NULL COMMENT '分段方式',
  `max_segment_length` int(11) DEFAULT NULL COMMENT '分段最大长度',
  `overlap_length` int(11) DEFAULT NULL COMMENT '重叠长度',
  `segment_num` int(11) DEFAULT NULL COMMENT '文档分段数',
  `status` varchar(20) DEFAULT NULL COMMENT '状态(pending-待处理，processing-处理中，completed-处理完成，failed-处理失败)',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档表';

-- ----------------------------
-- Table structure for dataset_document_segment
-- ----------------------------
DROP TABLE IF EXISTS `dataset_document_segment`;
CREATE TABLE `dataset_document_segment` (
  `segment_id` bigint(20) NOT NULL COMMENT '分段ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '所属租户ID',
  `dataset_id` bigint(20) DEFAULT NULL COMMENT '知识库ID',
  `document_id` bigint(20) DEFAULT NULL COMMENT '文档 ID',
  `position` int(11) DEFAULT NULL COMMENT '位置',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `content_length` int(11) DEFAULT NULL COMMENT '内容长度',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`segment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分段表';

-- ----------------------------
-- Table structure for invitation
-- ----------------------------
DROP TABLE IF EXISTS `invitation`;
CREATE TABLE `invitation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inviter_tenant_id` bigint(20) NOT NULL COMMENT '邀请租户ID',
  `inviter_user_id` bigint(20) NOT NULL COMMENT '邀请人ID',
  `invitee_email` varchar(100) NOT NULL COMMENT '被邀请人邮箱',
  `invitee_role` varchar(50) NOT NULL COMMENT '被邀请人角色',
  `invite_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '邀请时间',
  `status` varchar(20) NOT NULL DEFAULT '0' COMMENT '邀请状态',
  `token` varchar(64) NOT NULL COMMENT '邀请令牌',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `accept_time` datetime DEFAULT NULL COMMENT '接受时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`)
) ENGINE=InnoDB AUTO_INCREMENT=1924769803498348546 DEFAULT CHARSET=utf8mb4 COMMENT='邀请记录表';

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `message_id` bigint(20) NOT NULL COMMENT '消息ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `app_id` bigint(20) DEFAULT NULL COMMENT '应用 ID',
  `conversation_id` bigint(20) DEFAULT NULL COMMENT '会话 ID',
  `question` text COMMENT '问题',
  `answer` text COMMENT '回复',
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型表';

-- ----------------------------
-- Table structure for provider
-- ----------------------------
DROP TABLE IF EXISTS `provider`;
CREATE TABLE `provider` (
  `provider_id` bigint(20) NOT NULL COMMENT '提供商ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '所属租户ID',
  `provider_name` varchar(255) DEFAULT NULL COMMENT '提供商名称',
  `display_name` varchar(255) DEFAULT NULL COMMENT '显示名称',
  `logo` varchar(255) DEFAULT NULL COMMENT 'Logo',
  `api_key` varchar(255) DEFAULT NULL COMMENT 'API密钥',
  `endpoint` varchar(255) DEFAULT NULL COMMENT '终端地址',
  `last_used_time` datetime DEFAULT NULL COMMENT '最后使用时间',
  `status` varchar(20) DEFAULT NULL COMMENT '状态(枚举：active:激活 disabled:禁用)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型提供商表';

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `file_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '附件编号',
  `origin_name` varchar(200) DEFAULT NULL COMMENT '原始文件名称',
  `file_name` varchar(200) DEFAULT NULL COMMENT '文件名称',
  `url` varchar(500) DEFAULT NULL COMMENT '文件URL',
  `path` varchar(500) DEFAULT NULL COMMENT '文件路径',
  `hash` varchar(100) DEFAULT NULL COMMENT '文件hash值',
  `content_type` varchar(100) DEFAULT NULL COMMENT 'ContentType',
  `size` bigint(20) DEFAULT NULL COMMENT '文件大小',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `status` int(11) DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`file_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COMMENT='附件表';

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
  `avatar_id` int(11) DEFAULT NULL COMMENT '头像关联的附件表ID',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

SET FOREIGN_KEY_CHECKS = 1;
