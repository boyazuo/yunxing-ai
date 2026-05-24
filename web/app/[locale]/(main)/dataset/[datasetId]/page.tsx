'use client'

import { datasetService } from '@/api/dataset'
import { documentService } from '@/api/document'
import { DocumentUploadDialog } from '@/app/[locale]/(main)/dataset/_components/DocumentUploadDialog'
import { ConfirmDialog } from '@/components/blocks/confirm-dialog'
import { CustomPagination } from '@/components/blocks/pagination'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import type { Dataset } from '@/types/dataset'
import { type DatasetDocument, DocumentStatus, getDocumentStatusText } from '@/types/document'
import {
  AlertCircle,
  ArrowLeft,
  CheckCircle2,
  Clock,
  Download,
  File,
  FileJson,
  FileText,
  FileType2,
  Loader2,
  MoreHorizontal,
  Plus,
  Search,
  Trash,
  Upload,
  User,
} from 'lucide-react'
import { useSession } from 'next-auth/react'
import Link from 'next/link'
import { useParams } from 'next/navigation'
import { useCallback, useEffect, useState, useTransition } from 'react'
import { toast } from 'sonner'

function formatFileSize(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`
  return `${(bytes / 1024 / 1024 / 1024).toFixed(1)} GB`
}

function formatDateTime(str?: string) {
  if (!str) return '—'
  try {
    const d = new Date(str)
    return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  } catch {
    return '—'
  }
}

function getFileStyle(name: string) {
  const ext = name.split('.').pop()?.toLowerCase() ?? ''
  if (ext === 'pdf') return { Icon: FileText, bg: 'bg-red-50 dark:bg-red-950/60', color: 'text-red-500' }
  if (['doc', 'docx'].includes(ext)) return { Icon: FileType2, bg: 'bg-blue-50 dark:bg-blue-950/60', color: 'text-blue-500' }
  if (['txt', 'md'].includes(ext)) return { Icon: FileText, bg: 'bg-slate-50 dark:bg-slate-900', color: 'text-slate-500' }
  if (['csv', 'json'].includes(ext)) return { Icon: FileJson, bg: 'bg-emerald-50 dark:bg-emerald-950/60', color: 'text-emerald-500' }
  return { Icon: File, bg: 'bg-muted', color: 'text-muted-foreground' }
}

function StatusPill({ status }: { status: DocumentStatus }) {
  const configs = {
    [DocumentStatus.COMPLETED]: {
      icon: CheckCircle2,
      label: getDocumentStatusText(status),
      cls: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400',
      dot: 'bg-emerald-500',
    },
    [DocumentStatus.PROCESSING]: {
      icon: Loader2,
      label: getDocumentStatusText(status),
      cls: 'bg-blue-50 text-blue-700 dark:bg-blue-950/60 dark:text-blue-400',
      dot: 'bg-blue-500',
    },
    [DocumentStatus.PENDING]: {
      icon: Clock,
      label: getDocumentStatusText(status),
      cls: 'bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-400',
      dot: 'bg-amber-500',
    },
    [DocumentStatus.FAILED]: { icon: AlertCircle, label: getDocumentStatusText(status), cls: 'bg-red-50 text-red-700 dark:bg-red-950/60 dark:text-red-400', dot: 'bg-red-500' },
  } as const

  const cfg = configs[status] ?? configs[DocumentStatus.PENDING]
  const IconComp = cfg.icon
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium ${cfg.cls}`}>
      {status === DocumentStatus.PROCESSING ? <Loader2 className="h-3 w-3 animate-spin" /> : <span className={`h-1.5 w-1.5 rounded-full ${cfg.dot}`} />}
      {cfg.label}
    </span>
  )
}

