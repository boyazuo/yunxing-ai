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
 * 知识库配置类型
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
  variables?: VariableConfig[]
  datasets?: DatasetConfig[]
}
