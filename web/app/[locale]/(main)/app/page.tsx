'use client'

import { appService } from '@/api/apps'
import { AppFormDialog } from '@/app/[locale]/(main)/app/_components/AppFormDialog'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import type { App } from '@/types/app'
import { AppType } from '@/types/app'
import { Bot, Edit, GitBranch, MessageSquare, MoreHorizontal, Plus, Trash, User } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useRouter } from 'next/navigation'
import { useCallback, useEffect, useState } from 'react'

export default function SpacePage() {
  const router = useRouter()
  const { data: session, status } = useSession()
  const [apps, setApps] = useState<App[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [appFilter, setAppFilter] = useState('all')
  const [typeFilter, setTypeFilter] = useState('all')
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [currentApp, setCurrentApp] = useState<App | undefined>(undefined)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [appToDelete, setAppToDelete] = useState<App | null>(null)

  // 从会话中获取租户ID
  const tenantId = session?.tenant?.tenantId || ''

  // 加载应用列表
  const loadApps = useCallback(async () => {
    try {
      if (!tenantId) {
        console.error('租户ID不存在')
        setIsLoading(false)
        return
      }

      setIsLoading(true)
      const data = await appService.getApps(tenantId.toString())
      if (data && data.length > 0) {
        setApps(data)
      }
    } catch (error) {
      console.error('加载应用失败', error)
    } finally {
      setIsLoading(false)
    }
  }, [tenantId])

  // 首次加载
  useEffect(() => {
    if (status === 'authenticated' && tenantId) {
      loadApps()
    }
  }, [status, tenantId, loadApps])

  // 打开创建应用表单
  const handleCreateApp = () => {
    setCurrentApp(undefined)
    setIsFormOpen(true)
  }

  // 打开编辑应用表单
  const handleEditApp = (app: App) => {
    setCurrentApp(app)
    setIsFormOpen(true)
  }

  // 打开删除确认对话框
  const confirmDeleteApp = (app: App) => {
    // 关闭任何可能打开的其他UI组件
    // 使用setTimeout确保React完成当前渲染循环
    setTimeout(() => {
      setAppToDelete(app)
      setDeleteDialogOpen(true)
    }, 0)
  }

  // 删除应用
  const handleDeleteApp = async () => {
    try {
      if (!appToDelete) return

      await appService.deleteApp(appToDelete.appId)
      setDeleteDialogOpen(false)
      setAppToDelete(null)
      loadApps() // 重新加载应用列表
    } catch (error) {
      console.error('删除应用失败', error)
    }
  }

  // 格式化更新时间
  const formatUpdateTime = (updateTime?: string) => {
    if (!updateTime) return '未知'

    try {
      const date = new Date(updateTime)
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hour = date.getHours().toString().padStart(2, '0')
      const minute = date.getMinutes().toString().padStart(2, '0')

      return `${month}-${day} ${hour}:${minute}`
    } catch (error) {
      return '未知'
    }
  }

  // 过滤应用
  const filteredApps = apps.filter((app) => {
    // 搜索过滤
    const searchMatch = searchTerm ? app.appName.toLowerCase().includes(searchTerm.toLowerCase()) : true

    // 应用类型过滤
    const typeMatch = typeFilter === 'all' ? true : app.type.toString() === typeFilter

    return searchMatch && typeMatch
  })

  // 跳转到应用配置页面
  const navigateToAppConfig = (appId: string) => {
    router.push(`/app/${appId}/config`)
  }

  return (
    <div className="container mx-auto py-6 space-y-6">
      {/* 顶部筛选和搜索区 */}
      <div className="flex justify-between items-center">
        <div className="flex items-center space-x-4">
          <Select defaultValue="all" onValueChange={setAppFilter}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="筛选应用" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部应用</SelectItem>
              <SelectItem value="recent">最近使用</SelectItem>
              <SelectItem value="created">我创建的</SelectItem>
              <SelectItem value="shared">共享给我的</SelectItem>
            </SelectContent>
          </Select>
          <Select defaultValue="all" onValueChange={setTypeFilter}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="筛选类型" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部类型</SelectItem>
              <SelectItem value="chat">对话应用</SelectItem>
              <SelectItem value="agent">智能体应用</SelectItem>
              <SelectItem value="workflow">工作流应用</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="flex items-center space-x-2">
          <Input
            placeholder="搜索应用..."
            className="w-[250px]"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <Button onClick={handleCreateApp}>
            <Plus className="mr-2 h-4 w-4" />
            新建应用
          </Button>
        </div>
      </div>

      {/* 应用列表 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
        {isLoading ? (
          // 加载状态
          [...Array(8)].map((_, index) => (
            <Card key={`skeleton-${index}-${Date.now()}`} className="min-h-[120px] animate-pulse bg-muted p-0">
              <div className="h-full" />
            </Card>
          ))
        ) : filteredApps.length > 0 ? (
          // 应用列表
          filteredApps.map((app) => (
            <Card
              key={app.appId}
              className="cursor-pointer hover:shadow-md transition-shadow overflow-hidden border-muted p-0 gap-3"
              onClick={() => navigateToAppConfig(app.appId)}
            >
              {/* 卡片头部 */}
              <div className="px-4 pt-4 pb-0 flex items-start justify-between">
                <div className="flex space-x-2.5">
                  <div className="flex-shrink-0">
                    <div
                      className="h-10 w-10 rounded-md flex items-center justify-center text-xl shadow-sm border"
                      style={{ backgroundColor: app.logoBackground || '#f0f0f0' }}
                    >
                      {app.logo || app.appName.slice(0, 2).toUpperCase()}
                    </div>
                  </div>
                  <div>
                    <h3 className="font-medium text-sm leading-tight mb-1">{app.appName}</h3>
                    <Badge
                      variant={
                        app.type === AppType.CHAT ? 'secondary' : app.type === AppType.AGENT ? 'default' : 'outline'
                      }
                      className="text-[10px] px-1.5 py-0 h-5 gap-1 font-normal"
                    >
                      {app.type === AppType.CHAT && <MessageSquare className="h-3 w-3" />}
                      {app.type === AppType.AGENT && <Bot className="h-3 w-3" />}
                      {app.type === AppType.WORKFLOW && <GitBranch className="h-3 w-3" />}
                      <span>
                        {app.type === AppType.CHAT
                          ? '对话应用'
                          : app.type === AppType.AGENT
                            ? '智能体'
                            : app.type === AppType.WORKFLOW
                              ? '工作流'
                              : '未知类型'}
                      </span>
                    </Badge>
                  </div>
                </div>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-6 w-6 -mt-0.5 -mr-0.5"
                      onClick={(e) => e.stopPropagation()} // 防止点击触发卡片的点击事件
                    >
                      <MoreHorizontal className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem
                      onClick={(e) => {
                        e.stopPropagation() // 防止点击触发卡片的点击事件
                        handleEditApp(app)
                      }}
                    >
                      <Edit className="mr-2 h-4 w-4" />
                      <span>编辑信息</span>
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      className="text-destructive"
                      onClick={(e) => {
                        e.stopPropagation() // 防止点击触发卡片的点击事件
                        confirmDeleteApp(app)
                      }}
                    >
                      <Trash className="mr-2 h-4 w-4" />
                      <span>删除应用</span>
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>

              {/* 卡片内容 */}
              <div className="px-4 py-0 min-h-[60px]">
                <p className="text-[12px] text-muted-foreground line-clamp-2">{app.intro || '暂无介绍'}</p>
              </div>

              {/* 卡片底部 */}
              <div className="mt-auto px-3 py-1.5 flex items-center justify-between border-t border-muted/70 bg-muted/5 text-[11px]">
                <div className="flex items-center space-x-1.5">
                  <Avatar className="h-4 w-4">
                    {app.creatorAvatar ? (
                      app.creatorAvatar.startsWith('http') ? (
                        <AvatarImage src={app.creatorAvatar} alt={app.creatorUsername} />
                      ) : (
                        <AvatarFallback className="text-[9px]">{app.creatorAvatar}</AvatarFallback>
                      )
                    ) : (
                      <AvatarFallback>
                        {app.creatorUsername ? (
                          app.creatorUsername.slice(0, 1).toUpperCase()
                        ) : (
                          <User className="h-2 w-2" />
                        )}
                      </AvatarFallback>
                    )}
                  </Avatar>
                  <span className="text-[12px] text-muted-foreground">{app.creatorUsername || '创建者'}</span>
                </div>
                <span className="text-[12px] text-muted-foreground">更新于 {formatUpdateTime(app.updateTime)}</span>
              </div>
            </Card>
          ))
        ) : (
          // 无数据状态
          <div className="col-span-full flex flex-col items-center justify-center py-12 text-muted-foreground">
            <p>暂无应用数据</p>
            <Button variant="outline" className="mt-2" onClick={handleCreateApp}>
              <Plus className="mr-2 h-4 w-4" />
              创建第一个应用
            </Button>
          </div>
        )}
      </div>

      {/* 应用表单弹窗 */}
      <AppFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        app={currentApp}
        onSuccess={loadApps}
        tenantId={tenantId}
      />

      {/* 删除确认对话框 */}
      <AlertDialog
        open={deleteDialogOpen}
        onOpenChange={(open) => {
          if (open === false) {
            setDeleteDialogOpen(false)
            setAppToDelete(null)
          }
        }}
      >
        <AlertDialogContent
          onCloseAutoFocus={(event) => {
            // 阻止默认的焦点处理
            event.preventDefault()
            document.body.style.pointerEvents = ''
          }}
        >
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除应用</AlertDialogTitle>
            <AlertDialogDescription>
              您确定要删除应用 "{appToDelete?.appName}" 吗？此操作不可撤销。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={handleDeleteApp}
            >
              删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
