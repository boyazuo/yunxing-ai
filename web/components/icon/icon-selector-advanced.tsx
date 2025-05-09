'use client'

import {
  CheckCircleFillIcon,
  CrossIcon,
  GitIcon,
  ImageIcon,
  LogoAnthropic,
  LogoGoogle,
  LogoOpenAI,
  PythonIcon,
  UploadIcon,
  VercelIcon,
} from '@/components/icon/icons'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { cn } from '@/lib/utils'
import { useEffect, useRef, useState } from 'react'

// 所有可用的图标组件
const AVAILABLE_ICONS = [
  { id: 'openai', name: 'OpenAI', component: LogoOpenAI },
  { id: 'google', name: 'Google', component: LogoGoogle },
  { id: 'anthropic', name: 'Anthropic', component: LogoAnthropic },
  { id: 'git', name: 'Git', component: GitIcon },
  { id: 'vercel', name: 'Vercel', component: VercelIcon },
  { id: 'python', name: 'Python', component: PythonIcon },
]

// Icon子组件，用于渲染图标
interface IconProps {
  iconId?: string
  customIconUrl?: string
  size?: number
}

const Icon = ({ iconId, customIconUrl, size = 16 }: IconProps) => {
  // 如果有自定义图标URL，优先使用
  if (customIconUrl) {
    return <img src={customIconUrl} alt="自定义图标" className="w-full h-full" style={{ width: size, height: size }} />
  }

  // 如果有图标ID，查找并使用对应的内置图标
  if (iconId) {
    const icon = AVAILABLE_ICONS.find((i) => i.id === iconId)
    if (icon) {
      const IconComponent = icon.component
      return <IconComponent size={size} />
    }
  }

  // 默认图标
  return <ImageIcon size={size} />
}

interface IconSelectorAdvancedProps {
  selectedIcon?: string
  customIconUrl?: string
  onSelect: (iconId: string, customIconUrl?: string) => void
  triggerClassName?: string
}

