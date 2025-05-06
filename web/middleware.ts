import createMiddleware from 'next-intl/middleware'
import { routing } from './i18n/routing'

// 创建国际化中间件
export default createMiddleware(routing)

// 配置中间件匹配的路径规则
export const config = {
  // 跳过所有不需要国际化的路径
  matcher: ['/((?!api|_next|_vercel|.*\\..*).*)'],
}
