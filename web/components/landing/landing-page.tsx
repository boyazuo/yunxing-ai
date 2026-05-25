import { LandingCta } from '@/components/landing/landing-cta'
import { LandingFeatures } from '@/components/landing/landing-features'
import { LandingFooter } from '@/components/landing/landing-footer'
import { LandingHero } from '@/components/landing/landing-hero'
import { LandingNav } from '@/components/landing/landing-nav'
import { LandingWorkflow } from '@/components/landing/landing-workflow'
import { authOptions } from '@/lib/auth-options'
import { getServerSession } from 'next-auth'
import { getTranslations } from 'next-intl/server'

export async function LandingPage() {
  const t = await getTranslations('landing')
  const session = await getServerSession(authOptions)
  const isAuthenticated = Boolean(session)
  const year = new Date().getFullYear()

  return (
    <div className="min-h-screen bg-background">
      <LandingNav
        featuresLabel={t('nav.features')}
        workflowLabel={t('nav.workflow')}
        getStartedLabel={t('nav.getStarted')}
        isAuthenticated={isAuthenticated}
      />

      <main>
        <LandingHero
          badge={t('hero.badge')}
          title={t('hero.title')}
          titleHighlight={t('hero.titleHighlight')}
          description={t('hero.description')}
          ctaPrimary={t('hero.ctaPrimary')}
          ctaSecondary={t('hero.ctaSecondary')}
          isAuthenticated={isAuthenticated}
          stats={{
            apps: t('hero.stats.apps'),
            rag: t('hero.stats.rag'),
            teams: t('hero.stats.teams'),
          }}
          preview={{
            windowTitle: t('hero.preview.windowTitle'),
            appsLabel: t('hero.preview.appsLabel'),
            chatLabel: t('hero.preview.chatLabel'),
            appNames: t.raw('hero.preview.appNames') as string[],
            userMessage: t('hero.preview.userMessage'),
            assistantMessage: t('hero.preview.assistantMessage'),
          }}
        />

        <LandingFeatures
          title={t('features.title')}
          subtitle={t('features.subtitle')}
          items={{
            apps: {
              title: t('features.items.apps.title'),
              description: t('features.items.apps.description'),
            },
            rag: {
              title: t('features.items.rag.title'),
              description: t('features.items.rag.description'),
            },
            chat: {
              title: t('features.items.chat.title'),
              description: t('features.items.chat.description'),
            },
            workspace: {
              title: t('features.items.workspace.title'),
              description: t('features.items.workspace.description'),
            },
          }}
        />

        <LandingWorkflow
          title={t('workflow.title')}
          subtitle={t('workflow.subtitle')}
          steps={{
            one: {
              title: t('workflow.steps.one.title'),
              description: t('workflow.steps.one.description'),
            },
            two: {
              title: t('workflow.steps.two.title'),
              description: t('workflow.steps.two.description'),
            },
            three: {
              title: t('workflow.steps.three.title'),
              description: t('workflow.steps.three.description'),
            },
          }}
        />

        <LandingCta
          title={t('cta.title')}
          description={t('cta.description')}
          buttonLabel={t('cta.button')}
        />
      </main>

      <LandingFooter
        tagline={t('footer.tagline')}
        copyright={t('footer.copyright', { year })}
      />
    </div>
  )
}
