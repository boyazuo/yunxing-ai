'use client'

import { modelService } from '@/api/model'
import { providerService } from '@/api/provider'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import type { Model as AIModel, Provider } from '@/types/ai'
import type { ModelConfig as ModelConfigType } from '@/types/appConfig'
import { Bot, ChevronDown, ChevronUp, Plus, Trash2 } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { toast } from 'sonner'
import ModelSelectionDialog from './ModelSelectionDialog'

interface ModelConfigProps {
  appId: string
  models?: ModelConfigType[]
  onChange?: (models: ModelConfigType[]) => void
}

interface Model extends ModelConfigType {
  id: string
  isExpanded: boolean
}

// 模型参数设置组件
const ModelParameterControl = ({
  id,
  label,
  value,
  min,
  max,
  step,
  isActive,
  onChange,
}: {
  id: string
  label: string
  value: number
  min: string
  max: string
  step: string
  isActive: boolean
  onChange: (value: number) => void
}) => (
  <div className="space-y-1">
    <div className="flex justify-between">
      <label htmlFor={`${id}`} className="text-xs font-medium">
        {label}
      </label>
      <span className="text-xs">{typeof value === 'number' && value % 1 !== 0 ? value.toFixed(2) : value}</span>
    </div>
    <Input
      id={`${id}`}
      type="range"
      min={min}
      max={max}
      step={step}
      value={value}
      onChange={(e) => onChange(Number.parseFloat(e.target.value))}
      className="h-7"
      disabled={!isActive}
    />
  </div>
)

