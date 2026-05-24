'use client'

import { useSession } from 'next-auth/react'
import { useCallback, useRef, useState } from 'react'

import { appConfigService } from '@/api/appConfig'
import { ChatInterface, type ChatInterfaceHandle } from '@/components/chat/chat-interface'
import type { App } from '@/types/app'
import type { Conversation } from '@/types/chat'
import { HomeSidebar, type HomeSidebarHandle } from './_components/home-sidebar'

export default function HomePage() {
  const { data: session } = useSession()
  const tenantId = session?.tenant?.tenantId || ''
  const userId = session?.user?.userId || ''

  const sidebarRef = useRef<HomeSidebarHandle>(null)
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
      chatInterfaceRef.current?.cleanMessages()
    },
    [loadAppConfig],
  )

  const handleConversationChange = useCallback((conversation: Conversation) => {
    setActiveConversationId(conversation.conversationId)
    chatInterfaceRef.current?.loadMessages(conversation.conversationId)
  }, [])

  const handleNewConversation = useCallback(() => {
    setActiveConversationId(null)
    chatInterfaceRef.current?.cleanMessages()
  }, [])

  const handleNewConversationCreated = useCallback(
    (conversationId: string) => {
      setActiveConversationId(conversationId)
      if (sidebarRef.current && activeApp) {
        sidebarRef.current.loadConversations(activeApp.appId)
      }
    },
    [activeApp],
  )

  return (
    <div className="flex h-[calc(100vh-56px)] bg-background overflow-hidden">
      <HomeSidebar
        ref={sidebarRef}
        isCollapsed={isSidebarCollapsed}
        onToggleCollapse={() => setIsSidebarCollapsed((v) => !v)}
        activeApp={activeApp}
        activeConversationId={activeConversationId}
        onAppChange={handleAppChange}
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
        className="min-w-0"
      />
    </div>
  )
}
