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
  `type` varchar(20) DEFAULT NULL COMMENT '应用类型',
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用表';

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
  `is_active` tinyint(1) DEFAULT NULL COMMENT '是否活跃租户',
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
