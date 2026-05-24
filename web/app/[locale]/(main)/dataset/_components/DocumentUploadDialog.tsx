'use client'

import { documentService } from '@/api/document'
import { fileService } from '@/api/file'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { SegmentMethod } from '@/types/document'
import { CheckCircle2, File, Layers, Loader2, SplitSquareHorizontal, Upload, X } from 'lucide-react'
import { useCallback, useMemo, useState } from 'react'
import { toast } from 'sonner'

interface DocumentUploadDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  tenantId: string
  datasetId: string
  onSuccess: () => void
}

const DEFAULT_PARENT_CHUNK_SIZE = 1200
const DEFAULT_CHILD_CHUNK_SIZE = 300
const DEFAULT_CHILD_OVERLAP = 50

const ACCEPTED_EXTENSIONS = '.pdf,.doc,.docx,.txt,.md,.csv'
const ACCEPTED_LABEL = 'PDF、DOCX、TXT、MD、CSV'

export function DocumentUploadDialog({ open, onOpenChange, tenantId, datasetId, onSuccess }: DocumentUploadDialogProps) {
  const [file, setFile] = useState<File | null>(null)
  const [isDragging, setIsDragging] = useState(false)
  const [isUploading, setIsUploading] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)
  const [segmentMethod, setSegmentMethod] = useState<SegmentMethod>(SegmentMethod.PARENT_CHILD)
  const [parentChunkSize, setParentChunkSize] = useState<number>(DEFAULT_PARENT_CHUNK_SIZE)
  const [maxSegmentLength, setMaxSegmentLength] = useState<number>(DEFAULT_CHILD_CHUNK_SIZE)
  const [overlapLength, setOverlapLength] = useState<number>(DEFAULT_CHILD_OVERLAP)

  const childSizeWarning = useMemo(() => {
    if (segmentMethod !== SegmentMethod.PARENT_CHILD) return null
    if (maxSegmentLength > parentChunkSize * 0.5) {
      return '建议子块大小不超过父块大小的 50%，以保证检索精度'
    }
    return null
  }, [segmentMethod, maxSegmentLength, parentChunkSize])

  const handleDragOver = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(true)
  }, [])

  const handleDragLeave = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    setIsDragging(false)
  }, [])

  const handleDrop = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
    if (e.dataTransfer.files?.[0]) {
      setFile(e.dataTransfer.files[0])
    }
  }, [])

  const handleFileChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.[0]) {
      setFile(e.target.files[0])
    }
  }, [])

  const resetForm = useCallback(() => {
    setFile(null)
    setUploadProgress(0)
    setSegmentMethod(SegmentMethod.PARENT_CHILD)
    setParentChunkSize(DEFAULT_PARENT_CHUNK_SIZE)
    setMaxSegmentLength(DEFAULT_CHILD_CHUNK_SIZE)
    setOverlapLength(DEFAULT_CHILD_OVERLAP)
  }, [])

  const handleUpload = useCallback(async () => {
    if (!file) {
      toast.error('请选择文件')
      return
    }
    try {
      setIsUploading(true)
      setUploadProgress(10)
      const uploadedFile = await fileService.updateFile(file)
      setUploadProgress(60)
      const result = await documentService.createDocument({
        tenantId,
        datasetId,
        fileId: uploadedFile.fileId,
        fileName: file.name,
        fileSize: file.size,
        segmentMethod,
        maxSegmentLength,
        overlapLength,
        parentChunkSize: segmentMethod === SegmentMethod.PARENT_CHILD ? parentChunkSize : undefined,
      })
      if (result.code !== 0) {
        toast.error(result.msg)
        return
      }
      setUploadProgress(100)
      toast.success('文档上传成功，正在后台处理中')
      onSuccess()
      resetForm()
      onOpenChange(false)
    } catch (error) {
      console.error('上传文档失败', error)
      toast.error('上传失败，请重试')
    } finally {
      setIsUploading(false)
    }
  }, [file, tenantId, datasetId, segmentMethod, maxSegmentLength, overlapLength, parentChunkSize, onSuccess, resetForm, onOpenChange])

  const fileSizeMB = file ? (file.size / 1024 / 1024).toFixed(2) : '0'

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!isUploading) { onOpenChange(v); if (!v) resetForm() } }}>
      <DialogContent className="sm:max-w-[520px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Upload className="h-5 w-5 text-primary" />
            上传文档
          </DialogTitle>
          <DialogDescription>
            支持 {ACCEPTED_LABEL} 等常见文档格式
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-5">
          {/* 文件上传区域 */}
          {!file ? (
            <div
              className={`relative border-2 border-dashed rounded-xl p-8 text-center transition-all duration-200 cursor-pointer group
                ${isDragging
                  ? 'border-primary bg-primary/5 scale-[1.01]'
                  : 'border-muted-foreground/20 hover:border-primary/50 hover:bg-muted/30'
                }`}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
              onClick={() => window.document.getElementById('file-upload')?.click()}
              onKeyDown={(e) => e.key === 'Enter' && window.document.getElementById('file-upload')?.click()}
            >
              <div className="flex flex-col items-center gap-3">
                <div className={`h-14 w-14 rounded-2xl flex items-center justify-center transition-colors
                  ${isDragging ? 'bg-primary/15' : 'bg-muted/60 group-hover:bg-primary/10'}`}>
                  <Upload className={`h-6 w-6 transition-colors ${isDragging ? 'text-primary' : 'text-muted-foreground group-hover:text-primary'}`} />
                </div>
                <div className="space-y-1">
                  <p className="text-sm font-medium">
                    {isDragging ? '松开以上传文件' : '拖放文件至此，或点击选择'}
                  </p>
                  <p className="text-xs text-muted-foreground">{ACCEPTED_LABEL}</p>
                </div>
              </div>
              <Input
                id="file-upload"
                type="file"
                className="sr-only"
                onChange={handleFileChange}
                accept={ACCEPTED_EXTENSIONS}
              />
            </div>
          ) : (
            <div className="rounded-xl border bg-muted/30 overflow-hidden">
              {/* 文件信息 */}
              <div className="flex items-center gap-3 p-4">
                <div className="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                  <File className="h-5 w-5 text-primary" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">{file.name}</p>
                  <p className="text-xs text-muted-foreground mt-0.5">{fileSizeMB} MB</p>
                </div>
                {!isUploading && (
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8 flex-shrink-0 text-muted-foreground hover:text-foreground"
                    onClick={() => setFile(null)}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                )}
                {isUploading && uploadProgress === 100 && (
                  <CheckCircle2 className="h-5 w-5 text-emerald-500 flex-shrink-0" />
                )}
              </div>
              {/* 上传进度条 */}
              {isUploading && (
                <div className="px-4 pb-4 space-y-2">
                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <span>{uploadProgress < 60 ? '上传文件中...' : uploadProgress < 100 ? '处理文档中...' : '完成'}</span>
                    <span className="tabular-nums">{uploadProgress}%</span>
                  </div>
                  <div className="w-full h-1.5 bg-muted rounded-full overflow-hidden">
                    <div
                      className="h-full bg-primary rounded-full transition-all duration-500"
                      style={{ width: `${uploadProgress}%` }}
                    />
                  </div>
                </div>
              )}
            </div>
          )}

          {/* 分段设置 */}
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <SplitSquareHorizontal className="h-4 w-4 text-muted-foreground" />
              <Label className="text-sm font-medium">分段方式</Label>
            </div>
            <Tabs value={segmentMethod} onValueChange={(v) => setSegmentMethod(v as SegmentMethod)}>
              <TabsList className="grid w-full grid-cols-3 h-9">
                <TabsTrigger value={SegmentMethod.PARENT_CHILD} className="text-xs gap-1.5">
                  <Layers className="h-3.5 w-3.5" />
                  父子分块
                </TabsTrigger>
                <TabsTrigger value={SegmentMethod.PARAGRAPH} className="text-xs">按段落</TabsTrigger>
                <TabsTrigger value={SegmentMethod.CHAPTER} className="text-xs">按章节</TabsTrigger>
              </TabsList>

              <TabsContent value={SegmentMethod.PARENT_CHILD} className="space-y-4 pt-4">
                <div className="rounded-lg bg-blue-50 dark:bg-blue-950/50 border border-blue-100 dark:border-blue-900 px-3 py-2.5">
                  <p className="text-xs text-blue-700 dark:text-blue-300 leading-relaxed">
                    <span className="font-medium">推荐方式：</span>大块存储上下文、小块精准检索，适合大多数文档场景
                  </p>
                </div>
                <div className="space-y-3">
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="parentChunkSize" className="text-xs text-muted-foreground">父块大小（字）</Label>
                      <span className="text-xs font-medium tabular-nums">{parentChunkSize}</span>
                    </div>
                    <Input
                      id="parentChunkSize"
                      type="number"
                      value={parentChunkSize}
                      onChange={(e) => setParentChunkSize(Number(e.target.value))}
                      min={500}
                      max={4000}
                      step={100}
                      className="h-8 text-sm"
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div className="space-y-1.5">
                      <div className="flex items-center justify-between">
                        <Label htmlFor="childChunkSize" className="text-xs text-muted-foreground">子块大小（字）</Label>
                        <span className="text-xs font-medium tabular-nums">{maxSegmentLength}</span>
                      </div>
                      <Input
                        id="childChunkSize"
                        type="number"
                        value={maxSegmentLength}
                        onChange={(e) => setMaxSegmentLength(Number(e.target.value))}
                        min={100}
                        max={1000}
                        step={50}
                        className="h-8 text-sm"
                      />
                    </div>
                    <div className="space-y-1.5">
                      <div className="flex items-center justify-between">
                        <Label htmlFor="childOverlap" className="text-xs text-muted-foreground">子块重叠（字）</Label>
                        <span className="text-xs font-medium tabular-nums">{overlapLength}</span>
                      </div>
                      <Input
                        id="childOverlap"
                        type="number"
                        value={overlapLength}
                        onChange={(e) => setOverlapLength(Number(e.target.value))}
                        min={0}
                        max={200}
                        step={10}
                        className="h-8 text-sm"
                      />
                    </div>
                  </div>
                  {childSizeWarning && (
                    <p className="text-xs text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/50 rounded-md px-2.5 py-1.5">
                      ⚠ {childSizeWarning}
                    </p>
                  )}
                  <p className="text-xs text-muted-foreground">
                    预估：每 {parentChunkSize} 字 1 个父块 · 每 {maxSegmentLength} 字 1 个子块
                  </p>
                </div>
              </TabsContent>

              <TabsContent value={SegmentMethod.PARAGRAPH} className="space-y-4 pt-4">
                <div className="rounded-lg bg-muted/50 border px-3 py-2.5">
                  <p className="text-xs text-muted-foreground">通用方式，适合纯文本文档，按自然段落分割</p>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="maxSegmentLength" className="text-xs text-muted-foreground">最大分段长度</Label>
                      <span className="text-xs font-medium tabular-nums">{maxSegmentLength}</span>
                    </div>
                    <Input
                      id="maxSegmentLength"
                      type="number"
                      value={maxSegmentLength}
                      onChange={(e) => setMaxSegmentLength(Number(e.target.value))}
                      min={100}
                      max={5000}
                      className="h-8 text-sm"
                    />
                  </div>
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="overlapLength" className="text-xs text-muted-foreground">重叠长度</Label>
                      <span className="text-xs font-medium tabular-nums">{overlapLength}</span>
                    </div>
                    <Input
                      id="overlapLength"
                      type="number"
                      value={overlapLength}
                      onChange={(e) => setOverlapLength(Number(e.target.value))}
                      min={0}
                      max={500}
                      className="h-8 text-sm"
                    />
                  </div>
                </div>
              </TabsContent>

              <TabsContent value={SegmentMethod.CHAPTER} className="space-y-4 pt-4">
                <div className="rounded-lg bg-muted/50 border px-3 py-2.5">
                  <p className="text-xs text-muted-foreground">适合有明确章节标题结构（Markdown / Word）的文档</p>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="chapterMaxLength" className="text-xs text-muted-foreground">最大分段长度</Label>
                      <span className="text-xs font-medium tabular-nums">{maxSegmentLength}</span>
                    </div>
                    <Input
                      id="chapterMaxLength"
                      type="number"
                      value={maxSegmentLength}
                      onChange={(e) => setMaxSegmentLength(Number(e.target.value))}
                      min={100}
                      max={5000}
                      className="h-8 text-sm"
                    />
                  </div>
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="chapterOverlap" className="text-xs text-muted-foreground">重叠长度</Label>
                      <span className="text-xs font-medium tabular-nums">{overlapLength}</span>
                    </div>
                    <Input
                      id="chapterOverlap"
                      type="number"
                      value={overlapLength}
                      onChange={(e) => setOverlapLength(Number(e.target.value))}
                      min={0}
                      max={500}
                      className="h-8 text-sm"
                    />
                  </div>
                </div>
              </TabsContent>
            </Tabs>
          </div>
        </div>

        <DialogFooter className="gap-2 mt-2">
          <Button
            variant="outline"
            onClick={() => { onOpenChange(false); resetForm() }}
            disabled={isUploading}
          >
            取消
          </Button>
          <Button
            onClick={handleUpload}
            disabled={!file || isUploading}
            className="gap-2 min-w-[100px]"
          >
            {isUploading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                上传中...
              </>
            ) : (
              <>
                <Upload className="h-4 w-4" />
                上传文档
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
