'use client'

import { invitationService } from '@/api/invitation'
import { AuthForm } from '@/components/auth/auth-form'
import { Link } from '@/i18n/routing'
import { useTranslations } from 'next-intl'
import { useSearchParams } from 'next/navigation'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'

export default function RegisterPage() {
  const searchParams = useSearchParams()
  const token = searchParams.get('token')

  const [email, setEmail] = useState('')

  useEffect(() => {
    if (token) {
      invitationService.getInvitations(token).then((res: any) => {
        if (res.code === 0) {
          const invitation = res.data.invitation
          setEmail(invitation.inviteeEmail)
        } else {
          toast.error(res.msg)
        }
      })
    }
  }, [token])

  const t = useTranslations('auth')
  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-6 bg-background p-6 pt-20 md:p-10 md:pt-24">
      <div className="w-full max-w-sm grid gap-6">
        <div className="flex flex-col items-center gap-2 text-center">
          <h1 className="text-2xl font-bold">{t('registerTitle')}</h1>
          <p className="text-balance text-sm text-muted-foreground">
            {!!email ? '完成注册，加入团队' : t('registerDescription')}
          </p>
        </div>
        <AuthForm
          mode="register"
          redirectUrl="/login"
          email={email}
          token={token}
        />
        {!!email ? '' : <div className="text-center text-sm">
          {t('alreadyHaveAccount')}
          <Link href="/login" className="underline underline-offset-4 ml-1">
            {t('login')}
          </Link>
        </div>}
      </div>
    </div>
  )
}
