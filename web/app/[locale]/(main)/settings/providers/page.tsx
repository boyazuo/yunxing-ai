'use client'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import { Switch } from '@/components/ui/switch'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import { BadgeCheck, Eye, EyeOff, Plus, Search, Settings, Zap } from 'lucide-react'
import { useState } from 'react'
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
})

// 模型类型定义
type Model = {
  id: string
  name: string
  isSelected: boolean
  hasSpecialSettings?: boolean
}

// 模型供应商类型
type Provider = {
  id: string
  name: string
  logo: React.ReactNode
  apiKey?: string
  baseUrl?: string
  isActive: boolean
  models: Model[]
}

// 模型 logo 组件
const ProviderLogo = ({ provider }: { provider: string }) => {
  switch (provider) {
    case 'Silicon基础动力':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-purple-500 to-purple-600 flex items-center justify-center text-white shadow-sm text-xs">
          硅
        </div>
      )
    case 'O3':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-blue-500 to-blue-600 flex items-center justify-center text-white shadow-sm text-xs">
          O3
        </div>
      )
    case 'AiHubMix':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-blue-600 to-blue-700 flex items-center justify-center text-white shadow-sm text-xs">
          Ai
        </div>
      )
    case 'DeepSeek':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-blue-400 to-blue-500 flex items-center justify-center text-white shadow-sm text-xs">
          D
        </div>
      )
    case 'ocoolAI':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-gray-800 to-black flex items-center justify-center text-white shadow-sm text-xs">
          O
        </div>
      )
    case '百度云千帆':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-blue-500 to-blue-600 flex items-center justify-center text-white shadow-sm text-xs">
          百
        </div>
      )
    case 'Ollama':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-gray-200 to-gray-300 flex items-center justify-center shadow-sm text-xs">
          O
        </div>
      )
    case 'Anthropic':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-gray-200 to-gray-300 flex items-center justify-center shadow-sm text-xs">
          A
        </div>
      )
    case 'PPIO派欧云':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-blue-500 to-blue-600 flex items-center justify-center text-white shadow-sm text-xs">
          P
        </div>
      )
    case 'Alaya NeW':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-orange-500 to-orange-600 flex items-center justify-center text-white shadow-sm text-xs">
          A
        </div>
      )
    case '无间芯答':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-purple-600 to-purple-700 flex items-center justify-center text-white shadow-sm text-xs">
          无
        </div>
      )
    case '七牛云':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-blue-400 to-blue-500 flex items-center justify-center text-white shadow-sm text-xs">
          七
        </div>
      )
    case 'OpenAI':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-gray-800 to-black flex items-center justify-center text-white shadow-sm text-xs">
          O
        </div>
      )
    case 'Azure OpenAI':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-blue-600 to-blue-700 flex items-center justify-center text-white shadow-sm text-xs">
          A
        </div>
      )
    case 'Gemini':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-blue-500 to-blue-600 flex items-center justify-center text-white shadow-sm text-xs">
          G
        </div>
      )
    case 'GitHub Models':
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-gray-800 to-black flex items-center justify-center text-white shadow-sm text-xs">
          G
        </div>
      )
    default:
      return (
        <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-gray-500 to-gray-600 flex items-center justify-center text-white shadow-sm text-xs">
          {provider.charAt(0)}
        </div>
      )
  }
}

