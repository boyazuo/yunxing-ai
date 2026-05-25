import { Link } from '@/i18n/routing'
import { cn } from '@/lib/utils'
import Image from 'next/image'

export const BRAND_NAME = '云行 AI'

interface BrandLogoProps {
  href?: string
  showLabel?: boolean
  className?: string
}

export function BrandLogo({
  href = '/',
  showLabel = true,
  className,
}: BrandLogoProps) {
  return (
    <Link
      href={href}
      className={cn('group flex shrink-0 items-center gap-2.5', className)}
    >
      <div className="flex h-8 w-8 items-center justify-center rounded-lg border border-primary/20 bg-primary/10 transition-colors group-hover:bg-primary/15">
        <Image src="/logo.png" alt={BRAND_NAME} width={20} height={20} />
      </div>
      {showLabel ? (
        <span className="text-sm font-bold tracking-tight">{BRAND_NAME}</span>
      ) : null}
    </Link>
  )
}
