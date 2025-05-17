'use client'

import { useSession } from 'next-auth/react'
import { useCallback, useRef, useState } from 'react'

import { appConfigService } from '@/api/appConfig'
import { ChatInterface, type ChatInterfaceHandle, type ChatModel } from '@/components/chat/chat-interface'
import type { App } from '@/types/app'
import type { Conversation } from '@/types/chat'
import { AppsSidebar, ConversationList } from './_components'
import type { ConversationListHandle } from './_components/conversation-list'

// 应用配置类型
interface UIAppConfig {
  sysPrompt?: string
  models: ChatModel[]
}

export default function HomePage() {
  const { data: session } = useSession()
  const tenantId = session?.tenant?.tenantId || ''
  const userId = session?.user?.userId || ''

  // 组件引用
  const conversationListRef = useRef<ConversationListHandle>(null)
  const chatInterfaceRef = useRef<ChatInterfaceHandle>(null)

  // UI状态
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false)

  // 应用状态
  const [activeApp, setActiveApp] = useState<App | null>(null)
  const [appConfig, setAppConfig] = useState<UIAppConfig | null>(null)
  const [loadingConfig, setLoadingConfig] = useState(false)

  // 会话状态
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null)

  // 模型状态
  const [selectedModelId, setSelectedModelId] = useState<string>('')
  const [defaultModelId, setDefaultModelId] = useState<string>('')

  // 加载应用配置
  const loadAppConfig = useCallback(
    async (appId: string) => {
      if (!tenantId || !appId) return

      try {
        setLoadingConfig(true)
        const config = await appConfigService.getAppConfig(appId)

        // 处理models字符串转为JSON对象
        const parsedConfig = { ...config }
        if (parsedConfig && typeof parsedConfig.models === 'string') {
          try {
            parsedConfig.models = JSON.parse(parsedConfig.models)
          } catch (parseError) {
            console.error('解析models JSON字符串失败', parseError)
            parsedConfig.models = []
          }
        }

        setAppConfig(parsedConfig as UIAppConfig)

        // 设置默认模型
        if (parsedConfig?.models?.length > 0) {
          const activeModel = parsedConfig.models.find((model) => model.isActive) || parsedConfig.models[0]
          setSelectedModelId(activeModel.modelId)
          setDefaultModelId(activeModel.modelId)
        }
      } catch (error) {
        console.error('加载应用配置失败', error)
      } finally {
        setLoadingConfig(false)
      }
    },
    [tenantId],
  )

  // 处理应用变更
  const handleAppChange = useCallback(
    (app: App) => {
      setActiveApp(app)
      loadAppConfig(app.appId)
      // 当切换应用时，清空当前会话
      setActiveConversationId(null)
      // 清空消息
      if (chatInterfaceRef.current) {
        chatInterfaceRef.current.cleanMessages()
      }
    },
    [loadAppConfig],
  )

  // 处理会话变更
  const handleConversationChange = useCallback((conversation: Conversation) => {
    setActiveConversationId(conversation.conversationId)
    if (chatInterfaceRef.current) {
      chatInterfaceRef.current.loadMessages(conversation.conversationId)
    }
  }, [])

  // 创建新会话
  const handleNewConversation = useCallback(() => {
    setActiveConversationId(null)
    // 清空消息
    if (chatInterfaceRef.current) {
      chatInterfaceRef.current.cleanMessages()
    }
  }, [])

  // 处理新会话创建
  const handleNewConversationCreated = useCallback(
    (conversationId: string) => {
      setActiveConversationId(conversationId)
      // 刷新会话列表
      if (conversationListRef.current && activeApp) {
        conversationListRef.current.loadConversations(activeApp.appId)
      }
    },
    [activeApp],
  )

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
        activeApp={activeApp}
        onAppChange={handleAppChange}
      />

      {/* 右侧聊天区域 */}
      <div className="flex-1 flex">
        {/* 会话历史 */}
        <ConversationList
          ref={conversationListRef}
          activeApp={activeApp}
          activeConversationId={activeConversationId}
          onConversationChange={handleConversationChange}
          onNewConversation={handleNewConversation}
        />

        {/* 聊天界面组件 */}
        <ChatInterface
          ref={chatInterfaceRef}
          activeApp={activeApp}
          models={appConfig?.models || []}
          defaultModelId={defaultModelId}
          hasActiveConversation={!!activeConversationId}
          activeConversationId={activeConversationId}
          userId={userId}
          onNewConversation={handleNewConversationCreated}
        />
      </div>
    </div>
  )
}
