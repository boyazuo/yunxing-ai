'use client'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import { Switch } from '@/components/ui/switch'
import { ChevronDown, ChevronUp, Info, Plus, Settings } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import * as z from 'zod'

// 模型供应商表单验证
const providerFormSchema = z.object({
  name: z.string().min(2, {
    message: '名称至少需要 2 个字符',
  }),
  apiKey: z.string().min(1, {
    message: 'API 密钥不能为空',
  }),
  baseUrl: z
    .string()
    .url({
      message: '请输入有效的 URL',
    })
    .optional()
    .or(z.literal('')),
  isDefault: z.boolean().default(false),
})

// 模型供应商类型
type Provider = {
  id: string
  name: string
  logoComponent: React.ReactNode
  models: Model[]
  totalModels?: number
  apiKey: string
  baseUrl?: string
  quota?: { value: number; unit: string }
  isActive: boolean
}

type Model = {
  id: string
  name: string
  type: string
  context: string
  isActive: boolean
}

// OpenAI Logo 组件
const OpenAILogo = () => (
  <svg
    className="w-full h-full"
    width="24"
    height="24"
    viewBox="0 0 24 24"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-label="OpenAI Logo"
  >
    <title>OpenAI Logo</title>
    <path
      d="M12 1.5C6.201 1.5 1.5 6.201 1.5 12C1.5 17.799 6.201 22.5 12 22.5C17.799 22.5 22.5 17.799 22.5 12C22.5 6.201 17.799 1.5 12 1.5ZM16.254 16.345C15.618 16.712 13.803 17.565 12.015 17.565C9.451 17.565 7.278 16.115 7.278 12.285C7.278 9.792 8.919 7.374 12.015 7.374C13.704 7.374 15.408 8.208 16.209 8.718C16.344 8.802 16.516 8.685 16.516 8.532L16.611 6.591C16.617 6.51 16.587 6.438 16.533 6.387C15.684 5.598 13.878 4.95 11.901 4.95C7.536 4.95 4.398 8.253 4.398 12.477C4.398 16.836 7.392 19.98 11.901 19.98C13.968 19.98 15.9 19.143 16.731 18.486C16.812 18.426 16.848 18.33 16.836 18.225L16.701 16.488C16.68 16.377 16.461 16.253 16.254 16.345Z"
      fill="currentColor"
    />
  </svg>
)

// Anthropic Logo 组件
const AnthropicLogo = () => (
  <svg
    className="w-full h-full"
    width="24"
    height="24"
    viewBox="0 0 24 24"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-label="Anthropic Logo"
  >
    <title>Anthropic Logo</title>
    <path
      d="M10.8 3.6L4.8 15.6H16.8L10.8 3.6ZM13.2 3.6L19.2 15.6H7.2L13.2 3.6ZM12 20.4C13.326 20.4 14.4 19.326 14.4 18C14.4 16.674 13.326 15.6 12 15.6C10.674 15.6 9.6 16.674 9.6 18C9.6 19.326 10.674 20.4 12 20.4Z"
      fill="currentColor"
    />
  </svg>
)

// OpenAI 模型
const openaiModels: Model[] = [
  { id: 'o1', name: 'LLM', type: 'TEXT EMBEDDING', context: '', isActive: true },
  { id: 'o2', name: 'SPEECH2TEXT', type: 'MODERATION', context: '', isActive: true },
  { id: 'o3', name: 'TTS', type: '', context: '', isActive: true },
]

// Anthropic 模型
const anthropicModels: Model[] = [
  { id: 'a1', name: 'claude-3-5-haiku-20241022', type: 'LLM', context: 'CHAT', isActive: true },
  { id: 'a2', name: 'claude-3-5-sonnet-20241022', type: 'LLM', context: 'CHAT', isActive: true },
  { id: 'a3', name: 'claude-3-5-sonnet-20240620', type: 'LLM', context: 'CHAT', isActive: true },
  { id: 'a4', name: 'claude-3-haiku-20240307', type: 'LLM', context: 'CHAT', isActive: true },
  { id: 'a5', name: 'claude-3-opus-20240229', type: 'LLM', context: 'CHAT', isActive: true },
  { id: 'a6', name: 'claude-3-sonnet-20240229', type: 'LLM', context: 'CHAT', isActive: true },
  { id: 'a7', name: 'claude-2.1', type: 'LLM', context: 'CHAT', isActive: true },
  { id: 'a8', name: 'claude-instant-1.2', type: 'LLM', context: 'CHAT', isActive: true },
  { id: 'a9', name: 'claude-2', type: 'LLM', context: 'CHAT', isActive: true },
  { id: 'a10', name: 'claude-instant-1', type: 'LLM', context: 'CHAT', isActive: true },
]

