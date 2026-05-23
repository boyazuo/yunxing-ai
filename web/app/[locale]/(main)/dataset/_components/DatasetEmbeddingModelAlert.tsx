import { Badge } from '@/components/ui/badge'
import type { Dataset } from '@/types/dataset'
import { AlertTriangle } from 'lucide-react'

interface DatasetEmbeddingModelAlertProps {
  dataset: Pick<Dataset, 'embeddingModel' | 'embeddingModelMatched'>
}

export function DatasetEmbeddingModelAlert({ dataset }: DatasetEmbeddingModelAlertProps) {
  if (dataset.embeddingModelMatched === false) {
    return (
      <div className="flex items-start gap-2 rounded-md border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive">
        <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
        <div>
          <p className="font-medium">向量模型不一致</p>
          <p className="mt-1 text-destructive/90">
            该知识库使用 <span className="font-mono">{dataset.embeddingModel}</span> 向量化，与当前系统配置不一致。向量检索已禁用，请重新上传并向量化所有文档。
          </p>
        </div>
      </div>
    )
  }

  if (dataset.embeddingModelMatched == null && !dataset.embeddingModel) {
    return (
      <div className="flex items-start gap-2 rounded-md border border-amber-500/30 bg-amber-500/5 px-4 py-3 text-sm text-amber-700 dark:text-amber-400">
        <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
        <div>
          <p className="font-medium">未记录向量模型</p>
          <p className="mt-1 opacity-90">该知识库尚未记录向量化模型信息。建议重新处理文档以确保检索准确性。</p>
        </div>
      </div>
    )
  }

  return null
}

export function DatasetEmbeddingModelBadge({ dataset }: DatasetEmbeddingModelAlertProps) {
  if (dataset.embeddingModelMatched === false) {
    return (
      <Badge variant="destructive" className="text-[10px] px-1.5 py-0 h-5 gap-1 font-normal">
        <AlertTriangle className="h-3 w-3" />
        <span>模型不一致</span>
      </Badge>
    )
  }

  if (dataset.embeddingModelMatched == null && !dataset.embeddingModel) {
    return (
      <Badge variant="outline" className="text-[10px] px-1.5 py-0 h-5 gap-1 font-normal border-amber-500/50 text-amber-700 dark:text-amber-400">
        <AlertTriangle className="h-3 w-3" />
        <span>模型未记录</span>
      </Badge>
    )
  }

  return null
}
