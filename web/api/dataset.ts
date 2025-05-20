import { api } from '@/lib/api'
import type { CreateDatasetRequest, Dataset, UpdateDatasetRequest } from '@/types/dataset'

/**
 * 数据集服务
 */
export const datasetService = {
  /**
   * 获取租户下的数据集列表
   */
  getDatasets: async (tenantId: string): Promise<Dataset[]> => {
    const response = await api.get<Dataset[]>(`/datasets`, {
      params: { tenantId },
    })
    return response.data
  },

  /**
   * 获取数据集详情
   */
  getDataset: async (datasetId: string): Promise<Dataset> => {
    const response = await api.get<Dataset>(`/datasets/${datasetId}`)
    return response.data
  },

  /**
   * 创建数据集
   */
  createDataset: async (data: CreateDatasetRequest): Promise<Dataset> => {
    const response = await api.post<Dataset>(`/datasets`, data)
    return response.data
  },

  /**
   * 更新数据集
   */
  updateDataset: async (data: UpdateDatasetRequest): Promise<Dataset> => {
    const response = await api.put<Dataset>(`/datasets/${data.datasetId}`, data)
    return response.data
  },

  /**
   * 删除数据集
   */
  deleteDataset: async (datasetId: string): Promise<void> => {
    await api.delete(`/datasets/${datasetId}`)
  },
} 