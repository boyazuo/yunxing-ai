import { GetStartedButton } from '@/components/landing/get-started-button'

interface LandingCtaProps {
  title: string
  description: string
  buttonLabel: string
}

export function LandingCta({ title, description, buttonLabel }: LandingCtaProps) {
  return (
    <section className="py-24">
      <div className="mx-auto max-w-6xl px-6">
        <div className="relative overflow-hidden rounded-3xl border border-primary/20 bg-gradient-to-br from-primary/10 via-background to-chart-2/10 px-8 py-16 text-center shadow-xl md:px-16">
          <div
            className="pointer-events-none absolute inset-0 opacity-40"
            aria-hidden
            style={{
              backgroundImage: `radial-gradient(circle at 30% 20%, oklch(0.51 0.24 264 / 0.15), transparent 50%),
                radial-gradient(circle at 70% 80%, oklch(0.60 0.20 210 / 0.12), transparent 45%)`,
            }}
          />
          <div className="relative">
            <h2 className="text-3xl font-bold tracking-tight text-foreground md:text-4xl">
              {title}
            </h2>
            <p className="mx-auto mt-4 max-w-xl text-lg text-muted-foreground">{description}</p>
            <div className="mt-10 flex justify-center">
              <GetStartedButton
                label={buttonLabel}
                size="lg"
                className="h-12 rounded-full px-10 text-base shadow-lg shadow-primary/30"
              />
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
