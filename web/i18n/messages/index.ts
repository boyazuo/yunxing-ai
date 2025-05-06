// 合并所有语言的消息
import enAuth from './en/auth.json'
import enIndex from './en/index.json'

import zhAuth from './zh/auth.json'
import zhIndex from './zh/index.json'

const messagesMap = {
  en: {
    ...enAuth,
    ...enIndex,
  },
  zh: {
    ...zhAuth,
    ...zhIndex,
  },
}

export function getMessages(locale: string) {
  return messagesMap[locale as keyof typeof messagesMap] || {}
}
