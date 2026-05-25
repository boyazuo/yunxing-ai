'use client'

import { BrandLogo } from '@/components/brand/brand-logo'
import LocaleToggle from '@/components/locale/toggle'
import ThemeToggle from '@/components/theme/toggle'

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="relative">
      <header className="fixed top-0 left-0 right-0 z-10 flex items-center justify-between px-6 py-4 bg-background/80 backdrop-blur-xl border-b border-border/60">
        <BrandLogo href="/" />

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
