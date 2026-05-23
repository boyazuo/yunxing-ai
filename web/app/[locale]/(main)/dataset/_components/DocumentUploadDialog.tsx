'use client'

import { documentService } from '@/api/document'
import { fileService } from '@/api/file'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { SegmentMethod } from '@/types/document'
import { Upload } from 'lucide-react'
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

export function DocumentUploadDialog({ open, onOpenChange, tenantId, datasetId, onSuccess }: DocumentUploadDialogProps) {
  const [file, setFile] = useState<File | null>(null)
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
  }, [])

  const handleDrop = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    e.stopPropagation()

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
      toast.success('文档上传成功')
      onSuccess()
      resetForm()
      onOpenChange(false)
    } catch (error) {
      console.error('上传文档失败', error)
      toast.error('上传失败')
    } finally {
      setIsUploading(false)
    }
  }, [
    file,
    tenantId,
    datasetId,
    segmentMethod,
    maxSegmentLength,
    overlapLength,
    parentChunkSize,
    onSuccess,
    resetForm,
    onOpenChange,
  ])

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>上传文档</DialogTitle>
          <DialogDescription>上传文档到知识库，支持 PDF、DOCX、TXT 等格式</DialogDescription>
        </DialogHeader>

        <div
          className={`border-2 border-dashed rounded-lg p-6 text-center ${file ? 'border-primary' : 'border-muted-foreground/20'} transition-colors`}
          onDragOver={handleDragOver}
          onDrop={handleDrop}
        >
          {!file ? (
            <>
              <div className="flex flex-col items-center justify-center space-y-2">
                <Upload className="h-8 w-8 text-muted-foreground" />
                <div className="flex flex-col space-y-1">
                  <span className="text-sm font-medium">拖放文件至此处，或者</span>
                  <label htmlFor="file-upload" className="cursor-pointer text-sm text-primary hover:underline">
                    浏览文件
                  </label>
                </div>
                <span className="text-xs text-muted-foreground">支持 PDF, DOCX, TXT 等常见文档格式</span>
              </div>
              <Input id="file-upload" type="file" className="hidden" onChange={handleFileChange} accept=".pdf,.doc,.docx,.txt,.md,.csv" />
            </>
          ) : (
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">{file.name}</span>
                <Button variant="ghost" size="sm" onClick={() => setFile(null)}>
                  更改
                </Button>
              </div>
              <div className="text-xs text-muted-foreground">{(file.size / 1024 / 1024).toFixed(2)} MB</div>
              {isUploading && (
                <div className="w-full h-2 bg-muted rounded-full overflow-hidden">
                  <div className="h-full bg-primary transition-all" style={{ width: `${uploadProgress}%` }} />
                </div>
              )}
            </div>
          )}
        </div>

        <div className="space-y-4">
          <div className="space-y-2">
            <Label>分段方式</Label>
            <Tabs value={segmentMethod} onValueChange={(value) => setSegmentMethod(value as SegmentMethod)}>
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value={SegmentMethod.PARENT_CHILD}>父子分块</TabsTrigger>
                <TabsTrigger value={SegmentMethod.PARAGRAPH}>按段落</TabsTrigger>
                <TabsTrigger value={SegmentMethod.CHAPTER}>按章节</TabsTrigger>
              </TabsList>
              <TabsContent value={SegmentMethod.PARENT_CHILD} className="space-y-4 pt-4">
                <p className="text-xs text-muted-foreground">检索精准 + 上下文丰富，推荐用于大多数文档</p>
                <div className="space-y-2">
                  <Label htmlFor="parentChunkSize">父块大小（字）</Label>
                  <Input
                    id="parentChunkSize"
                    type="number"
                    value={parentChunkSize}
                    onChange={(e) => setParentChunkSize(Number(e.target.value))}
                    min={500}
                    max={4000}
                    step={100}
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="childChunkSize">子块大小（字）</Label>
                    <Input
                      id="childChunkSize"
                      type="number"
                      value={maxSegmentLength}
                      onChange={(e) => setMaxSegmentLength(Number(e.target.value))}
                      min={100}
                      max={1000}
                      step={50}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="childOverlap">子块重叠（字）</Label>
                    <Input
                      id="childOverlap"
                      type="number"
                      value={overlapLength}
                      onChange={(e) => setOverlapLength(Number(e.target.value))}
                      min={0}
                      max={200}
                      step={10}
                    />
                  </div>
                </div>
                {childSizeWarning && <p className="text-xs text-amber-600">{childSizeWarning}</p>}
                <p className="text-xs text-muted-foreground">
                  预估：约每 {parentChunkSize} 字生成 1 个父块，每 {maxSegmentLength} 字生成 1 个子块
                </p>
              </TabsContent>
              <TabsContent value={SegmentMethod.PARAGRAPH} className="space-y-4 pt-4">
                <p className="text-xs text-muted-foreground">通用方式，适合纯文本文档</p>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="maxSegmentLength">最大分段长度</Label>
                    <Input
                      id="maxSegmentLength"
                      type="number"
                      value={maxSegmentLength}
                      onChange={(e) => setMaxSegmentLength(Number(e.target.value))}
                      min={100}
                      max={5000}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="overlapLength">重叠长度</Label>
                    <Input
                      id="overlapLength"
                      type="number"
                      value={overlapLength}
                      onChange={(e) => setOverlapLength(Number(e.target.value))}
                      min={0}
                      max={500}
                    />
                  </div>
                </div>
              </TabsContent>
              <TabsContent value={SegmentMethod.CHAPTER} className="space-y-4 pt-4">
                <p className="text-xs text-muted-foreground">适合有明确章节结构的文档</p>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="chapterMaxLength">最大分段长度</Label>
                    <Input
                      id="chapterMaxLength"
                      type="number"
                      value={maxSegmentLength}
                      onChange={(e) => setMaxSegmentLength(Number(e.target.value))}
                      min={100}
                      max={5000}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="chapterOverlap">重叠长度</Label>
                    <Input
                      id="chapterOverlap"
                      type="number"
                      value={overlapLength}
                      onChange={(e) => setOverlapLength(Number(e.target.value))}
                      min={0}
                      max={500}
                    />
                  </div>
                </div>
              </TabsContent>
            </Tabs>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={isUploading}>
            取消
          </Button>
          <Button onClick={handleUpload} disabled={!file || isUploading}>
            {isUploading ? '上传中...' : '上传文档'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
