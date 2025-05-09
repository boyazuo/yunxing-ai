'use client'

import { IconSelectorAdvanced } from '@/components/icon/icon-selector-advanced'
import { GitIcon, LogoAnthropic, LogoGoogle, LogoOpenAI, PythonIcon, VercelIcon } from '@/components/icon/icons'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
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

export const IconSelectorAdvancedDemo = () => {
  const [selectedIconId, setSelectedIconId] = useState<string>('openai')
  const [customIconUrl, setCustomIconUrl] = useState<string | undefined>()
  const [selectedModelName, setSelectedModelName] = useState('OpenAI模型')

  // 处理图标选择
  const handleIconSelect = (iconId: string, customUrl?: string) => {
    setSelectedIconId(iconId)
    setCustomIconUrl(customUrl)

    // 根据图标ID设置模型名称
    if (iconId === 'custom') {
      setSelectedModelName('自定义模型')
    } else {
      // 根据选择的图标ID更新模型名称
      const iconName = iconMap[iconId as keyof typeof iconMap]
        ? AVAILABLE_ICONS.find((i) => i.id === iconId)?.name
        : '未知模型'
      setSelectedModelName(`${iconName}模型`)
    }
  }

  // 获取当前选中的图标组件
  const SelectedIcon = !customIconUrl && selectedIconId ? iconMap[selectedIconId as keyof typeof iconMap] : undefined

  // 可供选择的图标列表 (与IconSelectorAdvanced组件中保持一致)
  const AVAILABLE_ICONS = [
    { id: 'openai', name: 'OpenAI', component: LogoOpenAI },
    { id: 'google', name: 'Google', component: LogoGoogle },
    { id: 'anthropic', name: 'Anthropic', component: LogoAnthropic },
    { id: 'git', name: 'Git', component: GitIcon },
    { id: 'vercel', name: 'Vercel', component: VercelIcon },
    { id: 'python', name: 'Python', component: PythonIcon },
  ]

  return (
    <div className="container max-w-2xl mx-auto py-10">
      <Card>
        <CardHeader>
          <CardTitle>高级图标选择器示例</CardTitle>
          <CardDescription>支持选择内置图标或上传自定义SVG图标</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* 模拟的表单字段 */}
          <div className="space-y-4">
            <div className="grid gap-2">
              <Label htmlFor="model-name">模型名称</Label>
              <input
                id="model-name"
                type="text"
                value={selectedModelName}
                onChange={(e) => setSelectedModelName(e.target.value)}
                className="w-full px-3 py-2 border rounded-md"
              />
            </div>

            <div className="grid gap-2">
              <Label htmlFor="provider-icon">提供商图标</Label>
              <div id="provider-icon">
                <IconSelectorAdvanced
                  selectedIcon={selectedIconId}
                  customIconUrl={customIconUrl}
                  onSelect={handleIconSelect}
                />
              </div>
            </div>
          </div>

          {/* 预览区域 */}
          <div className="border rounded-lg p-6">
            <h3 className="text-lg font-medium mb-4">模型配置预览</h3>
            <div className="flex items-center p-4 bg-gray-50 dark:bg-gray-900 rounded-md">
              <div className="mr-4">
                {customIconUrl ? (
                  <img src={customIconUrl} alt="自定义图标" className="w-10 h-10" />
                ) : (
                  SelectedIcon && <SelectedIcon size={40} />
                )}
              </div>
              <div>
                <p className="font-medium">{selectedModelName}</p>
                <p className="text-sm text-gray-500">
                  {customIconUrl
                    ? '使用自定义上传图标'
                    : `使用内置${AVAILABLE_ICONS.find((i) => i.id === selectedIconId)?.name}图标`}
                </p>
              </div>
            </div>
          </div>

          {/* <Alert>
            <AlertDescription>
              <p className="font-medium mb-1">图标选择器特性:</p>
              <ul className="list-disc list-inside ml-2 mt-1 text-sm space-y-1">
                <li>支持选择内置提供商图标</li>
                <li>支持上传自定义SVG图标（最大100KB）</li>
                <li>提供搜索筛选功能</li>
                <li>适用于模型配置、应用设置等场景</li>
              </ul>
            </AlertDescription>
          </Alert> */}
        </CardContent>
      </Card>
    </div>
  )
}

export default IconSelectorAdvancedDemo
