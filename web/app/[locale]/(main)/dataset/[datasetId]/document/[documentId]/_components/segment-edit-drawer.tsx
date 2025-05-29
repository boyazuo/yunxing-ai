'use client'

import { Check, FileText, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { Textarea } from '@/components/ui/textarea'

import { segmentService } from '@/api/document'
import type { DocumentSegment } from '@/types/document'

// 格式化日期时间
function formatDateTime(dateTimeStr?: string) {
  if (!dateTimeStr) return '-'
  return new Date(dateTimeStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 分段编辑组件
interface SegmentEditDrawerProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  editingSegment: DocumentSegment | null
  onSave: () => void
}

export function SegmentEditDrawer({ open, onOpenChange, editingSegment, onSave }: SegmentEditDrawerProps) {
  const [editTitle, setEditTitle] = useState('')
  const [editContent, setEditContent] = useState('')
  const [isSaving, setIsSaving] = useState(false)

  // 当编辑的分段改变时，更新表单数据
  useEffect(() => {
    if (editingSegment) {
      setEditTitle(editingSegment.title || '')
      setEditContent(editingSegment.content)
    }
  }, [editingSegment])

  // 保存编辑
  const handleSaveEdit = async () => {
    if (!editingSegment || !editContent.trim() || !editTitle.trim()) {
      if (!editTitle.trim()) {
        toast.error('分段标题不能为空')
      } else if (!editContent.trim()) {
        toast.error('分段内容不能为空')
      }
      return
    }

    try {
      setIsSaving(true)

      await segmentService.updateSegment(editingSegment.segmentId.toString(), {
        title: editTitle.trim(),
        content: editContent.trim(),
      })

      toast.success('分段更新成功')
      onOpenChange(false)
      onSave()
    } catch (error) {
      console.error('更新分段失败', error)
      toast.error('更新分段失败')
    } finally {
      setIsSaving(false)
    }
  }

  // 取消编辑
  const handleCancelEdit = () => {
    onOpenChange(false)
    setEditTitle('')
    setEditContent('')
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[750px] sm:max-w-[750px] flex flex-col p-0">
        <SheetHeader className="px-5 py-3 border-b border-gray-100 bg-white">
          <SheetTitle>编辑分段</SheetTitle>
          <SheetDescription>修改分段的标题和内容</SheetDescription>
        </SheetHeader>

        {/* 表单内容区域 */}
        <div className="flex-1 overflow-y-auto">
          <div className="p-5 space-y-4">
            {/* 标题输入区域 */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="edit-title" className="text-sm font-medium text-gray-900">
                  分段标题
                </Label>
                <span className="text-xs text-red-600 bg-red-50 px-2 py-0.5 rounded font-medium flex items-center">
                  <span className="w-1 h-1 bg-red-500 rounded-full mr-1" />
                  必填
                </span>
              </div>
              <div className="relative">
                <Input
                  id="edit-title"
                  value={editTitle}
                  onChange={(e) => setEditTitle(e.target.value)}
                  placeholder="请输入分段标题..."
                  className="h-10 text-sm border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all duration-200"
                  maxLength={100}
                />
                <div className="absolute right-2.5 top-1/2 transform -translate-y-1/2 text-xs text-gray-400">
                  {editTitle.length}/100
                </div>
              </div>
              <p className="text-xs text-gray-500">标题将帮助您更好地组织和识别分段内容</p>
            </div>

            {/* 内容输入区域 */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="edit-content" className="text-sm font-medium text-gray-900">
                  分段内容
                </Label>
                <span className="text-xs text-red-600 bg-red-50 px-2 py-0.5 rounded font-medium flex items-center">
                  <span className="w-1 h-1 bg-red-500 rounded-full mr-1" />
                  必填
                </span>
              </div>
              <div className="relative">
                <Textarea
                  id="edit-content"
                  value={editContent}
                  onChange={(e) => setEditContent(e.target.value)}
                  placeholder="请输入分段的具体内容..."
                  className="min-h-[320px] text-sm leading-relaxed resize-none border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all duration-200"
                  maxLength={10000}
                />
                {/* 字符计数器 */}
                <div className="absolute bottom-3 right-3 bg-white/95 backdrop-blur-sm px-2.5 py-1 rounded text-xs text-gray-500 border border-gray-200 shadow-sm">
                  {editContent.length.toLocaleString()}/10,000
                </div>
              </div>
              <div className="bg-blue-50 rounded-lg p-2.5 space-y-1">
                <p className="text-xs text-blue-700 font-medium">💡 编辑建议</p>
                <ul className="text-xs text-blue-600 space-y-0.5 ml-3">
                  <li>• 内容应该完整且有意义，避免过于碎片化</li>
                  <li>• 建议每个分段包含一个完整的概念或主题</li>
                </ul>
              </div>
            </div>
          </div>

          {/* 分段信息区域 */}
          {editingSegment && (
            <div className="mx-5 mb-4">
              <div className="bg-gray-50 rounded-lg p-3 border border-gray-100">
                <div className="flex items-center mb-2">
                  <FileText className="mr-1.5 h-3.5 w-3.5 text-gray-600" />
                  <h4 className="text-xs font-medium text-gray-900">分段信息</h4>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-1.5">
                    <div className="flex justify-between items-center">
                      <span className="text-xs text-gray-500">创建者</span>
                      <span className="text-xs text-gray-900 font-medium">{editingSegment.creatorUsername}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-xs text-gray-500">创建时间</span>
                      <span className="text-xs text-gray-900">{formatDateTime(editingSegment.createTime)}</span>
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <div className="flex justify-between items-center">
                      <span className="text-xs text-gray-500">原始长度</span>
                      <span className="text-xs text-gray-900 font-medium">
                        {editingSegment.contentLength.toLocaleString()} 字符
                      </span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-xs text-gray-500">当前长度</span>
                      <span
                        className={`text-xs font-medium ${editContent.length !== editingSegment.contentLength ? 'text-orange-600' : 'text-gray-900'}`}
                      >
                        {editContent.length.toLocaleString()} 字符
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 底部操作区域 */}
        <div className="px-5 py-3 border-t border-gray-100 bg-gray-50/50">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              {editContent.trim() && editTitle.trim() ? (
                <div className="flex items-center text-green-600 bg-green-50 px-2.5 py-1 rounded">
                  <Check className="mr-1 h-3 w-3" />
                  <span className="text-xs font-medium">标题和内容已填写</span>
                </div>
              ) : (
                <div className="flex items-center text-red-600 bg-red-50 px-2.5 py-1 rounded">
                  <X className="mr-1 h-3 w-3" />
                  <span className="text-xs font-medium">
                    {!editTitle.trim() && !editContent.trim()
                      ? '请填写标题和内容'
                      : !editTitle.trim()
                        ? '请填写标题'
                        : '请填写内容'}
                  </span>
                </div>
              )}
            </div>
            <div className="flex space-x-2.5">
              <Button
                variant="outline"
                onClick={handleCancelEdit}
                disabled={isSaving}
                className="px-4 h-9 text-sm border-gray-200 hover:bg-gray-50"
              >
                取消
              </Button>
              <Button
                onClick={handleSaveEdit}
                disabled={isSaving || !editContent.trim() || !editTitle.trim()}
                className="bg-blue-600 hover:bg-blue-700 px-4 h-9 text-sm min-w-[100px] shadow-sm"
              >
                {isSaving ? (
                  <div className="flex items-center">
                    <div className="animate-spin rounded-full h-3.5 w-3.5 border-2 border-white border-t-transparent mr-1.5" />
                    保存中...
                  </div>
                ) : (
                  <div className="flex items-center">
                    <Check className="mr-1.5 h-3.5 w-3.5" />
                    保存更改
                  </div>
                )}
              </Button>
            </div>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  )
}
