// 合并所有语言的消息
import enAuth from './en/auth.json'
import enIndex from './en/index.json'
import enLanding from './en/landing.json'

import zhAuth from './zh/auth.json'
import zhIndex from './zh/index.json'
import zhLanding from './zh/landing.json'

const messagesMap = {
  en: {
    ...enAuth,
    ...enIndex,
    ...enLanding,
  },
  zh: {
    ...zhAuth,
    ...zhIndex,
    ...zhLanding,
  },
}

export function getMessages(locale: string) {
  return messagesMap[locale as keyof typeof messagesMap] || {}
}
