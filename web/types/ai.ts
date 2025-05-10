/**
 * 模型类型枚举
 */

export enum ModelType {
  CHAT = 'chat', // 聊天模型
  REASON = 'reason', // 推理模型
  EMBEDDING = 'embedding', // 向量模型
  AUDIO = 'audio', // 语音模型
  IMAGE = 'image', // 图像模型
  VIDEO = 'video', // 视频模型
  CODE = 'code', // 代码模型
  RERANK = 'rerank', // 重排序模型
}

/**
 * 通用状态枚举
 */
export enum CommonStatus {
  ACTIVE = 'active', // 激活
  DISABLED = 'disabled', // 禁用
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