// 初始模型供应商数据
const initialProviders: Provider[] = [
  {
    id: '1',
    name: 'Silicon基础动力',
    logo: <ProviderLogo provider="Silicon基础动力" />,
    isActive: true,
    models: [],
  },
  {
    id: '2',
    name: 'O3',
    logo: <ProviderLogo provider="O3" />,
    isActive: true,
    models: [],
  },
  {
    id: '3',
    name: 'AiHubMix',
    logo: <ProviderLogo provider="AiHubMix" />,
    isActive: true,
    models: [],
  },
  {
    id: '4',
    name: 'DeepSeek',
    logo: <ProviderLogo provider="DeepSeek" />,
    apiKey: '',
    baseUrl: 'https://api.deepseek.com',
    isActive: true,
    models: [
      { id: 'ds1', name: 'DeepSeek Chat', isSelected: true },
      { id: 'ds2', name: 'DeepSeek Reasoner', isSelected: true, hasSpecialSettings: true },
    ],
  },
  {
    id: '5',
    name: 'ocoolAI',
    logo: <ProviderLogo provider="ocoolAI" />,
    isActive: true,
    models: [],
  },
  {
    id: '6',
    name: '百度云千帆',
    logo: <ProviderLogo provider="百度云千帆" />,
    isActive: true,
    models: [],
  },
  {
    id: '7',
    name: 'Ollama',
    logo: <ProviderLogo provider="Ollama" />,
    isActive: true,
    models: [],
  },
  {
    id: '8',
    name: 'Anthropic',
    logo: <ProviderLogo provider="Anthropic" />,
    isActive: true,
    models: [],
  },
  {
    id: '9',
    name: 'PPIO派欧云',
    logo: <ProviderLogo provider="PPIO派欧云" />,
    isActive: true,
    models: [],
  },
  {
    id: '10',
    name: 'Alaya NeW',
    logo: <ProviderLogo provider="Alaya NeW" />,
    isActive: true,
    models: [],
  },
  {
    id: '11',
    name: '无间芯答',
    logo: <ProviderLogo provider="无间芯答" />,
    isActive: true,
    models: [],
  },
  {
    id: '12',
    name: '七牛云',
    logo: <ProviderLogo provider="七牛云" />,
    isActive: true,
    models: [],
  },
  {
    id: '13',
    name: 'OpenAI',
    logo: <ProviderLogo provider="OpenAI" />,
    isActive: true,
    models: [],
  },
]

