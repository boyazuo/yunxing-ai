'use client'

import {
  AttachmentIcon,
  BotIcon,
  BoxIcon,
  CheckCircleFillIcon,
  CheckedSquare,
  ChevronDownIcon,
  ClockRewind,
  CodeIcon,
  CopyIcon,
  CrossIcon,
  CrossSmallIcon,
  DeltaIcon,
  DownloadIcon,
  EyeIcon,
  FileIcon,
  FullscreenIcon,
  GPSIcon,
  GitIcon,
  GlobeIcon,
  HomeIcon,
  ImageIcon,
  InfoIcon,
  InvoiceIcon,
  LineChartIcon,
  LoaderIcon,
  LockIcon,
  LogoAnthropic,
  LogoGoogle,
  LogoOpenAI,
  LogsIcon,
  MenuIcon,
  MessageIcon,
  MoreHorizontalIcon,
  MoreIcon,
  PaperclipIcon,
  PenIcon,
  PencilEditIcon,
  PlayIcon,
  PlusIcon,
  PythonIcon,
  RedoIcon,
  RouteIcon,
  ShareIcon,
  SidebarLeftIcon,
  SparklesIcon,
  StopIcon,
  SummarizeIcon,
  TerminalIcon,
  TerminalWindowIcon,
  ThumbDownIcon,
  ThumbUpIcon,
  TrashIcon,
  UncheckedSquare,
  UndoIcon,
  UploadIcon,
  UserIcon,
  VercelIcon,
  WarningIcon,
} from '@/components/icon/icons'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'
import { useEffect, useState } from 'react'

// 将图标按类别分组
const ICON_CATEGORIES = [
  {
    name: '品牌图标',
    icons: [
      { id: 'openai', name: 'OpenAI', component: LogoOpenAI },
      { id: 'google', name: 'Google', component: LogoGoogle },
      { id: 'anthropic', name: 'Anthropic', component: LogoAnthropic },
      { id: 'git', name: 'Git', component: GitIcon },
      { id: 'vercel', name: 'Vercel', component: VercelIcon },
      { id: 'python', name: 'Python', component: PythonIcon },
    ],
  },
  {
    name: '导航图标',
    icons: [
      { id: 'home', name: '首页', component: HomeIcon },
      { id: 'menu', name: '菜单', component: MenuIcon },
      { id: 'sidebarLeft', name: '左侧边栏', component: SidebarLeftIcon },
      { id: 'route', name: '路由', component: RouteIcon },
      { id: 'globe', name: '全球', component: GlobeIcon },
      { id: 'gps', name: 'GPS定位', component: GPSIcon },
      { id: 'chevronDown', name: '向下箭头', component: ChevronDownIcon },
    ],
  },
  {
    name: '操作图标',
    icons: [
      { id: 'plus', name: '加号', component: PlusIcon },
      { id: 'pencilEdit', name: '编辑', component: PencilEditIcon },
      { id: 'pen', name: '钢笔', component: PenIcon },
      { id: 'trash', name: '删除', component: TrashIcon },
      { id: 'copy', name: '复制', component: CopyIcon },
      { id: 'cross', name: '关闭', component: CrossIcon },
      { id: 'crossSmall', name: '小关闭', component: CrossSmallIcon },
      { id: 'more', name: '更多', component: MoreIcon },
      { id: 'moreHorizontal', name: '更多(水平)', component: MoreHorizontalIcon },
      { id: 'share', name: '分享', component: ShareIcon },
      { id: 'download', name: '下载', component: DownloadIcon },
      { id: 'upload', name: '上传', component: UploadIcon },
      { id: 'eye', name: '查看', component: EyeIcon },
      { id: 'play', name: '播放', component: PlayIcon },
      { id: 'stop', name: '停止', component: StopIcon },
      { id: 'fullscreen', name: '全屏', component: FullscreenIcon },
      { id: 'undo', name: '撤销', component: UndoIcon },
      { id: 'redo', name: '重做', component: RedoIcon },
    ],
  },
  {
    name: '状态图标',
    icons: [
      { id: 'loader', name: '加载中', component: LoaderIcon },
      { id: 'checkCircleFill', name: '已完成', component: CheckCircleFillIcon },
      { id: 'checkedSquare', name: '已选中方框', component: CheckedSquare },
      { id: 'uncheckedSquare', name: '未选中方框', component: UncheckedSquare },
      { id: 'warning', name: '警告', component: WarningIcon },
      { id: 'info', name: '信息', component: InfoIcon },
      { id: 'lock', name: '锁定', component: LockIcon },
      { id: 'thumbUp', name: '点赞', component: ThumbUpIcon },
      { id: 'thumbDown', name: '踩', component: ThumbDownIcon },
      { id: 'sparkles', name: '星星', component: SparklesIcon },
    ],
  },
  {
    name: '内容图标',
    icons: [
      { id: 'file', name: '文件', component: FileIcon },
      { id: 'code', name: '代码', component: CodeIcon },
      { id: 'terminal', name: '终端', component: TerminalIcon },
      { id: 'terminalWindow', name: '终端窗口', component: TerminalWindowIcon },
      { id: 'image', name: '图片', component: ImageIcon },
      { id: 'box', name: '盒子', component: BoxIcon },
      { id: 'attachment', name: '附件', component: AttachmentIcon },
      { id: 'paperclip', name: '回形针', component: PaperclipIcon },
      { id: 'message', name: '消息', component: MessageIcon },
      { id: 'logs', name: '日志', component: LogsIcon },
      { id: 'summarize', name: '摘要', component: SummarizeIcon },
      { id: 'invoice', name: '发票', component: InvoiceIcon },
      { id: 'lineChart', name: '折线图表', component: LineChartIcon },
      { id: 'delta', name: '三角形', component: DeltaIcon },
      { id: 'clockRewind', name: '时钟回溯', component: ClockRewind },
    ],
  },
  {
    name: '用户图标',
    icons: [
      { id: 'user', name: '用户', component: UserIcon },
      { id: 'bot', name: '机器人', component: BotIcon },
    ],
  },
]

