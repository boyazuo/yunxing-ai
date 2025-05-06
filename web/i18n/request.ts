import { getRequestConfig } from 'next-intl/server'
import { getMessages } from './messages'
import { routing } from './routing'

// 从配置文件加载消息
export default getRequestConfig(async ({ requestLocale }) => {
  // 获取请求的语言
  const requested = await requestLocale
  const locale = requested && routing.locales.includes(requested) ? requested : routing.defaultLocale

  // 返回配置
  return {
    locale,
    // 加载语言消息
    messages: getMessages(locale),
    // 设置时区，可根据需要修改
    // timeZone: 'Asia/Shanghai',
    // 提供日期、时间和数字的全局格式配置
    formats: {
      dateTime: {
        short: {
          day: 'numeric',
          month: 'short',
          year: 'numeric',
        },
        medium: {
          day: 'numeric',
          month: 'long',
          year: 'numeric',
        },
        long: {
          day: 'numeric',
          month: 'long',
          year: 'numeric',
          hour: 'numeric',
          minute: 'numeric',
        },
      },
      number: {
        precise: {
          maximumFractionDigits: 2,
        },
        currency: {
          style: 'currency',
          currency: 'CNY',
        },
      },
    },
  }
})
