'use client'

import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import type { EmojiMartData } from '@emoji-mart/data'
import data from '@emoji-mart/data'
import { init } from 'emoji-mart'
import { useMemo, useState } from 'react'

init({ data })

const emojiData = data as EmojiMartData

// emoji-mart 分类列表（与 data.categories 对应）
const emojiCategories = [
  { id: 'people', name: '人物', icon: '😀' },
  { id: 'nature', name: '自然', icon: '🐶' },
  { id: 'foods', name: '食物', icon: '🍔' },
  { id: 'activity', name: '活动', icon: '⚽' },
  { id: 'places', name: '地点', icon: '🏠' },
  { id: 'objects', name: '物品', icon: '💡' },
  { id: 'symbols', name: '符号', icon: '❤️' },
  { id: 'flags', name: '旗帜', icon: '🏁' },
]

// 推荐的8种背景色（参考设计色彩搭配）
const backgroundColors = [
  '#FAFAFA', // 浅灰
  '#FFF8E1', // 浅黄
  '#FFF3E0', // 浅橙
  '#E8F5E9', // 浅绿
  '#E0F7FA', // 浅青
  '#E3F2FD', // 浅蓝
  '#EDE7F6', // 浅紫
  '#FCE4EC', // 浅粉
]

interface Emoji {
  id: string
  name: string
  native: string
  keywords: string[]
}

// 为 AppFormDialog 提供的 Emoji 对象接口
export interface EmojiObject {
  native: string
  bgColor: string
}

// 为 AppFormDialog 提供的选择器组件接口
export interface EmojiPickerProps {
  title?: string
  open: boolean
  onOpenChange: (open: boolean) => void
  onEmojiSelect: (emoji: EmojiObject) => void
}

export function EmojiPicker({ open, onOpenChange, onEmojiSelect, title = '选择图标' }: EmojiPickerProps) {
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState<string>('people')
  const [selectedEmoji, setSelectedEmoji] = useState<string | null>(null)
  const [bgColor, setBgColor] = useState(backgroundColors[0])

  // 从 data 中提取当前分类的 emoji 列表
  const emojis = useMemo(() => {
    const cat = emojiData.categories.find((c) => c.id === category)
    if (!cat) return [] as Emoji[]

    // cat.emojis 是 emoji id 数组，需从 data.emojis 取详情
    return cat.emojis
      .map((id: string) => {
        const e = emojiData.emojis[id]
        if (!e) return null

        // 从skin中获取native值
        const nativeValue = e.skins?.[0]?.native || ''

        return {
          id,
          name: e.name,
          native: nativeValue,
          keywords: e.keywords || [],
        } as Emoji
      })
      .filter((item): item is Emoji => item !== null)
      .filter((e) => {
        if (!search) return true
        const kw = `${e.keywords.join(' ')} ${e.name}`
        return kw.toLowerCase().includes(search.toLowerCase())
      })
  }, [category, search])

  // 选中 Emoji
  const handleSelectEmoji = (emoji: Emoji) => {
    setSelectedEmoji(emoji.native)
  }

  // 确认选择
  const handleConfirm = () => {
    if (selectedEmoji) {
      onEmojiSelect({ native: selectedEmoji, bgColor })
      setSearch('')
      setSelectedEmoji(null)
      setCategory('people')
      setBgColor(backgroundColors[0])
      onOpenChange(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="w-[420px] flex flex-col p-3 gap-2">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        {/* 顶部分类切换 */}
        <div className="flex space-x-1 mb-2 overflow-x-auto pb-1 border-b border-gray-200">
          {emojiCategories.map((cat) => (
            <button
              key={cat.id}
              className={`px-3 py-2 rounded-md ${category === cat.id ? 'bg-blue-500 text-white' : 'hover:bg-gray-200'}`}
              onClick={() => setCategory(cat.id)}
              type="button"
              title={cat.name}
              aria-label={cat.name}
            >
              {cat.icon}
            </button>
          ))}
        </div>

        {/* 搜索框 */}
        <Input
          type="search"
          placeholder="搜索 Emoji"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="mb-2"
          autoFocus
        />

        {/* Emoji 展示区域，带滚动条 */}
        <div className="flex flex-wrap content-start p-[1px] gap-1 overflow-y-auto" style={{ height: '210px' }}>
          {emojis.length === 0 && <p className="text-gray-500">无匹配结果</p>}
          {emojis.map((emoji) => (
            <button
              key={emoji.id}
              type="button"
              className={`w-10 h-10 text-2xl p-1 rounded-md ${
                selectedEmoji === emoji.native ? 'ring-1 ring-blue-500' : 'hover:bg-gray-100'
              }`}
              onClick={() => handleSelectEmoji(emoji)}
              style={{ backgroundColor: selectedEmoji === emoji.native ? bgColor : 'transparent' }}
              aria-label={emoji.name}
              title={emoji.name}
            >
              {emoji.native}
            </button>
          ))}
        </div>

        {/* 底部背景色选择 */}
        <div className="space-y-3 mt-2 mb-3">
          <h3 className="font-medium text-sm">选择样式</h3>
          <div className="flex flex-wrap gap-2">
            {backgroundColors.map((color) => (
              <button
                key={color}
                type="button"
                className={`w-10 h-10 text-2xl rounded-lg flex items-center justify-center ${
                  bgColor === color ? 'ring-1 ring-blue-500' : ''
                }`}
                style={{ backgroundColor: color }}
                onClick={() => setBgColor(color)}
                aria-label={`选择背景色 ${color}`}
              >
                {selectedEmoji || '😊'}
              </button>
            ))}
          </div>
        </div>

        {/* 底部按钮 */}
        <DialogFooter className="flex justify-end space-x-2">
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            取消
          </Button>
          <Button onClick={handleConfirm} disabled={!selectedEmoji}>
            确定
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
