'use client'

import { GetStartedButtonClient } from '@/components/landing/get-started-button-client'
import { Button } from '@/components/ui/button'
import { motion } from 'framer-motion'
import { ArrowRight, Bot, Database, Users } from 'lucide-react'

interface LandingHeroProps {
  badge: string
  title: string
  titleHighlight: string
  description: string
  ctaPrimary: string
  ctaSecondary: string
  isAuthenticated: boolean
  stats: {
    apps: string
    rag: string
    teams: string
  }
  preview: {
    windowTitle: string
    appsLabel: string
    chatLabel: string
    appNames: string[]
    userMessage: string
    assistantMessage: string
  }
}

const statIcons = [Bot, Database, Users] as const

export function LandingHero({
  badge,
  title,
  titleHighlight,
  description,
  ctaPrimary,
  ctaSecondary,
  isAuthenticated,
  stats,
  preview,
}: LandingHeroProps) {
  const statLabels = [stats.apps, stats.rag, stats.teams]

  return (
    <section className="relative overflow-hidden pt-28 pb-20 md:pt-36 md:pb-28">
      <div
        className="pointer-events-none absolute inset-0 -z-10"
        aria-hidden
      >
        <div className="absolute -top-32 left-1/2 h-[520px] w-[720px] -translate-x-1/2 rounded-full bg-primary/20 blur-[100px]" />
        <div className="absolute top-1/3 -right-24 h-72 w-72 rounded-full bg-chart-2/25 blur-[80px]" />
        <div className="absolute bottom-0 -left-16 h-64 w-64 rounded-full bg-chart-3/20 blur-[72px]" />
        <div
          className="absolute inset-0 opacity-[0.35] dark:opacity-[0.2]"
          style={{
            backgroundImage: `
              linear-gradient(to right, oklch(0.51 0.24 264 / 0.06) 1px, transparent 1px),
              linear-gradient(to bottom, oklch(0.51 0.24 264 / 0.06) 1px, transparent 1px)
            `,
            backgroundSize: '48px 48px',
          }}
        />
      </div>

      <div className="mx-auto max-w-6xl px-6">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="mx-auto max-w-3xl text-center"
        >
          <span className="inline-flex items-center rounded-full border border-primary/20 bg-primary/5 px-4 py-1.5 text-xs font-medium text-primary">
            {badge}
          </span>

          <h1 className="mt-8 text-4xl font-bold tracking-tight text-foreground sm:text-5xl md:text-6xl md:leading-[1.1]">
            {title}
            <br />
            <span className="bg-gradient-to-r from-primary via-primary to-chart-2 bg-clip-text text-transparent">
              {titleHighlight}
            </span>
          </h1>

          <p className="mx-auto mt-6 max-w-2xl text-lg leading-relaxed text-muted-foreground">
            {description}
          </p>

          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <GetStartedButtonClient
              label={ctaPrimary}
              isAuthenticated={isAuthenticated}
              size="lg"
              className="h-12 rounded-full px-8 text-base shadow-lg shadow-primary/25"
            />
            <Button variant="outline" size="lg" className="h-12 rounded-full px-8" asChild>
              <a href="#features" className="gap-2">
                {ctaSecondary}
                <ArrowRight className="size-4" />
              </a>
            </Button>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 32 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.15 }}
          className="mx-auto mt-16 grid max-w-2xl grid-cols-3 gap-4 sm:max-w-none sm:gap-6"
        >
          {statLabels.map((label, i) => {
            const Icon = statIcons[i]
            return (
              <div
                key={label}
                className="flex flex-col items-center gap-2 rounded-2xl border border-border/60 bg-card/60 px-4 py-5 backdrop-blur-sm"
              >
                <div className="flex size-10 items-center justify-center rounded-xl bg-primary/10 text-primary">
                  <Icon className="size-5" />
                </div>
                <span className="text-center text-sm font-medium text-foreground">{label}</span>
              </div>
            )
          })}
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 0.25 }}
          className="relative mx-auto mt-16 max-w-5xl"
        >
          <div className="rounded-2xl border border-border/80 bg-card/80 p-2 shadow-2xl shadow-primary/10 backdrop-blur-md dark:bg-card/40">
            <div className="overflow-hidden rounded-xl border border-border/60 bg-muted/30">
              <div className="flex items-center gap-2 border-b border-border/60 px-4 py-3">
                <span className="size-3 rounded-full bg-destructive/80" />
                <span className="size-3 rounded-full bg-chart-4/80" />
                <span className="size-3 rounded-full bg-chart-3/80" />
                <span className="ml-2 text-xs text-muted-foreground">{preview.windowTitle}</span>
              </div>
              <div className="grid gap-0 md:grid-cols-5">
                <div className="border-b border-border/60 p-6 md:col-span-2 md:border-b-0 md:border-r">
                  <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">
                    {preview.appsLabel}
                  </p>
                  <ul className="mt-4 space-y-3">
                    {preview.appNames.map((name) => (
                      <li
                        key={name}
                        className="flex items-center gap-2 rounded-lg bg-background/80 px-3 py-2 text-sm shadow-xs"
                      >
                        <span className="size-2 rounded-full bg-primary" />
                        {name}
                      </li>
                    ))}
                  </ul>
                </div>
                <div className="p-6 md:col-span-3">
                  <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">
                    {preview.chatLabel}
                  </p>
                  <div className="mt-4 space-y-3">
                    <div className="ml-auto max-w-[85%] rounded-2xl rounded-tr-sm bg-primary px-4 py-2.5 text-sm text-primary-foreground">
                      {preview.userMessage}
                    </div>
                    <div className="max-w-[90%] rounded-2xl rounded-tl-sm border border-border bg-background px-4 py-3 text-sm leading-relaxed text-foreground">
                      {preview.assistantMessage}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  )
}
