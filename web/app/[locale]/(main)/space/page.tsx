'use client'

import { appService } from '@/api/apps'
import { AppFormDialog } from '@/components/app/AppFormDialog'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import type { App } from '@/types/app'
import { AppType } from '@/types/app'
import { Edit, MoreHorizontal, Plus, Trash, User } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useCallback, useEffect, useState } from 'react'

export default function SpacePage() {
  const { data: session, status } = useSession()
  const [apps, setApps] = useState<App[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [appFilter, setAppFilter] = useState('all')
  const [typeFilter, setTypeFilter] = useState('all')
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [currentApp, setCurrentApp] = useState<App | undefined>(undefined)

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
      setApps(data)
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

  // 删除应用
  const handleDeleteApp = async (appId: number) => {
    try {
      await appService.deleteApp(appId)
      loadApps() // 重新加载应用列表
    } catch (error) {
      console.error('删除应用失败', error)
    }
  }

  // 过滤应用
  const filteredApps = apps.filter((app) => {
    // 搜索过滤
    const searchMatch = searchTerm ? app.appName.toLowerCase().includes(searchTerm.toLowerCase()) : true

    // 应用类型过滤
    const typeMatch = typeFilter === 'all' ? true : app.type.toString() === typeFilter.toUpperCase()

    return searchMatch && typeMatch
  })

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
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {isLoading ? (
          // 加载状态
          [...Array(4)].map((_, index) => (
            <Card key={`skeleton-item-${index}-${Date.now()}`} className="min-h-[200px] animate-pulse bg-muted">
              <div className="h-full" />
            </Card>
          ))
        ) : filteredApps.length > 0 ? (
          // 应用列表
          filteredApps.map((app) => (
            <Card key={app.appId} className="cursor-pointer hover:shadow-md transition-shadow overflow-hidden">
              {/* 卡片头部 */}
              <div className="px-4 flex items-start justify-between">
                <div className="flex space-x-3">
                  <div className="flex-shrink-0">
                    <Avatar className="h-10 w-10">
                      <AvatarImage src={app.logo} alt={app.appName} />
                      <AvatarFallback>{app.appName.slice(0, 2)}</AvatarFallback>
                    </Avatar>
                  </div>
                  <div>
                    <h3 className="font-medium text-sm">{app.appName}</h3>
                    <span className="inline-block px-2 py-0.5 mt-1 text-xs bg-muted rounded-full">
                      {app.type === AppType.CHAT
                        ? '对话应用'
                        : app.type === AppType.AGENT
                          ? '智能体'
                          : app.type === AppType.WORKFLOW
                            ? '工作流'
                            : '未知类型'}
                    </span>
                  </div>
                </div>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon" className="h-8 w-8">
                      <MoreHorizontal className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem onClick={() => handleEditApp(app)}>
                      <Edit className="mr-2 h-4 w-4" />
                      <span>编辑信息</span>
                    </DropdownMenuItem>
                    <DropdownMenuItem className="text-destructive" onClick={() => handleDeleteApp(app.appId)}>
                      <Trash className="mr-2 h-4 w-4" />
                      <span>删除应用</span>
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>

              {/* 卡片内容 */}
              <CardContent className="px-4 h-12">
                <p className="text-xs text-muted-foreground line-clamp-3">{app.intro || '暂无介绍'}</p>
              </CardContent>

              {/* 卡片底部 */}
              <CardFooter className="px-4 flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <Avatar className="h-6 w-6">
                    <AvatarFallback>
                      <User className="h-3 w-3" />
                    </AvatarFallback>
                  </Avatar>
                  <span className="text-xs">创建者</span>
                </div>
                <span className="text-xs text-muted-foreground">
                  更新于 {new Date(app.updateTime || '').toLocaleDateString()}
                </span>
              </CardFooter>
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
    </div>
  )
}
