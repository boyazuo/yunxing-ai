'use client'

import { Button } from '@/components/ui/button'
import { Link } from '@/i18n/routing'
import type { VariantProps } from 'class-variance-authority'
import type { buttonVariants } from '@/components/ui/button'

interface GetStartedButtonClientProps
  extends VariantProps<typeof buttonVariants> {
  label: string
  isAuthenticated: boolean
  className?: string
}

export function GetStartedButtonClient({
  label,
  isAuthenticated,
  className,
  variant = 'default',
  size = 'default',
}: GetStartedButtonClientProps) {
  const href = isAuthenticated ? '/home' : '/login'

  return (
    <Button variant={variant} size={size} className={className} asChild>
      <Link href={href}>{label}</Link>
    </Button>
  )
}
