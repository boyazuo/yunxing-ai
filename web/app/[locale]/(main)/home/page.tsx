'use client'

import { useSession } from 'next-auth/react'
import { useCallback, useRef, useState } from 'react'

import { appConfigService } from '@/api/appConfig'
import { ChatInterface, type ChatInterfaceHandle } from '@/components/chat/chat-interface'
import type { App } from '@/types/app'
import type { Conversation } from '@/types/chat'
import { AppsSidebar, ConversationList } from './_components'
import type { ConversationListHandle } from './_components/conversation-list'

export default function HomePage() {
  const { data: session } = useSession()
  const tenantId = session?.tenant?.tenantId || ''
  const userId = session?.user?.userId || ''

  const conversationListRef = useRef<ConversationListHandle>(null)
  const chatInterfaceRef = useRef<ChatInterfaceHandle>(null)

  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false)
  const [activeApp, setActiveApp] = useState<App | null>(null)
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null)

  const loadAppConfig = useCallback(
    async (appId: string) => {
      if (!tenantId || !appId) return
      try {
        await appConfigService.getAppConfig(appId)
      } catch (error) {
        console.error('加载应用配置失败', error)
      }
    },
    [tenantId],
  )

  const handleAppChange = useCallback(
    (app: App) => {
      setActiveApp(app)
      loadAppConfig(app.appId)
      setActiveConversationId(null)
      if (chatInterfaceRef.current) {
        chatInterfaceRef.current.cleanMessages()
      }
    },
    [loadAppConfig],
  )

  const handleConversationChange = useCallback((conversation: Conversation) => {
    setActiveConversationId(conversation.conversationId)
    if (chatInterfaceRef.current) {
      chatInterfaceRef.current.loadMessages(conversation.conversationId)
    }
  }, [])

  const handleNewConversation = useCallback(() => {
    setActiveConversationId(null)
    if (chatInterfaceRef.current) {
      chatInterfaceRef.current.cleanMessages()
    }
  }, [])

  const handleNewConversationCreated = useCallback(
    (conversationId: string) => {
      setActiveConversationId(conversationId)
      if (conversationListRef.current && activeApp) {
        conversationListRef.current.loadConversations(activeApp.appId)
      }
    },
    [activeApp],
  )

  const toggleSidebar = () => {
    setIsSidebarCollapsed(!isSidebarCollapsed)
  }

  return (
    <div className="flex h-[calc(100vh-60px)]">
      <AppsSidebar
        isSidebarCollapsed={isSidebarCollapsed}
        toggleSidebar={toggleSidebar}
        activeApp={activeApp}
        onAppChange={handleAppChange}
      />

      <div className="flex-1 flex">
        <ConversationList
          ref={conversationListRef}
          activeApp={activeApp}
          activeConversationId={activeConversationId}
          onConversationChange={handleConversationChange}
          onNewConversation={handleNewConversation}
        />

        <ChatInterface
          ref={chatInterfaceRef}
          activeApp={activeApp}
          hasActiveConversation={!!activeConversationId}
          activeConversationId={activeConversationId}
          userId={userId}
          onNewConversation={handleNewConversationCreated}
        />
      </div>
    </div>
  )
}
