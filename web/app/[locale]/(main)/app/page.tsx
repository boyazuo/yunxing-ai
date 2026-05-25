'use client'

import { appService } from '@/api/apps'
import { AppFormDialog } from '@/app/[locale]/(main)/app/_components/AppFormDialog'
import { ConfirmDialog } from '@/components/blocks/confirm-dialog'
import { RowActionsTrigger } from '@/components/blocks/row-actions'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import type { App } from '@/types/app'
import { AppType } from '@/types/app'
import { Bot, Edit, GitBranch, Layers, MessageSquare, Plus, Search, Trash, User } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useRouter } from 'next/navigation'
import { useCallback, useEffect, useState, useTransition } from 'react'

const APP_TYPE_ACCENTS: Record<
  AppType,
  { gradient: string; bg: string; text: string; glow: string; label: string; Icon: typeof MessageSquare }
> = {
  [AppType.CHAT]: {
    gradient: 'from-indigo-500 to-violet-500',
    bg: 'bg-indigo-50 dark:bg-indigo-950/60',
    text: 'text-indigo-600 dark:text-indigo-300',
    glow: 'hover:shadow-indigo-100 dark:hover:shadow-indigo-900/30',
    label: '对话应用',
    Icon: MessageSquare,
  },
  [AppType.AGENT]: {
    gradient: 'from-violet-500 to-purple-500',
    bg: 'bg-violet-50 dark:bg-violet-950/60',
    text: 'text-violet-600 dark:text-violet-300',
    glow: 'hover:shadow-violet-100 dark:hover:shadow-violet-900/30',
    label: '智能体',
    Icon: Bot,
  },
  [AppType.WORKFLOW]: {
    gradient: 'from-teal-500 to-emerald-600',
    bg: 'bg-teal-50 dark:bg-teal-950/60',
    text: 'text-teal-600 dark:text-teal-300',
    glow: 'hover:shadow-teal-100 dark:hover:shadow-teal-900/30',
    label: '工作流',
    Icon: GitBranch,
  },
}

function getAccent(appId: string) {
  const CARD_ACCENTS = [
    { gradient: 'from-indigo-500 to-violet-500', bg: 'bg-indigo-50 dark:bg-indigo-950/60', text: 'text-indigo-600 dark:text-indigo-300', glow: 'hover:shadow-indigo-100 dark:hover:shadow-indigo-900/30' },
    { gradient: 'from-sky-500 to-cyan-500', bg: 'bg-sky-50 dark:bg-sky-950/60', text: 'text-sky-600 dark:text-sky-300', glow: 'hover:shadow-sky-100 dark:hover:shadow-sky-900/30' },
    { gradient: 'from-rose-500 to-pink-500', bg: 'bg-rose-50 dark:bg-rose-950/60', text: 'text-rose-600 dark:text-rose-300', glow: 'hover:shadow-rose-100 dark:hover:shadow-rose-900/30' },
    { gradient: 'from-amber-500 to-orange-500', bg: 'bg-amber-50 dark:bg-amber-950/60', text: 'text-amber-600 dark:text-amber-300', glow: 'hover:shadow-amber-100 dark:hover:shadow-amber-900/30' },
  ]
  let hash = 0
  for (let i = 0; i < appId.length; i++) {
    hash = (hash * 31 + appId.charCodeAt(i)) & 0xffffffff
  }
  return CARD_ACCENTS[Math.abs(hash) % CARD_ACCENTS.length]
}

function formatUpdateTime(updateTime?: string) {
  if (!updateTime) return '—'
  try {
    const date = new Date(updateTime)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMins / 60)
    const diffDays = Math.floor(diffHours / 24)
    if (diffMins < 1) return '刚刚'
    if (diffMins < 60) return `${diffMins} 分钟前`
    if (diffHours < 24) return `${diffHours} 小时前`
    if (diffDays < 7) return `${diffDays} 天前`
    const m = (date.getMonth() + 1).toString().padStart(2, '0')
    const d = date.getDate().toString().padStart(2, '0')
    return `${m}-${d}`
  } catch {
    return '—'
  }
}

