'use client'

import { ChevronLeft, ChevronRight, Loader2, PanelLeftClose, PanelLeftOpen, Plus } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { forwardRef, useCallback, useEffect, useImperativeHandle, useRef, useState } from 'react'

import { appService } from '@/api/apps'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import type { App } from '@/types/app'
import type { Conversation } from '@/types/chat'
import { ConversationList, type ConversationListHandle } from './conversation-list'

export interface HomeSidebarHandle {
  loadConversations: (appId: string) => Promise<void>
}

interface HomeSidebarProps {
  isCollapsed: boolean
  onToggleCollapse: () => void
  activeApp: App | null
  activeConversationId: string | null
  onAppChange: (app: App) => void
  onConversationChange: (conversation: Conversation) => void
  onNewConversation: () => void
}

export const HomeSidebar = forwardRef<HomeSidebarHandle, HomeSidebarProps>(
  (
    {
      isCollapsed,
      onToggleCollapse,
      activeApp,
      activeConversationId,
      onAppChange,
      onConversationChange,
      onNewConversation,
    },
    ref,
  ) => {
    const { data: session } = useSession()
    const tenantId = session?.tenant?.tenantId || ''
    const conversationListRef = useRef<ConversationListHandle>(null)

    const [apps, setApps] = useState<App[]>([])
    const [loadingApps, setLoadingApps] = useState(true)
    const [isHistoryCollapsed, setIsHistoryCollapsed] = useState(false)
    const [isHistoryHovered, setIsHistoryHovered] = useState(false)

    const loadApps = useCallback(async () => {
      if (!tenantId) return
      try {
        setLoadingApps(true)
        const data = await appService.getApps(String(tenantId))
        setApps(data || [])
        if (data?.length > 0 && !activeApp) {
          onAppChange(data[0])
        }
      } catch (error) {
        console.error('加载应用失败', error)
      } finally {
        setLoadingApps(false)
      }
    }, [tenantId, activeApp, onAppChange])

    useEffect(() => {
      if (tenantId) loadApps()
    }, [tenantId, loadApps])

    useImperativeHandle(
      ref,
      () => ({
        loadConversations: (appId: string) => conversationListRef.current?.loadConversations(appId) ?? Promise.resolve(),
      }),
      [],
    )

    const handleAppSelect = (app: App) => {
      if (app.appId !== activeApp?.appId) {
        onAppChange(app)
      }
    }

    return (
      <aside
        className={cn(
          'border-r border-border/50 bg-sidebar/90 flex flex-col transition-[width] duration-300 ease-out shrink-0',
          isCollapsed ? 'w-12' : 'w-[260px]',
        )}
      >
        {isCollapsed ? (
          <div className="flex flex-col items-center py-2 gap-1 h-full">
            <Button
              size="icon"
              variant="ghost"
              className="h-8 w-8 text-muted-foreground hover:text-foreground cursor-pointer"
              onClick={onToggleCollapse}
              aria-label="展开侧边栏"
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
            <Button
              size="icon"
              variant="ghost"
              className="h-8 w-8 text-muted-foreground hover:text-foreground cursor-pointer"
              disabled={!activeApp}
              onClick={onNewConversation}
              title="新建会话"
            >
              <Plus className="h-4 w-4" />
            </Button>
            {activeApp && (
              <button
                type="button"
                title={activeApp.appName}
                className="mt-1 flex h-8 w-8 items-center justify-center rounded-lg border border-border/50 text-xs font-medium cursor-pointer hover:bg-muted/60 transition-colors duration-200"
                style={{ backgroundColor: activeApp.logoBackground || undefined }}
                onClick={onToggleCollapse}
              >
                {activeApp.logo || activeApp.appName.slice(0, 1).toUpperCase()}
              </button>
            )}
          </div>
        ) : (
          <>
            {/* 顶部：新建会话 */}
            <div className="px-3 pt-3 pb-2 shrink-0">
              <button
                type="button"
                disabled={!activeApp}
                onClick={onNewConversation}
                className={cn(
                  'w-full flex items-center justify-center gap-2 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer',
                  'bg-muted/70 hover:bg-muted border border-border/60 text-foreground',
                  'disabled:opacity-40 disabled:pointer-events-none',
                  !activeConversationId && activeApp && 'ring-1 ring-border/80 bg-muted',
                )}
              >
                <Plus className="h-4 w-4 shrink-0" />
                <span>新建会话</span>
              </button>
            </div>

            {/* 应用列表 */}
            <div className="px-2 pb-2 shrink-0">
              <p className="px-2 pb-1.5 text-[11px] font-medium uppercase tracking-wider text-muted-foreground/70">
                应用
              </p>
              {loadingApps ? (
                <div className="flex justify-center py-4">
                  <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
                </div>
              ) : apps.length === 0 ? (
                <p className="px-2 py-3 text-xs text-muted-foreground text-center">暂无应用</p>
              ) : (
                <ul className="space-y-0.5">
                  {apps.map((app) => {
                    const isActive = app.appId === activeApp?.appId
                    return (
                      <li key={app.appId}>
                        <button
                          type="button"
                          onClick={() => handleAppSelect(app)}
                          className={cn(
                            'w-full flex items-center gap-2.5 px-2.5 py-2 rounded-lg text-sm transition-colors duration-200 cursor-pointer',
                            isActive
                              ? 'bg-muted text-foreground font-medium'
                              : 'text-foreground/75 hover:bg-muted/50 hover:text-foreground',
                          )}
                        >
                          <span
                            className="h-7 w-7 rounded-lg flex items-center justify-center text-[11px] font-semibold border border-border/40 shrink-0"
                            style={{ backgroundColor: app.logoBackground || undefined }}
                          >
                            {app.logo || app.appName.slice(0, 1).toUpperCase()}
                          </span>
                          <span className="truncate">{app.appName}</span>
                        </button>
                      </li>
                    )
                  })}
                </ul>
              )}
            </div>

            <div className="mx-3 border-t border-border/40 shrink-0" />

            {/* 历史会话 — 悬浮显示折叠按钮 */}
            <div
              className="flex flex-col flex-1 min-h-0 pt-2"
              onMouseEnter={() => setIsHistoryHovered(true)}
              onMouseLeave={() => setIsHistoryHovered(false)}
            >
              <div className="flex items-center justify-between px-3 pb-1.5 shrink-0 group/history">
                <p className="text-[11px] font-medium uppercase tracking-wider text-muted-foreground/70">历史</p>
                <button
                  type="button"
                  onClick={() => setIsHistoryCollapsed((v) => !v)}
                  className={cn(
                    'h-6 w-6 flex items-center justify-center rounded-md text-muted-foreground hover:text-foreground hover:bg-muted/60 transition-all duration-200 cursor-pointer',
                    isHistoryHovered || isHistoryCollapsed ? 'opacity-100' : 'opacity-0 pointer-events-none',
                  )}
                  aria-label={isHistoryCollapsed ? '展开历史会话' : '收起历史会话'}
                  title={isHistoryCollapsed ? '展开历史' : '收起历史'}
                >
                  {isHistoryCollapsed ? (
                    <PanelLeftOpen className="h-3.5 w-3.5" />
                  ) : (
                    <PanelLeftClose className="h-3.5 w-3.5" />
                  )}
                </button>
              </div>

              {!isHistoryCollapsed && (
                <ConversationList
                  ref={conversationListRef}
                  activeApp={activeApp}
                  activeConversationId={activeConversationId}
                  onConversationChange={onConversationChange}
                  onNewConversation={onNewConversation}
                  className="flex-1 min-h-0"
                />
              )}

              {isHistoryCollapsed && (
                <button
                  type="button"
                  onClick={() => setIsHistoryCollapsed(false)}
                  className="mx-3 mt-1 px-2 py-2 text-xs text-muted-foreground hover:text-foreground hover:bg-muted/50 rounded-lg transition-colors duration-200 cursor-pointer text-left"
                >
                  点击展开历史会话
                </button>
              )}
            </div>

            <div className="px-3 py-2 border-t border-border/40 shrink-0">
              <Button
                variant="ghost"
                size="sm"
                className="w-full justify-start gap-2 h-8 text-xs text-muted-foreground hover:text-foreground cursor-pointer"
                onClick={onToggleCollapse}
              >
                <ChevronLeft className="h-3.5 w-3.5" />
                收起侧边栏
              </Button>
            </div>
          </>
        )}
      </aside>
    )
  },
)

HomeSidebar.displayName = 'HomeSidebar'