// 所有可用的图标组件 - 用于搜索和查找
export const AVAILABLE_ICONS = ICON_CATEGORIES.flatMap((category) => category.icons)

// Icon子组件，用于渲染图标
interface IconProps {
  iconId?: string
  size?: number
}

const Icon = ({ iconId, size = 16 }: IconProps) => {
  // 如果有图标ID，查找并使用对应的内置图标
  if (iconId) {
    const icon = AVAILABLE_ICONS.find((i) => i.id === iconId)
    if (icon) {
      const IconComponent = icon.component
      return <IconComponent size={size} />
    }
  }

  // 默认不渲染任何内容
  return null
}

interface IconSelectorProps {
  selectedIcon?: string
  onIconSelect: (iconId: string) => void
  triggerClassName?: string
}

const IconSelector = ({ selectedIcon, onIconSelect, triggerClassName }: IconSelectorProps) => {
  const [search, setSearch] = useState('')
  const [open, setOpen] = useState(false)
  const [filteredIcons, setFilteredIcons] = useState<typeof AVAILABLE_ICONS>([])
  const [filteredCategories, setFilteredCategories] = useState<typeof ICON_CATEGORIES>(ICON_CATEGORIES)

  // 获取当前选中的图标组件
  const selectedIconComponent = selectedIcon
    ? AVAILABLE_ICONS.find((icon) => icon.id === selectedIcon)?.component
    : undefined

  // 根据搜索词过滤图标
  useEffect(() => {
    if (!search) {
      setFilteredIcons([])
      setFilteredCategories(ICON_CATEGORIES)
      return
    }

    const filtered = AVAILABLE_ICONS.filter(
      (icon) =>
        icon.name.toLowerCase().includes(search.toLowerCase()) || icon.id.toLowerCase().includes(search.toLowerCase()),
    )
    setFilteredIcons(filtered)
    setFilteredCategories([])
  }, [search])

  // 处理图标选择
  const handleIconSelect = (iconId: string) => {
    onIconSelect(iconId)
    setOpen(false)
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" className={cn('w-full flex justify-between items-center', triggerClassName)}>
          <span className="mr-2">
            {selectedIcon ? AVAILABLE_ICONS.find((i) => i.id === selectedIcon)?.name : '选择图标'}
          </span>
          {selectedIconComponent && (
            <div className="flex items-center justify-center w-6 h-6">{selectedIconComponent({ size: 18 })}</div>
          )}
        </Button>
      </DialogTrigger>

      <DialogContent className="sm:max-w-[500px] md:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>选择图标</DialogTitle>
        </DialogHeader>

        <div className="flex items-center space-x-2 my-2">
          <Input placeholder="搜索图标..." value={search} onChange={(e) => setSearch(e.target.value)} className="h-8" />
        </div>

        <div className="max-h-[60vh] overflow-y-auto pr-1">
          {/* 搜索结果 */}
          {filteredIcons.length > 0 && (
            <div className="mb-3">
              <h3 className="text-xs font-medium mb-1.5 text-gray-500 dark:text-gray-400">搜索结果</h3>
              <div className="grid grid-cols-8 md:grid-cols-10 lg:grid-cols-12 gap-1.5">
                {filteredIcons.map((icon) => {
                  const IconComponent = icon.component
                  return (
                    <button
                      key={icon.id}
                      type="button"
                      title={icon.name}
                      className={cn(
                        'flex items-center justify-center p-0.5 rounded-md border transition-all h-8 w-8 mx-auto',
                        selectedIcon === icon.id
                          ? 'border-blue-500 bg-blue-50 dark:bg-blue-950 dark:border-blue-700'
                          : 'border-gray-200 hover:border-gray-300 dark:border-gray-800 dark:hover:border-gray-700',
                      )}
                      onClick={() => handleIconSelect(icon.id)}
                    >
                      <IconComponent size={16} />
                    </button>
                  )
                })}
              </div>
            </div>
          )}

          {/* 分类显示 */}
          {filteredCategories.map((category) => (
            <div key={category.name} className="mb-3">
              <h3 className="text-xs font-medium mb-1.5 text-gray-500 dark:text-gray-400">{category.name}</h3>
              <div className="grid grid-cols-8 md:grid-cols-10 lg:grid-cols-12 gap-1.5">
                {category.icons.map((icon) => {
                  const IconComponent = icon.component
                  return (
                    <button
                      key={icon.id}
                      type="button"
                      title={icon.name}
                      className={cn(
                        'flex items-center justify-center p-0.5 rounded-md border transition-all h-8 w-8 mx-auto',
                        selectedIcon === icon.id
                          ? 'border-blue-500 bg-blue-50 dark:bg-blue-950 dark:border-blue-700'
                          : 'border-gray-200 hover:border-gray-300 dark:border-gray-800 dark:hover:border-gray-700',
                      )}
                      onClick={() => handleIconSelect(icon.id)}
                    >
                      <IconComponent size={16} />
                    </button>
                  )
                })}
              </div>
            </div>
          ))}

          {filteredIcons.length === 0 && filteredCategories.length === 0 && (
            <div className="text-center py-4 text-gray-500">没有找到匹配的图标</div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}

export { IconSelector, type IconSelectorProps }

// 将Icon组件作为静态属性添加到IconSelector
IconSelector.Icon = Icon
