'use client'

import { type AppRequest, appService } from '@/api/apps'
import { type EmojiObject, EmojiPicker } from '@/components/emoji/picker'
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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import type { App, AppType } from '@/types/app'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import * as z from 'zod'

// 应用表单验证规则
const formSchema = z.object({
  appName: z.string().min(2, {
    message: '应用名称至少需要2个字符',
  }),
  intro: z.string().optional(),
  logo: z.string().optional(),
  logoBackground: z.string().optional(),
  type: z.string({
    required_error: '请选择应用类型',
  }),
  tenantId: z.string(),
})

export interface AppFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  app?: App
  onSuccess?: () => void
  tenantId: string
}

export function AppFormDialog({ open, onOpenChange, app, onSuccess, tenantId }: AppFormDialogProps) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isEmojiPickerOpen, setIsEmojiPickerOpen] = useState(false)
  const isEditing = !!app?.appId

  // 表单
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      appName: '',
      intro: '',
      logo: '',
      logoBackground: '',
      type: '',
      tenantId: tenantId,
    },
  })

  // 当编辑现有应用时，加载应用数据
  useEffect(() => {
    if (app) {
      form.reset({
        appName: app.appName,
        intro: app.intro || '',
        logo: app.logo || '',
        logoBackground: app.logoBackground || '',
        type: app.type.toString(),
        tenantId: app.tenantId,
      })
    } else {
      form.reset({
        appName: '',
        intro: '',
        logo: '',
        logoBackground: '',
        type: '',
        tenantId: tenantId,
      })
    }
  }, [app, form, tenantId])

  // 处理表情选择
  const handleEmojiSelect = (emoji: EmojiObject) => {
    form.setValue('logo', emoji.native, { shouldValidate: true })
    form.setValue('logoBackground', emoji.bgColor, { shouldValidate: true })
  }

  // 表单提交处理
  const onSubmit = async (values: z.infer<typeof formSchema>) => {
    try {
      setIsSubmitting(true)

      const appRequest: AppRequest = {
        appName: values.appName,
        intro: values.intro,
        logo: values.logo,
        logoBackground: values.logoBackground,
        type: values.type as unknown as AppType,
        tenantId: values.tenantId,
      }

      if (isEditing && app) {
        await appService.updateApp(app.appId, appRequest)
      } else {
        await appService.createApp(appRequest)
      }

      onOpenChange(false)
      if (onSuccess) {
        onSuccess()
      }
    } catch (error) {
      console.error('保存应用失败', error)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{isEditing ? '编辑应用' : '创建应用'}</DialogTitle>
          <DialogDescription>{isEditing ? '编辑应用基本信息' : '创建一个新的应用，填写基本信息'}</DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            <FormField
              control={form.control}
              name="appName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>应用名称</FormLabel>
                  <FormControl>
                    <Input placeholder="输入应用名称" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="intro"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>应用介绍</FormLabel>
                  <FormControl>
                    <Textarea placeholder="描述应用的功能和用途" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="logo"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>应用图标</FormLabel>
                  <div className="flex items-center gap-2">
                    <FormControl>
                      <div
                        className="flex h-9 w-9 items-center justify-center rounded-md border text-2xl shadow-xs"
                        style={{ backgroundColor: form.watch('logoBackground') || 'transparent' }}
                        aria-label="当前选择的图标"
                      >
                        {field.value || '🧩'}
                      </div>
                    </FormControl>
                    <Button type="button" variant="outline" onClick={() => setIsEmojiPickerOpen(true)}>
                      选择图标
                    </Button>
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="type"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>应用类型</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="选择应用类型" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value="CHAT">对话应用</SelectItem>
                      <SelectItem value="AGENT">智能体</SelectItem>
                      <SelectItem value="WORKFLOW">工作流</SelectItem>
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isSubmitting}>
                取消
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                {isEditing ? '保存修改' : '创建应用'}
              </Button>
            </DialogFooter>
          </form>
        </Form>

        {/* 表情选择器对话框 */}
        <EmojiPicker open={isEmojiPickerOpen} onOpenChange={setIsEmojiPickerOpen} onEmojiSelect={handleEmojiSelect} />
      </DialogContent>
    </Dialog>
  )
}
