'use client'

import { documentService } from '@/api/document'
import { fileService } from '@/api/file'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import { SegmentMethod } from '@/types/document'
import { Upload } from 'lucide-react'
import { useCallback, useState } from 'react'
import { toast } from 'sonner'

interface DocumentUploadDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  tenantId: string
  datasetId: string
  onSuccess: () => void
}

export function DocumentUploadDialog({
  open,
  onOpenChange,
  tenantId,
  datasetId,
  onSuccess,
}: DocumentUploadDialogProps) {
  const [file, setFile] = useState<File | null>(null)
  const [isUploading, setIsUploading] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)
  const [segmentMethod, setSegmentMethod] = useState<SegmentMethod>(SegmentMethod.PARAGRAPH)
  const [maxSegmentLength, setMaxSegmentLength] = useState<number>(1000)
  const [overlapLength, setOverlapLength] = useState<number>(100)

  // 文件拖放处理
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

  // 文件选择处理
  const handleFileChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.[0]) {
      setFile(e.target.files[0])
    }
  }, [])

  // 重置表单
  const resetForm = useCallback(() => {
    setFile(null)
    setUploadProgress(0)
    setSegmentMethod(SegmentMethod.PARAGRAPH)
    setMaxSegmentLength(1000)
    setOverlapLength(100)
  }, [])

  // 上传文档
  const handleUpload = useCallback(async () => {
    if (!file) {
      toast.error('请选择文件')
      return
    }

    try {
      setIsUploading(true)
      setUploadProgress(10)

      // 1. 上传文件
      const uploadedFile = await fileService.updateFile(file)
      setUploadProgress(60)

      // 2. 创建文档记录
      await documentService.createDocument({
        tenantId,
        datasetId,
        fileId: uploadedFile.fileId,
        fileName: file.name,
        fileSize: file.size,
        segmentMethod,
        maxSegmentLength,
        overlapLength,
      })

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
  }, [file, tenantId, datasetId, segmentMethod, maxSegmentLength, overlapLength, onSuccess, resetForm, onOpenChange])

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>上传文档</DialogTitle>
          <DialogDescription>上传文档到知识库，支持PDF、DOCX、TXT等格式</DialogDescription>
        </DialogHeader>

        {/* 文件上传区域 */}
        <div
          className={`border-2 border-dashed rounded-lg p-6 text-center ${
            file ? 'border-primary' : 'border-muted-foreground/20'
          } transition-colors`}
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
              <Input
                id="file-upload"
                type="file"
                className="hidden"
                onChange={handleFileChange}
                accept=".pdf,.doc,.docx,.txt,.md,.csv"
              />
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

        {/* 分段设置 */}
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="segmentMethod">分段方式</Label>
            <RadioGroup
              value={segmentMethod}
              onValueChange={(value) => setSegmentMethod(value as SegmentMethod)}
              className="flex flex-col space-y-1"
            >
              <div className="flex items-center space-x-2">
                <RadioGroupItem value={SegmentMethod.PARAGRAPH} id="paragraph" />
                <Label htmlFor="paragraph" className="cursor-pointer">
                  按段落分段
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value={SegmentMethod.SENTENCE} id="sentence" />
                <Label htmlFor="sentence" className="cursor-pointer">
                  按句子分段
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value={SegmentMethod.CUSTOM} id="custom" />
                <Label htmlFor="custom" className="cursor-pointer">
                  自定义分段
                </Label>
              </div>
            </RadioGroup>
          </div>

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
              <p className="text-xs text-muted-foreground">建议值：500-2000</p>
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
              <p className="text-xs text-muted-foreground">建议值：0-200</p>
            </div>
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
