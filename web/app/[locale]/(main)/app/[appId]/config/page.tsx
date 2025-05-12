'use client'

import { appConfigService } from '@/api/appConfig'
import { appService } from '@/api/apps'
import DatasetConfig from '@/app/[locale]/(main)/app/_components/config/DatasetConfig'
import ModelConfig from '@/app/[locale]/(main)/app/_components/config/ModelConfig'
import PromptConfig from '@/app/[locale]/(main)/app/_components/config/PromptConfig'
import VariableConfig from '@/app/[locale]/(main)/app/_components/config/VariableConfig'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import type {
  AppConfig,
  DatasetConfig as DatasetConfigType,
  ModelConfig as ModelConfigType,
  VariableConfig as VariableConfigType,
} from '@/types/appConfig'
import {
  Bot,
  ChevronDown,
  ChevronLeft,
  ChevronUp,
  Database,
  MessageSquare,
  PlusCircle,
  RefreshCw,
  Save,
} from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useParams, useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'

// 解析字符串为对象数组 - 移到组件外部避免在每次渲染时重新创建
const parseJsonField = <T,>(value: string | T[] | undefined): T[] => {
  if (!value) return []
  if (Array.isArray(value)) return value
  try {
    return JSON.parse(value) as T[]
  } catch (error) {
    console.error('JSON解析错误:', error)
    return []
  }
}

