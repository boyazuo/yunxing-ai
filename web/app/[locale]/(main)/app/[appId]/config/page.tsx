'use client'

import { appConfigService } from '@/api/appConfig'
import { appService } from '@/api/apps'
import DatasetConfig from '@/app/[locale]/(main)/app/_components/config/DatasetConfig'
import PromptConfig from '@/app/[locale]/(main)/app/_components/config/PromptConfig'
import VariableConfig from '@/app/[locale]/(main)/app/_components/config/VariableConfig'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import type {
  AppConfig,
  DatasetConfig as DatasetConfigType,
  VariableConfig as VariableConfigType,
} from '@/types/appConfig'
import {
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
            variables: [],
            datasets: [],
          })
        } else {
          const parsedConfig: AppConfig = {
            ...configData,
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
      <div className="flex flex-col items-center justify-center h-[calc(100vh-56px)] gap-3">
        <RefreshCw className="h-6 w-6 animate-spin text-primary/60" />
        <p className="text-sm text-muted-foreground">加载应用配置中...</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col h-[calc(100vh-56px)] overflow-hidden bg-background">
      {/* Top bar */}
      <div className="h-14 px-4 border-b border-border/60 flex justify-between items-center shrink-0 bg-background/80 backdrop-blur-sm">
        <div className="flex items-center gap-2 min-w-0">
          <Button variant="ghost" size="icon" className="h-8 w-8 shrink-0 cursor-pointer" onClick={handleBack}>
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <div className="h-5 w-px bg-border" />
          <div className="min-w-0">
            <p className="text-[10px] font-semibold tracking-widest text-primary/70 uppercase">App Config</p>
            <h1 className="font-semibold text-sm truncate">{appName}</h1>
          </div>
        </div>
        <Button
          size="sm"
          className="gap-1.5 shadow-sm shadow-primary/15 cursor-pointer"
          onClick={handleSaveConfig}
          disabled={loading}
        >
          {loading ? <RefreshCw className="h-3.5 w-3.5 animate-spin" /> : <Save className="h-3.5 w-3.5" />}
          保存配置
        </Button>
      </div>

      <div className="flex flex-1 overflow-hidden">
        {/* Left config panel */}
        <div className="w-[420px] min-h-0 border-r border-border/60 overflow-y-auto bg-muted/20">
          <div className="p-4 space-y-3">
            {/* Prompt section */}
            <div className="rounded-xl border border-border/60 bg-card overflow-hidden shadow-sm">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between text-left cursor-pointer hover:bg-muted/40 transition-colors duration-200"
                onClick={() => toggleSection('promptSection')}
              >
                <div className="flex items-center gap-2.5">
                  <div className="h-7 w-7 rounded-lg bg-indigo-50 dark:bg-indigo-950/60 flex items-center justify-center">
                    <MessageSquare className="h-3.5 w-3.5 text-indigo-600 dark:text-indigo-300" />
                  </div>
                  <span className="font-medium text-sm">提示词设置</span>
                </div>
                {sectionState.promptSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.promptSection && (
                <div className="px-4 pb-4 border-t border-border/40">
                  <PromptConfig appId={appId} sysPrompt={appConfig?.sysPrompt || ''} onChange={handlePromptChange} />
                </div>
              )}
            </div>

            {/* Variables section */}
            <div className="rounded-xl border border-border/60 bg-card overflow-hidden shadow-sm">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between text-left cursor-pointer hover:bg-muted/40 transition-colors duration-200"
                onClick={() => toggleSection('variableSection')}
              >
                <div className="flex items-center gap-2.5">
                  <div className="h-7 w-7 rounded-lg bg-violet-50 dark:bg-violet-950/60 flex items-center justify-center">
                    <RefreshCw className="h-3.5 w-3.5 text-violet-600 dark:text-violet-300" />
                  </div>
                  <span className="font-medium text-sm">变量配置</span>
                </div>
                {sectionState.variableSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.variableSection && (
                <div className="px-4 pb-4 border-t border-border/40">
                  <VariableConfig
                    appId={appId}
                    variables={appConfig?.variables || []}
                    onChange={handleVariablesChange}
                  />
                </div>
              )}
            </div>

            {/* Dataset section */}
            <div className="rounded-xl border border-border/60 bg-card overflow-hidden shadow-sm">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between text-left cursor-pointer hover:bg-muted/40 transition-colors duration-200"
                onClick={() => toggleSection('datasetSection')}
              >
                <div className="flex items-center gap-2.5">
                  <div className="h-7 w-7 rounded-lg bg-teal-50 dark:bg-teal-950/60 flex items-center justify-center">
                    <Database className="h-3.5 w-3.5 text-teal-600 dark:text-teal-300" />
                  </div>
                  <span className="font-medium text-sm">知识库</span>
                </div>
                {sectionState.datasetSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.datasetSection && (
                <div className="px-4 pb-4 border-t border-border/40">
                  <DatasetConfig appId={appId} datasets={appConfig?.datasets || []} onChange={handleDatasetsChange} />
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right preview panel */}
        <div className="flex-1 flex flex-col min-h-0 overflow-hidden">
          <div className="flex justify-between items-center px-5 h-12 border-b border-border/60 shrink-0">
            <div className="flex items-center gap-2">
              <div className="h-2 w-2 rounded-full bg-emerald-500" />
              <h2 className="font-medium text-sm">调试预览</h2>
            </div>
            <Button
              variant="outline"
              size="sm"
              className="h-7 text-xs gap-1 cursor-pointer"
              onClick={handleRefreshPreview}
            >
              <RefreshCw className="w-3 h-3" />
              刷新
            </Button>
          </div>

          <div className="flex-1 min-h-0 overflow-y-auto px-5 py-4 bg-muted/20">
            <div className="max-w-2xl mx-auto flex flex-col space-y-4">
              {chatMessages.map((msg) => (
                <div key={msg.id} className={`flex ${msg.role === 'assistant' ? 'justify-start' : 'justify-end'}`}>
                  <div
                    className={`max-w-[85%] px-3.5 py-2.5 rounded-2xl text-sm leading-relaxed ${
                      msg.role === 'assistant'
                        ? 'bg-card border border-border/60 rounded-bl-md shadow-sm'
                        : 'bg-primary text-primary-foreground rounded-br-md shadow-sm'
                    }`}
                  >
                    {msg.content}
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="px-5 py-3 border-t border-border/60 shrink-0">
            <form onSubmit={handleSendMessage} className="flex gap-2 max-w-2xl mx-auto">
              <Input
                placeholder="输入问题测试应用效果..."
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                className="flex-1 h-10 rounded-xl bg-muted/30 border-border/80"
              />
              <Button
                type="submit"
                size="icon"
                className="h-10 w-10 rounded-xl shrink-0 cursor-pointer"
                disabled={!chatInput.trim()}
              >
                <PlusCircle className="h-4 w-4" />
              </Button>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}
