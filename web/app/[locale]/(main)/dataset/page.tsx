'use client'

import { datasetService } from '@/api/dataset'
import { DatasetFormDialog } from '@/app/[locale]/(main)/dataset/_components/DatasetFormDialog'
import { ConfirmDialog } from '@/components/blocks/confirm-dialog'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import type { Dataset } from '@/types/dataset'
import { DatasetStatus } from '@/types/dataset'
import { Database, Edit, MoreHorizontal, Plus, Trash, User } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useRouter } from 'next/navigation'
import { useCallback, useEffect, useState } from 'react'

export default function DatasetsPage() {
  const router = useRouter()
  const { data: session, status } = useSession()
  const [datasets, setDatasets] = useState<Dataset[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [currentDataset, setCurrentDataset] = useState<Dataset | undefined>(undefined)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [datasetToDelete, setDatasetToDelete] = useState<Dataset | null>(null)

  // 从会话中获取租户ID
  const tenantId = session?.tenant?.tenantId || ''

  // 加载知识库列表
  const loadDatasets = useCallback(async () => {
    try {
      if (!tenantId) {
        console.error('租户ID不存在')
        setIsLoading(false)
        return
      }

      setIsLoading(true)
      const data = await datasetService.getDatasets(tenantId.toString())
      setDatasets(data || [])
    } catch (error) {
      console.error('加载知识库失败', error)
    } finally {
      setIsLoading(false)
    }
  }, [tenantId])

  // 首次加载
  useEffect(() => {
    if (status === 'authenticated' && tenantId) {
      loadDatasets()
    }
  }, [status, tenantId, loadDatasets])

  // 打开创建知识库表单
  const handleCreateDataset = () => {
    setCurrentDataset(undefined)
    setIsFormOpen(true)
  }

  // 打开编辑知识库表单
  const handleEditDataset = (dataset: Dataset) => {
    setCurrentDataset(dataset)
    setIsFormOpen(true)
  }

  // 打开删除确认对话框
  const confirmDeleteDataset = (dataset: Dataset) => {
    // 使用setTimeout确保React完成当前渲染循环
    setTimeout(() => {
      setDatasetToDelete(dataset)
      setDeleteDialogOpen(true)
    }, 0)
  }

  // 删除知识库
  const handleDeleteDataset = async () => {
    try {
      if (!datasetToDelete) return

      await datasetService.deleteDataset(datasetToDelete.datasetId)
      setDeleteDialogOpen(false)
      setDatasetToDelete(null)
      loadDatasets() // 重新加载知识库列表
    } catch (error) {
      console.error('删除知识库失败', error)
    }
  }

  // 格式化更新时间
  const formatUpdateTime = (updateTime?: string) => {
    if (!updateTime) return '未知'

    try {
      const date = new Date(updateTime)
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hour = date.getHours().toString().padStart(2, '0')
      const minute = date.getMinutes().toString().padStart(2, '0')

      return `${month}-${day} ${hour}:${minute}`
    } catch (error) {
      return '未知'
    }
  }

  // 过滤知识库
  const filteredDatasets = datasets.filter((dataset) => {
    // 搜索过滤
    const searchMatch = searchTerm ? dataset.datasetName.toLowerCase().includes(searchTerm.toLowerCase()) : true

    // 状态过滤
    const statusMatch = statusFilter === 'all' ? true : dataset.status === statusFilter

    return searchMatch && statusMatch
  })

  // 跳转到知识库文档列表页面
  const navigateToDatasetDetail = (datasetId: string) => {
    router.push(`/dataset/${datasetId}`)
  }

  return (
    <div className="container mx-auto py-6 space-y-6">
      {/* 顶部筛选和搜索区 */}
      <div className="flex justify-between items-center">
        <div className="flex items-center space-x-4">
          <Select defaultValue="all" onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="筛选状态" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部知识库</SelectItem>
              <SelectItem value={DatasetStatus.ACTIVE}>正常</SelectItem>
              <SelectItem value={DatasetStatus.DISABLED}>已禁用</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="flex items-center space-x-2">
          <Input
            placeholder="搜索知识库..."
            className="w-[250px]"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <Button onClick={handleCreateDataset}>
            <Plus className="mr-2 h-4 w-4" />
            新建知识库
          </Button>
        </div>
      </div>

      {/* 知识库列表 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
        {isLoading ? (
          // 加载状态
          [...Array(8)].map((_, index) => (
            <Card key={`skeleton-${index}-${Date.now()}`} className="min-h-[120px] animate-pulse bg-muted p-0">
              <div className="h-full" />
            </Card>
          ))
        ) : filteredDatasets.length > 0 ? (
          // 知识库列表
          filteredDatasets.map((dataset) => (
            <Card
              key={dataset.datasetId}
              className="cursor-pointer hover:shadow-md transition-shadow overflow-hidden border-muted p-0 gap-3"
              onClick={() => navigateToDatasetDetail(dataset.datasetId)}
            >
              {/* 卡片头部 */}
              <div className="px-4 pt-4 pb-0 flex items-start justify-between">
                <div className="flex space-x-2.5">
                  <div className="flex-shrink-0">
                    <div
                      className="h-10 w-10 rounded-md flex items-center justify-center text-xl shadow-sm border"
                      style={{ backgroundColor: '#f0f0f0' }}
                    >
                      <Database className="h-5 w-5" />
                    </div>
                  </div>
                  <div>
                    <h3 className="font-medium text-sm leading-tight mb-1">{dataset.datasetName}</h3>
                    <Badge
                      variant={dataset.status === DatasetStatus.ACTIVE ? 'secondary' : 'outline'}
                      className="text-[10px] px-1.5 py-0 h-5 gap-1 font-normal"
                    >
                      <Database className="h-3 w-3" />
                      <span>
                        {dataset.status === DatasetStatus.ACTIVE
                          ? '正常'
                          : dataset.status === DatasetStatus.DISABLED
                            ? '已禁用'
                            : '未知状态'}
                      </span>
                    </Badge>
                  </div>
                </div>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-6 w-6 -mt-0.5 -mr-0.5"
                      onClick={(e) => e.stopPropagation()} // 防止点击触发卡片的点击事件
                    >
                      <MoreHorizontal className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem
                      onClick={(e) => {
                        e.stopPropagation() // 防止点击触发卡片的点击事件
                        handleEditDataset(dataset)
                      }}
                    >
                      <Edit className="mr-2 h-4 w-4" />
                      <span>编辑信息</span>
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      className="text-destructive"
                      onClick={(e) => {
                        e.stopPropagation() // 防止点击触发卡片的点击事件
                        confirmDeleteDataset(dataset)
                      }}
                    >
                      <Trash className="mr-2 h-4 w-4" />
                      <span>删除知识库</span>
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>

              {/* 卡片内容 */}
              <div className="px-4 py-0 min-h-[60px]">
                <p className="text-[12px] text-muted-foreground line-clamp-2">{dataset.datasetDesc || '暂无描述'}</p>
              </div>

              {/* 卡片底部 */}
              <div className="mt-auto px-3 py-1.5 flex items-center justify-between border-t border-muted/70 bg-muted/5 text-[11px]">
                <div className="flex items-center space-x-1.5">
                  <Avatar className="h-4 w-4">
                    {dataset.creatorAvatar ? (
                      dataset.creatorAvatar.startsWith('http') ? (
                        <AvatarImage src={dataset.creatorAvatar} alt={dataset.creatorUsername} />
                      ) : (
                        <AvatarFallback className="text-[9px]">{dataset.creatorAvatar}</AvatarFallback>
                      )
                    ) : (
                      <AvatarFallback>
                        {dataset.creatorUsername ? (
                          dataset.creatorUsername.slice(0, 1).toUpperCase()
                        ) : (
                          <User className="h-2 w-2" />
                        )}
                      </AvatarFallback>
                    )}
                  </Avatar>
                  <span className="text-[12px] text-muted-foreground">{dataset.creatorUsername || '创建者'}</span>
                </div>
                <span className="text-[12px] text-muted-foreground">更新于 {formatUpdateTime(dataset.updateTime)}</span>
              </div>
            </Card>
          ))
        ) : (
          // 无数据状态
          <div className="col-span-full flex flex-col items-center justify-center py-12 text-muted-foreground">
            <p>暂无知识库数据</p>
            <Button variant="outline" className="mt-2" onClick={handleCreateDataset}>
              <Plus className="mr-2 h-4 w-4" />
              创建第一个知识库
            </Button>
          </div>
        )}
      </div>

      {/* 知识库表单弹窗 */}
      <DatasetFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        dataset={currentDataset}
        onSuccess={loadDatasets}
        tenantId={tenantId}
      />

      {/* 删除确认对话框 */}

      <ConfirmDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        title="确认删除知识库"
        description={`您确定要删除知识库 "${datasetToDelete?.datasetName}" 吗？此操作不可撤销。`}
        onConfirm={handleDeleteDataset}
      />
    </div>
  )
}
