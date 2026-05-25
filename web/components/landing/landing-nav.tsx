'use client'

import LocaleToggle from '@/components/locale/toggle'
import { GetStartedButtonClient } from '@/components/landing/get-started-button-client'
import ThemeToggle from '@/components/theme/toggle'
import { BrandLogo } from '@/components/brand/brand-logo'
import { cn } from '@/lib/utils'
import { useEffect, useState } from 'react'

interface LandingNavProps {
  featuresLabel: string
  workflowLabel: string
  getStartedLabel: string
  isAuthenticated: boolean
}

export function LandingNav({
  featuresLabel,
  workflowLabel,
  getStartedLabel,
  isAuthenticated,
}: LandingNavProps) {
  const [scrolled, setScrolled] = useState(false)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 12)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <header
      className={cn(
        'fixed inset-x-0 top-0 z-50 transition-all duration-300',
        scrolled
          ? 'border-b border-border/60 bg-background/80 backdrop-blur-xl shadow-sm'
          : 'bg-transparent',
      )}
    >
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
        <BrandLogo href="/" />

        <nav className="hidden items-center gap-8 md:flex">
          <a
            href="#features"
            className="text-sm text-muted-foreground transition-colors hover:text-foreground"
          >
            {featuresLabel}
          </a>
          <a
            href="#workflow"
            className="text-sm text-muted-foreground transition-colors hover:text-foreground"
          >
            {workflowLabel}
          </a>
        </nav>

        <div className="flex items-center gap-1 sm:gap-2">
          <LocaleToggle isIcon />
          <ThemeToggle isIcon />
          <GetStartedButtonClient
            label={getStartedLabel}
            isAuthenticated={isAuthenticated}
            size="sm"
            className="rounded-full px-4 shadow-md"
          />
        </div>
      </div>
    </header>
  )
}
