'use client'

import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Separator } from '@/components/ui/separator'
import type { Model } from '@/types/ai'
import { ModelType } from '@/types/ai'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import * as z from 'zod'

// 模型表单验证
export const modelFormSchema = z.object({
  modelName: z.string().min(2, {
    message: '模型名称至少需要 2 个字符',
  }),
  displayName: z.string().min(2, {
    message: '显示名称至少需要 2 个字符',
  }),
  modelType: z.enum(['chat', 'reason', 'embedding', 'audio', 'image', 'video', 'code', 'rerank'], {
    message: '请选择模型类型',
  }),
  contextLength: z.coerce.number().min(1, {
    message: '上下文长度至少为 1',
  }),
  maxTokens: z.coerce.number().min(1, {
    message: '最大标记数至少为 1',
  }),
  inputPrice: z.coerce.number().min(0, {
    message: '输入价格不能为负数',
  }),
  outputPrice: z.coerce.number().min(0, {
    message: '输出价格不能为负数',
  }),
})

export type ModelFormValues = z.infer<typeof modelFormSchema>

// 模型表单对话框属性
export type ModelFormDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  initialData?: Model | null
  onSubmit: (values: ModelFormValues) => Promise<void>
  providerId: string
}

export function ModelFormDialog({ open, onOpenChange, initialData, onSubmit, providerId }: ModelFormDialogProps) {
  const isEditMode = !!initialData

  // 表单处理
  const form = useForm<ModelFormValues>({
    resolver: zodResolver(modelFormSchema),
    defaultValues: {
      modelName: initialData?.modelName || '',
      displayName: initialData?.displayName || '',
      modelType: (initialData?.modelType || 'chat') as ModelType,
      contextLength: initialData?.contextLength || 4096,
      maxTokens: initialData?.maxTokens || 2048,
      inputPrice: initialData?.inputPrice || 0,
      outputPrice: initialData?.outputPrice || 0,
    },
  })

  // 当initialData变化时重置表单
  useEffect(() => {
    if (open) {
      form.reset({
        modelName: initialData?.modelName || '',
        displayName: initialData?.displayName || '',
        modelType: (initialData?.modelType || 'chat') as ModelType,
        contextLength: initialData?.contextLength || 4096,
        maxTokens: initialData?.maxTokens || 2048,
        inputPrice: initialData?.inputPrice || 0,
        outputPrice: initialData?.outputPrice || 0,
      })
    }
  }, [form, initialData, open])

  const handleSubmit = async (values: ModelFormValues) => {
    await onSubmit(values)
    form.reset()
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle className="text-base">{isEditMode ? '编辑模型' : '添加模型'}</DialogTitle>
        </DialogHeader>
        <Separator />
        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-3 py-2">
            <FormField
              control={form.control}
              name="modelName"
              render={({ field }) => (
                <FormItem className="space-y-1.5">
                  <FormLabel className="text-xs">模型名称</FormLabel>
                  <FormControl>
                    <Input placeholder="例如: gpt-3.5-turbo" className="h-8 text-xs" {...field} />
                  </FormControl>
                  <FormMessage className="text-[10px]" />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="displayName"
              render={({ field }) => (
                <FormItem className="space-y-1.5">
                  <FormLabel className="text-xs">显示名称</FormLabel>
                  <FormControl>
                    <Input placeholder="例如: GPT-3.5" className="h-8 text-xs" {...field} />
                  </FormControl>
                  <FormMessage className="text-[10px]" />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="modelType"
              render={({ field }) => (
                <FormItem className="space-y-1.5">
                  <FormLabel className="text-xs">模型类型</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger className="h-8 text-xs">
                        <SelectValue placeholder="选择模型类型" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value={ModelType.CHAT}>对话模型</SelectItem>
                      <SelectItem value={ModelType.REASON}>推理模型</SelectItem>
                      <SelectItem value={ModelType.EMBEDDING}>向量模型</SelectItem>
                      <SelectItem value={ModelType.AUDIO}>语音模型</SelectItem>
                      <SelectItem value={ModelType.IMAGE}>图像模型</SelectItem>
                      <SelectItem value={ModelType.VIDEO}>视频模型</SelectItem>
                      <SelectItem value={ModelType.CODE}>代码模型</SelectItem>
                      <SelectItem value={ModelType.RERANK}>重排序模型</SelectItem>
                    </SelectContent>
                  </Select>
                  <FormMessage className="text-[10px]" />
                </FormItem>
              )}
            />
            <div className="grid grid-cols-2 gap-3">
              <FormField
                control={form.control}
                name="contextLength"
                render={({ field }) => (
                  <FormItem className="space-y-1.5">
                    <FormLabel className="text-xs">上下文长度</FormLabel>
                    <FormControl>
                      <Input type="number" className="h-8 text-xs" {...field} />
                    </FormControl>
                    <FormMessage className="text-[10px]" />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="maxTokens"
                render={({ field }) => (
                  <FormItem className="space-y-1.5">
                    <FormLabel className="text-xs">最大响应 Token 数</FormLabel>
                    <FormControl>
                      <Input type="number" className="h-8 text-xs" {...field} />
                    </FormControl>
                    <FormMessage className="text-[10px]" />
                  </FormItem>
                )}
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <FormField
                control={form.control}
                name="inputPrice"
                render={({ field }) => (
                  <FormItem className="space-y-1.5">
                    <FormLabel className="text-xs">输入价格 (¥/1K Tokens)</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.0001" className="h-8 text-xs" {...field} />
                    </FormControl>
                    <FormMessage className="text-[10px]" />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="outputPrice"
                render={({ field }) => (
                  <FormItem className="space-y-1.5">
                    <FormLabel className="text-xs">输出价格 (¥/1K Tokens)</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.0001" className="h-8 text-xs" {...field} />
                    </FormControl>
                    <FormMessage className="text-[10px]" />
                  </FormItem>
                )}
              />
            </div>
            <Separator className="mt-4" />
            <DialogFooter>
              <Button variant="outline" onClick={() => onOpenChange(false)} className="text-xs h-8" type="button">
                取消
              </Button>
              <Button type="submit" className="text-xs h-8" disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting ? (
                  <>
                    <Loader2 className="h-3 w-3 animate-spin mr-1" />
                    {isEditMode ? '更新中...' : '保存中...'}
                  </>
                ) : isEditMode ? (
                  '更新模型'
                ) : (
                  '保存模型'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
