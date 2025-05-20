'use client'

import { datasetService } from '@/api/dataset'
import { documentService } from '@/api/document'
import { DocumentUploadDialog } from '@/app/[locale]/(main)/dataset/_components/DocumentUploadDialog'
import { ConfirmDialog } from '@/components/blocks/confirm-dialog'
import { CustomPagination } from '@/components/blocks/pagination'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import type { Dataset } from '@/types/dataset'
import { type DatasetDocument, DocumentStatus, getDocumentStatusText } from '@/types/document'
import { ArrowLeft, Download, File, MoreHorizontal, Plus, Trash, Upload, User } from 'lucide-react'
import { useSession } from 'next-auth/react'
import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'
import { useCallback, useEffect, useState } from 'react'
import { toast } from 'sonner'

// 将字节大小转换为易读的格式
function formatFileSize(sizeInBytes: number) {
  if (sizeInBytes < 1024) {
    return `${sizeInBytes} B`
  }
  if (sizeInBytes < 1024 * 1024) {
    return `${(sizeInBytes / 1024).toFixed(2)} KB`
  }
  if (sizeInBytes < 1024 * 1024 * 1024) {
    return `${(sizeInBytes / (1024 * 1024)).toFixed(2)} MB`
  }
  return `${(sizeInBytes / (1024 * 1024 * 1024)).toFixed(2)} GB`
}

// 格式化更新时间
function formatDateTime(dateTimeStr?: string) {
  if (!dateTimeStr) return '未知'

  try {
    const date = new Date(dateTimeStr)
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const hour = date.getHours().toString().padStart(2, '0')
    const minute = date.getMinutes().toString().padStart(2, '0')

    return `${year}-${month}-${day} ${hour}:${minute}`
  } catch (error) {
    return '未知'
  }
}

