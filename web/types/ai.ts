/**
 * 模型类型枚举
 */
export enum ModelType {
  CHAT = 'chat', // 聊天模型
  TEXT = 'text', // 文本生成
  EMBEDDING = 'embedding', // 向量嵌入
  IMAGE = 'image', // 图像生成
  REASONING = 'reasoning', // 推理增强
}

/**
 * 通用状态枚举
 */
export enum CommonStatus {
  ACTIVE = 'active', // 活跃
  INACTIVE = 'inactive', // 非活跃
}

/**
 * 模型信息
 */
export interface Model {
  modelId: string
  providerId: string
  modelName: string
  displayName: string
  modelType: ModelType
  contextLength: number
  maxTokens: number
  inputPrice: number
  outputPrice: number
  status: CommonStatus
  providerName?: string
  providerLogo?: string
  isSelected?: boolean
  hasSpecialSettings?: boolean
}

/**
 * 提供商信息
 */
export interface Provider {
  providerId: string
  tenantId: string
  providerName: string
  logo: string
  apiKey: string
  endpoint: string
  lastUsedTime: string
  status: CommonStatus
  createTime: string
  updateTime: string
  models?: Model[]
}
