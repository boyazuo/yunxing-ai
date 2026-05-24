'use client'

import { documentService } from '@/api/document'
import { fileService } from '@/api/file'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { CheckCircle2, File, Loader2, Upload, X } from 'lucide-react'
import { useCallback, useState } from 'react'
import { toast } from 'sonner'

interface DocumentUploadDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  tenantId: string
  datasetId: string
  onSuccess: () => void
}

const ACCEPTED_EXTENSIONS = '.pdf,.doc,.docx,.txt,.md,.csv'
const ACCEPTED_LABEL = 'PDF、DOCX、TXT、MD、CSV'

export function DocumentUploadDialog({ open, onOpenChange, tenantId, datasetId, onSuccess }: DocumentUploadDialogProps) {
  const [file, setFile] = useState<File | null>(null)
  const [isDragging, setIsDragging] = useState(false)
  const [isUploading, setIsUploading] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)

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
  }, [file, tenantId, datasetId, onSuccess, resetForm, onOpenChange])

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
