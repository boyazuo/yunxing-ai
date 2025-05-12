'use client'

import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import type { Model as AIModel, Provider } from '@/types/ai'
import { Loader2, Search } from 'lucide-react'

interface ModelSelectionDialogProps {
  isOpen: boolean
  onOpenChange: (open: boolean) => void
  providers: Provider[]
  tenantModels: AIModel[]
  selectedModels: string[]
  searchTerm: string
  onSearchChange: (term: string) => void
  onModelSelect: (modelId: string, checked: boolean) => void
  onAddModels: () => void
  isLoading: boolean
}

/**
 * 模型选择对话框组件
 * 用于展示和选择可用的AI模型
 */
export default function ModelSelectionDialog({
  isOpen,
  onOpenChange,
  providers,
  tenantModels,
  selectedModels,
  searchTerm,
  onSearchChange,
  onModelSelect,
  onAddModels,
  isLoading,
}: ModelSelectionDialogProps) {
  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-[80vw] h-[80vh] flex flex-col">
        <DialogHeader>
          <DialogTitle>添加模型</DialogTitle>
        </DialogHeader>

        {isLoading ? (
          <div className="flex-1 flex items-center justify-center">
            <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
            <span className="ml-2 text-sm text-muted-foreground">加载中...</span>
          </div>
        ) : providers.length === 0 ? (
          <div className="flex-1 flex items-center justify-center flex-col">
            <p className="text-muted-foreground text-sm mb-4">当前租户尚未配置模型提供商</p>
            <Button variant="outline" size="sm" onClick={() => onOpenChange(false)}>
              关闭
            </Button>
          </div>
        ) : (
          <div className="flex-1 flex flex-col min-h-0">
            {/* 搜索区域 */}
            <div className="relative mb-4">
              <Search className="absolute left-2 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索模型..."
                className="pl-8"
                value={searchTerm}
                onChange={(e) => onSearchChange(e.target.value)}
              />
            </div>

            {/* 分组模型列表 */}
            <div className="flex-1 overflow-y-auto border rounded-md">
              {/* 列表表头 */}
              <div className="sticky top-0 p-2 bg-muted/50 border-b flex items-center z-10">
                <div className="w-6" />
                <div className="flex-1 flex">
                  <div className="w-1/3 font-medium text-sm">模型名称</div>
                  <div className="w-1/3 font-medium text-sm">类型</div>
                  <div className="w-1/3 font-medium text-sm">最大Tokens</div>
                </div>
              </div>

              <div className="divide-y">
                {/* 按提供商分组展示模型 */}
                {providers.map((provider) => {
                  // 过滤属于当前提供商的模型，并应用搜索过滤
                  const providerModels = tenantModels.filter(
                    (model) =>
                      model.providerId === provider.providerId &&
                      (model.modelName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                        (model.displayName || '').toLowerCase().includes(searchTerm.toLowerCase())),
                  )

                  // 如果该提供商没有匹配的模型，不显示该分组
                  if (providerModels.length === 0) return null

                  return (
                    <div key={provider.providerId} className="group">
                      {/* 提供商分组标题 */}
                      <div className="p-2 bg-muted/20 font-medium flex items-center">
                        <div className="text-sm">{provider.providerName}</div>
                        <div className="text-xs text-muted-foreground ml-2">({providerModels.length}个模型)</div>
                      </div>

                      {/* 该提供商的模型列表 */}
                      <div className="space-y-1 p-1">
                        {providerModels.map((model) => (
                          <div key={model.modelId} className="flex items-center p-2 hover:bg-muted/30 rounded-md">
                            <div className="w-6 flex items-center justify-center">
                              <Checkbox
                                id={`model-select-${model.modelId}`}
                                checked={selectedModels.includes(model.modelId)}
                                onCheckedChange={(checked) => onModelSelect(model.modelId, !!checked)}
                              />
                            </div>
                            <label htmlFor={`model-select-${model.modelId}`} className="flex-1 flex cursor-pointer">
                              <div className="w-1/3 flex flex-col">
                                <span className="text-sm font-medium">{model.displayName || model.modelName}</span>
                                <span className="text-xs text-muted-foreground truncate max-w-[95%]">
                                  {model.modelName}
                                </span>
                              </div>
                              <div className="w-1/3 text-sm">{model.modelType || '未知'}</div>
                              <div className="w-1/3 text-sm">{model.maxTokens || '未知'}</div>
                            </label>
                          </div>
                        ))}
                      </div>
                    </div>
                  )
                })}

                {/* 如果没有匹配的模型，显示提示 */}
                {tenantModels.filter(
                  (model) =>
                    model.modelName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    (model.displayName || '').toLowerCase().includes(searchTerm.toLowerCase()),
                ).length === 0 && <div className="p-4 text-center text-muted-foreground">未找到匹配的模型</div>}
              </div>
            </div>

            <Separator className="my-4" />

            <DialogFooter>
              <div className="flex items-center justify-between w-full">
                <div className="text-sm text-muted-foreground">已选择 {selectedModels.length} 个模型</div>
                <div className="flex gap-2">
                  <Button variant="outline" onClick={() => onOpenChange(false)}>
                    取消
                  </Button>
                  <Button onClick={onAddModels} disabled={selectedModels.length === 0}>
                    添加
                  </Button>
                </div>
              </div>
            </DialogFooter>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
