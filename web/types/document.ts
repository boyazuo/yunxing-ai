/**
 * 文档状态枚举
 */
export enum DocumentStatus {
  PENDING = 'pending', // 待处理
  PROCESSING = 'processing', // 处理中
  COMPLETED = 'completed', // 已完成
  FAILED = 'failed', // 失败
  DELETED = 'deleted', // 已删除
}

/**
 * 文档分段方式枚举
 */
export enum SegmentMethod {
  SENTENCE = 'sentence', // 按句子分段
  PARAGRAPH = 'paragraph', // 按段落分段
  CUSTOM = 'custom', // 自定义分段
}

/**
 * 文档类型
 */
export interface DatasetDocument {
  documentId: string | number
  tenantId: string | number
  datasetId: string | number
  datasetName?: string
  fileId: string | number
  fileName: string
  fileSize: number
  fileHash?: string
  segmentMethod: SegmentMethod
  maxSegmentLength: number
  overlapLength: number
  segmentNum: number
  status: DocumentStatus
  statusText?: string
  creatorId: string | number
  creatorUsername?: string
  creatorAvatar?: string
  createTime: string
  updatorId: string | number
  updatorUsername?: string
  updateTime: string
  url?: string // 文件链接
}

/**
 * 文档分段类型
 */
export interface DocumentSegment {
  segmentId: string | number
  tenantId: string | number
  datasetId: string | number
  documentId: string | number
  position: number
  title?: string
  content: string
  contentLength: number
  creatorId: string | number
  creatorUsername?: string
  creatorAvatar?: string
  createTime: string
  updatorId: string | number
  updatorUsername?: string
  updateTime: string
  documentName?: string
  datasetName?: string
}

/**
 * 获取文档状态文本
 */
export function getDocumentStatusText(status?: DocumentStatus): string {
  if (!status) return '未知状态'

  switch (status) {
    case DocumentStatus.PENDING:
      return '待处理'
    case DocumentStatus.PROCESSING:
      return '处理中'
    case DocumentStatus.COMPLETED:
      return '已完成'
    case DocumentStatus.FAILED:
      return '处理失败'
    case DocumentStatus.DELETED:
      return '已删除'
    default:
      return '未知状态'
  }
}

/**
 * 获取文档状态对应的颜色
 */
export function getDocumentStatusColor(status?: DocumentStatus): string {
  if (!status) return 'gray'

  switch (status) {
    case DocumentStatus.PENDING:
      return 'yellow'
    case DocumentStatus.PROCESSING:
      return 'blue'
    case DocumentStatus.COMPLETED:
      return 'green'
    case DocumentStatus.FAILED:
      return 'red'
    case DocumentStatus.DELETED:
      return 'gray'
    default:
      return 'gray'
  }
}

/**
 * 获取分段方式文本
 */
export function getSegmentMethodText(method?: SegmentMethod): string {
  if (!method) return '未知方式'

  switch (method) {
    case SegmentMethod.SENTENCE:
      return '按句子分段'
    case SegmentMethod.PARAGRAPH:
      return '按段落分段'
    case SegmentMethod.CUSTOM:
      return '自定义分段'
    default:
      return '未知方式'
  }
}
