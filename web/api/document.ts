import { api } from '@/lib/api'
import type { DatasetDocument, DocumentStatus, SegmentMethod } from '@/types/document'

/**
 * 文档服务
 */
export const documentService = {
  /**
   * 获取数据集下的文档列表
   */
  getDocuments: async (
    datasetId: string,
    page = 1,
    size = 10,
  ): Promise<{
    records: DatasetDocument[]
    total: number
    current: number
    size: number
  }> => {
    const response = await api.get<{
      records: DatasetDocument[]
      total: number
      current: number
      size: number
    }>('/dataset-documents/page', {
      params: { datasetId, current: page, size },
    })
    return response.data
  },

  /**
   * 获取文档详情
   */
  getDocument: async (documentId: string): Promise<DatasetDocument> => {
    const response = await api.get<DatasetDocument>(`/dataset-documents/${documentId}`)
    return response.data
  },

  /**
   * 创建文档
   */
  createDocument: async (data: {
    tenantId: string
    datasetId: string
    fileId: string | number
    fileName: string
    fileSize: number
    segmentMethod: SegmentMethod
    maxSegmentLength: number
    overlapLength: number
  }): Promise<DatasetDocument> => {
    const response = await api.post<DatasetDocument>('/dataset-documents', data)
    return response.data
  },

  /**
   * 更新文档状态
   */
  updateDocumentStatus: async (documentId: string, status: DocumentStatus): Promise<void> => {
    await api.put(`/dataset-documents/${documentId}/status`, { status })
  },

  /**
   * 删除文档
   */
  deleteDocument: async (documentId: string): Promise<void> => {
    await api.delete(`/dataset-documents/${documentId}`)
  },
}