export default function SpacePage() {
  const router = useRouter()
  const { data: session, status } = useSession()
  const [apps, setApps] = useState<App[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [typeFilter, setTypeFilter] = useState('all')
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [currentApp, setCurrentApp] = useState<App | undefined>(undefined)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [appToDelete, setAppToDelete] = useState<App | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)
  const [, startTransition] = useTransition()

  const tenantId = session?.tenant?.tenantId || ''

  const loadApps = useCallback(async () => {
    try {
      if (!tenantId) {
        setIsLoading(false)
        return
      }
      setIsLoading(true)
      const data = await appService.getApps(tenantId.toString())
      setApps(data || [])
    } catch (error) {
      console.error('加载应用失败', error)
    } finally {
      setIsLoading(false)
    }
  }, [tenantId])

  useEffect(() => {
    if (status === 'authenticated' && tenantId) {
      loadApps()
    }
  }, [status, tenantId, loadApps])

  const handleCreateApp = () => {
    setCurrentApp(undefined)
    setIsFormOpen(true)
  }

  const handleEditApp = (app: App) => {
    setCurrentApp(app)
    setIsFormOpen(true)
  }

  const confirmDeleteApp = (app: App) => {
    startTransition(() => {
      setAppToDelete(app)
      setDeleteDialogOpen(true)
    })
  }

  const handleDeleteApp = async () => {
    try {
      if (!appToDelete) return
      setIsDeleting(true)
      await appService.deleteApp(appToDelete.appId)
      setDeleteDialogOpen(false)
      setAppToDelete(null)
      loadApps()
    } catch (error) {
      console.error('删除应用失败', error)
    } finally {
      setIsDeleting(false)
    }
  }

  const filteredApps = apps.filter((app) => {
    const searchMatch = searchTerm ? app.appName.toLowerCase().includes(searchTerm.toLowerCase()) : true
    const typeMatch = typeFilter === 'all' ? true : app.type.toString() === typeFilter
    return searchMatch && typeMatch
  })

  const navigateToAppConfig = (appId: string) => {
    router.push(`/app/${appId}/config`)
  }

  return (
    <div className="min-h-[calc(100vh-56px)]">
      {/* Hero header */}
      <div className="relative border-b bg-gradient-to-b from-primary/[0.04] to-background overflow-hidden">
        <div className="dot-pattern absolute inset-0 opacity-60" />
        <div className="relative container mx-auto px-6 py-10">
          <div className="flex items-end justify-between gap-6">
            <div className="space-y-2 animate-slide-up-fade">
              <div className="flex items-center gap-2.5">
                <div className="h-9 w-9 rounded-xl bg-primary/10 border border-primary/20 flex items-center justify-center">
                  <Layers className="h-[18px] w-[18px] text-primary" />
                </div>
                <span className="text-xs font-semibold tracking-widest text-primary/70 uppercase">Workspace</span>
              </div>
              <h1 className="text-3xl font-bold tracking-tight text-foreground">工作空间</h1>
              <p className="text-sm text-muted-foreground">
                构建、配置和管理你的 AI 应用
                {!isLoading && apps.length > 0 && (
                  <span className="ml-2 inline-flex items-center gap-1 rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
                    {apps.length} 个应用
                  </span>
                )}
              </p>
            </div>
            <div className="animate-slide-up-fade stagger-2">
              <Button
                onClick={handleCreateApp}
                size="lg"
                className="gap-2 shadow-md shadow-primary/20 hover:shadow-lg hover:shadow-primary/25 transition-all duration-200 cursor-pointer"
              >
                <Plus className="h-4 w-4" />
                新建应用
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Main content */}
      <div className="container mx-auto px-6 py-8 space-y-6">
        <div className="flex items-center justify-between gap-4 animate-slide-up-fade stagger-3">
          <div className="flex items-center gap-3">
            <Select defaultValue="all" onValueChange={setTypeFilter}>
              <SelectTrigger className="w-[130px] h-9 text-sm">
                <SelectValue placeholder="类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">全部类型</SelectItem>
                <SelectItem value="chat">对话应用</SelectItem>
                <SelectItem value="agent">智能体</SelectItem>
                <SelectItem value="workflow">工作流</SelectItem>
              </SelectContent>
            </Select>
            {!isLoading && filteredApps.length > 0 && (
              <span className="text-sm text-muted-foreground">{filteredApps.length} 个结果</span>
            )}
          </div>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <Input
              placeholder="搜索应用..."
              className="pl-9 w-[220px] h-9 text-sm bg-background"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {isLoading ? (
            [...Array(8)].map((_, i) => (
              // biome-ignore lint/suspicious/noArrayIndexKey: skeleton placeholders
              <div key={i} className={`h-[192px] rounded-2xl border bg-card overflow-hidden animate-pulse stagger-${Math.min(i + 1, 8)}`}>
                <div className="h-1 skeleton-shimmer" />
                <div className="p-5 space-y-4">
                  <div className="flex items-start gap-3">
                    <div className="h-10 w-10 rounded-xl skeleton-shimmer" />
                    <div className="flex-1 space-y-2 pt-1">
                      <div className="h-4 w-3/4 rounded skeleton-shimmer" />
                      <div className="h-3 w-1/3 rounded skeleton-shimmer" />
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <div className="h-3 rounded skeleton-shimmer" />
                    <div className="h-3 w-4/5 rounded skeleton-shimmer" />
                  </div>
                </div>
              </div>
            ))
          ) : filteredApps.length > 0 ? (
            filteredApps.map((app, i) => {
              const accent = getAccent(app.appId)
              const typeConfig = APP_TYPE_ACCENTS[app.type] ?? APP_TYPE_ACCENTS[AppType.CHAT]
              const TypeIcon = typeConfig.Icon
              const staggerClass = `stagger-${Math.min(i + 1, 8)}`
              return (
                <div
                  key={app.appId}
                  className={`group relative rounded-2xl border bg-card overflow-hidden cursor-pointer
                    animate-slide-up-fade ${staggerClass}
                    transition-all duration-250 ease-out
                    hover:-translate-y-1 hover:shadow-lg ${accent.glow}
                    hover:border-primary/30`}
                  onClick={() => navigateToAppConfig(app.appId)}
                  onKeyDown={(e) => e.key === 'Enter' && navigateToAppConfig(app.appId)}
                >
                  <div className={`h-1 bg-gradient-to-r ${accent.gradient} w-full`} />

                  <div className="p-5">
                    <div className="flex items-start justify-between gap-2 mb-4">
                      <div className="flex items-start gap-3 min-w-0">
                        <div
                          className={`h-10 w-10 rounded-xl ${accent.bg} flex items-center justify-center flex-shrink-0 text-base font-semibold border border-border/40`}
                          style={{ backgroundColor: app.logoBackground || undefined }}
                        >
                          {app.logo || app.appName.slice(0, 1).toUpperCase()}
                        </div>
                        <div className="min-w-0 pt-0.5">
                          <h3 className="font-semibold text-sm leading-snug truncate group-hover:text-primary transition-colors duration-200 mb-1">
                            {app.appName}
                          </h3>
                          <span
                            className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-medium ${typeConfig.bg} ${typeConfig.text}`}
                          >
                            <TypeIcon className="h-2.5 w-2.5" />
                            {typeConfig.label}
                          </span>
                        </div>
                      </div>

                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <RowActionsTrigger
                            size="sm"
                            className="-mr-1 -mt-0.5 cursor-pointer"
                            onClick={(e) => e.stopPropagation()}
                          />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-36">
                          <DropdownMenuItem
                            onClick={(e) => {
                              e.stopPropagation()
                              handleEditApp(app)
                            }}
                          >
                            <Edit className="mr-2 h-3.5 w-3.5" />
                            编辑信息
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={(e) => {
                              e.stopPropagation()
                              confirmDeleteApp(app)
                            }}
                          >
                            <Trash className="mr-2 h-3.5 w-3.5" />
                            删除
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>

                    <p className="text-xs text-muted-foreground line-clamp-2 leading-relaxed min-h-[2.5rem]">
                      {app.intro || <span className="italic opacity-60">暂无介绍</span>}
                    </p>
                  </div>

                  <div className="px-5 pb-4 flex items-center justify-between">
                    <div className="flex items-center gap-1.5">
                      <Avatar className="h-5 w-5 ring-1 ring-border">
                        {app.creatorAvatar?.startsWith('http') ? (
                          <AvatarImage src={app.creatorAvatar} alt={app.creatorUsername} />
                        ) : (
                          <AvatarFallback className="text-[8px] bg-primary/10 text-primary">
                            {app.creatorUsername?.slice(0, 1).toUpperCase() ?? <User className="h-2.5 w-2.5" />}
                          </AvatarFallback>
                        )}
                      </Avatar>
                      <span className="text-[11px] text-muted-foreground">{app.creatorUsername || '未知'}</span>
                    </div>
                    <span className="text-[11px] text-muted-foreground/70">{formatUpdateTime(app.updateTime)}</span>
                  </div>
                </div>
              )
            })
          ) : (
            <div className="col-span-full animate-fade-in">
              <div className="flex flex-col items-center justify-center py-20 text-center">
                <div className="relative mb-6">
                  <div className="h-20 w-20 rounded-3xl bg-primary/8 border border-primary/15 flex items-center justify-center">
                    <Layers className="h-9 w-9 text-primary/40" />
                  </div>
                  <div className="absolute -top-1 -right-1 h-6 w-6 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center">
                    <Plus className="h-3.5 w-3.5 text-primary/60" />
                  </div>
                </div>
                <h3 className="font-semibold text-base text-foreground mb-1.5">
                  {searchTerm ? `未找到 "${searchTerm}"` : '还没有应用'}
                </h3>
                <p className="text-sm text-muted-foreground max-w-xs mb-6">
                  {searchTerm
                    ? '请尝试其他关键词，或清空搜索条件查看全部'
                    : '创建你的第一个 AI 应用，开始构建智能对话体验'}
                </p>
                {!searchTerm && (
                  <Button onClick={handleCreateApp} className="gap-2 shadow-md shadow-primary/20 cursor-pointer">
                    <Plus className="h-4 w-4" />
                    创建第一个应用
                  </Button>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      <AppFormDialog open={isFormOpen} onOpenChange={setIsFormOpen} app={currentApp} onSuccess={loadApps} tenantId={tenantId} />

      <ConfirmDialog
        open={deleteDialogOpen}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteDialogOpen(false)
            setAppToDelete(null)
          }
        }}
        title="确认删除应用"
        description={`您确定要删除应用 "${appToDelete?.appName}" 吗？此操作不可撤销。`}
        confirmText="删除"
        cancelText="取消"
        confirmVariant="destructive"
        onConfirm={handleDeleteApp}
        isConfirming={isDeleting}
      />
    </div>
  )
}
