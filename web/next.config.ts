import type { NextConfig } from 'next'
import createNextIntlPlugin from 'next-intl/plugin'

// 创建国际化插件
const withNextIntl = createNextIntlPlugin()

const nextConfig: NextConfig = {
  // 其他配置可以添加在这里
}

export default withNextIntl(nextConfig)
