'use client'

import { datasetService } from '@/api/dataset'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import type { Dataset } from '@/types/dataset'
import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'

const datasetFormSchema = z.object({
  datasetName: z.string().min(1, '知识库名称不能为空').max(50, '知识库名称最长50个字符'),
  datasetDesc: z.string().max(200, '描述最长200个字符').optional(),
})

type DatasetFormValues = z.infer<typeof datasetFormSchema>

interface DatasetFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  dataset?: Dataset
  onSuccess: () => void
  tenantId: string
}

export function DatasetFormDialog({ open, onOpenChange, dataset, onSuccess, tenantId }: DatasetFormDialogProps) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const isEditing = !!dataset

  const form = useForm<DatasetFormValues>({
    resolver: zodResolver(datasetFormSchema),
    defaultValues: {
      datasetName: dataset?.datasetName || '',
      datasetDesc: dataset?.datasetDesc || '',
    },
  })

  useEffect(() => {
    if (open) {
      form.reset({
        datasetName: dataset?.datasetName || '',
        datasetDesc: dataset?.datasetDesc || '',
      })
    }
  }, [form, dataset, open])

  const onSubmit = async (values: DatasetFormValues) => {
    try {
      setIsSubmitting(true)

      if (isEditing && dataset) {
        await datasetService.updateDataset({
          datasetId: dataset.datasetId,
          ...values,
        })
        toast.success('知识库更新成功')
      } else {
        await datasetService.createDataset({
          tenantId,
          ...values,
        })
        toast.success('知识库创建成功')
      }

      onOpenChange(false)
      onSuccess()
    } catch (error) {
      console.error(isEditing ? '更新知识库失败' : '创建知识库失败', error)
      toast.error(isEditing ? '更新知识库失败' : '创建知识库失败')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{isEditing ? '编辑知识库' : '创建知识库'}</DialogTitle>
          <DialogDescription>
            {isEditing ? '修改知识库的基本信息' : '创建一个新的知识库用于训练和知识库检索'}
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="datasetName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>知识库名称</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入知识库名称" {...field} />
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
                  <FormLabel>知识库描述</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder="请输入知识库描述（选填）"
                      className="resize-none"
                      {...field}
                      value={field.value || ''}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                取消
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? (isEditing ? '更新中...' : '创建中...') : isEditing ? '更新' : '创建'}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
