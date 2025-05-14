'use client'

import { ChevronLeft, ChevronRight, Loader2, Plus, Search } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useCallback, useEffect, useState } from 'react'

import { appConfigService } from '@/api/appConfig'
import { appService } from '@/api/apps'
import { chatService } from '@/api/chat'
import { conversationService } from '@/api/conversation'
import { ChatInterface, type ChatMessage, type ChatModel } from '@/components/chat/chat-interface'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { formatDate } from '@/lib/utils'
import type { App } from '@/types/app'
import { type Conversation, MessageRole } from '@/types/chat'

// 定义内部应用配置类型
interface UIAppConfig {
  sysPrompt?: string
  models: ChatModel[]
}

export default function HomePage() {
  const { data: session } = useSession()
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
    if (diffDays === 1) return '昨天'
    if (diffDays < 7) return `${diffDays}天前`
    return formatDate(date, 'yyyy-MM-dd')
  }

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
      const parsedConfig = { ...config }
      if (parsedConfig && typeof parsedConfig.models === 'string') {
        try {
          parsedConfig.models = JSON.parse(parsedConfig.models)
        } catch (parseError) {
          console.error('解析 models JSON 字符串失败', parseError)
          parsedConfig.models = []
        }
      }

      setAppConfig(parsedConfig as UIAppConfig)

      // 默认选择第一个模型
      if (parsedConfig?.models && parsedConfig.models.length > 0) {
        // 优先选择 isActive 为 true 的第一个模型
        const activeModel = parsedConfig.models.find((model) => model.isActive) || parsedConfig.models[0]
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
      const formattedMessages: ChatMessage[] = data.flatMap((message) => [
        // 添加用户问题
        {
          id: `${message.messageId}-q`,
          role: MessageRole.USER,
          content: message.question,
          time: formatTime(message.createTime),
        },
        // 添加助手回答
        {
          id: `${message.messageId}-a`,
          role: MessageRole.ASSISTANT,
          content: message.answer,
          time: formatTime(message.createTime),
        },
      ])

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

  // 创建新会话
  const handleNewConversation = () => {
    if (!activeApp) return

    // 清空当前选中的会话和消息
    setActiveConversation(null)
    setMessages([])
  }

  // 发送消息
  const handleSendMessage = async (question: string, modelId: string) => {
    if (!question || !activeApp || !modelId || !tenantId || !userId) return

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

    try {
      // 准备请求数据
      const chatRequest = {
        appId: activeApp.appId,
        conversationId: activeConversation?.conversationId,
        modelId,
        prompt: question,
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
    }
  }

  // 切换侧边栏
  const toggleSidebar = () => {
    setIsSidebarCollapsed(!isSidebarCollapsed)
  }

  return (
    <div className="flex h-[calc(100vh-60px)]">
      {/* 左侧应用列表 */}
      <AppsSidebar
        isSidebarCollapsed={isSidebarCollapsed}
        toggleSidebar={toggleSidebar}
        apps={apps}
        loadingApps={loadingApps}
        activeApp={activeApp}
        handleAppClick={handleAppClick}
      />

      {/* 右侧聊天区域 */}
      <div className="flex-1 flex">
        {/* 会话历史 */}
        <ConversationList
          conversations={conversations}
          loadingConversations={loadingConversations}
          activeApp={activeApp}
          activeConversation={activeConversation}
          handleConversationClick={handleConversationClick}
          handleNewConversation={handleNewConversation}
          getRelativeTime={getRelativeTime}
        />

        {/* 聊天界面组件 */}
        <ChatInterface
          activeApp={activeApp}
          messages={messages}
          loadingMessages={loadingMessages}
          onSendMessage={handleSendMessage}
          models={appConfig?.models || []}
          defaultModelId={defaultModelId}
          hasActiveConversation={!!activeConversation}
        />
      </div>
    </div>
  )
}

// 应用侧边栏组件
interface AppsSidebarProps {
  isSidebarCollapsed: boolean
  toggleSidebar: () => void
  apps: App[]
  loadingApps: boolean
  activeApp: App | null
  handleAppClick: (app: App) => void
}

function AppsSidebar({
  isSidebarCollapsed,
  toggleSidebar,
  apps,
  loadingApps,
  activeApp,
  handleAppClick,
}: AppsSidebarProps) {
  return (
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
  )
}

// 会话列表组件
interface ConversationListProps {
  conversations: Conversation[]
  loadingConversations: boolean
  activeApp: App | null
  activeConversation: Conversation | null
  handleConversationClick: (conversation: Conversation) => void
  handleNewConversation: () => void
  getRelativeTime: (dateString: string) => string
}

function ConversationList({
  conversations,
  loadingConversations,
  activeApp,
  activeConversation,
  handleConversationClick,
  handleNewConversation,
  getRelativeTime,
}: ConversationListProps) {
  return (
    <div className="w-72 border-r flex flex-col bg-muted/10">
      <div className="p-4 border-b">
        <div className="flex items-center justify-between">
          <h2 className="font-semibold">会话历史</h2>
          <Button
            size="sm"
            variant="outline"
            className="h-8 px-2"
            disabled={!activeApp}
            onClick={handleNewConversation}
          >
            <Plus className="h-4 w-4 mr-1" />
            新建会话
          </Button>
        </div>
      </div>

      <div className="flex-1 overflow-auto">
        <ul className="space-y-1 p-2">
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
              <li key={conversation.conversationId}>
                <button
                  type="button"
                  className={`w-full px-3 py-2.5 rounded-md cursor-pointer transition-colors ${
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
              </li>
            ))
          )}
        </ul>
      </div>
    </div>
  )
}
