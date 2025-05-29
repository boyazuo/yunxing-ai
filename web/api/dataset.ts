import { api } from '@/lib/api'
import type { CreateDatasetRequest, Dataset, UpdateDatasetRequest } from '@/types/dataset'

/**
 * 知识库服务
 */
export const datasetService = {
  /**
   * 获取租户下的知识库列表
   */
  getDatasets: async (tenantId: string): Promise<Dataset[]> => {
    const response = await api.get<Dataset[]>(`/datasets`, {
      params: { tenantId },
    })
    return response.data
  },

  /**
   * 获取知识库详情
   */
  getDataset: async (datasetId: string): Promise<Dataset> => {
    const response = await api.get<Dataset>(`/datasets/${datasetId}`)
    return response.data
  },

  /**
   * 创建知识库
   */
  createDataset: async (data: CreateDatasetRequest): Promise<Dataset> => {
    const response = await api.post<Dataset>(`/datasets`, data)
    return response.data
  },

  /**
   * 更新知识库
   */
  updateDataset: async (data: UpdateDatasetRequest): Promise<Dataset> => {
    const response = await api.put<Dataset>(`/datasets/${data.datasetId}`, data)
    return response.data
  },

  /**
   * 删除知识库
   */
  deleteDataset: async (datasetId: string): Promise<void> => {
    await api.delete(`/datasets/${datasetId}`)
  },
} 