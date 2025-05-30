import { type ApiResponse, api } from '@/lib/api'
import type { DatasetDocument, DocumentSegment, DocumentStatus, SegmentMethod } from '@/types/document'

/**
 * 文档服务
 */
export const documentService = {
  /**
   * 获取知识库下的文档列表
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
  }): Promise<ApiResponse<DatasetDocument>> => {
    const response = await api.post<DatasetDocument>('/dataset-documents', data)
    return response
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

/**
 * 文档分段服务
 */
export const segmentService = {
  /**
   * 分页获取文档分段
   */
  getSegments: async (
    documentId: string,
    page = 1,
    size = 10,
    keyword?: string,
  ): Promise<{
    records: DocumentSegment[]
    total: number
    current: number
    size: number
  }> => {
    const params: Record<string, string | number> = { documentId, current: page, size }
    if (keyword?.trim()) {
      params.keyword = keyword.trim()
    }

    const response = await api.get<{
      records: DocumentSegment[]
      total: number
      current: number
      size: number
    }>('/document-segments/page', { params })
    return response.data
  },

  /**
   * 获取分段详情
   */
  getSegment: async (segmentId: string): Promise<DocumentSegment> => {
    const response = await api.get<DocumentSegment>(`/document-segments/${segmentId}`)
    return response.data
  },

  /**
   * 更新分段内容
   */
  updateSegment: async (segmentId: string, data: { title?: string; content: string }): Promise<void> => {
    await api.put(`/document-segments/${segmentId}`, data)
  },

  /**
   * 删除分段
   */
  deleteSegment: async (segmentId: string): Promise<void> => {
    await api.delete(`/document-segments/${segmentId}`)
  },

  /**
   * 批量删除分段
   */
  batchDeleteSegments: async (segmentIds: (string | number)[]): Promise<void> => {
    await api.delete('/document-segments/batch', { data: { segmentIds } })
  },
}
