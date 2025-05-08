import NextAuthSessionProvider from '@/components/auth/providers'
import { ThemeProvider } from '@/components/theme/provider'
import { Toaster } from '@/components/ui/sonner'
import type { Metadata } from 'next'
import { NextIntlClientProvider } from 'next-intl'
import { getMessages } from 'next-intl/server'
import '../globals.css'

export const metadata: Metadata = {
  title: '云行 AI',
  description: '云行 AI 是一个 AI 应用构建平台，提供 AI 应用的构建、部署、管理等服务。',
}

// 配置支持的语言
export function generateStaticParams() {
  return [{ locale: 'en' }, { locale: 'zh' }]
}

export default async function LocaleLayout({
  children,
  params,
}: Readonly<{
  children: React.ReactNode
  params: Promise<{ locale: string }>
}>) {
  const { locale } = await params
  const messages = await getMessages()

  return (
    <html lang={locale} suppressHydrationWarning>
      <body className="antialiased">
        <NextAuthSessionProvider>
          <NextIntlClientProvider locale={locale} messages={messages}>
            <ThemeProvider>
              {children}
              <Toaster position="top-center" richColors />
            </ThemeProvider>
          </NextIntlClientProvider>
        </NextAuthSessionProvider>
      </body>
    </html>
  )
}
