/**
 * 数据集状态枚举
 */
export enum DatasetStatus {
  ACTIVE = 'active',
  DISABLED = 'disabled',
  DELETED = 'deleted',
}

/**
 * 数据集类型
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
 * 创建数据集请求参数
 */
export interface CreateDatasetRequest {
  tenantId: string
  datasetName: string
  datasetDesc?: string
  embeddingModelId: string
}

/**
 * 更新数据集请求参数
 */
export interface UpdateDatasetRequest {
  datasetId: string
  datasetName: string
  datasetDesc?: string
  embeddingModelId: string
} 