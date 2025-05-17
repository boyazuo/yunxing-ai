'use client'

import { useSession } from 'next-auth/react'
import { useCallback, useEffect, useState } from 'react'

import { appConfigService } from '@/api/appConfig'
import { appService } from '@/api/apps'
import { chatService } from '@/api/chat'
import { conversationService } from '@/api/conversation'
import { ChatInterface, type ChatMessage, type ChatModel } from '@/components/chat/chat-interface'
import type { App } from '@/types/app'
import { type Conversation, MessageRole } from '@/types/chat'
import { AppsSidebar, ConversationList } from './_components'

// 应用配置类型
interface UIAppConfig {
  sysPrompt?: string
  models: ChatModel[]
}

export default function HomePage() {
  const { data: session } = useSession()
  const tenantId = session?.tenant?.tenantId || ''
  const userId = session?.user?.userId || ''

  // UI状态
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false)

  // 应用状态
  const [apps, setApps] = useState<App[]>([])
  const [activeApp, setActiveApp] = useState<App | null>(null)
  const [appConfig, setAppConfig] = useState<UIAppConfig | null>(null)
  const [loadingApps, setLoadingApps] = useState(true)
  const [loadingConfig, setLoadingConfig] = useState(false)

  // 会话状态
  const [conversations, setConversations] = useState<Conversation[]>([])
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null)
  const [loadingConversations, setLoadingConversations] = useState(false)

  // 消息状态
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [loadingMessages, setLoadingMessages] = useState(false)

  // 模型状态
  const [selectedModelId, setSelectedModelId] = useState<string>('')
  const [defaultModelId, setDefaultModelId] = useState<string>('')

  // 格式化时间
  const formatTime = useCallback((dateString: string) => {
    return new Date(dateString).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })
  }, [])

  // 加载应用列表
  const loadApps = useCallback(async () => {
    if (!tenantId) return

    try {
      setLoadingApps(true)
      const data = await appService.getApps(String(tenantId))
      setApps(data || [])

      if (data?.length > 0) {
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
  }

  // 加载会话列表
  const loadConversations = useCallback(
    async (appId: string) => {
      if (!userId || !appId) return
      try {
        setLoadingConversations(true)
        const data = await conversationService.getConversations(userId, appId)
        setConversations(data || [])
      } catch (error) {
        console.error('加载会话失败', error)
      } finally {
        setLoadingConversations(false)
      }
    },
    [userId],
  )

  // 加载会话消息
  const loadMessages = useCallback(
    async (conversationId: string) => {
      if (!conversationId) return

      try {
        setLoadingMessages(true)
        const data = await conversationService.getConversationMessages(conversationId)

        // 将后端消息格式转换为前端UI格式
        const formattedMessages: ChatMessage[] = data.flatMap((message) => [
          {
            id: `${message.messageId}-q`,
            role: MessageRole.USER,
            content: message.question,
            time: formatTime(message.createTime),
          },
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

  // 首次加载
  useEffect(() => {
    if (tenantId && userId) {
      loadApps()
    }
  }, [tenantId, userId, loadApps])

  // 选择应用
  const handleAppClick = (app: App) => {
    if (app.appId === activeApp?.appId) return
    setActiveApp(app)
    loadAppConfig(app.appId)
    loadConversations(app.appId)
  }

  // 选择会话
  const handleConversationClick = (conversation: Conversation) => {
    if (conversation.conversationId === activeConversationId) return
    setActiveConversationId(conversation.conversationId)
    loadMessages(conversation.conversationId)
  }

  // 创建新会话
  const handleNewConversation = () => {
    if (!activeApp) return
    setActiveConversationId(null)
    setMessages([])
  }

  // 发送消息
  const handleSendMessage = async (question: string, modelId: string) => {
    if (!question || !activeApp || !modelId || !tenantId || !userId) return

    const currentTime = formatTime(new Date().toISOString())
    const tempUserId = `temp-${Date.now()}`
    const tempAssistantId = `temp-assistant-${Date.now()}`

    // 添加用户消息到UI
    setMessages((prev) => [
      ...prev,
      {
        id: tempUserId,
        role: MessageRole.USER,
        content: question,
        time: currentTime,
      },
      {
        id: tempAssistantId,
        role: MessageRole.ASSISTANT,
        content: '',
        time: currentTime,
      },
    ])

    try {
      // 准备请求数据
      const chatRequest = {
        appId: activeApp.appId,
        conversationId: activeConversationId,
        modelId,
        prompt: question,
      }

      // 创建处理流式消息的回调
      let conversationId = activeConversationId
      let messageId = ''
      let messageContent = ''

      await chatService.streamMessageWithHandling(chatRequest, {
        onMessage: (event) => {
          try {
            const data = event.data
            if (event.name === 'metadata') {
              try {
                const metaData = JSON.parse(data)
                if (metaData.conversationId) {
                  conversationId = metaData.conversationId
                }
                if (metaData.messageId) {
                  messageId = metaData.messageId
                }
                if (!activeConversationId) {
                  loadConversations(activeApp.appId)
                  setActiveConversationId(conversationId)
                }
              } catch (jsonError) {
                console.error('解析metadata JSON失败:', jsonError, data)
              }
            } else if (event.name === 'end') {
              return
            } else {
              messageContent += data
            }
            setMessages((prev) => {
              const updated = [...prev]
              const assistantMessageIndex = updated.findIndex((msg) => msg.id === tempAssistantId)
              if (assistantMessageIndex !== -1) {
                updated[assistantMessageIndex] = {
                  ...updated[assistantMessageIndex],
                  content: messageContent,
                }
              }
              return updated
            })
          } catch (error) {
            console.error('处理消息数据失败', error)
          }
        },
        onError: (error) => {
          console.error('处理流数据失败', error)
          // 显示错误消息
          setMessages((prev) => {
            const updated = [...prev]
            const assistantMessageIndex = updated.findIndex((msg) => msg.id === tempAssistantId)
            if (assistantMessageIndex !== -1) {
              updated[assistantMessageIndex] = {
                ...updated[assistantMessageIndex],
                content: `抱歉，${error.message || '请求发送失败，请重试。'}`,
              }
            }
            return updated
          })
        },
        onComplete: () => {
          try {
            if (conversationId && messageId) {
              // 更新消息的ID为真实ID
              setMessages((prev) => {
                const updated = [...prev]
                const userMsgIndex = updated.findIndex((msg) => msg.id === tempUserId)
                const assistantMsgIndex = updated.findIndex((msg) => msg.id === tempAssistantId)

                if (userMsgIndex !== -1) {
                  updated[userMsgIndex] = {
                    ...updated[userMsgIndex],
                    id: `${messageId}-q`,
                  }
                }

                if (assistantMsgIndex !== -1) {
                  updated[assistantMsgIndex] = {
                    ...updated[assistantMsgIndex],
                    id: `${messageId}-a`,
                  }
                }

                return updated
              })

              // // 刷新会话列表
              // loadConversations(activeApp.appId)
            }
          } catch (completeError) {
            console.error('流完成处理失败', completeError)
            // 不需要向用户显示此错误，因为内容已经显示出来了
          }
        },
      })
    } catch (error) {
      console.error('发送消息失败', error)

      // 显示错误消息
      setMessages((prev) => {
        const updated = [...prev]
        const assistantMessageIndex = updated.findIndex((msg) => msg.id === tempAssistantId)
        if (assistantMessageIndex !== -1) {
          updated[assistantMessageIndex] = {
            ...updated[assistantMessageIndex],
            content: '抱歉，请求发送失败，请重试。',
          }
        }
        return updated
      })
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
          activeConversationId={activeConversationId}
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
          hasActiveConversation={!!activeConversationId}
        />
      </div>
    </div>
  )
}
