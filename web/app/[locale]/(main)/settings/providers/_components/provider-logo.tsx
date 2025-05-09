'use client'

import { AVAILABLE_ICONS, IconSelector } from '@/components/icon/icon-selector'

// 模型 logo 组件
export function ProviderLogo({ provider, logo }: { provider: string; logo?: string }) {
  // 检查logo是否为有效的图标ID
  const isLogoIconId = logo && AVAILABLE_ICONS.some((icon: { id: string }) => icon.id === logo)

  // 如果logo是有效的图标ID，使用图标组件
  if (isLogoIconId) {
    return (
      <div className="w-7 h-7 rounded-full bg-background flex items-center justify-center shadow-sm">
        <IconSelector.Icon iconId={logo} size={18} />
      </div>
    )
  }

  // 默认情况下显示提供商名称的首字母
  return (
    <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-gray-500 to-gray-600 flex items-center justify-center text-white shadow-sm text-xs">
      {provider.charAt(0)}
    </div>
  )
}
