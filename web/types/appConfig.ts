/**
 * 模型配置类型
 */
export interface ModelConfig {
  id?: string
  modelId: string
  name: string
  provider: string
  temperature: number
  topP: number
  maxTokens: number
  isActive: boolean
}

/**
 * 变量配置类型
 */
export interface VariableConfig {
  id?: string
  key: string
  name: string
  description: string
  required: boolean
}

/**
 * 数据集配置类型
 */
export interface DatasetConfig {
  id?: string
  datasetId: string
  name: string
  isActive: boolean
}

/**
 * 应用配置类型 - 前端使用
 */
export interface AppConfig {
  configId?: string
  appId: string
  tenantId: string
  sysPrompt?: string
  models: ModelConfig[]
  variables?: VariableConfig[]
  datasets?: DatasetConfig[]
}
