import { Button, type buttonVariants } from '@/components/ui/button'
import { Link } from '@/i18n/routing'
import { authOptions } from '@/lib/auth-options'
import type { VariantProps } from 'class-variance-authority'
import { getServerSession } from 'next-auth'

interface GetStartedButtonProps extends VariantProps<typeof buttonVariants> {
  label: string
  className?: string
}

export async function GetStartedButton({ label, className, variant = 'default', size = 'default' }: GetStartedButtonProps) {
  const session = await getServerSession(authOptions)
  const href = session ? '/home' : '/login'

  return (
    <Button variant={variant} size={size} className={className} asChild>
      <Link href={href}>{label}</Link>
    </Button>
  )
}
