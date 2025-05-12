import { Button } from '@/components/ui/button'
import { Switch } from '@/components/ui/switch'
import type { DatasetConfig as DatasetConfigType } from '@/types/appConfig'
import { Database, Plus, Trash2 } from 'lucide-react'
import { useEffect, useState } from 'react'

interface DatasetConfigProps {
  appId: string
  datasets?: DatasetConfigType[]
  onChange?: (datasets: DatasetConfigType[]) => void
}

interface Dataset extends DatasetConfigType {
  id: string
}

export default function DatasetConfig({ appId, datasets = [], onChange }: DatasetConfigProps) {
  const [datasetList, setDatasetList] = useState<Dataset[]>([])

  // 这里应该从API获取可用的数据集列表
  const [availableDatasets, setAvailableDatasets] = useState([
    { id: 'ds-1', name: '公司产品知识库' },
    { id: 'ds-2', name: '行业知识库' },
    { id: 'ds-3', name: '客户FAQ知识库' },
  ])

  // 初始化和同步外部传入的知识库配置
  useEffect(() => {
    if (datasets.length > 0) {
      setDatasetList(
        datasets.map((ds) => ({
          ...ds,
          id: ds.id || `ds-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
        })),
      )
    }
  }, [datasets])

  // 将内部状态转换为外部知识库配置并通知父组件
  const updateParent = (newDatasets: Dataset[]) => {
    setDatasetList(newDatasets)

    if (onChange) {
      onChange(newDatasets)
    }
  }

  // 添加数据集
  const addDataset = (datasetId: string, name: string) => {
    // 检查是否已经添加过
    if (datasetList.some((ds) => ds.datasetId === datasetId)) {
      return
    }

    const newDataset = {
      id: `ds-${Date.now()}`,
      datasetId,
      name,
      isActive: true,
    }

    updateParent([...datasetList, newDataset])
  }

  // 删除数据集
  const removeDataset = (id: string) => {
    updateParent(datasetList.filter((ds) => ds.id !== id))
  }

  // 切换知识库启用状态
  const toggleDatasetActive = (id: string) => {
    updateParent(datasetList.map((ds) => (ds.id === id ? { ...ds, isActive: !ds.isActive } : ds)))
  }

  // 获取未添加的知识库
  const getUnavailableDatasets = () => {
    return availableDatasets.filter((ds) => !datasetList.some((existingDs) => existingDs.datasetId === ds.id))
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col space-y-3">
        {datasetList.length > 0 ? (
          datasetList.map((ds) => (
            <div key={ds.id} className={`border rounded-md p-3 ${!ds.isActive ? 'opacity-60' : ''}`}>
              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <Database className="h-4 w-4 text-muted-foreground mr-2" />
                  <span className="text-sm font-medium">{ds.name}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Switch
                    id={`ds-active-${ds.id}`}
                    checked={ds.isActive}
                    onCheckedChange={() => toggleDatasetActive(ds.id)}
                  />
                  <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => removeDataset(ds.id)}>
                    <Trash2 className="h-4 w-4 text-muted-foreground" />
                  </Button>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="py-4 text-center text-muted-foreground">
            <p className="text-sm">暂未关联知识库</p>
          </div>
        )}
      </div>

      {/* 添加知识库部分 */}
      {getUnavailableDatasets().length > 0 && (
        <div className="mt-4 pt-4 border-t">
          <h4 className="text-sm font-medium mb-2">添加数据集</h4>
          <div className="space-y-2">
            {getUnavailableDatasets().map((ds) => (
              <div key={ds.id} className="flex items-center justify-between p-2 border rounded-md">
                <div className="flex items-center">
                  <Database className="h-4 w-4 text-muted-foreground mr-2" />
                  <span className="text-sm">{ds.name}</span>
                </div>
                <Button variant="ghost" size="sm" className="h-7 gap-1" onClick={() => addDataset(ds.id, ds.name)}>
                  <Plus className="h-3.5 w-3.5" />
                  添加
                </Button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