export default function DatasetDocumentsPage() {
  const router = useRouter()
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

  // 从会话中获取租户ID
  const tenantId = session?.tenant?.tenantId || ''
  // 从URL参数中获取数据集ID
  const datasetId = params.datasetId as string

  // 加载数据集信息
  const loadDataset = useCallback(async () => {
    try {
      if (!datasetId) {
        return
      }

      const data = await datasetService.getDataset(datasetId)
      setDataset(data)
    } catch (error) {
      console.error('加载数据集信息失败', error)
      toast.error('加载数据集信息失败')
    }
  }, [datasetId])

  // 加载文档列表
  const loadDocuments = useCallback(async () => {
    try {
      if (!datasetId) {
        return
      }

      setIsLoading(true)

      const response = await documentService.getDocuments(datasetId, page, pageSize)
      setDocuments(response.records)
      setTotal(response.total)
    } catch (error) {
      console.error('加载文档列表失败', error)
      toast.error('加载文档列表失败')
    } finally {
      setIsLoading(false)
    }
  }, [datasetId, page, pageSize])

  // 首次加载
  useEffect(() => {
    if (status === 'authenticated' && datasetId) {
      loadDataset()
      loadDocuments()
    }
  }, [status, datasetId, loadDataset, loadDocuments])

  // 处理页码变化
  const handlePageChange = (newPage: number) => {
    setPage(newPage)
  }

  // 打开上传文档对话框
  const handleOpenUploadDialog = () => {
    setUploadDialogOpen(true)
  }

  // 删除文档
  const confirmDeleteDocument = (document: DatasetDocument) => {
    setDocumentToDelete(document)
    setDeleteDialogOpen(true)
  }

  const handleDeleteDocument = async () => {
    try {
      if (!documentToDelete) return

      await documentService.deleteDocument(documentToDelete.documentId.toString())

      toast.success('文档已成功删除')

      loadDocuments()
    } catch (error) {
      console.error('删除文档失败', error)
      toast.error('删除文档失败')
    } finally {
      setDeleteDialogOpen(false)
      setDocumentToDelete(null)
    }
  }

  // 过滤文档
  const filteredDocuments = documents.filter((document) => {
    // 名称搜索
    const nameMatch = searchTerm ? document.fileName.toLowerCase().includes(searchTerm.toLowerCase()) : true

    // 状态过滤
    const statusMatch = statusFilter === 'all' ? true : document.status === statusFilter

    return nameMatch && statusMatch
  })

  return (
    <div className="container mx-auto py-6 space-y-6">
      {/* 返回按钮和标题 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <Button variant="ghost" size="icon" asChild>
            <Link href="/dataset">
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div>
            <h1 className="text-2xl font-bold">{dataset?.datasetName || '数据集'}</h1>
          </div>
        </div>

        <Button onClick={handleOpenUploadDialog}>
          <Upload className="mr-2 h-4 w-4" />
          上传文档
        </Button>
      </div>

      {/* 过滤和搜索区域 */}
      <div className="flex justify-between items-center">
        <div className="flex items-center space-x-4">
          <Select defaultValue="all" onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[180px]">
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
        </div>

        <div className="flex items-center space-x-2">
          <Input
            placeholder="搜索文档名称..."
            className="w-[250px]"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      {/* 文档表格 */}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[40%]">文档名称</TableHead>
              <TableHead>大小</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>分段数</TableHead>
              <TableHead>创建者</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              // 加载状态
              Array.from({ length: 5 }).map((_, index) => (
                <TableRow key={`skeleton-${Math.random().toString(36).substring(2, 7)}`}>
                  <TableCell>
                    <div className="flex items-center space-x-2">
                      <Skeleton className="h-8 w-8 rounded" />
                      <Skeleton className="h-4 w-40" />
                    </div>
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-16" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-16" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-8" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-20" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-24" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-8 w-8 rounded-full ml-auto" />
                  </TableCell>
                </TableRow>
              ))
            ) : filteredDocuments.length > 0 ? (
              // 文档列表
              filteredDocuments.map((document) => (
                <TableRow key={document.documentId}>
                  <TableCell>
                    <div className="flex items-center space-x-2">
                      <div className="h-8 w-8 rounded bg-muted flex items-center justify-center">
                        <File className="h-4 w-4 text-muted-foreground" />
                      </div>
                      <span className="font-medium">{document.fileName}</span>
                    </div>
                  </TableCell>
                  <TableCell>{formatFileSize(document.fileSize)}</TableCell>
                  <TableCell>
                    <Badge
                      variant={
                        document.status === DocumentStatus.COMPLETED
                          ? 'default'
                          : document.status === DocumentStatus.PROCESSING
                            ? 'secondary'
                            : document.status === DocumentStatus.PENDING
                              ? 'outline'
                              : 'destructive'
                      }
                    >
                      {getDocumentStatusText(document.status)}
                    </Badge>
                  </TableCell>
                  <TableCell>{document.segmentNum}</TableCell>
                  <TableCell>
                    <div className="flex items-center space-x-1">
                      <Avatar className="h-6 w-6">
                        {document.creatorAvatar ? (
                          <AvatarImage src={document.creatorAvatar} alt={document.creatorUsername} />
                        ) : (
                          <AvatarFallback>
                            {document.creatorUsername ? (
                              document.creatorUsername.slice(0, 1).toUpperCase()
                            ) : (
                              <User className="h-3 w-3" />
                            )}
                          </AvatarFallback>
                        )}
                      </Avatar>
                      <span className="text-sm">{document.creatorUsername}</span>
                    </div>
                  </TableCell>
                  <TableCell>{formatDateTime(document.createTime)}</TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreHorizontal className="h-4 w-4" />
                          <span className="sr-only">操作菜单</span>
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem disabled={document.status !== DocumentStatus.COMPLETED}>
                          <Download className="mr-2 h-4 w-4" />
                          <span>下载文档</span>
                        </DropdownMenuItem>
                        <DropdownMenuItem className="text-destructive" onClick={() => confirmDeleteDocument(document)}>
                          <Trash className="mr-2 h-4 w-4" />
                          <span>删除文档</span>
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              // 空状态
              <TableRow>
                <TableCell colSpan={7} className="h-32 text-center">
                  <div className="flex flex-col items-center justify-center">
                    <p className="text-muted-foreground">暂无文档</p>
                    <Button variant="outline" className="mt-2" onClick={handleOpenUploadDialog}>
                      <Plus className="mr-2 h-4 w-4" />
                      上传第一个文档
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* 分页 */}
      {total > pageSize && (
        <div className="flex justify-center">
          <CustomPagination currentPage={page} pageSize={pageSize} totalItems={total} onPageChange={handlePageChange} />
        </div>
      )}

      {/* 上传文档对话框 */}
      <DocumentUploadDialog
        open={uploadDialogOpen}
        onOpenChange={setUploadDialogOpen}
        tenantId={tenantId}
        datasetId={datasetId}
        onSuccess={loadDocuments}
      />

      {/* 删除确认对话框 */}

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
