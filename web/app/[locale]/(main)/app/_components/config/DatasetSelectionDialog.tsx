'use client'

import { datasetService } from '@/api/dataset'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import type { Dataset } from '@/types/dataset'
import { Database, Loader2, Search } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'

interface DatasetSelectionDialogProps {
  isOpen: boolean
  onOpenChange: (open: boolean) => void
  selectedDatasetIds: string[]
  onConfirm: (selectedDatasets: Dataset[]) => void
}

export default function DatasetSelectionDialog({
  isOpen,
  onOpenChange,
  selectedDatasetIds,
  onConfirm,
}: DatasetSelectionDialogProps) {
  const { data: session } = useSession()
  const tenantId = session?.tenant?.tenantId || ''

  const [datasets, setDatasets] = useState<Dataset[]>([])
  const [loading, setLoading] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedIds, setSelectedIds] = useState<string[]>([])

  // 加载知识库列表
  useEffect(() => {
    if (isOpen && tenantId) {
      loadDatasets()
    }
  }, [isOpen, tenantId])

  // 重置选中状态
  useEffect(() => {
    if (isOpen) {
      setSelectedIds([])
      setSearchTerm('')
    }
  }, [isOpen])

  const loadDatasets = async () => {
    try {
      setLoading(true)
      const data = await datasetService.getDatasets(tenantId)
      // 过滤掉已经添加的知识库
      const availableDatasets = data.filter(
        (dataset) => !selectedDatasetIds.includes(dataset.datasetId) && dataset.status === 'active',
      )
      setDatasets(availableDatasets)
    } catch (error) {
      console.error('加载知识库列表失败:', error)
      toast.error('加载知识库列表失败')
    } finally {
      setLoading(false)
    }
  }

  // 过滤知识库
  const filteredDatasets = datasets.filter(
    (dataset) =>
      dataset.datasetName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      dataset.datasetDesc.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  // 处理知识库选择
  const handleDatasetSelect = (datasetId: string, checked: boolean) => {
    if (checked) {
      setSelectedIds([...selectedIds, datasetId])
    } else {
      setSelectedIds(selectedIds.filter((id) => id !== datasetId))
    }
  }

  // 确认添加
  const handleConfirm = () => {
    const selectedDatasets = datasets.filter((dataset) => selectedIds.includes(dataset.datasetId))
    onConfirm(selectedDatasets)
    onOpenChange(false)
  }

  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] max-h-[80vh] flex flex-col">
        <DialogHeader>
          <DialogTitle>选择知识库</DialogTitle>
        </DialogHeader>

        {/* 搜索框 */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="搜索知识库..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>

        {/* 知识库列表 */}
        <div className="flex-1 min-h-0 overflow-y-auto">
          {loading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin mr-2" />
              <span>加载中...</span>
            </div>
          ) : filteredDatasets.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-8 text-muted-foreground">
              <Database className="h-12 w-12 mb-2" />
              <p className="text-sm">{searchTerm ? '未找到匹配的知识库' : '暂无可用的知识库'}</p>
            </div>
          ) : (
            <div className="space-y-2">
              {filteredDatasets.map((dataset) => (
                <div
                  key={dataset.datasetId}
                  className="flex items-start space-x-3 p-3 border rounded-lg hover:bg-muted/50 transition-colors"
                >
                  <Checkbox
                    id={`dataset-${dataset.datasetId}`}
                    checked={selectedIds.includes(dataset.datasetId)}
                    onCheckedChange={(checked) => handleDatasetSelect(dataset.datasetId, checked as boolean)}
                    className="mt-1"
                  />
                  <div className="flex-1 min-w-0">
                    <label
                      htmlFor={`dataset-${dataset.datasetId}`}
                      className="block text-sm font-medium cursor-pointer"
                    >
                      {dataset.datasetName}
                    </label>
                    {dataset.datasetDesc && (
                      <p className="text-xs text-muted-foreground mt-1 line-clamp-2">{dataset.datasetDesc}</p>
                    )}
                    <div className="flex items-center mt-2 text-xs text-muted-foreground">
                      <span>嵌入模型: {dataset.embeddingModelName}</span>
                      <span className="mx-2">•</span>
                      <span>创建者: {dataset.creatorUsername}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            取消
          </Button>
          <Button onClick={handleConfirm} disabled={selectedIds.length === 0}>
            添加 {selectedIds.length > 0 && `(${selectedIds.length})`}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
