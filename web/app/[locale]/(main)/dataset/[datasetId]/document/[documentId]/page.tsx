'use client'

import { documentService, segmentService } from '@/api/document'
import { ConfirmDialog } from '@/components/blocks/confirm-dialog'
import { RowActionsTrigger } from '@/components/blocks/row-actions'
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
import { SegmentMethod, SegmentType } from '@/types/document'
import {
  ArrowLeft,
  ChevronDown,
  ChevronRight,
  Edit,
  FileText,
  Layers,
  Search,
  Trash2,
  X,
} from 'lucide-react'
import { useSession } from 'next-auth/react'
import Link from 'next/link'
import { useParams } from 'next/navigation'
import { useCallback, useEffect, useState, useTransition } from 'react'
import { toast } from 'sonner'
import { SegmentEditDrawer } from './_components/segment-edit-drawer'

function formatDateTime(dateTimeStr?: string) {
  if (!dateTimeStr) return '—'
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
  const [segmentView, setSegmentView] = useState<'segments' | 'parents'>('segments')
  const [parentCount, setParentCount] = useState(0)
  const [expandedParents, setExpandedParents] = useState<Set<string | number>>(new Set())
  const [childSegmentsMap, setChildSegmentsMap] = useState<Record<string, DocumentSegment[]>>({})

  const [page, setPage] = useState(1)
  const [pageSize] = useState(10)
  const [total, setTotal] = useState(0)

  const [selectedSegments, setSelectedSegments] = useState<Set<string | number>>(new Set())
  const [selectAll, setSelectAll] = useState(false)

  const [editDrawerOpen, setEditDrawerOpen] = useState(false)
  const [editingSegment, setEditingSegment] = useState<DocumentSegment | null>(null)

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [segmentToDelete, setSegmentToDelete] = useState<DocumentSegment | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)
  const [isPending, startTransition] = useTransition()

  const [batchDeleteDialogOpen, setBatchDeleteDialogOpen] = useState(false)
  const [isBatchDeleting, setIsBatchDeleting] = useState(false)

  const datasetId = params.datasetId as string
  const documentId = params.documentId as string

  const loadDocument = useCallback(async () => {
    try {
      if (!documentId) return
      const data = await documentService.getDocument(documentId)
      setDocument(data)
      if (data.segmentMethod === SegmentMethod.PARENT_CHILD) {
        const parentResponse = await segmentService.getSegments(documentId, 1, 1, undefined, 'parents')
        setParentCount(parentResponse.total)
      }
    } catch (error) {
      console.error('加载文档信息失败', error)
      toast.error('加载文档信息失败')
    }
  }, [documentId])

  const loadSegments = useCallback(async () => {
    try {
      if (!documentId) return
      setIsLoading(true)
      const response = await segmentService.getSegments(documentId, page, pageSize, searchTerm || undefined, segmentView)
      setSegments(response.records)
      setTotal(response.total)
    } catch (error) {
      console.error('加载分段列表失败', error)
      toast.error('加载分段列表失败')
    } finally {
      setIsLoading(false)
    }
  }, [documentId, page, pageSize, searchTerm, segmentView])

  useEffect(() => {
    if (status === 'authenticated' && documentId) {
      loadDocument()
      loadSegments()
    }
  }, [status, documentId, loadDocument, loadSegments])

  const isParentChildDoc = document?.segmentMethod === SegmentMethod.PARENT_CHILD

  const handleToggleParentExpand = async (parentSegmentId: string | number) => {
    const key = String(parentSegmentId)
    const newExpanded = new Set(expandedParents)
    if (newExpanded.has(parentSegmentId)) {
      newExpanded.delete(parentSegmentId)
      setExpandedParents(newExpanded)
      return
    }
    if (!childSegmentsMap[key]) {
      const children = await segmentService.getChildSegmentsByParent(parentSegmentId)
      setChildSegmentsMap((prev) => ({ ...prev, [key]: children }))
    }
    newExpanded.add(parentSegmentId)
    setExpandedParents(newExpanded)
  }

  const handleViewChange = (view: 'segments' | 'parents') => {
    setSegmentView(view)
    setPage(1)
    setSelectedSegments(new Set())
    setSelectAll(false)
    setExpandedParents(new Set())
  }

  const handlePageChange = (newPage: number) => {
    setPage(newPage)
    setSelectedSegments(new Set())
    setSelectAll(false)
  }

  const handleSearchInputChange = (value: string) => {
    setSearchTerm(value)
    if (!value.trim()) {
      setPage(1)
      setSelectedSegments(new Set())
      setSelectAll(false)
    }
  }

  const handleClearSearch = () => {
    setSearchTerm('')
    setPage(1)
    setSelectedSegments(new Set())
    setSelectAll(false)
  }

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      if (searchTerm !== undefined) loadSegments()
    }, 300)
    return () => clearTimeout(timeoutId)
  }, [searchTerm, loadSegments])

  const handleSelectAll = (checked: boolean) => {
    setSelectAll(checked)
    setSelectedSegments(checked ? new Set(segments.map((s) => s.segmentId)) : new Set())
  }

  const handleSelectSegment = (segmentId: string | number, checked: boolean) => {
    const newSelected = new Set(selectedSegments)
    if (checked) newSelected.add(segmentId)
    else newSelected.delete(segmentId)
    setSelectedSegments(newSelected)
    setSelectAll(newSelected.size === segments.length && segments.length > 0)
  }

  const handleEditSegment = (segment: DocumentSegment) => {
    setEditingSegment(segment)
    setEditDrawerOpen(true)
  }

  const confirmDeleteSegment = (segment: DocumentSegment) => {
    startTransition(() => {
      setSegmentToDelete(segment)
      setDeleteDialogOpen(true)
    })
  }

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

  const confirmBatchDelete = () => {
    if (selectedSegments.size === 0) {
      toast.error('请选择要删除的分段')
      return
    }
    setBatchDeleteDialogOpen(true)
  }

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
    <div className="container mx-auto py-8 space-y-6">
      {/* 导航面包屑 */}
      <div className="flex items-center gap-1.5 text-sm">
        <Button variant="ghost" size="sm" className="h-8 gap-1.5 text-muted-foreground hover:text-foreground px-2" asChild>
          <Link href={`/dataset/${datasetId}`}>
            <ArrowLeft className="h-3.5 w-3.5" />
            文档列表
          </Link>
        </Button>
        <span className="text-muted-foreground/50">/</span>
        <span className="text-muted-foreground truncate max-w-[180px]">{document?.fileName || '文档'}</span>
        <span className="text-muted-foreground/50">/</span>
        <span className="text-foreground font-medium">分段管理</span>
      </div>

      {/* 页面标题和操作栏 */}
      <div className="flex items-start justify-between gap-4">
        <div className="space-y-2">
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-semibold tracking-tight">文档分段</h1>
            <div className="flex items-center gap-2">
              {isParentChildDoc ? (
                <>
                  <Badge variant="secondary" className="gap-1 font-normal">
                    <Layers className="h-3 w-3" />
                    {total} 个子块
                  </Badge>
                  {parentCount > 0 && (
                    <Badge variant="outline" className="font-normal">
                      {parentCount} 个父块
                    </Badge>
                  )}
                </>
              ) : (
                <Badge variant="secondary" className="gap-1 font-normal">
                  <FileText className="h-3 w-3" />
                  {total} 个分段
                </Badge>
              )}
            </div>
          </div>
          <p className="text-sm text-muted-foreground truncate max-w-lg">
            {document?.fileName || '加载中...'}
          </p>
        </div>

        {/* 右侧操作区 */}
        <div className="flex items-center gap-3 flex-shrink-0">
          {/* 视图切换（父子块模式） */}
          {isParentChildDoc && (
            <div className="flex rounded-md border bg-muted/40 p-0.5 gap-0.5">
              <Button
                variant={segmentView === 'segments' ? 'secondary' : 'ghost'}
                size="sm"
                className="h-7 px-3 text-xs"
                onClick={() => handleViewChange('segments')}
              >
                子块视图
              </Button>
              <Button
                variant={segmentView === 'parents' ? 'secondary' : 'ghost'}
                size="sm"
                className="h-7 px-3 text-xs"
                onClick={() => handleViewChange('parents')}
              >
                父块视图
              </Button>
            </div>
          )}

          {/* 批量删除 */}
          {selectedSegments.size > 0 && (
            <Button variant="destructive" size="sm" className="gap-1.5 h-9" onClick={confirmBatchDelete}>
              <Trash2 className="h-3.5 w-3.5" />
              删除选中 ({selectedSegments.size})
            </Button>
          )}

          {/* 搜索框 */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <Input
              placeholder="搜索分段内容..."
              className="pl-9 pr-9 w-56 h-9"
              value={searchTerm}
              onChange={(e) => handleSearchInputChange(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && loadSegments()}
            />
            {searchTerm && (
              <Button
                variant="ghost"
                size="icon"
                className="absolute right-1 top-1/2 -translate-y-1/2 h-6 w-6"
                onClick={handleClearSearch}
              >
                <X className="h-3 w-3" />
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* 已选中提示条 */}
      {selectedSegments.size > 0 && (
        <div className="flex items-center gap-3 px-4 py-2.5 rounded-lg bg-primary/5 border border-primary/20 text-sm">
          <span className="text-primary font-medium">已选中 {selectedSegments.size} 个分段</span>
          <Button variant="ghost" size="sm" className="h-6 px-2 text-muted-foreground" onClick={() => { setSelectedSegments(new Set()); setSelectAll(false) }}>
            取消选择
          </Button>
        </div>
      )}

      {/* 分段表格 */}
      <div className="rounded-lg border bg-card shadow-xs overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/40 hover:bg-muted/40">
              <TableHead className="w-10">
                <Checkbox
                  checked={selectAll}
                  onCheckedChange={handleSelectAll}
                  disabled={segments.length === 0 || segmentView === 'parents'}
                  aria-label="全选"
                />
              </TableHead>
              <TableHead className="w-28 font-medium">位置</TableHead>
              <TableHead className="w-36 hidden sm:table-cell font-medium">标题</TableHead>
              <TableHead className="font-medium">内容预览</TableHead>
              <TableHead className="w-20 hidden md:table-cell font-medium">字数</TableHead>
              <TableHead className="w-32 hidden lg:table-cell font-medium">创建者</TableHead>
              <TableHead className="w-36 hidden lg:table-cell font-medium">创建时间</TableHead>
              <TableHead className="w-16 text-right font-medium">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }, (_, i) => `skeleton-${i}`).map((key) => (
                <TableRow key={key}>
                  <TableCell><Skeleton className="h-4 w-4" /></TableCell>
                  <TableCell><Skeleton className="h-5 w-10 rounded-full" /></TableCell>
                  <TableCell className="hidden sm:table-cell"><Skeleton className="h-4 w-24" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-full max-w-sm" /></TableCell>
                  <TableCell className="hidden md:table-cell"><Skeleton className="h-5 w-14 rounded-full" /></TableCell>
                  <TableCell className="hidden lg:table-cell">
                    <div className="flex items-center gap-2">
                      <Skeleton className="h-6 w-6 rounded-full" />
                      <Skeleton className="h-4 w-16" />
                    </div>
                  </TableCell>
                  <TableCell className="hidden lg:table-cell"><Skeleton className="h-4 w-28" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-8 rounded ml-auto" /></TableCell>
                </TableRow>
              ))
            ) : segments.length > 0 ? (
              segments.flatMap((segment) => {
                const isExpanded = expandedParents.has(segment.segmentId)
                const rows = [
                  <TableRow key={segment.segmentId} className="group hover:bg-muted/30 transition-colors">
                    <TableCell>
                      {segmentView === 'parents' ? (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-6 w-6 text-muted-foreground"
                          onClick={() => handleToggleParentExpand(segment.segmentId)}
                        >
                          {isExpanded ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
                        </Button>
                      ) : (
                        <Checkbox
                          checked={selectedSegments.has(segment.segmentId)}
                          onCheckedChange={(checked) => handleSelectSegment(segment.segmentId, checked as boolean)}
                          aria-label={`选择分段 ${segment.position + 1}`}
                        />
                      )}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1.5 flex-wrap">
                        <Badge
                          variant="outline"
                          className="bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950 dark:text-blue-400 dark:border-blue-800 tabular-nums font-mono text-[11px] h-5 px-1.5"
                        >
                          #{segment.position + 1}
                        </Badge>
                        {isParentChildDoc && segmentView === 'segments' && segment.segmentType === SegmentType.CHILD && (
                          <Badge variant="secondary" className="text-[10px] h-4 px-1 font-normal">向量</Badge>
                        )}
                        {segmentView === 'parents' && (
                          <Badge variant="secondary" className="text-[10px] h-4 px-1 font-normal">父块</Badge>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="hidden sm:table-cell">
                      <span className="font-medium text-sm truncate block max-w-[130px]">
                        {segment.title || <span className="text-muted-foreground italic font-normal">无标题</span>}
                      </span>
                    </TableCell>
                    <TableCell>
                      <p className="text-sm text-muted-foreground truncate max-w-sm leading-relaxed">
                        {segment.content}
                      </p>
                    </TableCell>
                    <TableCell className="hidden md:table-cell">
                      <Badge variant="secondary" className="tabular-nums font-mono text-xs font-normal">
                        {segment.contentLength}
                      </Badge>
                    </TableCell>
                    <TableCell className="hidden lg:table-cell">
                      <div className="flex items-center gap-2">
                        <Avatar className="h-6 w-6">
                          {segment.creatorAvatar ? (
                            <AvatarImage src={segment.creatorAvatar} alt={segment.creatorUsername} />
                          ) : (
                            <AvatarFallback className="text-[10px]">
                              {segment.creatorUsername?.slice(0, 1).toUpperCase() || 'U'}
                            </AvatarFallback>
                          )}
                        </Avatar>
                        <span className="text-sm text-muted-foreground truncate">{segment.creatorUsername || '—'}</span>
                      </div>
                    </TableCell>
                    <TableCell className="hidden lg:table-cell">
                      <span className="text-xs text-muted-foreground tabular-nums">{formatDateTime(segment.createTime)}</span>
                    </TableCell>
                    <TableCell className="text-right">
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <RowActionsTrigger className="ml-auto" label="操作菜单" />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-28">
                          <DropdownMenuItem onClick={() => handleEditSegment(segment)}>
                            <Edit className="mr-2 h-3.5 w-3.5" />
                            编辑
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={() => confirmDeleteSegment(segment)}
                          >
                            <Trash2 className="mr-2 h-3.5 w-3.5" />
                            删除
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>,
                ]

                if (segmentView === 'parents' && isExpanded) {
                  const children = childSegmentsMap[String(segment.segmentId)] ?? []
                  for (const child of children) {
                    rows.push(
                      <TableRow key={`child-${child.segmentId}`} className="bg-muted/20 hover:bg-muted/30 transition-colors">
                        <TableCell />
                        <TableCell>
                          <Badge
                            variant="outline"
                            className="ml-4 text-[10px] h-4 px-1 tabular-nums font-mono text-slate-500 border-slate-200 dark:border-slate-700"
                          >
                            #{child.position + 1}
                          </Badge>
                        </TableCell>
                        <TableCell className="hidden sm:table-cell">
                          <span className="text-xs text-muted-foreground truncate block">
                            {child.title || '子块'}
                          </span>
                        </TableCell>
                        <TableCell>
                          <p className="text-xs text-muted-foreground truncate pl-4 max-w-sm">
                            {child.content}
                          </p>
                        </TableCell>
                        <TableCell className="hidden md:table-cell">
                          <Badge variant="secondary" className="tabular-nums font-mono text-xs font-normal">
                            {child.contentLength}
                          </Badge>
                        </TableCell>
                        <TableCell colSpan={3} />
                      </TableRow>,
                    )
                  }
                }

                return rows
              })
            ) : (
              <TableRow>
                <TableCell colSpan={8} className="h-44 text-center">
                  <div className="flex flex-col items-center justify-center gap-3">
                    <div className="h-12 w-12 rounded-2xl bg-muted/60 flex items-center justify-center">
                      <FileText className="h-6 w-6 text-muted-foreground/50" />
                    </div>
                    <div className="space-y-1">
                      <p className="font-medium text-sm">
                        {searchTerm ? '未找到匹配的分段' : '暂无分段'}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {searchTerm ? '尝试调整搜索条件' : '文档还没有生成分段内容'}
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
        <div className="flex items-center justify-between gap-4">
          <span className="min-w-48 shrink-0 whitespace-nowrap text-sm text-muted-foreground">
            第 <span className="tabular-nums">{(page - 1) * pageSize + 1}</span>–<span className="tabular-nums">{Math.min(page * pageSize, total)}</span> 条，共 <span className="tabular-nums">{total}</span> 条
          </span>
          <CustomPagination currentPage={page} pageSize={pageSize} totalItems={total} onPageChange={handlePageChange} />
        </div>
      )}

      <SegmentEditDrawer open={editDrawerOpen} onOpenChange={setEditDrawerOpen} editingSegment={editingSegment} onSave={loadSegments} />

      <ConfirmDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        title="确认删除分段"
        description="您确定要删除这个分段吗？此操作不可撤销。"
        confirmVariant="destructive"
        isConfirming={isDeleting}
        onConfirm={handleDeleteSegment}
      />

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
