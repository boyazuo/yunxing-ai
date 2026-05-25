import type { NextConfig } from 'next'
import createNextIntlPlugin from 'next-intl/plugin'

// 创建国际化插件
const withNextIntl = createNextIntlPlugin()

const nextConfig: NextConfig = {
  output: 'standalone',
}

export default withNextIntl(nextConfig)
