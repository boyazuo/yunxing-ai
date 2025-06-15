'use client'

import { documentService, segmentService } from '@/api/document'
import { ConfirmDialog } from '@/components/blocks/confirm-dialog'
import { CustomPagination } from '@/components/blocks/pagination'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import type { DatasetDocument, DocumentSegment } from '@/types/document'
import { ArrowLeft, Edit, FileText, MoreHorizontal, Search, Trash2, X } from 'lucide-react'
import { useSession } from 'next-auth/react'
import Link from 'next/link'
import { useParams } from 'next/navigation'
import { useCallback, useEffect, useState, useTransition } from 'react'
import { toast } from 'sonner'
import { SegmentEditDrawer } from './_components/segment-edit-drawer'

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

export default function DocumentSegmentsPage() {
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

  // 删除确认对话框状态
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [segmentToDelete, setSegmentToDelete] = useState<DocumentSegment | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)
  const [isPending, startTransition] = useTransition()

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
    setEditDrawerOpen(true)
  }

  // 确认删除单个分段
  const confirmDeleteSegment = (segment: DocumentSegment) => {
    startTransition(() => {
      setSegmentToDelete(segment)
      setDeleteDialogOpen(true)
    })
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
    <div className="container mx-auto py-6 space-y-6">
      {/* 页面头部 */}
      <div className="space-y-4">
        {/* 导航栏 */}
        <div className="flex items-center space-x-2 text-sm text-muted-foreground">
          <Button variant="ghost" size="sm" asChild>
            <Link href={`/dataset/${datasetId}`}>
              <ArrowLeft className="h-4 w-4 mr-1" />
              文档列表
            </Link>
          </Button>
          <span>/</span>
          <span className="truncate">{document?.fileName || '文档'}</span>
          <span>/</span>
          <span className="text-foreground font-medium">分段管理</span>
        </div>

        {/* 主标题区域 */}
        <div className="flex items-center justify-between">
          <div className="flex items-baseline space-x-4">
            <h1 className="text-2xl font-bold">文档分段</h1>
            <div className="flex items-center space-x-3 text-sm">
              <Badge variant="secondary">{total} 个分段</Badge>
              {selectedSegments.size > 0 && (
                <Badge variant="outline" className="bg-orange-50 text-orange-700 border-orange-200">
                  已选中 {selectedSegments.size} 个
                </Badge>
              )}
            </div>
          </div>

          <div className="flex items-center space-x-3">
            {selectedSegments.size > 0 && (
              <Button variant="destructive" size="sm" onClick={confirmBatchDelete}>
                <Trash2 className="mr-2 h-4 w-4" />
                删除选中
              </Button>
            )}

            {/* 搜索区域 */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索分段内容..."
                className="pl-10 pr-10 w-64"
                value={searchTerm}
                onChange={(e) => handleSearchInputChange(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    handleSearch()
                  }
                }}
              />
              {searchTerm && (
                <Button variant="ghost" size="icon" className="absolute right-1 top-1/2 -translate-y-1/2 h-6 w-6" onClick={handleClearSearch}>
                  <X className="h-3 w-3" />
                </Button>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* 分段表格 */}
      <div className="bg-card rounded-lg border shadow-sm overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-12">
                <Checkbox checked={selectAll} onCheckedChange={handleSelectAll} disabled={segments.length === 0} />
              </TableHead>
              <TableHead className="w-16">位置</TableHead>
              <TableHead className="w-32 hidden sm:table-cell">标题</TableHead>
              <TableHead>内容预览</TableHead>
              <TableHead className="w-20 hidden md:table-cell">长度</TableHead>
              <TableHead className="w-32 hidden lg:table-cell">创建者</TableHead>
              <TableHead className="w-36 hidden lg:table-cell">创建时间</TableHead>
              <TableHead className="w-16 text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              // 加载状态
              Array.from({ length: 5 }, (_, index) => `skeleton-${Date.now()}-${index}`).map((key) => (
                <TableRow key={key}>
                  <TableCell>
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
                  <TableCell>
                    <Skeleton className="h-8 w-8 rounded ml-auto" />
                  </TableCell>
                </TableRow>
              ))
            ) : segments.length > 0 ? (
              // 分段列表
              segments.map((segment) => (
                <TableRow key={segment.segmentId} className="group">
                  <TableCell>
                    <Checkbox checked={selectedSegments.has(segment.segmentId)} onCheckedChange={(checked) => handleSelectSegment(segment.segmentId, checked as boolean)} />
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200">
                      {segment.position + 1}
                    </Badge>
                  </TableCell>
                  <TableCell className="hidden sm:table-cell">
                    <span className="font-medium truncate">{segment.title || '无标题'}</span>
                  </TableCell>
                  <TableCell>
                    <div className="max-w-lg">
                      <p className="text-sm text-muted-foreground truncate">{segment.content}</p>
                    </div>
                  </TableCell>
                  <TableCell className="hidden md:table-cell">
                    <Badge variant="secondary" className="text-xs">
                      {segment.contentLength}
                    </Badge>
                  </TableCell>
                  <TableCell className="hidden lg:table-cell">
                    <div className="flex items-center space-x-2">
                      <Avatar className="h-6 w-6">
                        {segment.creatorAvatar ? (
                          <AvatarImage src={segment.creatorAvatar} alt={segment.creatorUsername} />
                        ) : (
                          <AvatarFallback className="text-xs">{segment.creatorUsername?.slice(0, 1).toUpperCase() || 'U'}</AvatarFallback>
                        )}
                      </Avatar>
                      <span className="text-sm truncate">{segment.creatorUsername}</span>
                    </div>
                  </TableCell>
                  <TableCell className="hidden lg:table-cell">
                    <span className="text-xs text-muted-foreground">{formatDateTime(segment.createTime)}</span>
                  </TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreHorizontal className="h-4 w-4" />
                          <span className="sr-only">操作菜单</span>
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end" className="w-32">
                        <DropdownMenuItem onClick={() => handleEditSegment(segment)}>
                          <Edit className="mr-2 h-4 w-4" />
                          编辑
                        </DropdownMenuItem>
                        <DropdownMenuItem className="text-destructive focus:text-destructive" onClick={() => confirmDeleteSegment(segment)}>
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
                    <div className="p-3 bg-muted rounded-full">
                      <FileText className="h-8 w-8 text-muted-foreground" />
                    </div>
                    <div>
                      <p className="font-medium">{searchTerm ? '未找到匹配的分段' : '暂无分段'}</p>
                      <p className="text-sm text-muted-foreground mt-1">{searchTerm ? '尝试调整搜索条件' : '文档还没有分段内容'}</p>
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
          <div className="text-sm text-muted-foreground">
            共 {total} 个分段，第 {page} / {Math.ceil(total / pageSize)} 页
          </div>
          <CustomPagination currentPage={page} pageSize={pageSize} totalItems={total} onPageChange={handlePageChange} />
        </div>
      )}

      {/* 编辑分段抽屉 */}
      <SegmentEditDrawer open={editDrawerOpen} onOpenChange={setEditDrawerOpen} editingSegment={editingSegment} onSave={loadSegments} />

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
  )
}