// 模型供应商数据
const initialProviders: Provider[] = [
  {
    id: '1',
    name: 'OpenAI',
    logoComponent: <OpenAILogo />,
    models: openaiModels,
    totalModels: 38,
    apiKey: 'sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    quota: { value: 199, unit: '消息额度' },
    isActive: true,
  },
  {
    id: '2',
    name: 'ANTHROPIC',
    logoComponent: <AnthropicLogo />,
    models: anthropicModels,
    totalModels: 10,
    apiKey: 'sk-ant-xxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    quota: { value: 0, unit: 'Tokens' },
    isActive: true,
  },
]

export default function ModelProvidersPage() {
  const [isOpenAddDialog, setIsOpenAddDialog] = useState(false)
  const [providers, setProviders] = useState<Provider[]>(initialProviders)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [openProvider, setOpenProvider] = useState<string | null>(null)
  const [formValues, setFormValues] = useState({
    name: '',
    apiKey: '',
    baseUrl: '',
    isDefault: false,
  })
  const [errors, setErrors] = useState<Record<string, string>>({})

  // 简化版表单验证
  const validateForm = () => {
    try {
      providerFormSchema.parse(formValues)
      setErrors({})
      return true
    } catch (error) {
      if (error instanceof z.ZodError) {
        const newErrors: Record<string, string> = {}
        for (const err of error.errors) {
          if (err.path[0]) {
            newErrors[err.path[0].toString()] = err.message
          }
        }
        setErrors(newErrors)
      }
      return false
    }
  }

  // 处理表单字段变化
  const handleFormChange = (field: string, value: string | boolean) => {
    setFormValues((prev) => ({ ...prev, [field]: value }))
  }

  // 处理添加供应商提交
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!validateForm()) return

    setIsSubmitting(true)
    try {
      // 添加新供应商逻辑...
      toast.success('供应商添加成功')
      setFormValues({
        name: '',
        apiKey: '',
        baseUrl: '',
        isDefault: false,
      })
      setIsOpenAddDialog(false)
    } catch (error) {
      toast.error('添加供应商失败')
      console.error(error)
    } finally {
      setIsSubmitting(false)
    }
  }

  // 切换供应商模型列表的展开/折叠状态
  const toggleProviderModels = (providerId: string) => {
    setOpenProvider(openProvider === providerId ? null : providerId)
  }

  // 切换模型开关状态
  const toggleModelActive = (providerId: string, modelId: string) => {
    setProviders(
      providers.map((provider) => {
        if (provider.id === providerId) {
          return {
            ...provider,
            models: provider.models.map((model) => {
              if (model.id === modelId) {
                return { ...model, isActive: !model.isActive }
              }
              return model
            }),
          }
        }
        return provider
      }),
    )
  }

  return (
    <div className="container px-4 py-4 max-w-5xl space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold tracking-tight">模型供应商</h2>
          <p className="text-muted-foreground text-sm">管理您的AI模型供应商和API密钥</p>
        </div>
        <Dialog open={isOpenAddDialog} onOpenChange={setIsOpenAddDialog}>
          <DialogTrigger asChild>
            <Button className="gap-2">
              <Plus className="h-4 w-4" />
              添加供应商
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>添加模型供应商</DialogTitle>
              <DialogDescription>添加新的AI模型供应商及其API密钥</DialogDescription>
            </DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">供应商名称</Label>
                <Input
                  id="name"
                  placeholder="例如: OpenAI, Anthropic"
                  value={formValues.name}
                  onChange={(e) => handleFormChange('name', e.target.value)}
                />
                {errors.name && <p className="text-sm text-destructive">{errors.name}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="apiKey">API 密钥</Label>
                <Input
                  id="apiKey"
                  placeholder="请输入API密钥"
                  type="password"
                  value={formValues.apiKey}
                  onChange={(e) => handleFormChange('apiKey', e.target.value)}
                />
                {errors.apiKey && <p className="text-sm text-destructive">{errors.apiKey}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="baseUrl">API 基础URL (可选)</Label>
                <Input
                  id="baseUrl"
                  placeholder="https://api.example.com"
                  value={formValues.baseUrl}
                  onChange={(e) => handleFormChange('baseUrl', e.target.value)}
                />
                <p className="text-sm text-muted-foreground">对于自定义部署或代理，可以指定API基础URL</p>
                {errors.baseUrl && <p className="text-sm text-destructive">{errors.baseUrl}</p>}
              </div>

              <DialogFooter>
                <Button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? '保存中...' : '保存供应商'}
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <Separator className="my-2" />

      {/* 模型供应商列表 */}
      <div className="grid gap-3">
        {providers.map((provider) => (
          <Card key={provider.id} className="overflow-hidden border shadow-sm">
            <CardHeader className="bg-muted/30 p-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-background/80 flex items-center justify-center p-1.5 shadow-sm">
                    {provider.logoComponent}
                  </div>
                  <h3 className="font-semibold">{provider.name}</h3>
                </div>
                <div className="flex items-center gap-2">
                  {provider.quota && (
                    <div className="flex items-center gap-1 bg-background/80 px-2 py-1 rounded-full text-sm shadow-sm">
                      <span className="font-medium">额度</span>
                      <Info className="h-3.5 w-3.5 text-muted-foreground" />
                      <span className="font-medium">{provider.quota.value}</span>
                      <span className="text-muted-foreground text-xs">{provider.quota.unit}</span>
                    </div>
                  )}
                  <div className="flex items-center">
                    <Badge
                      variant={provider.apiKey ? 'outline' : 'secondary'}
                      className={
                        provider.apiKey ? 'text-red-500 border-red-200 text-xs px-1.5 py-0' : 'text-xs px-1.5 py-0'
                      }
                    >
                      API-KEY
                    </Badge>
                    <Button variant="ghost" size="icon" className="h-7 w-7">
                      <Settings className="h-3.5 w-3.5" />
                    </Button>
                  </div>
                </div>
              </div>
            </CardHeader>

            <CardContent className="p-0">
              {/* 模型标签 */}
              <div className="px-3 py-2 border-b flex flex-wrap gap-1">
                {Array.from(new Set(provider.models.map((model) => model.type)))
                  .filter(Boolean)
                  .map((type) => (
                    <Badge key={type} variant="secondary" className="rounded-md font-normal text-xs">
                      {type}
                    </Badge>
                  ))}
              </div>

              {/* 模型列表 */}
              <div>
                <button
                  type="button"
                  className="w-full px-3 py-2 flex justify-between items-center hover:bg-muted/50 transition-colors text-left"
                  onClick={() => toggleProviderModels(provider.id)}
                  aria-expanded={openProvider === provider.id}
                  aria-controls={`models-${provider.id}`}
                >
                  <span className="text-sm font-medium">{provider.totalModels} 个模型</span>
                  {openProvider === provider.id ? (
                    <ChevronUp className="h-4 w-4" />
                  ) : (
                    <ChevronDown className="h-4 w-4" />
                  )}
                </button>

                {openProvider === provider.id && (
                  <div id={`models-${provider.id}`} className="px-3 pb-3">
                    <div className="rounded-lg overflow-hidden border divide-y">
                      {provider.models.map((model) => (
                        <div
                          key={model.id}
                          className="flex items-center justify-between px-3 py-2 hover:bg-muted/30 transition-colors"
                        >
                          <div className="flex items-center">
                            <div className="mr-2 w-8 h-8 rounded-md bg-muted flex items-center justify-center text-sm font-semibold">
                              {provider.name === 'ANTHROPIC' ? 'A' : 'O'}
                            </div>
                            <div>
                              <div className="font-medium text-sm">{model.name}</div>
                              <div className="flex gap-1 text-xs mt-0.5">
                                {model.type && (
                                  <Badge
                                    variant="outline"
                                    className="rounded font-normal bg-muted/30 text-xs border-none px-1.5 py-0"
                                  >
                                    {model.type}
                                  </Badge>
                                )}
                                {model.context && (
                                  <Badge
                                    variant="outline"
                                    className="rounded font-normal bg-muted/30 text-xs border-none px-1.5 py-0"
                                  >
                                    {model.context}
                                  </Badge>
                                )}
                                {model.type === 'LLM' && !model.context && (
                                  <Badge
                                    variant="outline"
                                    className="rounded font-normal bg-muted/30 text-xs border-none px-1.5 py-0"
                                  >
                                    200K
                                  </Badge>
                                )}
                              </div>
                            </div>
                          </div>
                          <Switch
                            checked={model.isActive}
                            onCheckedChange={() => toggleModelActive(provider.id, model.id)}
                          />
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* 系统模型设置按钮 */}
      <div className="flex justify-end mt-3">
        <Button variant="outline" size="sm" className="gap-2">
          <Settings className="h-3.5 w-3.5" />
          系统模型设置
        </Button>
      </div>
    </div>
  )
}
