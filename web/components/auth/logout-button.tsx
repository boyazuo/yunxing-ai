'use client'

import { Button } from '@/components/ui/button'
import { signOut } from 'next-auth/react'
import { useTranslations } from 'next-intl'
import { useRouter } from 'next/navigation'

interface LogoutButtonProps {
  className?: string
  variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link'
}

export function LogoutButton({ className, variant = 'default' }: LogoutButtonProps) {
  const t = useTranslations('auth')
  const router = useRouter()

  const handleLogout = async () => {
    await signOut({ redirect: false })
    router.push('/login')
    router.refresh()
  }

  return (
    <Button onClick={handleLogout} variant={variant} className={className}>
      {t('logout')}
    </Button>
  )
}