const IconSelectorAdvanced = ({
  selectedIcon,
  customIconUrl,
  onSelect,
  triggerClassName,
}: IconSelectorAdvancedProps) => {
  const [search, setSearch] = useState('')
  const [open, setOpen] = useState(false)
  const [activeTab, setActiveTab] = useState<'built-in' | 'custom'>('built-in')
  const [filteredIcons, setFilteredIcons] = useState(AVAILABLE_ICONS)
  const [uploadedSvgUrl, setUploadedSvgUrl] = useState<string | undefined>(customIconUrl)
  const [uploadError, setUploadError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  // 当外部customIconUrl改变时更新本地状态
  useEffect(() => {
    setUploadedSvgUrl(customIconUrl)
  }, [customIconUrl])

  // 获取当前选中的图标组件
  const selectedIconComponent =
    selectedIcon && !customIconUrl ? AVAILABLE_ICONS.find((icon) => icon.id === selectedIcon)?.component : undefined

  // 根据搜索词过滤图标
  useEffect(() => {
    if (!search) {
      setFilteredIcons(AVAILABLE_ICONS)
      return
    }

    const filtered = AVAILABLE_ICONS.filter((icon) => icon.name.toLowerCase().includes(search.toLowerCase()))
    setFilteredIcons(filtered)
  }, [search])

  // 处理图标选择
  const handleIconSelect = (iconId: string) => {
    onSelect(iconId)
    setUploadedSvgUrl(undefined)
    setOpen(false)
  }

  // 处理SVG文件上传
  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    setUploadError(null)

    if (!file) return

    // 验证文件类型
    if (file.type !== 'image/svg+xml') {
      setUploadError('仅支持SVG格式图标')
      return
    }

    // 验证文件大小 (最大 100KB)
    if (file.size > 100 * 1024) {
      setUploadError('文件大小不能超过100KB')
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      const result = e.target?.result as string
      setUploadedSvgUrl(result)
    }
    reader.readAsDataURL(file)
  }

  // 处理自定义图标选择
  const handleCustomIconSelect = () => {
    if (uploadedSvgUrl) {
      onSelect('custom', uploadedSvgUrl)
      setOpen(false)
    }
  }

  // 清除上传的图标
  const clearUploadedIcon = () => {
    setUploadedSvgUrl(undefined)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" className={cn('w-full flex justify-between items-center', triggerClassName)}>
          <span className="mr-2">
            {customIconUrl
              ? '自定义图标'
              : selectedIcon
                ? AVAILABLE_ICONS.find((i) => i.id === selectedIcon)?.name
                : '选择图标'}
          </span>
          {selectedIconComponent ? (
            <div className="flex items-center justify-center w-6 h-6">{selectedIconComponent({ size: 18 })}</div>
          ) : customIconUrl ? (
            <div className="flex items-center justify-center w-6 h-6">
              <img src={customIconUrl} alt="自定义图标" className="w-4 h-4" />
            </div>
          ) : null}
        </Button>
      </DialogTrigger>

      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>选择提供商图标</DialogTitle>
        </DialogHeader>

        <Tabs
          defaultValue="built-in"
          value={activeTab}
          onValueChange={(value) => setActiveTab(value as 'built-in' | 'custom')}
          className="mt-4"
        >
          <TabsList className="grid grid-cols-2 mb-4">
            <TabsTrigger value="built-in">内置图标</TabsTrigger>
            <TabsTrigger value="custom">自定义上传</TabsTrigger>
          </TabsList>

          <TabsContent value="built-in">
            <div className="flex items-center space-x-2 mb-4">
              <Input
                placeholder="搜索图标..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="h-9"
              />
            </div>

            <div className="grid grid-cols-3 gap-4">
              {filteredIcons.map((icon) => {
                const IconComponent = icon.component
                return (
                  <button
                    key={icon.id}
                    type="button"
                    className={cn(
                      'flex flex-col items-center justify-center p-4 rounded-md border transition-all',
                      selectedIcon === icon.id && !customIconUrl
                        ? 'border-blue-500 bg-blue-50 dark:bg-blue-950 dark:border-blue-700'
                        : 'border-gray-200 hover:border-gray-300 dark:border-gray-800 dark:hover:border-gray-700',
                    )}
                    onClick={() => handleIconSelect(icon.id)}
                  >
                    <div className="flex items-center justify-center h-10 w-10 mb-2">
                      <IconComponent size={24} />
                    </div>
                    <span className="text-xs">{icon.name}</span>
                  </button>
                )
              })}
            </div>

            {filteredIcons.length === 0 && <div className="text-center py-8 text-gray-500">没有找到匹配的图标</div>}
          </TabsContent>

          <TabsContent value="custom">
            <div className="space-y-4">
              <div className="border-2 border-dashed border-gray-300 dark:border-gray-700 rounded-lg p-6 text-center">
                {uploadedSvgUrl ? (
                  <div className="flex flex-col items-center">
                    <div className="relative mb-4">
                      <img src={uploadedSvgUrl} alt="上传的SVG图标" className="w-16 h-16" />
                      <button
                        type="button"
                        onClick={clearUploadedIcon}
                        className="absolute -top-2 -right-2 p-1 bg-white dark:bg-gray-900 rounded-full border border-gray-300 dark:border-gray-700"
                      >
                        <CrossIcon size={12} />
                      </button>
                    </div>
                    <p className="text-sm text-green-600 dark:text-green-400 flex items-center">
                      <CheckCircleFillIcon size={14} />
                      SVG图标已上传
                    </p>
                  </div>
                ) : (
                  <>
                    <ImageIcon size={36} />
                    <p className="mb-2 text-sm text-gray-600 dark:text-gray-400">上传SVG格式图标</p>
                    <p className="text-xs text-gray-500 mb-4">最大100KB，仅支持SVG格式</p>
                    <input
                      ref={fileInputRef}
                      type="file"
                      accept=".svg"
                      onChange={handleFileChange}
                      className="hidden"
                      id="svg-upload"
                    />
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => fileInputRef.current?.click()}
                      className="flex items-center"
                    >
                      <UploadIcon size={14} />
                      选择文件
                    </Button>
                  </>
                )}
              </div>

              {uploadError && (
                <div className="text-red-500 text-sm p-2 bg-red-50 dark:bg-red-950 border border-red-200 dark:border-red-900 rounded-md">
                  {uploadError}
                </div>
              )}
            </div>
          </TabsContent>
        </Tabs>

        <DialogFooter className="mt-6">
          <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
            取消
          </Button>
          {activeTab === 'custom' && (
            <Button type="button" disabled={!uploadedSvgUrl} onClick={handleCustomIconSelect}>
              确认选择
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

// 将Icon组件作为静态属性添加到IconSelectorAdvanced
IconSelectorAdvanced.Icon = Icon

export { IconSelectorAdvanced, type IconSelectorAdvancedProps }
