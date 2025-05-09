'use client'

import { AVAILABLE_ICONS, IconSelector } from '@/components/icon/icon-selector'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import type { Provider } from '@/types/ai'
import { zodResolver } from '@hookform/resolvers/zod'
import { Eye, EyeOff, Loader2, Plus } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import * as z from 'zod'

// 模型供应商表单验证
export const providerFormSchema = z.object({
  providerName: z.string().min(2, {
    message: '名称至少需要 2 个字符',
  }),
  logo: z.string().optional(),
  apiKey: z.string().min(1, {
    message: 'API 密钥不能为空',
  }),
  endpoint: z
    .string()
    .url({
      message: '请输入有效的 URL',
    })
    .optional()
    .or(z.literal('')),
})

export type ProviderFormValues = z.infer<typeof providerFormSchema>

// 供应商表单对话框组件
export type ProviderFormDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  initialData?: Provider | null
  onSubmit: (values: ProviderFormValues) => Promise<void>
  tenantId: string
}

export function ProviderFormDialog({ open, onOpenChange, initialData, onSubmit, tenantId }: ProviderFormDialogProps) {
  const [showApiKey, setShowApiKey] = useState(false)
  const isEditMode = !!initialData

  // 表单处理
  const form = useForm<ProviderFormValues>({
    resolver: zodResolver(providerFormSchema),
    defaultValues: {
      providerName: initialData?.providerName || '',
      logo: initialData?.logo || '',
      apiKey: initialData?.apiKey || '',
      endpoint: initialData?.endpoint || '',
    },
  })

  // 当initialData变化时重置表单
  useEffect(() => {
    if (open) {
      form.reset({
        providerName: initialData?.providerName || '',
        logo: initialData?.logo || '',
        apiKey: initialData?.apiKey || '',
        endpoint: initialData?.endpoint || '',
      })
    }
  }, [form, initialData, open])

  const handleSubmit = async (values: ProviderFormValues) => {
    await onSubmit(values)
    form.reset()
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle className="text-base">{isEditMode ? '编辑模型供应商' : '添加模型供应商'}</DialogTitle>
        </DialogHeader>
        <Separator />
        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-3 py-2">
            <div>
              <div className="mb-1.5">
                <FormLabel className="text-xs">模型提供商 Logo & 名称</FormLabel>
              </div>
              <div className="flex items-center gap-2">
                <div className="relative">
                  <Button
                    variant="outline"
                    className="h-8 w-8 rounded-md p-0 border shadow-sm hover:border-primary/30"
                    type="button"
                    onClick={() => {
                      const iconTriggerEl = document.querySelector('.provider-icon-selector button')
                      if (iconTriggerEl && iconTriggerEl instanceof HTMLElement) {
                        iconTriggerEl.click()
                      }
                    }}
                  >
                    {form.watch('logo') && AVAILABLE_ICONS.some((icon) => icon.id === form.watch('logo')) ? (
                      <IconSelector.Icon iconId={form.watch('logo')} size={20} />
                    ) : (
                      <Plus className="h-4 w-4 text-muted-foreground" />
                    )}
                  </Button>
                  <div
                    className="provider-icon-selector opacity-0 absolute pointer-events-none"
                    style={{ width: 0, height: 0, overflow: 'hidden' }}
                  >
                    <IconSelector
                      selectedIcon={form.watch('logo')}
                      onIconSelect={(iconId) => {
                        form.setValue('logo', iconId)
                      }}
                      triggerClassName="h-8 w-8"
                    />
                  </div>
                </div>
                <div className="flex-1 flex flex-col">
                  <FormField
                    control={form.control}
                    name="providerName"
                    render={({ field }) => (
                      <FormItem className="space-y-0">
                        <FormControl>
                          <Input placeholder="例如: OpenAI, Anthropic" className="h-8 text-xs" {...field} />
                        </FormControl>
                        <FormMessage className="text-[10px]" />
                      </FormItem>
                    )}
                  />
                </div>
              </div>
            </div>
            <FormField
              control={form.control}
              name="apiKey"
              render={({ field }) => (
                <FormItem className="space-y-1.5">
                  <FormLabel className="text-xs">API 密钥</FormLabel>
                  <FormControl>
                    <div className="relative">
                      <Input
                        placeholder="请输入API密钥"
                        type={showApiKey ? 'text' : 'password'}
                        className="h-8 text-xs pr-8"
                        {...field}
                      />
                      <button
                        type="button"
                        onClick={() => setShowApiKey(!showApiKey)}
                        className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                      >
                        {showApiKey ? <EyeOff className="h-3.5 w-3.5" /> : <Eye className="h-3.5 w-3.5" />}
                      </button>
                    </div>
                  </FormControl>
                  <FormMessage className="text-[10px]" />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="endpoint"
              render={({ field }) => (
                <FormItem className="space-y-1.5">
                  <FormLabel className="text-xs">API 基础URL</FormLabel>
                  <FormControl>
                    <Input placeholder="https://api.example.com" className="h-8 text-xs" {...field} />
                  </FormControl>
                  <p className="text-[10px] text-muted-foreground">对于自定义部署或代理，可以指定API基础URL</p>
                  <FormMessage className="text-[10px]" />
                </FormItem>
              )}
            />
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
                  '更新供应商'
                ) : (
                  '保存供应商'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
