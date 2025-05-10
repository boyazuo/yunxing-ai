'use client'

import { providerService } from '@/api/provider'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import type { Model, Provider } from '@/types/ai'
import { CommonStatus } from '@/types/ai'
import { BadgeCheck, Eye, EyeOff, Loader2, Plus, Search, Settings, Zap } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useCallback, useEffect, useState } from 'react'
import { toast } from 'sonner'
import { ProviderFormDialog, type ProviderFormValues, ProviderLogo } from './_components'

export default function ModelProvidersPage() {
  const { data: session } = useSession()
  const [providers, setProviders] = useState<Provider[]>([])
  const [selectedProvider, setSelectedProvider] = useState<Provider | null>(null)
  const [showApiKey, setShowApiKey] = useState(false)
  const [isProviderDialogOpen, setIsProviderDialogOpen] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [loading, setLoading] = useState(true)
  const [providerModels, setProviderModels] = useState<Model[]>([])
  const [loadingModels, setLoadingModels] = useState(false)

  // 从会话获取租户ID
  const tenantId = session?.tenant?.tenantId || ''

  // 加载提供商列表
  const loadProviders = useCallback(async () => {
    if (!tenantId) return

    try {
      setLoading(true)
      const data = await providerService.getProviders(tenantId)
      const providers = Array.isArray(data) ? data : []
      setProviders(providers)
      if (providers.length > 0) {
        setSelectedProvider(providers[0])
        loadModelsByProvider(providers[0].providerId)
      }
    } catch (error) {
      console.error('加载提供商列表失败', error)
      toast.error('获取提供商列表失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }, [tenantId])

  // 加载提供商下的模型
  const loadModelsByProvider = async (providerId: string) => {
    try {
      setLoadingModels(true)
      const models = await providerService.getModelsByProviderId(providerId)
      setProviderModels(Array.isArray(models) ? models : [])
    } catch (error) {
      console.error('加载模型列表失败', error)
    } finally {
      setLoadingModels(false)
    }
  }

  // 首次加载
  useEffect(() => {
    if (tenantId) {
      loadProviders()
    }
  }, [tenantId, loadProviders])

  // 选择提供商
  const handleSelectProvider = (provider: Provider) => {
    setSelectedProvider(provider)
    loadModelsByProvider(provider.providerId)
  }

  // 处理提供商表单提交
  const handleProviderFormSubmit = async (values: ProviderFormValues) => {
    if (!tenantId) return

    try {
      if (selectedProvider && isProviderDialogOpen) {
        // 更新提供商
        await providerService.updateProvider(selectedProvider.providerId, {
          providerName: values.providerName,
          logo: values.logo,
          apiKey: values.apiKey,
          endpoint: values.endpoint || '',
        })
        toast.success('提供商更新成功')
      } else {
        // 创建新提供商
        await providerService.createProvider({
          tenantId,
          providerName: values.providerName,
          logo: values.logo,
          apiKey: values.apiKey,
          endpoint: values.endpoint || '',
        })
        toast.success('提供商添加成功')
      }

      setIsProviderDialogOpen(false)
      loadProviders()
    } catch (error) {
      toast.error(selectedProvider ? '提供商更新失败，请稍后重试' : '提供商添加失败，请稍后重试')
    }
  }

  // 更新模型状态
  const handleToggleModelStatus = async (model: Model, active: boolean) => {
    try {
      const newStatus = active ? CommonStatus.ACTIVE : CommonStatus.DISABLED
      await providerService.updateModelStatus(model.modelId, newStatus)

      // 更新本地状态
      setProviderModels((prev) => prev.map((m) => (m.modelId === model.modelId ? { ...m, status: newStatus } : m)))

      toast.success(`模型 ${model.displayName} ${active ? '已启用' : '已禁用'}`)
    } catch (error) {
      console.error('更新模型状态失败', error)
      toast.error('更新模型状态失败，请稍后重试')
    }
  }

  // 更新提供商状态
  const handleToggleProviderStatus = async (provider: Provider, active: boolean) => {
    try {
      const newStatus = active ? CommonStatus.ACTIVE : CommonStatus.DISABLED
      await providerService.updateProvider(provider.providerId, { status: newStatus })

      // 更新本地状态
      setProviders((prev) => prev.map((p) => (p.providerId === provider.providerId ? { ...p, status: newStatus } : p)))

      if (selectedProvider?.providerId === provider.providerId) {
        setSelectedProvider({ ...selectedProvider, status: newStatus })
      }

      toast.success(`提供商 ${provider.providerName} ${active ? '已启用' : '已禁用'}`)
    } catch (error) {
      console.error('更新提供商状态失败', error)
      toast.error('更新提供商状态失败，请稍后重试')
    }
  }

  // 过滤供应商列表
  const filteredProviders = providers.filter((provider) =>
    provider.providerName.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  // 格式化API密钥，展示部分字符（首尾字符和中间掩码）
  const formatApiKey = (apiKey: string) => {
    if (!apiKey) return '未设置'
    const length = apiKey.length
    if (length <= 8) return '********'
    return `${apiKey.substring(0, 4)}${'*'.repeat(Math.min(12, length - 8))}${apiKey.substring(length - 4)}`
  }

  return (
    <div className="flex h-full">
      {/* 左侧供应商列表 */}
      <div className="w-[280px] border-r bg-background/50 overflow-y-auto h-full">
        <div className="p-3 sticky top-0 bg-background/95 backdrop-blur-sm z-10 border-b">
          <Button
            variant="outline"
            size="sm"
            className="w-full justify-center gap-1 h-8 mb-2 shadow-sm hover:shadow transition-all text-xs"
            onClick={() => {
              setSelectedProvider(null)
              setIsProviderDialogOpen(true)
            }}
          >
            <Plus className="h-3.5 w-3.5" />
            <span>添加提供商</span>
          </Button>
          <div className="relative">
            <Input
              placeholder="搜索模型平台..."
              className="pl-8 pr-3 py-1.5 h-8 bg-background/80 transition-all focus-visible:bg-background text-xs"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground" />
          </div>
        </div>

        <div className="py-1">
          {loading ? (
            <div className="flex items-center justify-center py-6">
              <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
            </div>
          ) : filteredProviders.length === 0 ? (
            <div className="px-3 py-6 text-center text-xs text-muted-foreground">无可用提供商，请添加新提供商</div>
          ) : (
            filteredProviders.map((provider) => (
              <button
                key={provider.providerId}
                className={`flex w-full items-center justify-between px-3 py-2 cursor-pointer transition-colors hover:bg-muted text-left ${
                  selectedProvider?.providerId === provider.providerId
                    ? 'bg-primary/5 hover:bg-primary/5 border-l-2 border-primary'
                    : ''
                }`}
                onClick={() => handleSelectProvider(provider)}
                type="button"
              >
                <div className="flex items-center gap-2">
                  <ProviderLogo provider={provider.providerName} logo={provider.logo} />
                  <div>
                    <span className="text-xs font-medium">{provider.providerName}</span>
                  </div>
                </div>
                <div>
                  {provider.status === CommonStatus.ACTIVE ? (
                    <Badge variant="outline" className="bg-primary/10 text-primary border-0 text-[10px] px-1.5 py-0">
                      激活
                    </Badge>
                  ) : (
                    <Badge
                      variant="outline"
                      className="bg-destructive/10 text-destructive border-0 text-[10px] px-1.5 py-0"
                    >
                      禁用
                    </Badge>
                  )}
                </div>
              </button>
            ))
          )}
        </div>
      </div>

      {/* 右侧详情区域 */}
      <div className="flex-1 bg-background overflow-y-auto">
        {!selectedProvider ? (
          <div className="h-full flex items-center justify-center">
            <div className="text-center">
              <p className="text-sm text-muted-foreground mb-4">请选择或添加一个模型提供商</p>
              <Button variant="outline" onClick={() => setIsProviderDialogOpen(true)} className="text-xs">
                <Plus className="h-3.5 w-3.5 mr-1" />
                添加提供商
              </Button>
            </div>
          </div>
        ) : (
          <div className="max-w-3xl mx-auto p-6">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-2.5">
                <ProviderLogo provider={selectedProvider.providerName} logo={selectedProvider.logo} />
                <div>
                  <h1 className="text-base font-medium flex items-center gap-1.5">
                    {selectedProvider.providerName}
                    {selectedProvider.status === 'active' && <BadgeCheck className="h-4 w-4 text-blue-500" />}
                  </h1>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <div className="flex items-center gap-1.5">
                  <span className="text-xs font-medium">启用</span>
                  <Switch
                    checked={selectedProvider.status === CommonStatus.ACTIVE}
                    onCheckedChange={(checked) => {
                      handleToggleProviderStatus(selectedProvider, checked)
                    }}
                  />
                </div>
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        variant="outline"
                        size="sm"
                        className="h-7 w-7 p-0 rounded-full ml-1.5"
                        onClick={() => setIsProviderDialogOpen(true)}
                      >
                        <Settings className="h-3.5 w-3.5" />
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent>
                      <p className="text-xs">编辑供应商设置</p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
            </div>

            <div className="bg-card rounded-md border shadow-sm p-4 mb-5">
              <h3 className="text-sm font-medium mb-3">连接信息</h3>

              <div className="space-y-3">
                {/* API 密钥展示 */}
                <div className="flex items-start">
                  <div className="w-24 shrink-0">
                    <span className="text-xs font-medium text-muted-foreground">API 密钥</span>
                  </div>
                  <div className="flex-1 flex items-center gap-2">
                    <p className="text-xs font-mono bg-muted/30 px-2 py-1 rounded flex-1 break-all">
                      {showApiKey ? selectedProvider.apiKey : formatApiKey(selectedProvider.apiKey)}
                    </p>
                    <button
                      type="button"
                      onClick={() => setShowApiKey(!showApiKey)}
                      className="text-muted-foreground hover:text-foreground transition-colors"
                    >
                      {showApiKey ? <EyeOff className="h-3.5 w-3.5" /> : <Eye className="h-3.5 w-3.5" />}
                    </button>
                  </div>
                </div>

                {/* API 基础URL展示 */}
                <div className="flex items-start">
                  <div className="w-24 shrink-0">
                    <span className="text-xs font-medium text-muted-foreground">API 端点</span>
                  </div>
                  <div className="flex-1">
                    {selectedProvider.endpoint ? (
                      <p className="text-xs font-mono bg-muted/30 px-2 py-1 rounded break-all">
                        {selectedProvider.endpoint}
                      </p>
                    ) : (
                      <p className="text-xs text-muted-foreground italic px-2 py-1">默认端点</p>
                    )}
                  </div>
                </div>
              </div>
            </div>

            {/* 模型区域 */}
            <div>
              <div className="flex items-center justify-between mb-3">
                <div>
                  <h2 className="text-sm font-medium">可用模型</h2>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  className="flex items-center gap-1 h-7 shadow-sm hover:shadow transition-all text-xs"
                >
                  <Plus className="h-3 w-3" />
                  <span>添加模型</span>
                </Button>
              </div>

              {/* 模型列表 */}
              <div className="space-y-2.5">
                {loadingModels ? (
                  <div className="flex items-center justify-center py-8">
                    <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
                  </div>
                ) : providerModels.length === 0 ? (
                  <div className="p-6 text-center border rounded-md bg-card">
                    <p className="text-xs text-muted-foreground">该提供商下暂无可用模型</p>
                  </div>
                ) : (
                  providerModels.map((model) => (
                    <div
                      key={model.modelId}
                      className="p-3 flex items-center justify-between border rounded-md hover:border-primary/30 hover:shadow-sm transition-all bg-card"
                    >
                      <div className="flex items-center gap-2.5">
                        <ProviderLogo provider={selectedProvider.providerName} logo={selectedProvider.logo} />
                        <div>
                          <div className="flex items-center gap-1.5">
                            <span className="text-xs font-medium">{model.displayName}</span>
                            <Badge
                              className={`${
                                model.modelType === 'reasoning'
                                  ? 'bg-orange-100 text-orange-700 hover:bg-orange-200'
                                  : 'bg-blue-100 text-blue-700 hover:bg-blue-200'
                              } border-0 text-[10px] py-0 px-1.5`}
                            >
                              {model.modelType === 'reasoning' && <Zap className="h-2.5 w-2.5 mr-0.5" />}
                              {model.modelType === 'chat' ? '通用对话' : '推理增强'}
                            </Badge>
                          </div>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <Switch
                          checked={model.status === CommonStatus.ACTIVE}
                          onCheckedChange={(checked) => handleToggleModelStatus(model, checked)}
                        />
                        <TooltipProvider>
                          <Tooltip>
                            <TooltipTrigger asChild>
                              <Button variant="outline" size="sm" className="h-7 w-7 p-0 rounded-full">
                                <Settings className="h-3.5 w-3.5" />
                              </Button>
                            </TooltipTrigger>
                            <TooltipContent>
                              <p className="text-xs">模型设置</p>
                            </TooltipContent>
                          </Tooltip>
                        </TooltipProvider>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 提供商表单对话框 */}
      <ProviderFormDialog
        open={isProviderDialogOpen}
        onOpenChange={setIsProviderDialogOpen}
        initialData={selectedProvider}
        onSubmit={handleProviderFormSubmit}
        tenantId={tenantId}
      />
    </div>
  )
}
