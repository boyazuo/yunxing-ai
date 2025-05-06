'use client'

import GithubLink from '@/components/layouts/header/github-link'
import UserAvatar from '@/components/layouts/header/user-avatar'
import LocaleToggle from '@/components/locale/toggle'
import ThemeToggle from '@/components/theme/toggle'
import { cn } from '@/lib/utils'
import Image from 'next/image'
import Link from 'next/link'
import { usePathname } from 'next/navigation'

// 导航菜单
const navigation = [
  { name: '首页', href: '/home' },
  { name: '工作空间', href: '/space' },
  { name: '知识库', href: '/dataset' },
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
          <ul className="flex space-x-4">
            {navigation.map((item) => {
              const isActive = pathname === item.href || pathname?.startsWith(`${item.href}/`)
              return (
                <li key={item.name}>
                  <Link
                    href={item.href}
                    className={cn(
                      'text-sm font-medium transition-colors hover:text-primary cursor-pointer',
                      isActive ? 'text-foreground' : 'text-muted-foreground',
                    )}
                  >
                    {item.name}
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
