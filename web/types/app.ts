/**
 * 应用类型枚举
 */
export enum AppType {
  CHAT = 'chat', // 对话应用
  AGENT = 'agent', // 智能体应用
  WORKFLOW = 'workflow', // 工作流应用
}

/**
 * 应用状态枚举
 */
export enum AppStatus {
  DRAFT = 'draft', // 草稿
  PUBLISHED = 'published', // 已发布
  DISABLED = 'disabled', // 已禁用
  DELETED = 'deleted', // 已删除
}

/**
 * 应用实体类型
 */
export interface App {
  appId: string
  appName: string
  intro?: string
  logo?: string
  logoBackground?: string
  type: AppType
  status: AppStatus
  tenantId: string
  createTime?: string
  creatorId?: number
  creatorUsername?: string
  creatorAvatar?: string
  updateTime?: string
  updatorId?: number
}
