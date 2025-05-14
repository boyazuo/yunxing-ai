'use client'

import { appConfigService } from '@/api/appConfig'
import { appService } from '@/api/apps'
import { chatService } from '@/api/chat'
import { conversationService } from '@/api/conversation'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { formatDate } from '@/lib/utils'
import type { App } from '@/types/app'
import { type Conversation, MessageRole } from '@/types/chat'
import {
  ArrowRight,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  FileText,
  Loader2,
  MessageSquare,
  Search,
  Send,
  Settings,
  User,
} from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useCallback, useEffect, useState } from 'react'

// 定义UI消息类型
interface ChatMessage {
  id: string
  role: MessageRole
  content: string
  time: string
}

// 定义内部应用配置类型
interface UIAppConfig {
  sysPrompt?: string
  models: {
    id: string
    modelId: string
    name: string
    provider: string
    temperature: number
    topP: number
    maxTokens: number
    isActive: boolean
  }[]
}

export default function AppsPage() {
  const { data: session } = useSession()
  const [userInput, setUserInput] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false)

  // 应用与会话状态
  const [apps, setApps] = useState<App[]>([])
  const [conversations, setConversations] = useState<Conversation[]>([])
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [activeApp, setActiveApp] = useState<App | null>(null)
  const [activeConversation, setActiveConversation] = useState<Conversation | null>(null)
  const [defaultModelId, setDefaultModelId] = useState<string>('')

  // 应用配置
  const [appConfig, setAppConfig] = useState<UIAppConfig | null>(null)
  const [selectedModelId, setSelectedModelId] = useState<string>('')

  // 加载状态
  const [loadingApps, setLoadingApps] = useState(true)
  const [loadingConfig, setLoadingConfig] = useState(false)
  const [loadingConversations, setLoadingConversations] = useState(false)
  const [loadingMessages, setLoadingMessages] = useState(false)

  // 从会话中获取用户和租户信息
  const tenantId = session?.tenant?.tenantId || ''
  const userId = session?.user?.userId?.toString() || ''

  // 加载应用列表
  const loadApps = useCallback(async () => {
    if (!tenantId) return

    try {
      setLoadingApps(true)
      const data = await appService.getApps(tenantId)
      setApps(data || [])

      // 如果有应用，默认选中第一个
      if (data && data.length > 0) {
        setActiveApp(data[0])
        loadAppConfig(data[0].appId)
        loadConversations(data[0].appId)
      }
    } catch (error) {
      console.error('加载应用失败', error)
    } finally {
      setLoadingApps(false)
    }
  }, [tenantId])

  // 加载应用配置
  const loadAppConfig = async (appId: string) => {
    if (!tenantId || !appId) return

    try {
      setLoadingConfig(true)
      const config = await appConfigService.getAppConfig(appId)

      // 处理 models 字符串转为 JSON 对象
      if (config && typeof config.models === 'string') {
        try {
          config.models = JSON.parse(config.models)
        } catch (parseError) {
          console.error('解析 models JSON 字符串失败', parseError)
          config.models = []
        }
      }

      setAppConfig(config as UIAppConfig)

      // 默认选择第一个模型
      if (config?.models && config.models.length > 0) {
        // 优先选择 isActive 为 true 的第一个模型
        const activeModel = config.models.find((model) => model.isActive) || config.models[0]
        setSelectedModelId(activeModel.modelId)
        setDefaultModelId(activeModel.modelId)
      }
    } catch (error) {
      console.error('加载应用配置失败', error)
    } finally {
      setLoadingConfig(false)
    }
  }

  // 加载会话列表
  const loadConversations = async (appId: string) => {
    if (!tenantId || !userId || !appId) return

    try {
      setLoadingConversations(true)
      const data = await conversationService.getConversations(tenantId, userId, appId)
      setConversations(data || [])

      // 如果有会话，默认选中第一个
      if (data && data.length > 0) {
        setActiveConversation(data[0])
        loadMessages(data[0].conversationId)
      } else {
        setActiveConversation(null)
        setMessages([])
      }
    } catch (error) {
      console.error('加载会话失败', error)
    } finally {
      setLoadingConversations(false)
    }
  }

  // 加载会话消息
  const loadMessages = async (conversationId: string) => {
    if (!conversationId) return

    try {
      setLoadingMessages(true)
      const data = await conversationService.getConversationMessages(conversationId)

      // 将后端消息格式转换为前端UI格式
      const formattedMessages: ChatMessage[] = []

      // 遍历消息，每条消息包含问题(user)和回答(assistant)
      for (const message of data) {
        // 添加用户问题
        formattedMessages.push({
          id: `${message.messageId}-q`,
          role: MessageRole.USER,
          content: message.question,
          time: formatTime(message.createTime),
        })

        // 添加助手回答
        formattedMessages.push({
          id: `${message.messageId}-a`,
          role: MessageRole.ASSISTANT,
          content: message.answer,
          time: formatTime(message.createTime),
        })
      }

      setMessages(formattedMessages)
    } catch (error) {
      console.error('加载消息失败', error)
    } finally {
      setLoadingMessages(false)
    }
  }

  // 首次加载
  useEffect(() => {
    if (tenantId && userId) {
      loadApps()
    }
  }, [tenantId, userId, loadApps])

  // 选择应用时加载会话
  const handleAppClick = (app: App) => {
    if (app.appId === activeApp?.appId) return
    setActiveApp(app)
    loadAppConfig(app.appId)
    loadConversations(app.appId)
  }

  // 选择会话时加载消息
  const handleConversationClick = (conversation: Conversation) => {
    if (conversation.conversationId === activeConversation?.conversationId) return
    setActiveConversation(conversation)
    loadMessages(conversation.conversationId)
  }

  // 选择模型
  const handleModelSelect = (modelId: string) => {
    setSelectedModelId(modelId)
  }

  // 发送消息
  const handleSendMessage = async () => {
    if (!userInput.trim() || !activeApp || !selectedModelId) return

    // 准备发送数据
    const question = userInput.trim()
    const now = new Date()
    const currentTime = formatTime(now.toISOString())

    // 添加用户消息到UI
    const tempUserId = `temp-${Date.now()}`
    setMessages((prev) => [
      ...prev,
      {
        id: tempUserId,
        role: MessageRole.USER,
        content: question,
        time: currentTime,
      },
    ])

    // 清空输入框和设置加载状态
    setUserInput('')
    setIsLoading(true)

    try {
      // 准备请求数据
      const chatRequest = {
        tenantId,
        userId,
        appId: activeApp.appId,
        conversationId: activeConversation?.conversationId,
        modelId: selectedModelId,
        question,
      }

      // 发送消息
      const response = await chatService.sendMessage(chatRequest)

      // 添加回复到UI
      setMessages((prev) => [
        ...prev,
        {
          id: `${response.messageId}-a`,
          role: MessageRole.ASSISTANT,
          content: response.content,
          time: formatTime(new Date().toISOString()),
        },
      ])

      // 如果是新会话，更新当前活动会话
      if (!activeConversation && response.conversationId) {
        // 刷新会话列表
        loadConversations(activeApp.appId)
      }
    } catch (error) {
      console.error('发送消息失败', error)
      // 可以在这里添加错误提示
    } finally {
      setIsLoading(false)
    }
  }

  // 切换侧边栏
  const toggleSidebar = () => {
    setIsSidebarCollapsed(!isSidebarCollapsed)
  }

  // 格式化时间
  const formatTime = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })
  }

  // 根据日期返回相对时间
  const getRelativeTime = (dateString: string) => {
    const now = new Date()
    const date = new Date(dateString)
    const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24))

    if (diffDays === 0) {
      const hours = now.getHours() - date.getHours()
      if (hours < 1) return '刚刚'
      return `${hours}小时前`
    }
    if (diffDays === 1) {
      return '昨天'
    }
    if (diffDays < 7) {
      return `${diffDays}天前`
    }
    return formatDate(date, 'yyyy-MM-dd')
  }

  // 获取当前选中的模型名称
  const getSelectedModelName = () => {
    if (!appConfig) return '选择模型'

    // 确保 models 是对象数组而非字符串
    let models = appConfig.models
    if (typeof models === 'string') {
      try {
        models = JSON.parse(models)
      } catch (error) {
        console.error('解析 models JSON 字符串失败', error)
        return '选择模型'
      }
    }

    if (!models || !Array.isArray(models) || models.length === 0) return '选择模型'

    const model = models.find((m) => m.modelId === selectedModelId)
    return model ? model.name : '选择模型'
  }

  return (
    <div className="flex h-[calc(100vh-60px)]">
      {/* 左侧应用列表 */}
      <div
        className={`border-r bg-card flex flex-col transition-all duration-300 ${isSidebarCollapsed ? 'w-16' : 'w-64'}`}
      >
        <div className="p-4 flex items-center justify-between border-b">
          {!isSidebarCollapsed && <h2 className="font-semibold text-foreground">应用列表</h2>}
          <Button
            size="icon"
            variant="ghost"
            className={`h-8 w-8 ${isSidebarCollapsed ? 'mx-auto' : 'ml-auto'}`}
            onClick={toggleSidebar}
          >
            {isSidebarCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
          </Button>
        </div>

        {!isSidebarCollapsed && (
          <div className="relative px-4 pt-4 pb-2">
            <Search className="absolute left-6.5 top-6.5 h-4 w-4 text-muted-foreground" />
            <Input placeholder="搜索应用..." className="pl-9" />
          </div>
        )}

        <nav className="overflow-auto flex-1 p-2">
          {loadingApps ? (
            <div className="flex justify-center items-center h-20">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : apps.length === 0 ? (
            <div className="text-center p-4 text-muted-foreground text-sm">暂无应用</div>
          ) : (
            apps.map((app) => (
              <button
                key={app.appId}
                type="button"
                className={`flex items-center rounded-md cursor-pointer transition-colors mb-1 ${
                  app.appId === activeApp?.appId ? 'bg-accent text-accent-foreground' : 'hover:bg-muted'
                } ${isSidebarCollapsed ? 'justify-center py-3 px-2' : 'px-3 py-2'}`}
                onClick={() => handleAppClick(app)}
                aria-pressed={app.appId === activeApp?.appId}
              >
                <div className={`text-xl flex-shrink-0 ${isSidebarCollapsed ? '' : 'mr-3'}`}>{app.logo || '🤖'}</div>
                {!isSidebarCollapsed && <div className="font-medium text-sm truncate">{app.appName}</div>}
              </button>
            ))
          )}
        </nav>
      </div>

      {/* 右侧聊天区域 */}
      <div className="flex-1 flex">
        {/* 会话历史 */}
        <div className="w-72 border-r flex flex-col bg-muted/10">
          <div className="p-4 border-b">
            <div className="flex items-center justify-between">
              <h2 className="font-semibold">会话历史</h2>
            </div>
          </div>

          <div className="space-y-1 overflow-auto flex-1 p-2">
            {loadingConversations ? (
              <div className="flex justify-center items-center h-20">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : !activeApp ? (
              <div className="text-center p-4 text-muted-foreground text-sm">请选择一个应用</div>
            ) : conversations.length === 0 ? (
              <div className="text-center p-4 text-muted-foreground text-sm">暂无会话记录</div>
            ) : (
              conversations.map((conversation) => (
                <button
                  key={conversation.conversationId}
                  type="button"
                  className={`px-3 py-2.5 rounded-md cursor-pointer transition-colors ${
                    conversation.conversationId === activeConversation?.conversationId
                      ? 'bg-primary text-primary-foreground'
                      : 'hover:bg-accent/50'
                  }`}
                  onClick={() => handleConversationClick(conversation)}
                  aria-pressed={conversation.conversationId === activeConversation?.conversationId}
                >
                  <div className="flex items-center justify-between">
                    <h3 className="font-medium text-sm truncate">{conversation.title}</h3>
                    <span
                      className={`text-xs ${
                        conversation.conversationId === activeConversation?.conversationId
                          ? 'text-primary-foreground/70'
                          : 'text-muted-foreground'
                      }`}
                    >
                      {getRelativeTime(conversation.updateTime)}
                    </span>
                  </div>
                  <p
                    className={`text-xs mt-1 line-clamp-1 ${
                      conversation.conversationId === activeConversation?.conversationId
                        ? 'text-primary-foreground/90'
                        : 'text-muted-foreground'
                    }`}
                  >
                    {conversation.lastMessage || '无消息记录'}
                  </p>
                </button>
              ))
            )}
          </div>
        </div>

        {/* 对话区域 */}
        <div className="flex-1 flex flex-col bg-background">
          {/* 对话头部 */}
          <div className="border-b p-4 flex items-center justify-between bg-card">
            <div className="flex items-center">
              <Avatar className="h-9 w-9 mr-3">
                <AvatarFallback>{activeApp?.logo || '🤖'}</AvatarFallback>
              </Avatar>
              <div>
                <h3 className="font-medium">{activeApp?.appName || '请选择应用'}</h3>
                <p className="text-xs text-muted-foreground flex items-center">
                  <span className="bg-green-500 h-1.5 w-1.5 rounded-full inline-block mr-1.5" />
                  在线
                </p>
              </div>
            </div>
            <div className="flex gap-2">
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline" size="sm" className="gap-1" disabled={!appConfig}>
                    {loadingConfig ? (
                      <Loader2 className="h-3.5 w-3.5 animate-spin" />
                    ) : (
                      <>
                        {getSelectedModelName()}
                        <ChevronDown className="h-3.5 w-3.5 ml-1" />
                      </>
                    )}
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  {(() => {
                    // 处理models可能是字符串的情况
                    let models = appConfig?.models
                    if (typeof models === 'string') {
                      try {
                        models = JSON.parse(models)
                      } catch (error) {
                        console.error('解析 models JSON 字符串失败', error)
                        return null
                      }
                    }

                    if (!models || !Array.isArray(models)) return null

                    return models.map((model) => (
                      <DropdownMenuItem
                        key={model.modelId}
                        onClick={() => handleModelSelect(model.modelId)}
                        className={selectedModelId === model.modelId ? 'bg-muted' : ''}
                      >
                        {model.name}
                      </DropdownMenuItem>
                    ))
                  })()}
                </DropdownMenuContent>
              </DropdownMenu>
              <Button variant="outline" size="icon" className="h-8 w-8">
                <Settings className="h-4 w-4" />
              </Button>
              <Button variant="outline" size="sm">
                <MessageSquare className="h-4 w-4 mr-2" />
                分享会话
              </Button>
            </div>
          </div>

          {/* 消息区域 */}
          <div className="flex-1 overflow-auto p-4 space-y-6">
            {loadingMessages ? (
              <div className="flex justify-center items-center h-20">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : !activeConversation && (!activeApp || messages.length === 0) ? (
              <div className="text-center p-4 text-muted-foreground">
                {activeApp ? '开始新的对话，或选择一个已有会话' : '请先选择一个应用'}
              </div>
            ) : messages.length === 0 ? (
              <div className="text-center p-4 text-muted-foreground">暂无消息记录</div>
            ) : (
              messages.map((message) => (
                <div
                  key={message.id}
                  className={`flex ${message.role === MessageRole.USER ? 'justify-end' : 'justify-start'}`}
                >
                  <div
                    className={`flex gap-3 max-w-[85%] ${message.role === MessageRole.USER ? 'flex-row-reverse' : ''}`}
                  >
                    <Avatar className={`h-8 w-8 ${message.role === MessageRole.USER ? 'mt-1' : ''}`}>
                      {message.role === MessageRole.USER ? (
                        <AvatarFallback className="bg-primary text-primary-foreground">
                          <User className="h-4 w-4" />
                        </AvatarFallback>
                      ) : (
                        <AvatarFallback className="bg-muted">{activeApp?.logo || '🤖'}</AvatarFallback>
                      )}
                    </Avatar>
                    <div>
                      <div
                        className={`px-4 py-3 rounded-lg ${
                          message.role === MessageRole.USER
                            ? 'bg-primary text-primary-foreground shadow-sm'
                            : 'bg-muted shadow-sm'
                        }`}
                      >
                        <div className="whitespace-pre-line text-sm">{message.content}</div>
                      </div>
                      <div
                        className={`text-xs mt-1 text-muted-foreground ${message.role === MessageRole.USER ? 'text-right' : ''}`}
                      >
                        {message.time}
                      </div>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* 输入区域 */}
          <div className="border-t p-4 bg-card">
            <div className="flex items-center gap-2 relative">
              <Textarea
                placeholder="输入消息..."
                className="flex-1 pr-24 min-h-[4.5rem] resize-none"
                value={userInput}
                onChange={(e) => setUserInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault()
                    handleSendMessage()
                  }
                }}
                disabled={!activeApp || isLoading}
              />
              <div className="absolute right-2 bottom-2 flex gap-1.5">
                <Button size="icon" variant="ghost" className="h-7 w-7 rounded-full">
                  <FileText className="h-4 w-4 text-muted-foreground" />
                </Button>
                <Button
                  variant="ghost"
                  className="rounded-full h-7 w-7"
                  onClick={handleSendMessage}
                  disabled={!userInput.trim() || isLoading || !activeApp || !selectedModelId}
                >
                  {isLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
                </Button>
              </div>
            </div>
            <div className="flex justify-between mt-2 text-xs">
              <div className="text-muted-foreground">支持 Markdown 格式</div>
              <div className="text-muted-foreground flex items-center hover:text-foreground cursor-pointer">
                <span>开启知识库增强</span>
                <ArrowRight className="h-3 w-3 ml-1" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
