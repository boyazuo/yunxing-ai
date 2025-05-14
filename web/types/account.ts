/**
 * 租户计划类型
 */
export enum TenantPlan {
  FREE = 'free', // 免费版
  BASIC = 'basic', // 基础版
  PRO = 'pro', // 专业版
}

/**
 * 租户状态
 */
export enum TenantStatus {
  ACTIVE = 'active', // 活跃
  CLOSED = 'closed', // 关闭
}

/**
 * 租户角色
 */
export enum TenantUserRole {
  OWNER = 'owner', // 所有者
  ADMIN = 'admin', // 管理员
  NORMAL = 'normal', // 普通成员
}

/**
 * 用户状态
 */
export enum UserStatus {
  PENDING = 'pending', // 待处理
  UNINITIALIZED = 'uninitialized', // 未初始化
  ACTIVE = 'active', // 活跃
  BANNED = 'banned', // 已禁止
  CLOSED = 'closed', // 已关闭
}

/**
 * 租户类型
 */
export interface Tenant {
  tenantId?: number
  tenantName: string
  plan?: TenantPlan
  status?: TenantStatus
  role?: TenantUserRole
  isActive?: boolean
  memberCount?: number
  createTime?: string
  updateTime?: string
}

/**
 * 用户类型
 */
export interface User {
  userId?: string
  username?: string
  email?: string
  avatar?: string
}
