import { Bot, Database, MessageSquare, Users } from 'lucide-react'

interface FeatureItem {
  title: string
  description: string
}

interface LandingFeaturesProps {
  title: string
  subtitle: string
  items: {
    apps: FeatureItem
    rag: FeatureItem
    chat: FeatureItem
    workspace: FeatureItem
  }
}

const icons = {
  apps: Bot,
  rag: Database,
  chat: MessageSquare,
  workspace: Users,
} as const

const keys = ['apps', 'rag', 'chat', 'workspace'] as const

export function LandingFeatures({ title, subtitle, items }: LandingFeaturesProps) {
  return (
    <section id="features" className="scroll-mt-24 border-t border-border/60 bg-muted/30 py-24">
      <div className="mx-auto max-w-6xl px-6">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-3xl font-bold tracking-tight text-foreground md:text-4xl">
            {title}
          </h2>
          <p className="mt-4 text-lg text-muted-foreground">{subtitle}</p>
        </div>

        <div className="mt-16 grid gap-6 sm:grid-cols-2">
          {keys.map((key) => {
            const Icon = icons[key]
            const item = items[key]
            return (
              <article
                key={key}
                className="group relative overflow-hidden rounded-2xl border border-border/80 bg-card p-8 shadow-sm transition-shadow hover:shadow-md"
              >
                <div className="absolute -right-8 -top-8 size-32 rounded-full bg-primary/5 transition-transform group-hover:scale-110" />
                <div className="relative">
                  <div className="flex size-12 items-center justify-center rounded-xl bg-primary/10 text-primary">
                    <Icon className="size-6" />
                  </div>
                  <h3 className="mt-6 text-xl font-semibold text-foreground">{item.title}</h3>
                  <p className="mt-3 leading-relaxed text-muted-foreground">{item.description}</p>
                </div>
              </article>
            )
          })}
        </div>
      </div>
    </section>
  )
}
