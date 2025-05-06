import { createNavigation } from 'next-intl/navigation'
import { defineRouting } from 'next-intl/routing'

import { defaultLocale, localePrefix, locales } from './locale'

export const routing = defineRouting({
  locales,
  defaultLocale,
  localePrefix,

  // 可选：如果需要自定义域名路由，可以取消下面的注释并配置
  // domains: [
  //   {
  //     domain: 'example.com',
  //     defaultLocale: 'en',
  //     locales: ['en']
  //   },
  //   {
  //     domain: 'example.cn',
  //     defaultLocale: 'zh',
  //     locales: ['zh']
  //   }
  // ]
})

export const { Link, redirect, usePathname, useRouter } = createNavigation(routing)
