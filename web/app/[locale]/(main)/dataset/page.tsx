'use client'

import { datasetService } from '@/api/dataset'
import { DatasetFormDialog } from '@/app/[locale]/(main)/dataset/_components/DatasetFormDialog'
import { ConfirmDialog } from '@/components/blocks/confirm-dialog'
import { RowActionsTrigger } from '@/components/blocks/row-actions'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import type { Dataset } from '@/types/dataset'
import { DatasetStatus } from '@/types/dataset'
import { Edit, FileStack, Plus, Search, Trash, User } from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useRouter } from 'next/navigation'
import { useCallback, useEffect, useState, useTransition } from 'react'

// Color palette for dataset cards
const CARD_ACCENTS = [
  {
    gradient: 'from-indigo-500 to-violet-500',
    bg: 'bg-indigo-50 dark:bg-indigo-950/60',
    text: 'text-indigo-600 dark:text-indigo-300',
    glow: 'hover:shadow-indigo-100 dark:hover:shadow-indigo-900/30',
  },
  {
    gradient: 'from-violet-500 to-purple-500',
    bg: 'bg-violet-50 dark:bg-violet-950/60',
    text: 'text-violet-600 dark:text-violet-300',
    glow: 'hover:shadow-violet-100 dark:hover:shadow-violet-900/30',
  },
  {
    gradient: 'from-emerald-500 to-teal-500',
    bg: 'bg-emerald-50 dark:bg-emerald-950/60',
    text: 'text-emerald-600 dark:text-emerald-300',
    glow: 'hover:shadow-emerald-100 dark:hover:shadow-emerald-900/30',
  },
  { gradient: 'from-sky-500 to-cyan-500', bg: 'bg-sky-50 dark:bg-sky-950/60', text: 'text-sky-600 dark:text-sky-300', glow: 'hover:shadow-sky-100 dark:hover:shadow-sky-900/30' },
  {
    gradient: 'from-rose-500 to-pink-500',
    bg: 'bg-rose-50 dark:bg-rose-950/60',
    text: 'text-rose-600 dark:text-rose-300',
    glow: 'hover:shadow-rose-100 dark:hover:shadow-rose-900/30',
  },
  {
    gradient: 'from-amber-500 to-orange-500',
    bg: 'bg-amber-50 dark:bg-amber-950/60',
    text: 'text-amber-600 dark:text-amber-300',
    glow: 'hover:shadow-amber-100 dark:hover:shadow-amber-900/30',
  },
  {
    gradient: 'from-teal-500 to-emerald-600',
    bg: 'bg-teal-50 dark:bg-teal-950/60',
    text: 'text-teal-600 dark:text-teal-300',
    glow: 'hover:shadow-teal-100 dark:hover:shadow-teal-900/30',
  },
  {
    gradient: 'from-fuchsia-500 to-violet-600',
    bg: 'bg-fuchsia-50 dark:bg-fuchsia-950/60',
    text: 'text-fuchsia-600 dark:text-fuchsia-300',
    glow: 'hover:shadow-fuchsia-100 dark:hover:shadow-fuchsia-900/30',
  },
]

function getAccent(datasetId: string) {
  let hash = 0
  for (let i = 0; i < datasetId.length; i++) {
    hash = (hash * 31 + datasetId.charCodeAt(i)) & 0xffffffff
  }
  return CARD_ACCENTS[Math.abs(hash) % CARD_ACCENTS.length]
}

function formatUpdateTime(updateTime?: string) {
  if (!updateTime) return '—'
  try {
    const date = new Date(updateTime)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMins / 60)
    const diffDays = Math.floor(diffHours / 24)
    if (diffMins < 1) return '刚刚'
    if (diffMins < 60) return `${diffMins} 分钟前`
    if (diffHours < 24) return `${diffHours} 小时前`
    if (diffDays < 7) return `${diffDays} 天前`
    const m = (date.getMonth() + 1).toString().padStart(2, '0')
    const d = date.getDate().toString().padStart(2, '0')
    return `${m}-${d}`
  } catch {
    return '—'
  }
}

// Database icon as SVG for crisp rendering
function DatabaseIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" role="img">
      <title>Database</title>
      <ellipse cx="12" cy="5" rx="9" ry="3" />
      <path d="M3 5v14a9 3 0 0 0 18 0V5" />
      <path d="M3 12a9 3 0 0 0 18 0" />
    </svg>
  )
}