export default function ModelProvidersPage() {
  const [providers] = useState<Provider[]>(initialProviders)
  const [selectedProvider, setSelectedProvider] = useState<Provider>(providers[3]) // DeepSeek 默认选中
  const [showApiKey, setShowApiKey] = useState(false)
  const [isAddProviderOpen, setIsAddProviderOpen] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')

  // 过滤供应商列表
  const filteredProviders = providers.filter((provider) =>
    provider.name.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  return (
    <div className="flex h-full">
      {/* 左侧供应商列表 */}
      <div className="w-[280px] border-r bg-background/50 overflow-y-auto h-full">
        <div className="p-3 sticky top-0 bg-background/95 backdrop-blur-sm z-10 border-b">
          <Button
            variant="outline"
            size="sm"
            className="w-full justify-center gap-1 h-7 mb-2 shadow-sm hover:shadow transition-all text-xs"
            onClick={() => setIsAddProviderOpen(true)}
          >
            <Plus className="h-3.5 w-3.5" />
            <span>添加供应商</span>
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
          {filteredProviders.map((provider) => (
            <button
              key={provider.id}
              className={`flex w-full items-center justify-between px-3 py-2 cursor-pointer transition-colors hover:bg-muted text-left ${
                selectedProvider.id === provider.id ? 'bg-primary/5 hover:bg-primary/5 border-l-2 border-primary' : ''
              }`}
              onClick={() => setSelectedProvider(provider)}
              type="button"
            >
              <div className="flex items-center gap-2">
                {provider.logo}
                <div>
                  <span className="text-xs font-medium">{provider.name}</span>
                </div>
              </div>
              <div>
                {provider.name === 'DeepSeek' && (
                  <Badge variant="outline" className="bg-primary/10 text-primary border-0 text-[10px] px-1.5 py-0">
                    活跃
                  </Badge>
                )}
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* 右侧详情区域 */}
      <div className="flex-1 bg-background overflow-y-auto">
        <div className="max-w-3xl mx-auto p-6">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2.5">
              {selectedProvider.logo}
              <div>
                <h1 className="text-base font-medium flex items-center gap-1.5">
                  深度求索
                  <BadgeCheck className="h-4 w-4 text-blue-500" />
                </h1>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <div className="flex items-center gap-1.5">
                <span className="text-xs font-medium">启用</span>
                <Switch />
              </div>
            </div>
          </div>

          <div className="bg-card rounded-md border shadow-sm p-4 mb-5">
            {/* API 密钥区域 */}
            <div className="mb-4">
              <div className="mb-2">
                <Label className="text-sm font-medium">API 密钥</Label>
              </div>
              <div className="relative mb-1 group">
                <Input
                  placeholder="API 密钥"
                  type={showApiKey ? 'text' : 'password'}
                  className="pr-16 h-8 text-xs transition-all focus-visible:ring-1 focus-visible:ring-primary/30"
                />
                <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1.5">
                  <button
                    type="button"
                    onClick={() => setShowApiKey(!showApiKey)}
                    className="text-muted-foreground hover:text-foreground transition-colors"
                  >
                    {showApiKey ? <EyeOff className="h-3.5 w-3.5" /> : <Eye className="h-3.5 w-3.5" />}
                  </button>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-6 px-1.5 hover:bg-primary/10 hover:text-primary text-xs"
                  >
                    检测
                  </Button>
                </div>
              </div>
            </div>

            {/* API 地址区域 */}
            <div>
              <div className="mb-2">
                <Label className="text-sm font-medium">API 地址</Label>
              </div>
              <div className="mb-1">
                <Input
                  placeholder="API 地址"
                  defaultValue="https://api.deepseek.com"
                  className="h-8 text-xs transition-all focus-visible:ring-1 focus-visible:ring-primary/30"
                />
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
              {/* DeepSeek Chat 模型 */}
              <div className="p-3 flex items-center justify-between border rounded-md hover:border-primary/30 hover:shadow-sm transition-all bg-card">
                <div className="flex items-center gap-2.5">
                  {selectedProvider.logo}
                  <div>
                    <div className="flex items-center gap-1.5">
                      <span className="text-xs font-medium">DeepSeek Chat</span>
                      <Badge className="bg-blue-100 text-blue-700 hover:bg-blue-200 border-0 text-[10px] py-0 px-1.5">
                        通用对话
                      </Badge>
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Switch />
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

              {/* DeepSeek Reasoner 模型 */}
              <div className="p-3 flex items-center justify-between border rounded-md hover:border-primary/30 hover:shadow-sm transition-all bg-card">
                <div className="flex items-center gap-2.5">
                  {selectedProvider.logo}
                  <div>
                    <div className="flex items-center gap-1.5">
                      <span className="text-xs font-medium">DeepSeek Reasoner</span>
                      <Badge className="bg-orange-100 text-orange-700 hover:bg-orange-200 border-0 text-[10px] py-0 px-1.5">
                        <Zap className="h-2.5 w-2.5 mr-0.5" />
                        推理增强
                      </Badge>
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Switch />
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
            </div>
          </div>
        </div>
      </div>

      {/* 添加供应商对话框 */}
      <Dialog open={isAddProviderOpen} onOpenChange={setIsAddProviderOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle className="text-base">添加模型供应商</DialogTitle>
            <DialogDescription className="text-xs">添加新的AI模型供应商及其API密钥</DialogDescription>
          </DialogHeader>
          <Separator />
          <div className="space-y-3 py-2">
            <div className="space-y-1.5">
              <Label htmlFor="name" className="text-xs">
                供应商名称
              </Label>
              <Input id="name" placeholder="例如: OpenAI, Anthropic" className="h-8 text-xs" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="apiKey" className="text-xs">
                API 密钥
              </Label>
              <Input id="apiKey" placeholder="请输入API密钥" type="password" className="h-8 text-xs" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="baseUrl" className="text-xs">
                API 基础URL (可选)
              </Label>
              <Input id="baseUrl" placeholder="https://api.example.com" className="h-8 text-xs" />
              <p className="text-[10px] text-muted-foreground">对于自定义部署或代理，可以指定API基础URL</p>
            </div>
          </div>
          <Separator />
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsAddProviderOpen(false)} className="text-xs h-8">
              取消
            </Button>
            <Button type="submit" className="text-xs h-8">
              保存供应商
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
