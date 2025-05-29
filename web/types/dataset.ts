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
  embeddingModelId: string
  embeddingModelName: string
  status: DatasetStatus
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
  embeddingModelId: string
}

/**
 * 更新知识库请求参数
 */
export interface UpdateDatasetRequest {
  datasetId: string
  datasetName: string
  datasetDesc?: string
  embeddingModelId: string
}
