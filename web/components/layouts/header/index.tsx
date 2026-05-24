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
    <header className="sticky top-0 z-40 w-full border-b border-border/60 bg-background/80 backdrop-blur-xl supports-[backdrop-filter]:bg-background/70">
      <div className="flex h-14 items-center px-6">
        {/* Logo */}
        <Link href="/home" className="flex items-center gap-2.5 mr-8 flex-shrink-0 cursor-pointer group">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10 border border-primary/20 group-hover:bg-primary/15 transition-colors">
            <Image src="/logo.png" alt="云行 AI" width={20} height={20} />
          </div>
          <span className="font-bold text-sm tracking-tight">云行 AI</span>
        </Link>

        {/* Nav */}
        <nav className="hidden flex-1 md:flex">
          <ul className="flex items-center gap-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href || pathname?.startsWith(`${item.href}/`)
              const Icon = item.icon
              return (
                <li key={item.name}>
                  <Link
                    href={item.href}
                    title={item.description}
                    className={cn(
                      'relative flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm font-medium transition-all duration-200',
                      isActive
                        ? 'text-primary'
                        : 'text-muted-foreground hover:text-foreground hover:bg-muted/60',
                    )}
                  >
                    <Icon size={15} className="flex-shrink-0" />
                    {item.name}
                    {/* Active underline indicator */}
                    {isActive && (
                      <span className="absolute bottom-0 left-3 right-3 h-0.5 rounded-full bg-primary/70" />
                    )}
                  </Link>
                </li>
              )
            })}
          </ul>
        </nav>

        {/* Right actions */}
        <div className="ml-auto flex items-center gap-1.5">
          <LocaleToggle />
          <GithubLink isIcon />
          <ThemeToggle isIcon />
          <div className="h-5 w-px bg-border mx-1" />
          <UserAvatar />
        </div>
      </div>
    </header>
  )
}
