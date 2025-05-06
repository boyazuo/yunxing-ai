'use client'

import LocaleToggle from '@/components/locale/toggle'
import ThemeToggle from '@/components/theme/toggle'
import { Link } from '@/i18n/routing'
import { useTranslations } from 'next-intl'
import Image from 'next/image'

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const t = useTranslations('common')

  return (
    <div className="relative">
      {/* 头部导航 */}
      <header className="fixed top-0 left-0 right-0 z-10 flex items-center justify-between px-6 py-4 bg-background/80 backdrop-blur-sm">
        {/* Logo和产品名称 */}
        <Link href="/" className="flex items-center gap-2">
          <Image src="/logo.png" alt="云行AI" width={32} height={32} />
          <span className="text-lg font-semibold">云行AI</span>
        </Link>

        {/* 右侧操作区 */}
        <div className="flex items-center gap-2">
          <LocaleToggle />
          <ThemeToggle isIcon />
        </div>
      </header>

      {/* 主内容区 */}
      {children}
    </div>
  )
}