export default function AppConfigPage() {
  const params = useParams()
  const router = useRouter()

  const appId = params.appId as string
  const [appName, setAppName] = useState('我的AI应用')
  const [loading, setLoading] = useState(false)
  const [appConfig, setAppConfig] = useState<AppConfig | null>(null)
  const { data: session } = useSession()

  const tenantId = session?.tenant?.tenantId || ''

  // 控制各个部分是否展开的状态
  const [sectionState, setSectionState] = useState({
    promptSection: true,
    modelSection: false,
    variableSection: false,
    datasetSection: false,
  })

  // 切换某一部分的展开/折叠状态，确保每次只有一个部分展开
  const toggleSection = (section: keyof typeof sectionState) => {
    setSectionState(
      Object.fromEntries(
        Object.keys(sectionState).map((key) => [
          key,
          key === section ? !sectionState[key as keyof typeof sectionState] : false,
        ]),
      ) as typeof sectionState,
    )
  }

  // 调试预览区域的模拟对话
  const [chatInput, setChatInput] = useState('')
  const [chatMessages, setChatMessages] = useState([
    {
      id: 'welcome-msg',
      role: 'assistant',
      content: '您好！我是您的AI助手。请问有什么我可以帮助您的？',
    },
  ])

  // 加载应用信息和配置
  useEffect(() => {
    const fetchAppData = async () => {
      try {
        setLoading(true)
        // 获取应用信息
        const appData = await appService.getAppById(appId)
        setAppName(appData.appName)

        // 获取应用配置
        const configData = await appConfigService.getAppConfig(appId)
        if (!configData) {
          // 如果没有配置，创建一个默认配置
          setAppConfig({
            appId,
            tenantId,
            sysPrompt: '',
            models: [],
            variables: [],
            datasets: [],
          })
        } else {
          // 将后端返回的JSON字符串解析为对象
          const parsedConfig: AppConfig = {
            ...configData,
            models: parseJsonField<ModelConfigType>(configData.models),
            variables: parseJsonField<VariableConfigType>(configData.variables),
            datasets: parseJsonField<DatasetConfigType>(configData.datasets),
          }
          setAppConfig(parsedConfig)
        }
      } catch (error) {
        toast.error('无法加载应用信息和配置')
      } finally {
        setLoading(false)
      }
    }

    fetchAppData()
  }, [appId, tenantId])

  // 返回应用列表页
  const handleBack = () => {
    router.push('/app')
  }

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault()
    if (!chatInput.trim()) return

    // 添加用户消息
    const userMsgId = `user-${Date.now()}`
    setChatMessages((prev) => [...prev, { id: userMsgId, role: 'user', content: chatInput }])

    // 模拟AI回复
    setTimeout(() => {
      const aiMsgId = `ai-${Date.now()}`
      setChatMessages((prev) => [
        ...prev,
        {
          id: aiMsgId,
          role: 'assistant',
          content: `我是AI助手，正在回复您的问题："${chatInput}"`,
        },
      ])
    }, 1000)

    setChatInput('')
  }

  // 从子组件获取配置数据
  const handlePromptChange = (sysPrompt: string) => {
    if (appConfig) {
      setAppConfig({
        ...appConfig,
        sysPrompt,
      })
    }
  }

  const handleModelsChange = (models: AppConfig['models']) => {
    // 检查models中是否包含isActive字段
    const hasActiveField = models.length > 0 ? models.some((model) => 'isActive' in model) : false

    if (appConfig) {
      setAppConfig({
        ...appConfig,
        models,
      })
    }
  }

  const handleVariablesChange = (variables: AppConfig['variables']) => {
    if (appConfig) {
      setAppConfig({
        ...appConfig,
        variables,
      })
    }
  }

  const handleDatasetsChange = (datasets: AppConfig['datasets']) => {
    if (appConfig) {
      setAppConfig({
        ...appConfig,
        datasets,
      })
    }
  }

  // 保存配置
  const handleSaveConfig = async () => {
    if (!appConfig) return

    try {
      setLoading(true)

      // 准备要发送到后端的数据，将数组转换为JSON字符串
      const configToSave = {
        ...appConfig,
        models: JSON.stringify(appConfig.models),
        variables: JSON.stringify(appConfig.variables || []),
        datasets: JSON.stringify(appConfig.datasets || []),
      }

      // 判断是新建还是更新
      let result: AppConfig
      if (appConfig.configId === undefined) {
        // 没有配置，创建
        result = await appConfigService.createAppConfig(configToSave as unknown as AppConfig)
      } else {
        // 更新配置
        result = await appConfigService.updateAppConfig(appId, configToSave as unknown as Partial<AppConfig>)
      }

      // 将接收到的结果中的JSON字符串解析回对象
      const parsedResult: AppConfig = {
        ...result,
        models: parseJsonField<ModelConfigType>(result.models),
        variables: parseJsonField<VariableConfigType>(result.variables),
        datasets: parseJsonField<DatasetConfigType>(result.datasets),
      }

      setAppConfig(parsedResult)
      toast.success('应用配置已成功保存')
    } catch (error) {
      console.error('保存配置失败:', error)
      toast.error('无法保存应用配置')
    } finally {
      setLoading(false)
    }
  }

  // 刷新预览
  const handleRefreshPreview = () => {
    // 重置聊天消息
    setChatMessages([
      {
        id: 'welcome-msg',
        role: 'assistant',
        content: '您好！我是您的AI助手。请问有什么我可以帮助您的？',
      },
    ])
  }

  if (loading && !appConfig) {
    return (
      <div className="flex justify-center items-center h-[calc(100vh-60px)]">
        <p>加载中...</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col h-[calc(100vh-60px)] overflow-hidden">
      {/* 顶部导航 */}
      <div className="p-4 border-b border-border flex justify-between items-center shrink-0">
        <div className="flex items-center">
          <Button variant="ghost" size="icon" className="mr-2" onClick={handleBack}>
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <h1 className="font-medium">{appName}</h1>
        </div>
        <Button
          variant="default"
          size="sm"
          className="flex items-center gap-1"
          onClick={handleSaveConfig}
          disabled={loading}
        >
          {loading ? <RefreshCw className="h-4 w-4 mr-1 animate-spin" /> : <Save className="h-4 w-4 mr-1" />}
          保存配置
        </Button>
      </div>

      <div className="flex flex-1 overflow-hidden">
        {/* 左侧配置区域 */}
        <div className="w-[450px] min-h-0 border-r border-border overflow-y-auto">
          <div className="p-4 space-y-2">
            {/* 提示词设置部分 */}
            <div className="bg-background rounded-lg overflow-hidden">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between bg-muted/50 text-left"
                onClick={() => toggleSection('promptSection')}
              >
                <div className="flex items-center">
                  <MessageSquare className="h-4 w-4 mr-2 text-primary" />
                  <span className="font-medium text-sm">提示词设置</span>
                </div>
                {sectionState.promptSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.promptSection && (
                <div className="p-4">
                  <PromptConfig appId={appId} sysPrompt={appConfig?.sysPrompt || ''} onChange={handlePromptChange} />
                </div>
              )}
            </div>

            {/* AI 模型配置部分 */}
            <div className="bg-background rounded-lg overflow-hidden">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between bg-muted/50 text-left"
                onClick={() => toggleSection('modelSection')}
              >
                <div className="flex items-center">
                  <Bot className="h-4 w-4 mr-2 text-primary" />
                  <span className="font-medium text-sm">AI 模型配置</span>
                </div>
                {sectionState.modelSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.modelSection && (
                <div className="p-4">
                  <ModelConfig appId={appId} models={appConfig?.models || []} onChange={handleModelsChange} />
                </div>
              )}
            </div>

            {/* 变量配置部分 */}
            <div className="bg-background rounded-lg overflow-hidden">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between bg-muted/50 text-left"
                onClick={() => toggleSection('variableSection')}
              >
                <div className="flex items-center">
                  <RefreshCw className="h-4 w-4 mr-2 text-primary" />
                  <span className="font-medium text-sm">变量配置</span>
                </div>
                {sectionState.variableSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.variableSection && (
                <div className="p-4">
                  <VariableConfig
                    appId={appId}
                    variables={appConfig?.variables || []}
                    onChange={handleVariablesChange}
                  />
                </div>
              )}
            </div>

            {/* 关联知识库部分 */}
            <div className="bg-background rounded-lg overflow-hidden">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between bg-muted/50 text-left"
                onClick={() => toggleSection('datasetSection')}
              >
                <div className="flex items-center">
                  <Database className="h-4 w-4 mr-2 text-primary" />
                  <span className="font-medium text-sm">关联知识库</span>
                </div>
                {sectionState.datasetSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.datasetSection && (
                <div className="p-4">
                  <DatasetConfig appId={appId} datasets={appConfig?.datasets || []} onChange={handleDatasetsChange} />
                </div>
              )}
            </div>
          </div>
        </div>

        {/* 右侧调试预览区域 */}
        <div className="flex-1 flex flex-col min-h-0 overflow-hidden">
          {/* 调试预览标题栏 */}
          <div className="flex justify-between items-center px-6 py-3 border-b border-border bg-white shrink-0">
            <h2 className="font-medium">调试预览</h2>
            <Button
              variant="outline"
              size="sm"
              className="flex items-center gap-1 px-3 text-xs"
              onClick={handleRefreshPreview}
            >
              <RefreshCw className="w-3.5 h-3.5 mr-1" />
              刷新
            </Button>
          </div>

          {/* 聊天消息区域 */}
          <div className="flex-1 min-h-0 overflow-y-auto px-6 py-4 bg-gray-50">
            <div className="flex flex-col space-y-4">
              {chatMessages.map((msg) => (
                <div key={msg.id} className={`flex ${msg.role === 'assistant' ? 'justify-start' : 'justify-end'}`}>
                  <div
                    className={`max-w-[80%] p-3 rounded-lg shadow-sm ${
                      msg.role === 'assistant'
                        ? 'bg-white border border-gray-100'
                        : 'bg-primary text-primary-foreground'
                    }`}
                  >
                    {msg.content}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 输入区域 */}
          <div className="px-6 py-3 border-t border-border bg-white shrink-0">
            <form onSubmit={handleSendMessage} className="flex gap-2">
              <Input
                placeholder="输入问题，按Enter键执行 [Ctrl(Alt/Shift) + Enter]"
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                className="flex-1 border-gray-200"
              />
              <Button
                type="submit"
                className="bg-primary text-primary-foreground rounded-full w-10 h-10 p-0 flex items-center justify-center"
              >
                <PlusCircle className="h-5 w-5" />
              </Button>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}
