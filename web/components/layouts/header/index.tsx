'use client'

import GithubLink from '@/components/layouts/header/github-link'
import UserAvatar from '@/components/layouts/header/user-avatar'
import LocaleToggle from '@/components/locale/toggle'
import ThemeToggle from '@/components/theme/toggle'
import { cn } from '@/lib/utils'
import { Database, Home, Layers } from 'lucide-react'
import Image from 'next/image'
import Link from 'next/link'
import { usePathname } from 'next/navigation'

// 导航菜单
const navigation = [
  {
    name: '首页',
    href: '/home',
    icon: Home,
    description: '返回主页',
  },
  {
    name: '工作空间',
    href: '/app',
    icon: Layers,
    description: 'AI应用管理',
  },
  {
    name: '知识库',
    href: '/dataset',
    icon: Database,
    description: '数据与知识管理',
  },
]

export default function Header() {
  const pathname = usePathname()

  return (
    <header className="sticky top-0 z-40 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="flex h-16 items-center">
        <div className="mx-10 flex">
          <Link href="/home" className="flex items-center space-x-2 cursor-pointer">
            <div className="flex size-8 items-center justify-center rounded-lg">
              <Image src="/logo.png" alt="云行 AI Logo" width={32} height={32} />
            </div>
            <span className="font-bold">云行 AI</span>
          </Link>
        </div>
        <nav className="hidden flex-1 md:flex">
          <ul className="flex space-x-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href || pathname?.startsWith(`${item.href}/`)
              const Icon = item.icon
              return (
                <li key={item.name}>
                  <Link
                    href={item.href}
                    className={cn(
                      'group relative flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 cursor-pointer',
                      'hover:bg-muted/50 hover:scale-105',
                      isActive ? 'text-primary bg-primary/10 shadow-sm' : 'text-muted-foreground hover:text-foreground',
                    )}
                    title={item.description}
                  >
                    <Icon
                      size={16}
                      className={cn(
                        'transition-all duration-200',
                        isActive ? 'text-primary' : 'text-muted-foreground group-hover:text-foreground',
                      )}
                    />
                    <span className="relative">
                      {item.name}
                      {isActive && <span className="absolute left-0 right-0 h-0.5 bg-gradient-to-r rounded-full" />}
                    </span>
                  </Link>
                </li>
              )
            })}
          </ul>
        </nav>
        <div className="ml-auto flex items-center gap-2 mr-10">
          <LocaleToggle />
          <GithubLink isIcon />
          <ThemeToggle isIcon />
          <UserAvatar />
        </div>
      </div>
    </header>
  )
}
