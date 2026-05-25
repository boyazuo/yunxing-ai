import { BrandLogo } from '@/components/brand/brand-logo'

interface LandingFooterProps {
  tagline: string
  copyright: string
}

export function LandingFooter({ tagline, copyright }: LandingFooterProps) {
  return (
    <footer className="border-t border-border/60 bg-muted/20 py-12">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-6 px-6 sm:flex-row">
        <div className="flex items-center gap-3">
          <BrandLogo href="/" showLabel={false} />
          <span className="text-sm text-muted-foreground">{tagline}</span>
        </div>
        <p className="text-sm text-muted-foreground">{copyright}</p>
      </div>
    </footer>
  )
}
