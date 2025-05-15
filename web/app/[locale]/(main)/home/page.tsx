'use client'

import { appConfigService } from '@/api/appConfig'
import { appService } from '@/api/apps'
import { chatService } from '@/api/chat'
import { conversationService } from '@/api/conversation'
import { ChatInterface, type ChatMessage, type ChatModel } from '@/components/chat/chat-interface'
import type { App } from '@/types/app'
import { type Conversation, MessageRole } from '@/types/chat'
import { useSession } from 'next-auth/react'
import { useCallback, useEffect, useState } from 'react'
import { AppsSidebar, ConversationList } from './_components'

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
  const userId = session?.user?.userId || ''

  // 格式化时间
  const formatTime = useCallback((dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })
  }, [])

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

  // 加载会话消息
  const loadMessages = useCallback(
    async (conversationId: string) => {
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
    },
    [formatTime],
  )

  // 加载会话列表
  const loadConversations = useCallback(
    async (appId: string) => {
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
    },
    [tenantId, userId, loadMessages],
  )

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

  // 刷新会话列表
  const refreshConversations = useCallback(() => {
    if (activeApp) {
      loadConversations(activeApp.appId)
    }
  }, [activeApp, loadConversations])

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
          refreshConversations={refreshConversations}
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
