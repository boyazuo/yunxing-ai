'use client'

import { authService } from '@/api/auth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'
import { Label } from '@radix-ui/react-label'
import { signIn } from 'next-auth/react'
import { useTranslations } from 'next-intl'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'

interface AuthFormProps extends React.ComponentPropsWithoutRef<'form'> {
  mode: 'login' | 'register'
  redirectUrl?: string
  email?: string
  token?: string | null
}

export function AuthForm({
  mode,
  redirectUrl = '/home',
  email,
  token,
  children,
  className,
  ...props
}: AuthFormProps) {
  const t = useTranslations('auth')
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    username: '',
    email: email || '',
    password: '',
    token: token || '',
  })

  useEffect(() => {
    if (email) {
      setFormData({ ...formData, email })
    }
  }, [email])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    setError(null) // 清除错误
  }

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setIsLoading(true)
    setError(null)

    try {
      if (mode === 'login') {
        const result = await signIn('credentials', {
          redirect: false,
          email: formData.email,
          password: formData.password,
          callbackUrl: redirectUrl,
          token: token
        })

        if (result?.error) {
          if (result.status === 401) {
            setError(t('loginFailed'))
            toast.error(t('loginFailed'))
            return
          }
          setError(result.error)
          toast.error(result.error || t('loginFailed'))
          return
        }

        toast.success(t('loginSuccess'))
        router.push(redirectUrl)
        router.refresh()
      } else if (mode === 'register') {
        try {
          await authService.register({
            username: formData.username,
            email: formData.email,
            password: formData.password,
            token: token,
          })

          // 注册成功后自动登录
          const result = await signIn('credentials', {
            redirect: false,
            email: formData.email,
            password: formData.password,
          })

          if (result?.error) {
            setError(result.error)
            toast.error(result.error || t('loginFailed'))
            return
          }

          toast.success(t('registerSuccess'))
          router.push(redirectUrl)
          router.refresh()
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : String(error)
          setError(errorMessage)
          toast.error(errorMessage || t('registerFailed'))
          return
        }
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : String(error)
      setError(errorMessage)
      toast.error(errorMessage || t('error'))
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <form
      className={cn('flex flex-col gap-6', className)}
      onSubmit={handleSubmit}
      {...props}
    >
      <div className="grid gap-6">
        {error && (
          <div className="text-sm text-red-500 p-2 bg-red-50 rounded border border-red-200">
            {error}
          </div>
        )}
        {mode === 'register' && (
          <div className="grid gap-2">
            <Label htmlFor="username">{t('username')}</Label>
            <Input
              id="username"
              name="username"
              type="text"
              placeholder="请输入用户名"
              required
              value={formData.username}
              onChange={handleChange}
              disabled={isLoading}
            />
          </div>
        )}

        <div className="grid gap-2">
          <Label htmlFor="email">{t('email')}</Label>
          <Input
            id="email"
            name="email"
            type="email"
            placeholder="m@example.com"
            required
            readOnly={!!email}
            value={formData.email}
            onChange={handleChange}
            disabled={isLoading}
          />
        </div>
        <div className="grid gap-2">
          <div className="flex items-center">
            <Label htmlFor="password">{t('password')}</Label>
            {mode === 'login' && (
              <a
                href="/forgot-password"
                className="ml-auto text-sm underline-offset-4 hover:underline"
              >
                {t('forgotPassword')}
              </a>
            )}
          </div>
          <Input
            id="password"
            name="password"
            type="password"
            required
            value={formData.password}
            onChange={handleChange}
            disabled={isLoading}
          />
        </div>
        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading
            ? t('loading')
            : mode === 'login'
            ? t('login')
            : t('register')}
        </Button>
      </div>
    </form>
  )
}
