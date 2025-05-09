'use client'

import {
  Sidebar,
  SidebarContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
} from '@/components/ui/sidebar'
import { Sparkles, User, Users } from 'lucide-react'
import Link from 'next/link'
import { usePathname } from 'next/navigation'

// 设置菜单项
const settingsMenuItems = [
  {
    name: '账户设置',
    href: '/settings/account',
    icon: User,
  },
  {
    name: '团队管理',
    href: '/settings/teams',
    icon: Users,
  },
  {
    name: '模型提供商',
    href: '/settings/providers',
    icon: Sparkles,
  },
]

export default function SettingsLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const pathname = usePathname()

  return (
    <div className="container max-w-screen-2xl pb-0">
      <div className="grid grid-cols-1 md:grid-cols-[260px_1fr] gap-6">
        {/* 使用 Sidebar 组件重构侧边栏菜单 */}
        <div className="py-6 pr-3">
          <SidebarProvider className="min-h-auto">
            <Sidebar variant="inset" className="static w-full  md:w-[260px] bg-transparent pl-5" collapsible="none">
              <SidebarHeader className="px-3 pb-4 pt-2">
                <h2 className="text-base font-semibold px-1">设置</h2>
              </SidebarHeader>
              <SidebarContent>
                <SidebarMenu className="space-y-1">
                  {settingsMenuItems.map((item) => {
                    const isActive = pathname === item.href || pathname?.startsWith(`${item.href}/`)
                    return (
                      <SidebarMenuItem key={item.name}>
                        <SidebarMenuButton
                          isActive={isActive}
                          asChild
                          className={isActive ? 'bg-amber-50 text-amber-600 hover:bg-amber-50/80' : ''}
                        >
                          <Link href={item.href}>
                            <item.icon className="h-4 w-4" />
                            <span>{item.name}</span>
                          </Link>
                        </SidebarMenuButton>
                      </SidebarMenuItem>
                    )
                  })}
                </SidebarMenu>
              </SidebarContent>
            </Sidebar>
          </SidebarProvider>
        </div>

        {/* 内容区域 */}
        <div className="py-6">{children}</div>
      </div>
    </div>
  )
}