// 单个模型卡片组件
const ModelCard = ({
  model,
  onToggleActive,
  onToggleExpanded,
  onRemove,
  onUpdateParam,
}: {
  model: Model
  onToggleActive: () => void
  onToggleExpanded: () => void
  onRemove: () => void
  onUpdateParam: (param: keyof Model, value: number | string | boolean) => void
}) => (
  <div className={`border rounded-md overflow-hidden ${!model.isActive ? 'opacity-60' : ''}`}>
    {/* 模型标题栏 */}
    <div className="flex items-center p-3 bg-muted/30">
      <RadioGroup className="flex items-center mr-2" onValueChange={onToggleActive}>
        <RadioGroupItem value={model.id} id={`model-active-${model.id}`} checked={model.isActive} />
      </RadioGroup>

      <Bot className="h-4 w-4 text-muted-foreground mr-2" />

      <div className="flex-1">
        <span className="font-medium text-sm">{model.name}</span>
        <span className="text-xs text-muted-foreground ml-2">({model.provider})</span>
      </div>

      <div className="flex items-center gap-1">
        <Button variant="ghost" size="icon" className="h-7 w-7" onClick={onRemove}>
          <Trash2 className="h-4 w-4 text-muted-foreground" />
        </Button>

        <Button variant="ghost" size="icon" className="h-7 w-7" onClick={onToggleExpanded}>
          {model.isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
        </Button>
      </div>
    </div>

    {/* 模型详细设置 */}
    {model.isExpanded && (
      <div className="p-3 border-t space-y-4">
        {/* 温度设置 */}
        <ModelParameterControl
          id={`temp-${model.id}`}
          label="温度 (Temperature)"
          value={model.temperature}
          min="0"
          max="1"
          step="0.1"
          isActive={model.isActive}
          onChange={(value) => onUpdateParam('temperature', value)}
        />

        {/* Top P 设置 */}
        <ModelParameterControl
          id={`topP-${model.id}`}
          label="Top P"
          value={model.topP}
          min="0"
          max="1"
          step="0.01"
          isActive={model.isActive}
          onChange={(value) => onUpdateParam('topP', value)}
        />

        {/* 最大 Tokens 设置 */}
        <ModelParameterControl
          id={`maxTokens-${model.id}`}
          label="最大 Tokens"
          value={model.maxTokens}
          min="100"
          max="8000"
          step="100"
          isActive={model.isActive}
          onChange={(value) => onUpdateParam('maxTokens', value)}
        />
      </div>
    )}
  </div>
)

// 主组件
export default function ModelConfig({ appId, models = [], onChange }: ModelConfigProps) {
  const { data: session } = useSession()
  const tenantId = session?.tenant?.tenantId || ''

  // 已选择的模型列表，包含UI状态
  const [modelList, setModelList] = useState<Model[]>([])
  // 使用 ref 来存储当前的 modelList，避免依赖循环
  const modelListRef = useRef<Model[]>([])

  // 租户的提供商和模型列表
  const [providers, setProviders] = useState<Provider[]>([])
  const [tenantModels, setTenantModels] = useState<AIModel[]>([])
  const [loadingProviders, setLoadingProviders] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')

  // 模型选择对话框状态
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [selectedModels, setSelectedModels] = useState<string[]>([])

  // 初始化和同步外部传入的模型配置
  useEffect(() => {
    if (!models) return

    if (models.length === 0) {
      setModelList([])
      return
    }

    // 使用 ref 中存储的最新值
    const currentModelList = modelListRef.current

    // 记录当前模型的展开状态
    const expandedState = new Map(currentModelList.map((model) => [model.modelId, model.isExpanded]))

    // 将外部模型配置转换为内部状态，保留已有模型的展开状态
    const newModelList = models.map((model) => ({
      ...model,
      id: model.id || `model-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      // 保留已有模型的展开状态
      isExpanded: expandedState.get(model.modelId) || false,
    }))

    setModelList(newModelList)
  }, [models])

  // 同步 modelList 到 ref
  useEffect(() => {
    modelListRef.current = modelList
  }, [modelList])

  // 加载指定提供商下的模型
  const loadModelsByProvider = useCallback(async (providerId: string) => {
    if (!providerId) return []

    try {
      const models = await modelService.getModelsByProviderId(providerId)
      return Array.isArray(models) ? models : []
    } catch (error) {
      console.error('加载模型列表失败', error)
      toast.error('模型列表加载失败，请稍后重试')
      return []
    }
  }, [])

  // 从租户获取提供商列表
  const loadProviders = useCallback(async () => {
    if (!tenantId) return

    try {
      setLoadingProviders(true)
      const data = await providerService.getProviders(tenantId)
      const providers = Array.isArray(data) ? data : []
      setProviders(providers)

      if (providers.length > 0) {
        const firstProviderId = providers[0].providerId
        // 加载所有提供商的模型
        const allModels = await loadAllProviderModels(providers)
        setTenantModels(allModels)
      } else {
        setTenantModels([])
      }
    } catch (error) {
      console.error('加载提供商列表失败', error)
      toast.error('提供商列表加载失败，请稍后重试')
      setProviders([])
      setTenantModels([])
    } finally {
      setLoadingProviders(false)
    }
  }, [tenantId])

  // 加载所有提供商的模型
  const loadAllProviderModels = useCallback(
    async (providersList: Provider[] = []) => {
      if (!tenantId) return []

      const providersToUse = providersList.length > 0 ? providersList : providers

      if (providersToUse.length === 0) return []

      try {
        setLoadingProviders(true)
        const allModels = []
        for (const provider of providersToUse) {
          const models = await loadModelsByProvider(provider.providerId)
          // 为每个模型添加提供商信息
          const modelsWithProvider = models.map((model) => ({
            ...model,
            providerName: provider.providerName,
            providerId: provider.providerId,
          }))
          allModels.push(...modelsWithProvider)
        }
        return allModels
      } catch (error) {
        console.error('加载所有模型失败', error)
        toast.error('模型数据加载失败，请稍后重试')
        return []
      } finally {
        setLoadingProviders(false)
      }
    },
    [tenantId, providers, loadModelsByProvider],
  )

  // 首次加载时获取租户提供商和模型
  useEffect(() => {
    if (tenantId) {
      loadProviders()
    }
  }, [tenantId, loadProviders])

  // 将内部状态转换为外部模型配置并通知父组件
  const updateParent = useCallback(
    (newModels: Model[]) => {
      if (onChange) {
        const configModels: ModelConfigType[] = newModels.map(({ isExpanded, ...model }) => model)
        onChange(configModels)
      }
    },
    [onChange],
  )

  // 切换模型展开/折叠状态
  const toggleModelExpanded = useCallback((id: string) => {
    setModelList((prevList) =>
      prevList.map((model) => (model.id === id ? { ...model, isExpanded: !model.isExpanded } : model)),
    )
  }, [])

  // 切换模型启用状态
  const toggleModelActive = useCallback(
    (id: string) => {
      setModelList((prevList) => {
        // 确保至少有一个模型是激活的
        const activeModelsCount = prevList.filter((m) => m.id !== id && m.isActive).length

        if (prevList.find((m) => m.id === id)?.isActive && activeModelsCount === 0) {
          toast.error('必须保留至少一个激活的模型')
          return prevList // 不允许禁用最后一个激活的模型
        }

        const newModels = prevList.map((model) => (model.id === id ? { ...model, isActive: !model.isActive } : model))
        updateParent(newModels)
        return newModels
      })
    },
    [updateParent],
  )

  // 删除模型
  const removeModel = useCallback(
    (id: string) => {
      setModelList((prevList) => {
        // 确保至少保留一个模型
        if (prevList.length <= 1) {
          toast.error('必须保留至少一个模型')
          return prevList
        }

        // 如果删除的是激活的模型，且只有一个激活的模型，则不允许删除
        const targetModel = prevList.find((m) => m.id === id)
        if (targetModel?.isActive && prevList.filter((m) => m.isActive).length <= 1) {
          toast.error('必须保留至少一个激活的模型')
          return prevList
        }

        const newModels = prevList.filter((model) => model.id !== id)
        updateParent(newModels)
        return newModels
      })
    },
    [updateParent],
  )

  // 更新模型参数
  const updateModelParam = useCallback(
    (id: string, param: keyof Model, value: number | string | boolean) => {
      setModelList((prevList) => {
        const updatedList = prevList.map((model) => (model.id === id ? { ...model, [param]: value } : model))
        updateParent(updatedList)
        return updatedList
      })
    },
    [updateParent],
  )

  // 处理模型选择变更
  const handleModelSelection = useCallback((modelId: string, checked: boolean) => {
    setSelectedModels((prev) => (checked ? [...prev, modelId] : prev.filter((id) => id !== modelId)))
  }, [])

  // 添加选中的模型
  const addSelectedModels = useCallback(() => {
    const newModels: Model[] = []

    for (const modelId of selectedModels) {
      const modelInfo = tenantModels.find((m) => m.modelId === modelId)
      if (modelInfo) {
        newModels.push({
          id: `model-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
          modelId,
          name: modelInfo.displayName || modelInfo.modelName,
          provider: modelInfo.providerName || '',
          temperature: 0.7,
          topP: 1.0,
          maxTokens: modelInfo.maxTokens || 4000,
          isExpanded: false,
          isActive: true,
        })
      }
    }

    setModelList((prevList) => {
      const updatedList = [...prevList, ...newModels]
      updateParent(updatedList)
      return updatedList
    })

    setSelectedModels([])
    setIsDialogOpen(false)
  }, [selectedModels, tenantModels, updateParent])

  // 处理打开模型选择对话框
  const handleOpenModelDialog = useCallback(() => {
    setIsDialogOpen(true)
    setSelectedModels([])

    // 只有当没有提供商数据或上次加载失败时才重新加载
    if (providers.length === 0) {
      loadProviders()
    } else if (tenantModels.length === 0) {
      // 如果已有提供商但没有模型数据，加载所有模型
      loadAllProviderModels()
    }
  }, [providers.length, loadProviders, tenantModels.length, loadAllProviderModels])

  // 使用 useMemo 过滤模型列表，避免不必要的重新计算
  const filteredModels = useMemo(
    () =>
      tenantModels.filter(
        (model) =>
          model.modelName.toLowerCase().includes(searchTerm.toLowerCase()) ||
          (model.displayName || '').toLowerCase().includes(searchTerm.toLowerCase()),
      ),
    [tenantModels, searchTerm],
  )

  return (
    <div className="space-y-4">
      {/* 添加模型按钮 */}
      <div className="flex justify-end">
        <Button
          variant="outline"
          size="sm"
          className="flex items-center gap-1 text-primary"
          onClick={handleOpenModelDialog}
        >
          <Plus className="h-4 w-4" />
          添加模型
        </Button>
      </div>

      {/* 模型列表 */}
      <div className="space-y-2">
        {modelList.length === 0 && (
          <div className="p-6 text-center text-muted-foreground border rounded-md">
            暂未添加模型，请点击「添加模型」按钮
          </div>
        )}

        {modelList.map((model) => (
          <ModelCard
            key={model.id}
            model={model}
            onToggleActive={() => toggleModelActive(model.id)}
            onToggleExpanded={() => toggleModelExpanded(model.id)}
            onRemove={() => removeModel(model.id)}
            onUpdateParam={(param, value) => updateModelParam(model.id, param, value)}
          />
        ))}
      </div>

      {/* 模型选择对话框 */}
      <ModelSelectionDialog
        isOpen={isDialogOpen}
        onOpenChange={setIsDialogOpen}
        providers={providers}
        tenantModels={tenantModels}
        selectedModels={selectedModels}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        onModelSelect={handleModelSelection}
        onAddModels={addSelectedModels}
        isLoading={loadingProviders}
      />
    </div>
  )
}
