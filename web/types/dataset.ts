/**
 * 知识库状态枚举
 */
export enum DatasetStatus {
  ACTIVE = 'active',
  DISABLED = 'disabled',
  DELETED = 'deleted',
}

/**
 * 知识库类型
 */
export interface Dataset {
  datasetId: string
  tenantId: string
  datasetName: string
  datasetDesc: string
  status: DatasetStatus
  /** 向量化使用的模型标识，格式 provider:model */
  embeddingModel?: string
  /** 向量模型是否与当前系统配置一致，null 表示未记录 */
  embeddingModelMatched?: boolean | null
  creatorId: string
  creatorUsername: string
  creatorAvatar: string
  createTime: string
  updatorId: string
  updatorUsername: string
  updateTime: string
}

/**
 * 创建知识库请求参数
 */
export interface CreateDatasetRequest {
  tenantId: string
  datasetName: string
  datasetDesc?: string
}

/**
 * 更新知识库请求参数
 */
export interface UpdateDatasetRequest {
  datasetId: string
  datasetName: string
  datasetDesc?: string
}
