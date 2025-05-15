'use client'

import { ChevronLeft, ChevronRight, Loader2 } from 'lucide-react'

import { Button } from '@/components/ui/button'
import type { App } from '@/types/app'

// 应用侧边栏组件
interface AppsSidebarProps {
  isSidebarCollapsed: boolean
  toggleSidebar: () => void
  apps: App[]
  loadingApps: boolean
  activeApp: App | null
  handleAppClick: (app: App) => void
}

export function AppsSidebar({
  isSidebarCollapsed,
  toggleSidebar,
  apps,
  loadingApps,
  activeApp,
  handleAppClick,
}: AppsSidebarProps) {
  return (
    <div
      className={`border-r bg-card flex flex-col transition-all duration-300 ${isSidebarCollapsed ? 'w-16' : 'w-60'}`}
    >
      <div className="p-4 flex items-center justify-between border-b">
        <h2
          className={`font-semibold text-foreground whitespace-nowrap overflow-hidden transition-all duration-300 ${isSidebarCollapsed ? 'w-0 opacity-0' : 'w-32 opacity-100'}`}
        >
          应用列表
        </h2>
        <Button
          size="icon"
          variant="ghost"
          className={`h-8 w-8 transition-all duration-300 ${isSidebarCollapsed ? 'mx-auto' : 'ml-auto'}`}
          onClick={toggleSidebar}
        >
          {isSidebarCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
        </Button>
      </div>

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
