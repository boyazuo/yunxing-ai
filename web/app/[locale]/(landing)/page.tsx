import { LandingPage } from '@/components/landing/landing-page'
import type { Metadata } from 'next'
import { getTranslations } from 'next-intl/server'

export async function generateMetadata({
  params,
}: {
  params: Promise<{ locale: string }>
}): Promise<Metadata> {
  const { locale } = await params
  const t = await getTranslations({ locale, namespace: 'landing' })

  return {
    title: t('meta.title'),
    description: t('meta.description'),
  }
}

export default function Page() {
  return <LandingPage />
}
