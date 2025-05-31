'use client'

import { Button } from '@/components/ui/button'
import { Switch } from '@/components/ui/switch'
import type { DatasetConfig as DatasetConfigType } from '@/types/appConfig'
import type { Dataset } from '@/types/dataset'
import { Database, Plus, Trash2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import DatasetSelectionDialog from './DatasetSelectionDialog'

interface DatasetConfigProps {
  appId: string
  datasets?: DatasetConfigType[]
  onChange?: (datasets: DatasetConfigType[]) => void
}

export default function DatasetConfig({ appId, datasets = [], onChange }: DatasetConfigProps) {
  const [datasetList, setDatasetList] = useState<DatasetConfigType[]>([])
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  // 这里应该从API获取可用的知识库列表
  const [availableDatasets, setAvailableDatasets] = useState([
    { id: 'ds-1', name: '公司产品知识库' },
    { id: 'ds-2', name: '行业知识库' },
    { id: 'ds-3', name: '客户FAQ知识库' },
  ])

  // 初始化和同步外部传入的知识库配置
  useEffect(() => {
    setDatasetList(datasets)
  }, [datasets])

  // 将内部状态转换为外部知识库配置并通知父组件
  const updateParent = (newDatasets: DatasetConfigType[]) => {
    setDatasetList(newDatasets)
    if (onChange) {
      onChange(newDatasets)
    }
  }

  // 添加知识库
  const handleAddDatasets = (selectedDatasets: Dataset[]) => {
    const newDatasets = selectedDatasets.map((dataset) => ({
      id: `ds-${Date.now()}-${dataset.datasetId}`,
      datasetId: dataset.datasetId,
      name: dataset.datasetName,
      isActive: true,
    }))

    updateParent([...datasetList, ...newDatasets])
    toast.success(`成功添加 ${selectedDatasets.length} 个知识库`)
  }

  // 删除知识库
  const removeDataset = (id: string) => {
    const dataset = datasetList.find((ds) => ds.id === id)
    updateParent(datasetList.filter((ds) => ds.id !== id))
    if (dataset) {
      toast.success(`已移除知识库 "${dataset.name}"`)
    }
  }

  // 切换知识库启用状态
  const toggleDatasetActive = (id: string) => {
    updateParent(datasetList.map((ds) => (ds.id === id ? { ...ds, isActive: !ds.isActive } : ds)))
  }

  // 获取已添加的知识库ID列表
  const selectedDatasetIds = datasetList.map((ds) => ds.datasetId)

  return (
    <div className="space-y-4">
      {/* 添加知识库按钮 */}
      {datasetList.length > 0 && (
        <div className="flex justify-between items-center">
          <h4 className="text-sm font-medium text-muted-foreground">已添加的知识库</h4>
          <Button variant="outline" size="sm" className="h-8 gap-1" onClick={() => setIsDialogOpen(true)}>
            <Plus className="h-3.5 w-3.5" />
            添加知识库
          </Button>
        </div>
      )}

      {/* 已添加的知识库列表 */}
      <div className="space-y-3">
        {datasetList.length > 0 ? (
          datasetList.map((ds) => (
            <div
              key={ds.id}
              className={`border rounded-lg p-4 transition-all ${
                !ds.isActive ? 'opacity-60 bg-muted/30' : 'bg-background'
              }`}
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center flex-1 min-w-0">
                  <Database className="h-4 w-4 text-muted-foreground mr-3 shrink-0" />
                  <div className="flex-1 min-w-0">
                    <h5 className="text-sm font-medium truncate">{ds.name}</h5>
                    <p className="text-xs text-muted-foreground mt-1">ID: {ds.datasetId}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3 ml-4">
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-muted-foreground">启用</span>
                    <Switch
                      id={`ds-active-${ds.id}`}
                      checked={ds.isActive}
                      onCheckedChange={() => toggleDatasetActive(ds.id || '')}
                    />
                  </div>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8 text-muted-foreground hover:text-destructive"
                    onClick={() => removeDataset(ds.id || '')}
                    title="移除知识库"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="border-2 border-dashed border-muted rounded-lg p-8 text-center">
            <Database className="h-12 w-12 text-muted-foreground mx-auto mb-3" />
            <h4 className="text-sm font-medium text-muted-foreground mb-2">暂未添加知识库</h4>
            <p className="text-xs text-muted-foreground mb-4">添加知识库可以为AI应用提供专业领域的知识支持</p>
            <Button variant="outline" size="sm" className="gap-1" onClick={() => setIsDialogOpen(true)}>
              <Plus className="h-3.5 w-3.5" />
              添加第一个知识库
            </Button>
          </div>
        )}
      </div>

      {/* 知识库选择对话框 */}
      <DatasetSelectionDialog
        isOpen={isDialogOpen}
        onOpenChange={setIsDialogOpen}
        selectedDatasetIds={selectedDatasetIds}
        onConfirm={handleAddDatasets}
      />
    </div>
  )
}
