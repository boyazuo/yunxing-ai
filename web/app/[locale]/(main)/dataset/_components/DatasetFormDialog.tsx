'use client'

import { datasetService } from '@/api/dataset'
import { modelService } from '@/api/model'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { ModelType, type Model } from '@/types/ai'
import type { Dataset } from '@/types/dataset'
import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'

// 表单验证规则
const datasetFormSchema = z.object({
  datasetName: z.string().min(1, '数据集名称不能为空').max(50, '数据集名称最长50个字符'),
  datasetDesc: z.string().max(200, '描述最长200个字符').optional(),
  embeddingModelId: z.string().min(1, '请选择嵌入模型'),
})

type DatasetFormValues = z.infer<typeof datasetFormSchema>

interface DatasetFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  dataset?: Dataset
  onSuccess: () => void
  tenantId: string
}

export function DatasetFormDialog({
  open,
  onOpenChange,
  dataset,
  onSuccess,
  tenantId,
}: DatasetFormDialogProps) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [embeddingModels, setEmbeddingModels] = useState<Model[]>([])
  const isEditing = !!dataset

  // 获取嵌入模型列表
  useEffect(() => {
    if (open) {
      const fetchModels = async () => {
        const models = await modelService.getModelsByType(ModelType.EMBEDDING)
        setEmbeddingModels(models)
      }
      fetchModels()
    }
  }, [open])

  // 表单初始化
  const form = useForm<DatasetFormValues>({
    resolver: zodResolver(datasetFormSchema),
    defaultValues: {
      datasetName: dataset?.datasetName || '',
      datasetDesc: dataset?.datasetDesc || '',
      embeddingModelId: dataset?.embeddingModelId || '',
    },
  })

  // 当dataset改变时，重置表单值
  useEffect(() => {
    if (open) {
      form.reset({
        datasetName: dataset?.datasetName || '',
        datasetDesc: dataset?.datasetDesc || '',
        embeddingModelId: dataset?.embeddingModelId || '',
      })
    }
  }, [form, dataset, open])

  // 表单提交
  const onSubmit = async (values: DatasetFormValues) => {
    try {
      setIsSubmitting(true)

      if (isEditing && dataset) {
        // 更新数据集
        await datasetService.updateDataset({
          datasetId: dataset.datasetId,
          ...values,
        })
        toast.success('数据集更新成功')
      } else {
        // 创建数据集
        await datasetService.createDataset({
          tenantId,
          ...values,
        })
        toast.success('数据集创建成功')
      }

      onOpenChange(false)
      onSuccess()
    } catch (error) {
      console.error(isEditing ? '更新数据集失败' : '创建数据集失败', error)
      toast.error(isEditing ? '更新数据集失败' : '创建数据集失败')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{isEditing ? '编辑数据集' : '创建数据集'}</DialogTitle>
          <DialogDescription>
            {isEditing
              ? '修改数据集的基本信息'
              : '创建一个新的数据集用于训练和知识库检索'}
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="datasetName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>数据集名称</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入数据集名称" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="datasetDesc"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>数据集描述</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder="请输入数据集描述（选填）"
                      className="resize-none"
                      {...field}
                      value={field.value || ''}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="embeddingModelId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>嵌入模型</FormLabel>
                  <Select
                    onValueChange={field.onChange}
                    defaultValue={field.value}
                    value={field.value}
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="请选择嵌入模型" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {embeddingModels.map((model) => (
                        <SelectItem key={model.modelId} value={model.modelId}>
                          {model.modelName}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => onOpenChange(false)}
              >
                取消
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting
                  ? isEditing
                    ? '更新中...'
                    : '创建中...'
                  : isEditing
                  ? '更新'
                  : '创建'}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
} 