export default function DatasetsPage() {
  const router = useRouter()
  const { data: session, status } = useSession()
  const [datasets, setDatasets] = useState<Dataset[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [currentDataset, setCurrentDataset] = useState<Dataset | undefined>(undefined)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [datasetToDelete, setDatasetToDelete] = useState<Dataset | null>(null)
  const [isPending, startTransition] = useTransition()

  const tenantId = session?.tenant?.tenantId || ''

  const loadDatasets = useCallback(async () => {
    try {
      if (!tenantId) {
        setIsLoading(false)
        return
      }
      setIsLoading(true)
      const data = await datasetService.getDatasets(tenantId.toString())
      setDatasets(data || [])
    } catch (error) {
      console.error('加载知识库失败', error)
    } finally {
      setIsLoading(false)
    }
  }, [tenantId])

  useEffect(() => {
    if (status === 'authenticated' && tenantId) loadDatasets()
  }, [status, tenantId, loadDatasets])

  const handleCreateDataset = () => {
    setCurrentDataset(undefined)
    setIsFormOpen(true)
  }
  const handleEditDataset = (dataset: Dataset) => {
    setCurrentDataset(dataset)
    setIsFormOpen(true)
  }
  const confirmDeleteDataset = (dataset: Dataset) => {
    startTransition(() => {
      setDatasetToDelete(dataset)
      setDeleteDialogOpen(true)
    })
  }
  const handleDeleteDataset = async () => {
    try {
      if (!datasetToDelete) return
      await datasetService.deleteDataset(datasetToDelete.datasetId)
      setDeleteDialogOpen(false)
      setDatasetToDelete(null)
      loadDatasets()
    } catch (error) {
      console.error('删除知识库失败', error)
    }
  }

  const filteredDatasets = datasets.filter((d) => {
    const matchSearch = searchTerm ? d.datasetName.toLowerCase().includes(searchTerm.toLowerCase()) : true
    const matchStatus = statusFilter === 'all' ? true : d.status === statusFilter
    return matchSearch && matchStatus
  })

  return (
    <div className="min-h-[calc(100vh-64px)]">
      {/* ── Page hero header ── */}
      <div className="relative border-b bg-gradient-to-b from-primary/[0.04] to-background overflow-hidden">
        <div className="dot-pattern absolute inset-0 opacity-60" />
        <div className="relative container mx-auto px-6 py-10">
          <div className="flex items-end justify-between gap-6">
            <div className="space-y-2 animate-slide-up-fade">
              <div className="flex items-center gap-2.5">
                <div className="h-9 w-9 rounded-xl bg-primary/10 border border-primary/20 flex items-center justify-center">
                  <DatabaseIcon className="h-[18px] w-[18px] text-primary" />
                </div>
                <span className="text-xs font-semibold tracking-widest text-primary/70 uppercase">Knowledge Base</span>
              </div>
              <h1 className="text-3xl font-bold tracking-tight text-foreground">知识库</h1>
              <p className="text-sm text-muted-foreground">
                管理你的知识库，支持多种文档格式和向量检索
                {!isLoading && datasets.length > 0 && (
                  <span className="ml-2 inline-flex items-center gap-1 rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">{datasets.length} 个知识库</span>
                )}
              </p>
            </div>
            <div className="animate-slide-up-fade stagger-2">
              <Button onClick={handleCreateDataset} size="lg" className="gap-2 shadow-md shadow-primary/20 hover:shadow-lg hover:shadow-primary/25 transition-all duration-200">
                <Plus className="h-4 w-4" />
                新建知识库
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* ── Main content ── */}
      <div className="container mx-auto px-6 py-8 space-y-6">
        {/* Filter bar */}
        <div className="flex items-center justify-between gap-4 animate-slide-up-fade stagger-3">
          <div className="flex items-center gap-3">
            <Select defaultValue="all" onValueChange={setStatusFilter}>
              <SelectTrigger className="w-[130px] h-9 text-sm">
                <SelectValue placeholder="状态" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">全部状态</SelectItem>
                <SelectItem value={DatasetStatus.ACTIVE}>正常</SelectItem>
                <SelectItem value={DatasetStatus.DISABLED}>已禁用</SelectItem>
              </SelectContent>
            </Select>
            {!isLoading && filteredDatasets.length > 0 && <span className="text-sm text-muted-foreground">{filteredDatasets.length} 个结果</span>}
          </div>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <Input placeholder="搜索知识库..." className="pl-9 w-[220px] h-9 text-sm bg-background" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
          </div>
        </div>

        {/* Dataset grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {isLoading ? (
            // Skeleton cards
            [...Array(8)].map((_, i) => (
              // biome-ignore lint/suspicious/noArrayIndexKey: skeleton placeholders
              <div key={i} className={`h-[192px] rounded-2xl border bg-card overflow-hidden animate-pulse stagger-${Math.min(i + 1, 8)}`}>
                <div className="h-1 skeleton-shimmer" />
                <div className="p-5 space-y-4">
                  <div className="flex items-start gap-3">
                    <div className="h-10 w-10 rounded-xl skeleton-shimmer" />
                    <div className="flex-1 space-y-2 pt-1">
                      <div className="h-4 w-3/4 rounded skeleton-shimmer" />
                      <div className="h-3 w-1/3 rounded skeleton-shimmer" />
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <div className="h-3 rounded skeleton-shimmer" />
                    <div className="h-3 w-4/5 rounded skeleton-shimmer" />
                  </div>
                </div>
              </div>
            ))
          ) : filteredDatasets.length > 0 ? (
            filteredDatasets.map((dataset, i) => {
              const accent = getAccent(dataset.datasetId)
              const isActive = dataset.status === DatasetStatus.ACTIVE
              const staggerClass = `stagger-${Math.min(i + 1, 8)}`
              return (
                <div
                  key={dataset.datasetId}
                  className={`group relative rounded-2xl border bg-card overflow-hidden cursor-pointer
                    animate-slide-up-fade ${staggerClass}
                    transition-all duration-250 ease-out
                    hover:-translate-y-1 hover:shadow-lg ${accent.glow}
                    hover:border-primary/30`}
                  onClick={() => router.push(`/dataset/${dataset.datasetId}`)}
                  onKeyDown={(e) => e.key === 'Enter' && router.push(`/dataset/${dataset.datasetId}`)}
                >
                  {/* Top gradient accent bar */}
                  <div className={`h-1 bg-gradient-to-r ${accent.gradient} w-full`} />

                  {/* Card body */}
                  <div className="p-5">
                    <div className="flex items-start justify-between gap-2 mb-4">
                      {/* Icon + title */}
                      <div className="flex items-start gap-3 min-w-0">
                        <div className={`h-10 w-10 rounded-xl ${accent.bg} flex items-center justify-center flex-shrink-0`}>
                          <DatabaseIcon className={`h-5 w-5 ${accent.text}`} />
                        </div>
                        <div className="min-w-0 pt-0.5">
                          <h3 className="font-semibold text-sm leading-snug truncate group-hover:text-primary transition-colors duration-200 mb-1">{dataset.datasetName}</h3>
                          <span
                            className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-medium
                            ${isActive ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400' : 'bg-muted text-muted-foreground'}`}
                          >
                            <span className={`h-1.5 w-1.5 rounded-full ${isActive ? 'bg-emerald-500' : 'bg-muted-foreground/50'}`} />
                            {isActive ? '正常' : '已禁用'}
                          </span>
                        </div>
                      </div>

                      {/* Actions menu */}
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <RowActionsTrigger
                            size="sm"
                            className="-mr-1 -mt-0.5"
                            onClick={(e) => e.stopPropagation()}
                          />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-36">
                          <DropdownMenuItem
                            onClick={(e) => {
                              e.stopPropagation()
                              handleEditDataset(dataset)
                            }}
                          >
                            <Edit className="mr-2 h-3.5 w-3.5" />
                            编辑信息
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={(e) => {
                              e.stopPropagation()
                              confirmDeleteDataset(dataset)
                            }}
                          >
                            <Trash className="mr-2 h-3.5 w-3.5" />
                            删除
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>

                    {/* Description */}
                    <p className="text-xs text-muted-foreground line-clamp-2 leading-relaxed min-h-[2.5rem]">
                      {dataset.datasetDesc || <span className="italic opacity-60">暂无描述</span>}
                    </p>
                  </div>

                  {/* Footer */}
                  <div className="px-5 pb-4 flex items-center justify-between">
                    <div className="flex items-center gap-1.5">
                      <Avatar className="h-5 w-5 ring-1 ring-border">
                        {dataset.creatorAvatar && <AvatarImage src={dataset.creatorAvatar} alt={dataset.creatorUsername} />}
                        <AvatarFallback className="text-[8px] bg-primary/10 text-primary">
                          {dataset.creatorUsername?.slice(0, 1).toUpperCase() ?? <User className="h-2.5 w-2.5" />}
                        </AvatarFallback>
                      </Avatar>
                      <span className="text-[11px] text-muted-foreground">{dataset.creatorUsername || '未知'}</span>
                    </div>
                    <span className="text-[11px] text-muted-foreground/70">{formatUpdateTime(dataset.updateTime)}</span>
                  </div>
                </div>
              )
            })
          ) : (
            /* Empty state */
            <div className="col-span-full animate-fade-in">
              <div className="flex flex-col items-center justify-center py-20 text-center">
                {/* Illustrated empty state */}
                <div className="relative mb-6">
                  <div className="h-20 w-20 rounded-3xl bg-primary/8 border border-primary/15 flex items-center justify-center">
                    <FileStack className="h-9 w-9 text-primary/40" />
                  </div>
                  <div className="absolute -top-1 -right-1 h-6 w-6 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center">
                    <Plus className="h-3.5 w-3.5 text-primary/60" />
                  </div>
                </div>
                <h3 className="font-semibold text-base text-foreground mb-1.5">{searchTerm ? `未找到 "${searchTerm}"` : '还没有知识库'}</h3>
                <p className="text-sm text-muted-foreground max-w-xs mb-6">
                  {searchTerm ? '请尝试其他关键词，或清空搜索条件查看全部' : '创建你的第一个知识库，开始上传文档并构建 AI 知识体系'}
                </p>
                {!searchTerm && (
                  <Button onClick={handleCreateDataset} className="gap-2 shadow-md shadow-primary/20">
                    <Plus className="h-4 w-4" />
                    创建第一个知识库
                  </Button>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      <DatasetFormDialog open={isFormOpen} onOpenChange={setIsFormOpen} dataset={currentDataset} onSuccess={loadDatasets} tenantId={tenantId} />
      <ConfirmDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        title="确认删除知识库"
        description={`您确定要删除知识库 "${datasetToDelete?.datasetName}" 吗？此操作不可撤销，知识库内所有文档也将被删除。`}
        onConfirm={handleDeleteDataset}
      />
    </div>
  )
}
