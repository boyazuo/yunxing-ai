'use client'

import { IconSelector } from '@/components/icon/icon-selector'
import { GitIcon, LogoAnthropic, LogoGoogle, LogoOpenAI, PythonIcon, VercelIcon } from '@/components/icon/icons'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useState } from 'react'

// 图标映射表，用于根据ID获取对应的组件
const iconMap = {
  openai: LogoOpenAI,
  google: LogoGoogle,
  anthropic: LogoAnthropic,
  git: GitIcon,
  vercel: VercelIcon,
  python: PythonIcon,
}

export const IconSelectorDemo = () => {
  const [selectedIconId, setSelectedIconId] = useState<string>('openai')

  // 获取当前选中的图标组件
  const SelectedIcon = selectedIconId ? iconMap[selectedIconId as keyof typeof iconMap] : undefined

  return (
    <div className="container max-w-2xl mx-auto py-10">
      <Card>
        <CardHeader>
          <CardTitle>图标选择器示例</CardTitle>
          <CardDescription>用于选择模型提供商的Logo图标，支持搜索和预览</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <p className="text-sm font-medium">选择提供商图标：</p>
            <IconSelector selectedIcon={selectedIconId} onIconSelect={setSelectedIconId} />
          </div>

          <div className="border rounded-lg p-6">
            <h3 className="text-lg font-medium mb-4">当前选中的图标</h3>
            <div className="flex flex-col items-center justify-center p-6">
              {SelectedIcon && (
                <div className="mb-4">
                  <SelectedIcon size={48} />
                </div>
              )}
              <p className="text-sm text-gray-500">{selectedIconId ? `已选择: ${selectedIconId}` : '未选择图标'}</p>
            </div>
          </div>

          <div className="text-sm text-gray-500">
            <p>使用说明:</p>
            <ul className="list-disc list-inside ml-2 mt-2">
              <li>点击选择图标按钮打开图标选择器</li>
              <li>可以通过搜索框筛选图标</li>
              <li>点击图标进行选择</li>
              <li>选中的图标ID会通过onIconSelect回调返回</li>
            </ul>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

export default IconSelectorDemo
