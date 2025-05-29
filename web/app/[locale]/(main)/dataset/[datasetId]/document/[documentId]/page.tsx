'use client'

import { ArrowLeft, Check, Edit, FileText, MoreHorizontal, Search, Trash2, X } from 'lucide-react'
import { useSession } from 'next-auth/react'
import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'
import { useCallback, useEffect, useState } from 'react'
import { toast } from 'sonner'

import { ConfirmDialog } from '@/components/blocks/confirm-dialog'
import { CustomPagination } from '@/components/blocks/pagination'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Textarea } from '@/components/ui/textarea'

import { documentService, segmentService } from '@/api/document'
import type { DatasetDocument, DocumentSegment } from '@/types/document'

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

// 截取内容预览
function truncateContent(content: string, maxLength = 100) {
  if (content.length <= maxLength) return content
  return `${content.substring(0, maxLength)}...`
}

export default function DocumentSegmentsPage() {
  const router = useRouter()
  const params = useParams()
  const { data: session, status } = useSession()

  const [document, setDocument] = useState<DatasetDocument | null>(null)
  const [segments, setSegments] = useState<DocumentSegment[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')

  const [page, setPage] = useState(1)
  const [pageSize] = useState(10)
  const [total, setTotal] = useState(0)

  // 选择状态
  const [selectedSegments, setSelectedSegments] = useState<Set<string | number>>(new Set())
  const [selectAll, setSelectAll] = useState(false)

  // 编辑抽屉状态
  const [editDrawerOpen, setEditDrawerOpen] = useState(false)
  const [editingSegment, setEditingSegment] = useState<DocumentSegment | null>(null)
  const [editTitle, setEditTitle] = useState('')
  const [editContent, setEditContent] = useState('')
  const [isSaving, setIsSaving] = useState(false)

  // 删除确认对话框状态
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [segmentToDelete, setSegmentToDelete] = useState<DocumentSegment | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)

  // 批量删除确认对话框状态
  const [batchDeleteDialogOpen, setBatchDeleteDialogOpen] = useState(false)
  const [isBatchDeleting, setIsBatchDeleting] = useState(false)

  // 从URL参数中获取ID
  const datasetId = params.datasetId as string
  const documentId = params.documentId as string

  // 加载文档信息
  const loadDocument = useCallback(async () => {
    try {
      if (!documentId) return

      const data = await documentService.getDocument(documentId)
      setDocument(data)
    } catch (error) {
      console.error('加载文档信息失败', error)
      toast.error('加载文档信息失败')
    }
  }, [documentId])

  // 加载分段列表
  const loadSegments = useCallback(async () => {
    try {
      if (!documentId) return

      setIsLoading(true)

      const response = await segmentService.getSegments(documentId, page, pageSize, searchTerm || undefined)
      setSegments(response.records)
      setTotal(response.total)
    } catch (error) {
      console.error('加载分段列表失败', error)
      toast.error('加载分段列表失败')
    } finally {
      setIsLoading(false)
    }
  }, [documentId, page, pageSize, searchTerm])

  // 首次加载
  useEffect(() => {
    if (status === 'authenticated' && documentId) {
      loadDocument()
      loadSegments()
    }
  }, [status, documentId, loadDocument, loadSegments])

  // 处理页码变化
  const handlePageChange = (newPage: number) => {
    setPage(newPage)
    setSelectedSegments(new Set())
    setSelectAll(false)
  }

  // 处理搜索
  const handleSearch = () => {
    setPage(1)
    setSelectedSegments(new Set())
    setSelectAll(false)
    loadSegments()
  }

  // 处理搜索输入
  const handleSearchInputChange = (value: string) => {
    setSearchTerm(value)
    if (!value.trim()) {
      // 如果搜索词为空，立即触发搜索以显示所有结果
      setPage(1)
      setSelectedSegments(new Set())
      setSelectAll(false)
    }
  }

  // 清空搜索
  const handleClearSearch = () => {
    setSearchTerm('')
    setPage(1)
    setSelectedSegments(new Set())
    setSelectAll(false)
  }

  // 监听搜索词变化，实现实时搜索（防抖）
  useEffect(() => {
    const timeoutId = setTimeout(() => {
      if (searchTerm !== undefined) {
        loadSegments()
      }
    }, 300) // 300ms 防抖

    return () => clearTimeout(timeoutId)
  }, [searchTerm, loadSegments])

  // 处理全选
  const handleSelectAll = (checked: boolean) => {
    setSelectAll(checked)
    if (checked) {
      setSelectedSegments(new Set(segments.map((s) => s.segmentId)))
    } else {
      setSelectedSegments(new Set())
    }
  }

  // 处理单个选择
  const handleSelectSegment = (segmentId: string | number, checked: boolean) => {
    const newSelected = new Set(selectedSegments)
    if (checked) {
      newSelected.add(segmentId)
    } else {
      newSelected.delete(segmentId)
    }
    setSelectedSegments(newSelected)
    setSelectAll(newSelected.size === segments.length && segments.length > 0)
  }

  // 打开编辑抽屉
  const handleEditSegment = (segment: DocumentSegment) => {
    setEditingSegment(segment)
    setEditTitle(segment.title || '')
    setEditContent(segment.content)
    setEditDrawerOpen(true)
  }

  // 保存编辑
  const handleSaveEdit = async () => {
    if (!editingSegment || !editContent.trim()) {
      toast.error('分段内容不能为空')
      return
    }

    try {
      setIsSaving(true)

      await segmentService.updateSegment(editingSegment.segmentId.toString(), {
        title: editTitle.trim() || undefined,
        content: editContent.trim(),
      })

      toast.success('分段更新成功')
      setEditDrawerOpen(false)
      loadSegments()
    } catch (error) {
      console.error('更新分段失败', error)
      toast.error('更新分段失败')
    } finally {
      setIsSaving(false)
    }
  }

  // 取消编辑
  const handleCancelEdit = () => {
    setEditDrawerOpen(false)
    setEditingSegment(null)
    setEditTitle('')
    setEditContent('')
  }

  // 确认删除单个分段
  const confirmDeleteSegment = (segment: DocumentSegment) => {
    setSegmentToDelete(segment)
    setDeleteDialogOpen(true)
  }

  // 删除单个分段
  const handleDeleteSegment = async () => {
    try {
      if (!segmentToDelete) return

      setIsDeleting(true)
      await segmentService.deleteSegment(segmentToDelete.segmentId.toString())
      toast.success('分段已删除')
      loadSegments()
    } catch (error) {
      console.error('删除分段失败', error)
      toast.error('删除分段失败')
    } finally {
      setIsDeleting(false)
      setDeleteDialogOpen(false)
      setSegmentToDelete(null)
    }
  }

  // 确认批量删除
  const confirmBatchDelete = () => {
    if (selectedSegments.size === 0) {
      toast.error('请选择要删除的分段')
      return
    }
    setBatchDeleteDialogOpen(true)
  }

  // 批量删除分段
  const handleBatchDelete = async () => {
    try {
      setIsBatchDeleting(true)
      await segmentService.batchDeleteSegments(Array.from(selectedSegments))
      toast.success(`已删除 ${selectedSegments.size} 个分段`)
      setSelectedSegments(new Set())
      setSelectAll(false)
      loadSegments()
    } catch (error) {
      console.error('批量删除失败', error)
      toast.error('批量删除失败')
    } finally {
      setIsBatchDeleting(false)
      setBatchDeleteDialogOpen(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50/30">
      <div className="container mx-auto py-6 space-y-6">
        {/* 页面头部 */}
        <div className="space-y-4">
          {/* 导航栏 */}
          <div className="flex items-center space-x-2 text-sm">
            <Button variant="ghost" size="sm" asChild className="text-gray-500 hover:text-gray-700 px-2">
              <Link href={`/dataset/${datasetId}`}>
                <ArrowLeft className="h-4 w-4 mr-1" />
                文档列表
              </Link>
            </Button>
            <span className="text-gray-300">/</span>
            <span className="text-gray-600 truncate">{document?.fileName || '文档'}</span>
            <span className="text-gray-300">/</span>
            <span className="text-gray-900 font-medium">分段管理</span>
          </div>

          {/* 主标题区域 */}
          <div className="flex items-center justify-between">
            <div className="flex items-baseline space-x-4">
              <h1 className="text-2xl font-bold text-gray-900">文档分段</h1>
              <div className="flex items-center space-x-3 text-sm">
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  {total} 个分段
                </span>
                {selectedSegments.size > 0 && (
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                    已选中 {selectedSegments.size} 个
                  </span>
                )}
              </div>
            </div>

            {selectedSegments.size > 0 && (
              <Button variant="destructive" size="sm" onClick={confirmBatchDelete}>
                <Trash2 className="mr-2 h-4 w-4" />
                删除选中
              </Button>
            )}
          </div>
        </div>

        {/* 搜索区域 */}
        <div className="flex items-center space-x-2">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
            <Input
              placeholder="搜索分段内容..."
              className="pl-10 pr-10 border-gray-200 focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
              value={searchTerm}
              onChange={(e) => handleSearchInputChange(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  handleSearch()
                }
              }}
            />
            {searchTerm && (
              <Button
                variant="ghost"
                size="icon"
                className="absolute right-1 top-1/2 transform -translate-y-1/2 h-6 w-6 text-gray-400 hover:text-gray-600"
                onClick={handleClearSearch}
              >
                <X className="h-3 w-3" />
              </Button>
            )}
          </div>
        </div>

        {/* 分段表格 */}
        <div className="bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow className="bg-gray-50/50 border-gray-200">
                <TableHead className="w-12 pl-4">
                  <Checkbox
                    checked={selectAll}
                    onCheckedChange={handleSelectAll}
                    disabled={segments.length === 0}
                    className="border-gray-300"
                  />
                </TableHead>
                <TableHead className="w-16 font-medium text-gray-700">位置</TableHead>
                <TableHead className="w-32 hidden sm:table-cell font-medium text-gray-700">标题</TableHead>
                <TableHead className="font-medium text-gray-700">内容预览</TableHead>
                <TableHead className="w-20 hidden md:table-cell font-medium text-gray-700">长度</TableHead>
                <TableHead className="w-32 hidden lg:table-cell font-medium text-gray-700">创建者</TableHead>
                <TableHead className="w-36 hidden lg:table-cell font-medium text-gray-700">创建时间</TableHead>
                <TableHead className="w-16 text-right pr-4 font-medium text-gray-700">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                // 加载状态
                Array.from({ length: 5 }).map((_, index) => (
                  <TableRow key={`skeleton-${Math.random().toString(36).substring(2, 7)}`} className="border-gray-100">
                    <TableCell className="pl-4">
                      <Skeleton className="h-4 w-4" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-5 w-8 rounded-full" />
                    </TableCell>
                    <TableCell className="hidden sm:table-cell">
                      <Skeleton className="h-4 w-20" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-full max-w-md" />
                    </TableCell>
                    <TableCell className="hidden md:table-cell">
                      <Skeleton className="h-4 w-12" />
                    </TableCell>
                    <TableCell className="hidden lg:table-cell">
                      <Skeleton className="h-6 w-20 rounded-full" />
                    </TableCell>
                    <TableCell className="hidden lg:table-cell">
                      <Skeleton className="h-4 w-28" />
                    </TableCell>
                    <TableCell className="pr-4">
                      <Skeleton className="h-8 w-8 rounded ml-auto" />
                    </TableCell>
                  </TableRow>
                ))
              ) : segments.length > 0 ? (
                // 分段列表
                segments.map((segment) => (
                  <TableRow
                    key={segment.segmentId}
                    className="border-gray-100 hover:bg-gray-50/50 transition-colors group"
                  >
                    <TableCell className="pl-4">
                      <Checkbox
                        checked={selectedSegments.has(segment.segmentId)}
                        onCheckedChange={(checked) => handleSelectSegment(segment.segmentId, checked as boolean)}
                        className="border-gray-300"
                      />
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200 font-medium text-xs">
                        {segment.position + 1}
                      </Badge>
                    </TableCell>
                    <TableCell className="hidden sm:table-cell">
                      <span className="text-sm font-medium text-gray-900 truncate">{segment.title || '无标题'}</span>
                    </TableCell>
                    <TableCell>
                      <div className="max-w-md">
                        <p className="text-sm text-gray-600 line-clamp-2 leading-relaxed">
                          {truncateContent(segment.content)}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell className="hidden md:table-cell">
                      <span className="text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded-full">
                        {segment.contentLength}
                      </span>
                    </TableCell>
                    <TableCell className="hidden lg:table-cell">
                      <div className="flex items-center space-x-2">
                        <Avatar className="h-6 w-6">
                          {segment.creatorAvatar ? (
                            <AvatarImage src={segment.creatorAvatar} alt={segment.creatorUsername} />
                          ) : (
                            <AvatarFallback className="bg-blue-500 text-white text-xs">
                              {segment.creatorUsername?.slice(0, 1).toUpperCase() || 'U'}
                            </AvatarFallback>
                          )}
                        </Avatar>
                        <span className="text-sm text-gray-700 truncate">{segment.creatorUsername}</span>
                      </div>
                    </TableCell>
                    <TableCell className="hidden lg:table-cell">
                      <span className="text-xs text-gray-500">{formatDateTime(segment.createTime)}</span>
                    </TableCell>
                    <TableCell className="text-right pr-4">
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8 opacity-0 group-hover:opacity-100 transition-opacity text-gray-400 hover:text-gray-600"
                          >
                            <MoreHorizontal className="h-4 w-4" />
                            <span className="sr-only">操作菜单</span>
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-32">
                          <DropdownMenuItem onClick={() => handleEditSegment(segment)} className="text-sm">
                            <Edit className="mr-2 h-4 w-4" />
                            编辑
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-red-600 text-sm focus:text-red-600"
                            onClick={() => confirmDeleteSegment(segment)}
                          >
                            <Trash2 className="mr-2 h-4 w-4" />
                            删除
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                // 空状态
                <TableRow>
                  <TableCell colSpan={8} className="h-32 text-center">
                    <div className="flex flex-col items-center justify-center space-y-3">
                      <div className="p-3 bg-gray-100 rounded-full">
                        <FileText className="h-8 w-8 text-gray-400" />
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-900">
                          {searchTerm ? '未找到匹配的分段' : '暂无分段'}
                        </p>
                        <p className="text-xs text-gray-500 mt-1">
                          {searchTerm ? '尝试调整搜索条件' : '文档还没有分段内容'}
                        </p>
                      </div>
                    </div>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>

        {/* 分页 */}
        {total > pageSize && (
          <div className="flex flex-col items-center space-y-3">
            <div className="text-sm text-gray-500">
              共 {total} 个分段，第 {page} / {Math.ceil(total / pageSize)} 页
            </div>
            <CustomPagination
              currentPage={page}
              pageSize={pageSize}
              totalItems={total}
              onPageChange={handlePageChange}
            />
          </div>
        )}

        {/* 编辑分段抽屉 */}
        <Sheet open={editDrawerOpen} onOpenChange={setEditDrawerOpen}>
          <SheetContent className="w-[600px] sm:max-w-[600px]">
            <SheetHeader className="space-y-3">
              <SheetTitle className="text-lg font-semibold">编辑分段</SheetTitle>
              <SheetDescription className="text-sm text-gray-600">
                修改分段的标题和内容。内容不能为空。
              </SheetDescription>
            </SheetHeader>
            <div className="space-y-4 mt-6">
              <div className="space-y-2">
                <Label htmlFor="edit-title" className="text-sm font-medium">
                  标题（可选）
                </Label>
                <Input
                  id="edit-title"
                  value={editTitle}
                  onChange={(e) => setEditTitle(e.target.value)}
                  placeholder="输入分段标题..."
                  className="border-gray-200 focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-content" className="text-sm font-medium">
                  内容 <span className="text-red-500">*</span>
                </Label>
                <Textarea
                  id="edit-content"
                  value={editContent}
                  onChange={(e) => setEditContent(e.target.value)}
                  placeholder="输入分段内容..."
                  className="min-h-[300px] resize-none border-gray-200 focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                />
                <p className="text-xs text-gray-500">字符数：{editContent.length}</p>
              </div>
              <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
                <Button variant="outline" onClick={handleCancelEdit} disabled={isSaving}>
                  取消
                </Button>
                <Button
                  onClick={handleSaveEdit}
                  disabled={isSaving || !editContent.trim()}
                  className="bg-blue-600 hover:bg-blue-700"
                >
                  <Check className="mr-2 h-4 w-4" />
                  {isSaving ? '保存中...' : '保存'}
                </Button>
              </div>
            </div>
          </SheetContent>
        </Sheet>

        {/* 删除单个分段确认对话框 */}
        <ConfirmDialog
          open={deleteDialogOpen}
          onOpenChange={setDeleteDialogOpen}
          title="确认删除分段"
          description="您确定要删除这个分段吗？此操作不可撤销。"
          confirmVariant="destructive"
          isConfirming={isDeleting}
          onConfirm={handleDeleteSegment}
        />

        {/* 批量删除确认对话框 */}
        <ConfirmDialog
          open={batchDeleteDialogOpen}
          onOpenChange={setBatchDeleteDialogOpen}
          title="确认批量删除"
          description={`您确定要删除选中的 ${selectedSegments.size} 个分段吗？此操作不可撤销。`}
          confirmVariant="destructive"
          isConfirming={isBatchDeleting}
          onConfirm={handleBatchDelete}
        />
      </div>
    </div>
  )
}