export default function DatasetDocumentsPage() {
  const params = useParams()
  const { data: session, status } = useSession()

  const [dataset, setDataset] = useState<Dataset | null>(null)
  const [documents, setDocuments] = useState<DatasetDocument[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')
  const [page, setPage] = useState(1)
  const [pageSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [documentToDelete, setDocumentToDelete] = useState<DatasetDocument | null>(null)
  const [isPending, startTransition] = useTransition()

  const tenantId = session?.tenant?.tenantId || ''
  const datasetId = params.datasetId as string

  const loadDataset = useCallback(async () => {
    if (!datasetId) return
    try {
      setDataset(await datasetService.getDataset(datasetId))
    } catch {
      toast.error('加载知识库信息失败')
    }
  }, [datasetId])

  const loadDocuments = useCallback(async () => {
    if (!datasetId) return
    setIsLoading(true)
    try {
      const res = await documentService.getDocuments(datasetId, page, pageSize)
      setDocuments(res.records)
      setTotal(res.total)
    } catch {
      toast.error('加载文档列表失败')
    } finally {
      setIsLoading(false)
    }
  }, [datasetId, page, pageSize])

  useEffect(() => {
    if (status === 'authenticated' && datasetId) {
      loadDataset()
      loadDocuments()
    }
  }, [status, datasetId, loadDataset, loadDocuments])

  const confirmDeleteDocument = (doc: DatasetDocument) => {
    startTransition(() => {
      setDocumentToDelete(doc)
      setDeleteDialogOpen(true)
    })
  }
  const handleDeleteDocument = async () => {
    if (!documentToDelete) return
    try {
      const res = await documentService.deleteDocument(documentToDelete.documentId.toString())
      if (res.code === 0) toast.success('文档已成功删除')
      else toast.error(res.msg)
      loadDocuments()
    } catch {
      toast.error('删除文档失败')
    } finally {
      setDeleteDialogOpen(false)
      setDocumentToDelete(null)
    }
  }

  const filtered = documents.filter((d) => {
    const nm = searchTerm ? d.fileName.toLowerCase().includes(searchTerm.toLowerCase()) : true
    const st = statusFilter === 'all' ? true : d.status === statusFilter
    return nm && st
  })

  const completedCount = documents.filter((d) => d.status === DocumentStatus.COMPLETED).length
  const processingCount = documents.filter((d) => [DocumentStatus.PROCESSING, DocumentStatus.PENDING].includes(d.status)).length

  return (
    <div className="min-h-[calc(100vh-64px)]">
      {/* ── Page header banner ── */}
      <div className="relative border-b bg-gradient-to-b from-primary/[0.04] to-background overflow-hidden">
        <div className="dot-pattern absolute inset-0 opacity-50" />
        <div className="relative container mx-auto px-6 py-8">
          {/* Breadcrumb */}
          <div className="flex items-center gap-1.5 text-sm mb-4 animate-slide-down-fade">
            <Link href="/dataset" className="inline-flex items-center gap-1.5 text-muted-foreground hover:text-foreground transition-colors">
              <ArrowLeft className="h-3.5 w-3.5" />
              知识库
            </Link>
            <span className="text-muted-foreground/40">/</span>
            <span className="text-foreground font-medium truncate max-w-[200px]">{dataset?.datasetName || '加载中…'}</span>
          </div>

          <div className="flex items-end justify-between gap-6">
            <div className="space-y-2 animate-slide-up-fade">
              <h1 className="text-2xl font-bold tracking-tight">{dataset?.datasetName || '文档列表'}</h1>
              {dataset?.datasetDesc && <p className="text-sm text-muted-foreground max-w-xl">{dataset.datasetDesc}</p>}
              {/* Stats row */}
              <div className="flex items-center gap-4 pt-0.5">
                <div className="flex items-center gap-1.5 text-sm">
                  <span className="h-2 w-2 rounded-full bg-emerald-500" />
                  <span className="text-muted-foreground">{completedCount} 已完成</span>
                </div>
                {processingCount > 0 && (
                  <div className="flex items-center gap-1.5 text-sm">
                    <span className="h-2 w-2 rounded-full bg-blue-500 animate-pulse" />
                    <span className="text-muted-foreground">{processingCount} 处理中</span>
                  </div>
                )}
                <span className="text-muted-foreground/50 text-sm">· 共 {total} 个文档</span>
              </div>
            </div>
            <div className="animate-slide-up-fade stagger-2 flex-shrink-0">
              <Button
                onClick={() => setUploadDialogOpen(true)}
                size="lg"
                className="gap-2 shadow-md shadow-primary/20 hover:shadow-lg hover:shadow-primary/25 transition-all duration-200"
              >
                <Upload className="h-4 w-4" />
                上传文档
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* ── Main content ── */}
      <div className="container mx-auto px-6 py-8 space-y-6">
        {/* Filter bar */}
        <div className="flex items-center justify-between gap-4 animate-slide-up-fade stagger-3">
          <Select defaultValue="all" onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[140px] h-9 text-sm">
              <SelectValue placeholder="文档状态" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部状态</SelectItem>
              <SelectItem value={DocumentStatus.PENDING}>待处理</SelectItem>
              <SelectItem value={DocumentStatus.PROCESSING}>处理中</SelectItem>
              <SelectItem value={DocumentStatus.COMPLETED}>已完成</SelectItem>
              <SelectItem value={DocumentStatus.FAILED}>处理失败</SelectItem>
            </SelectContent>
          </Select>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <Input placeholder="搜索文档名称..." className="pl-9 w-[220px] h-9 text-sm bg-background" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
          </div>
        </div>

        {/* Table card */}
        <div className="rounded-2xl border bg-card shadow-sm overflow-hidden animate-slide-up-fade stagger-4">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/30 hover:bg-muted/30">
                <TableHead className="w-[42%] text-xs font-semibold text-muted-foreground uppercase tracking-wider pl-5">文档名称</TableHead>
                <TableHead className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">大小</TableHead>
                <TableHead className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">状态</TableHead>
                <TableHead className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">分段数</TableHead>
                <TableHead className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">创建者</TableHead>
                <TableHead className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">创建时间</TableHead>
                <TableHead className="text-right text-xs font-semibold text-muted-foreground uppercase tracking-wider pr-5">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                [...Array(5)].map((_, i) => (
                  // biome-ignore lint/suspicious/noArrayIndexKey: skeleton
                  <TableRow key={i} className="hover:bg-transparent">
                    <TableCell className="pl-5">
                      <div className="flex items-center gap-3">
                        <Skeleton className="h-9 w-9 rounded-xl" />
                        <Skeleton className="h-4 w-48" />
                      </div>
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-14" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-6 w-22 rounded-full" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-8" />
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <Skeleton className="h-6 w-6 rounded-full" />
                        <Skeleton className="h-4 w-16" />
                      </div>
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-28" />
                    </TableCell>
                    <TableCell className="pr-5">
                      <Skeleton className="h-8 w-8 rounded-lg ml-auto" />
                    </TableCell>
                  </TableRow>
                ))
              ) : filtered.length > 0 ? (
                filtered.map((doc) => {
                  const { Icon, bg, color } = getFileStyle(doc.fileName)
                  const canView = doc.status === DocumentStatus.COMPLETED
                  return (
                    <TableRow key={doc.documentId} className="group border-b border-border/50 hover:bg-primary/[0.02] transition-colors">
                      <TableCell className="pl-5">
                        <div className="flex items-center gap-3">
                          <div className={`h-9 w-9 rounded-xl ${bg} flex items-center justify-center flex-shrink-0`}>
                            <Icon className={`h-4.5 w-4.5 ${color}`} />
                          </div>
                          {canView ? (
                            <Link
                              href={`/dataset/${datasetId}/document/${doc.documentId}`}
                              className="font-medium text-sm text-foreground hover:text-primary transition-colors truncate max-w-[280px]"
                            >
                              {doc.fileName}
                            </Link>
                          ) : (
                            <span className="font-medium text-sm text-foreground truncate max-w-[280px]">{doc.fileName}</span>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>
                        <span className="text-sm text-muted-foreground tabular-nums">{formatFileSize(doc.fileSize)}</span>
                      </TableCell>
                      <TableCell>
                        <StatusPill status={doc.status} />
                      </TableCell>
                      <TableCell>
                        <span className="text-sm tabular-nums font-medium">{doc.segmentNum ?? '—'}</span>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Avatar className="h-6 w-6 ring-1 ring-border">
                            {doc.creatorAvatar ? (
                              <AvatarImage src={doc.creatorAvatar} alt={doc.creatorUsername} />
                            ) : (
                              <AvatarFallback className="text-[10px] bg-primary/10 text-primary">
                                {doc.creatorUsername ? doc.creatorUsername[0].toUpperCase() : <User className="h-3 w-3" />}
                              </AvatarFallback>
                            )}
                          </Avatar>
                          <span className="text-sm text-muted-foreground">{doc.creatorUsername || '—'}</span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <span className="text-sm text-muted-foreground tabular-nums">{formatDateTime(doc.createTime)}</span>
                      </TableCell>
                      <TableCell className="pr-5">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity">
                              <MoreHorizontal className="h-4 w-4" />
                              <span className="sr-only">操作</span>
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end" className="w-36">
                            <DropdownMenuItem disabled={!canView} asChild={canView}>
                              {canView ? (
                                <Link href={`/dataset/${datasetId}/document/${doc.documentId}`}>
                                  <FileText className="mr-2 h-3.5 w-3.5" />
                                  查看分段
                                </Link>
                              ) : (
                                <span className="flex items-center">
                                  <FileText className="mr-2 h-3.5 w-3.5" />
                                  查看分段
                                </span>
                              )}
                            </DropdownMenuItem>
                            <DropdownMenuItem disabled={!canView}>
                              <Download className="mr-2 h-3.5 w-3.5" />
                              下载文档
                            </DropdownMenuItem>
                            <DropdownMenuItem className="text-destructive focus:text-destructive" onClick={() => confirmDeleteDocument(doc)}>
                              <Trash className="mr-2 h-3.5 w-3.5" />
                              删除文档
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  )
                })
              ) : (
                <TableRow>
                  <TableCell colSpan={7} className="h-48 text-center">
                    <div className="flex flex-col items-center gap-3">
                      <div className="h-14 w-14 rounded-2xl bg-muted/60 flex items-center justify-center">
                        <FileText className="h-6 w-6 text-muted-foreground/40" />
                      </div>
                      <div>
                        <p className="font-semibold text-sm">{searchTerm ? `未找到 "${searchTerm}"` : '暂无文档'}</p>
                        <p className="text-xs text-muted-foreground mt-0.5">{searchTerm ? '请尝试其他关键词' : '点击上传按钮开始添加文档'}</p>
                      </div>
                      {!searchTerm && (
                        <Button variant="outline" size="sm" className="gap-1.5 mt-1" onClick={() => setUploadDialogOpen(true)}>
                          <Plus className="h-3.5 w-3.5" />
                          上传第一个文档
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>

        {/* Pagination */}
        {total > pageSize && (
          <div className="flex items-center justify-between animate-fade-in">
            <span className="text-sm text-muted-foreground">
              第 <span className="tabular-nums font-medium">{(page - 1) * pageSize + 1}</span>–<span className="tabular-nums font-medium">{Math.min(page * pageSize, total)}</span>{' '}
              条， 共 <span className="tabular-nums font-medium">{total}</span> 条
            </span>
            <CustomPagination currentPage={page} pageSize={pageSize} totalItems={total} onPageChange={setPage} />
          </div>
        )}
      </div>

      <DocumentUploadDialog open={uploadDialogOpen} onOpenChange={setUploadDialogOpen} tenantId={tenantId} datasetId={datasetId} onSuccess={loadDocuments} />
      <ConfirmDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        title="确认删除文档"
        description={`您确定要删除文档 "${documentToDelete?.fileName}" 吗？此操作不可撤销。`}
        onConfirm={handleDeleteDocument}
      />
    </div>
  )
}
