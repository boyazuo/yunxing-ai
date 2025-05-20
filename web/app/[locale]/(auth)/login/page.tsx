'use client'

import { invitationService } from '@/api/invitation'
import { AuthForm } from '@/components/auth/auth-form'
import { Link } from '@/i18n/routing'
import { useTranslations } from 'next-intl'
import { useSearchParams, useRouter } from 'next/navigation'
import { useState } from 'react'
import { toast } from 'sonner'

export default function LoginPage() {
  const router = useRouter()
  const t = useTranslations('auth')
  const searchParams = useSearchParams()
  const token = searchParams.get('token')

  const [email, setEmail] = useState('')

  if (token) {
    // 判断是否登录用户
    invitationService.getInvitations(token).then((res: any) => {
      if (res.code === 0) {
        const user = res.data.user
        const invitation = res.data.invitation
        // 没有用户信息 跳转到注册页面
        if(!user && invitation) {
          toast.success('不存在受邀用户账号，请注册！')
          router.push(`/register?token=${token}`)
        }
        setEmail(invitation.inviteeEmail)
      } else {
        toast.error(res.msg)
      }
    })
  }

  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-6 bg-background p-6 pt-20 md:p-10 md:pt-24">
      <div className="w-full max-w-sm grid gap-6">
        <div className="flex flex-col items-center gap-2 text-center">
          <h1 className="text-2xl font-bold">{t('loginTitle')}</h1>
          <p className="text-balance text-sm text-muted-foreground">
            {!!token ? '输入密码登录，加入团队' : t('loginDescription')}
          </p>
        </div>
        <AuthForm mode="login" redirectUrl="/home" token={token} email={email}/>
        {!!email ? '' : <div className="text-center text-sm">
          {t('noAccount')}
          <Link href="/register" className="underline underline-offset-4 ml-1">
            {t('register')}
          </Link>
        </div>}
      </div>
    </div>
  )
}
