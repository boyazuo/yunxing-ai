'use client'

import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import { Bot, ChevronDown, ChevronUp, Plus, Trash2 } from 'lucide-react'
import { useState } from 'react'

interface ModelConfigProps {
  appId: string
}

// 可用的模型列表
const AVAILABLE_MODELS = [
  { id: 'gpt-4', name: 'GPT-4', provider: 'OpenAI' },
  { id: 'gpt-4o', name: 'GPT-4o', provider: 'OpenAI' },
  { id: 'gpt-4o-mini', name: 'GPT-4o-mini', provider: 'OpenAI' },
  { id: 'gpt-3.5-turbo', name: 'GPT-3.5 Turbo', provider: 'OpenAI' },
  { id: 'claude-3-opus', name: 'Claude 3 Opus', provider: 'Anthropic' },
  { id: 'claude-3-sonnet', name: 'Claude 3 Sonnet', provider: 'Anthropic' },
  { id: 'gemini-pro', name: 'Gemini Pro', provider: 'Google' },
]

interface Model {
  id: string
  modelId: string
  name: string
  provider: string
  temperature: number
  topP: number
  maxTokens: number
  isExpanded: boolean
  isActive: boolean
}

export default function ModelConfig({ appId }: ModelConfigProps) {
  // 已选择的模型列表
  const [models, setModels] = useState<Model[]>([
    {
      id: 'model-1',
      modelId: 'gpt-4',
      name: 'GPT-4',
      provider: 'OpenAI',
      temperature: 0.7,
      topP: 1.0,
      maxTokens: 4000,
      isExpanded: true,
      isActive: true,
    },
  ])

  // 模型选择对话框状态
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  // 当前选中准备添加的模型
  const [selectedModels, setSelectedModels] = useState<string[]>([])

  // 切换模型展开/折叠状态
  const toggleModelExpanded = (id: string) => {
    setModels(models.map((model) => (model.id === id ? { ...model, isExpanded: !model.isExpanded } : model)))
  }

  // 切换模型启用状态
  const toggleModelActive = (id: string) => {
    // 确保至少有一个模型是激活的
    const activeModelsCount = models.filter((m) => m.id !== id && m.isActive).length

    if (models.find((m) => m.id === id)?.isActive && activeModelsCount === 0) {
      return // 不允许禁用最后一个激活的模型
    }

    setModels(models.map((model) => (model.id === id ? { ...model, isActive: !model.isActive } : model)))
  }

  // 删除模型
  const removeModel = (id: string) => {
    // 确保至少保留一个模型
    if (models.length <= 1) return

    // 如果删除的是激活的模型，且只有一个激活的模型，则不允许删除
    const targetModel = models.find((m) => m.id === id)
    if (targetModel?.isActive && models.filter((m) => m.isActive).length <= 1) {
      return
    }

    setModels(models.filter((model) => model.id !== id))
  }

  // 添加选中的模型
  const addSelectedModels = () => {
    const newModels: Model[] = []

    for (const modelId of selectedModels) {
      const modelInfo = AVAILABLE_MODELS.find((m) => m.id === modelId)
      if (modelInfo) {
        newModels.push({
          id: `model-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
          modelId,
          name: modelInfo.name,
          provider: modelInfo.provider,
          temperature: 0.7,
          topP: 1.0,
          maxTokens: 4000,
          isExpanded: false,
          isActive: true,
        })
      }
    }

    setModels([...models, ...newModels])
    setSelectedModels([])
    setIsDialogOpen(false)
  }

  // 更新模型参数
  const updateModelParam = (id: string, param: keyof Model, value: number | string | boolean) => {
    setModels(models.map((model) => (model.id === id ? { ...model, [param]: value } : model)))
  }

  return (
    <div className="space-y-4">
      {/* 添加模型按钮 */}
      <div className="flex justify-end">
        <Button
          variant="outline"
          size="sm"
          className="flex items-center gap-1 text-primary"
          onClick={() => setIsDialogOpen(true)}
        >
          <Plus className="h-4 w-4" />
          添加模型
        </Button>
      </div>

      {/* 模型列表 */}
      <div className="space-y-2">
        {models.map((model) => (
          <div key={model.id} className={`border rounded-md overflow-hidden ${!model.isActive ? 'opacity-60' : ''}`}>
            {/* 模型标题栏 */}
            <div className="flex items-center p-3 bg-muted/30">
              <RadioGroup className="flex items-center mr-2" onValueChange={() => toggleModelActive(model.id)}>
                <RadioGroupItem value={model.id} id={`model-active-${model.id}`} checked={model.isActive} />
              </RadioGroup>

              <Bot className="h-4 w-4 text-muted-foreground mr-2" />

              <div className="flex-1">
                <span className="font-medium text-sm">{model.name}</span>
                <span className="text-xs text-muted-foreground ml-2">({model.provider})</span>
              </div>

              <div className="flex items-center gap-1">
                <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => removeModel(model.id)}>
                  <Trash2 className="h-4 w-4 text-muted-foreground" />
                </Button>

                <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => toggleModelExpanded(model.id)}>
                  {model.isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                </Button>
              </div>
            </div>

            {/* 模型详细设置 */}
            {model.isExpanded && (
              <div className="p-3 border-t space-y-4">
                {/* 温度设置 */}
                <div className="space-y-1">
                  <div className="flex justify-between">
                    <label htmlFor={`temp-${model.id}`} className="text-xs font-medium">
                      温度 (Temperature)
                    </label>
                    <span className="text-xs">{model.temperature}</span>
                  </div>
                  <Input
                    id={`temp-${model.id}`}
                    type="range"
                    min="0"
                    max="1"
                    step="0.1"
                    value={model.temperature}
                    onChange={(e) => updateModelParam(model.id, 'temperature', Number.parseFloat(e.target.value))}
                    className="h-7"
                    disabled={!model.isActive}
                  />
                </div>

                {/* Top P 设置 */}
                <div className="space-y-1">
                  <div className="flex justify-between">
                    <label htmlFor={`topP-${model.id}`} className="text-xs font-medium">
                      Top P
                    </label>
                    <span className="text-xs">{model.topP.toFixed(2)}</span>
                  </div>
                  <Input
                    id={`topP-${model.id}`}
                    type="range"
                    min="0"
                    max="1"
                    step="0.01"
                    value={model.topP}
                    onChange={(e) => updateModelParam(model.id, 'topP', Number.parseFloat(e.target.value))}
                    className="h-7"
                    disabled={!model.isActive}
                  />
                </div>

                {/* 最大Token设置 */}
                <div className="space-y-1">
                  <label htmlFor={`tokens-${model.id}`} className="text-xs font-medium block">
                    最大Token数
                  </label>
                  <Input
                    id={`tokens-${model.id}`}
                    type="number"
                    className="h-8"
                    placeholder="4000"
                    value={model.maxTokens}
                    onChange={(e) => updateModelParam(model.id, 'maxTokens', Number.parseInt(e.target.value) || 0)}
                    disabled={!model.isActive}
                  />
                </div>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* 模型选择对话框 */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>添加AI模型</DialogTitle>
          </DialogHeader>

          <div className="py-4 space-y-3">
            {AVAILABLE_MODELS.map((model) => (
              <div key={model.id} className="flex items-center space-x-2">
                <Checkbox
                  id={`select-model-${model.id}`}
                  checked={selectedModels.includes(model.id)}
                  onCheckedChange={(checked: boolean | 'indeterminate') => {
                    if (checked === true) {
                      setSelectedModels([...selectedModels, model.id])
                    } else if (checked === false) {
                      setSelectedModels(selectedModels.filter((id) => id !== model.id))
                    }
                  }}
                />
                <label
                  htmlFor={`select-model-${model.id}`}
                  className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                >
                  {model.name} <span className="text-xs text-muted-foreground">({model.provider})</span>
                </label>
              </div>
            ))}
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDialogOpen(false)}>
              取消
            </Button>
            <Button onClick={addSelectedModels} disabled={selectedModels.length === 0} className="ml-2">
              确认添加
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
