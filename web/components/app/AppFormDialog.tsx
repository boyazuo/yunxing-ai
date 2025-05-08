'use client'

import { type AppRequest, appService } from '@/api/apps'
import { type EmojiObject, EmojiPicker } from '@/components/emoji/picker'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import type { App, AppType } from '@/types/app'
import { zodResolver } from '@hookform/resolvers/zod'
import { Bot, Loader2, MessageSquare, Workflow } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
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

// 定义表单数据类型
type AppFormValues = z.infer<typeof formSchema>

// 默认表单值
const DEFAULT_LOGO = '🧩'
const DEFAULT_LOGO_BG = '#FAFAFA'
const DEFAULT_APP_TYPE = 'chat'

// 应用类型配置
const APP_TYPES = [
  {
    value: 'chat',
    label: '对话应用',
    description: '简单配置即可构建基于 LLM 的对话机器人',
    icon: <MessageSquare className="h-6 w-6 text-white" />,
    bgColor: 'bg-blue-500',
    disabled: false,
  },
  {
    value: 'agent',
    label: '智能体应用',
    description: '具备推理与自主工具调用的智能助手',
    icon: <Bot className="h-6 w-6 text-white" />,
    bgColor: 'bg-purple-500',
    disabled: true,
  },
  {
    value: 'workflow',
    label: '工作流应用',
    description: '构建自动化任务的编排工作流应用',
    icon: <Workflow className="h-6 w-6 text-white" />,
    bgColor: 'bg-teal-500',
    disabled: true,
  },
]

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

  // 获取默认表单值
  const getDefaultValues = useCallback(
    (): AppFormValues => ({
      appName: app?.appName || '',
      intro: app?.intro || '',
      logo: app?.logo || DEFAULT_LOGO,
      logoBackground: app?.logoBackground || DEFAULT_LOGO_BG,
      type: app?.type?.toString() || DEFAULT_APP_TYPE,
      tenantId,
    }),
    [app, tenantId],
  )

  // 表单
  const form = useForm<AppFormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: getDefaultValues(),
  })

  // 当编辑现有应用或tenantId变化时，重置表单数据
  useEffect(() => {
    form.reset(getDefaultValues())
  }, [form, getDefaultValues])

  // 处理表情选择
  const handleEmojiSelect = (emoji: EmojiObject) => {
    form.setValue('logo', emoji.native, { shouldValidate: true })
    form.setValue('logoBackground', emoji.bgColor, { shouldValidate: true })
    setIsEmojiPickerOpen(false)
  }

  // 表单提交处理
  const onSubmit = async (values: AppFormValues) => {
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
      onSuccess?.()
    } catch (error) {
      console.error('保存应用失败', error)
    } finally {
      setIsSubmitting(false)
    }
  }

  // 处理对话框关闭
  const handleDialogChange = (newOpenState: boolean) => {
    if (!newOpenState && !isSubmitting) {
      form.reset(getDefaultValues())
    }
    onOpenChange(newOpenState)
  }

  return (
    <Dialog open={open} onOpenChange={handleDialogChange}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader className="pb-4">
          <DialogTitle>{isEditing ? '编辑应用' : '创建应用'}</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            <FormItem>
              <FormLabel>应用图标 & 名称</FormLabel>
              <div className="flex items-center gap-2">
                <FormField
                  control={form.control}
                  name="logo"
                  render={({ field }) => (
                    <FormControl>
                      <button
                        type="button"
                        className="flex h-9 w-9 items-center justify-center rounded-md border text-2xl shadow-xs cursor-pointer"
                        style={{ backgroundColor: form.watch('logoBackground') || 'transparent' }}
                        aria-label="点击选择应用图标"
                        onClick={() => setIsEmojiPickerOpen(true)}
                      >
                        {field.value || DEFAULT_LOGO}
                      </button>
                    </FormControl>
                  )}
                />
                <FormField
                  control={form.control}
                  name="appName"
                  render={({ field }) => (
                    <FormControl>
                      <Input className="w-full" placeholder="输入应用名称" {...field} />
                    </FormControl>
                  )}
                />
              </div>
              <FormMessage>{form.formState.errors.appName?.message || form.formState.errors.logo?.message}</FormMessage>
            </FormItem>

            <FormField
              control={form.control}
              name="type"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>应用类型</FormLabel>
                  <div className="mt-1">
                    <div className="grid grid-cols-3 gap-4">
                      {APP_TYPES.map((type) => (
                        <button
                          key={type.value}
                          type="button"
                          className={`group relative flex flex-col h-[100px] p-2 border rounded-lg cursor-pointer transition-all ${
                            type.disabled
                              ? 'opacity-50 cursor-not-allowed border-dashed'
                              : `hover:bg-muted/30 ${
                                  field.value === type.value
                                    ? 'border-primary border-2 bg-muted/30'
                                    : 'border hover:border-primary/50'
                                }`
                          }`}
                          onClick={() => !type.disabled && field.onChange(type.value)}
                          disabled={type.disabled}
                        >
                          <div className="flex items-center space-x-2.5">
                            <div
                              className={`${type.bgColor} rounded-md p-1.5 w-7 h-7 flex items-center justify-center flex-shrink-0`}
                            >
                              {type.icon}
                            </div>
                            <h3 className="font-medium text-sm">
                              {type.label}
                              {type.disabled && <span className="ml-1 text-xs">(即将推出)</span>}
                            </h3>
                          </div>
                          <p className="text-xs text-muted-foreground mt-2 text-left line-clamp-3">
                            {type.description}
                          </p>
                          {field.value === type.value && !type.disabled && (
                            <div className="absolute top-2 right-2 rounded-full bg-primary h-2 w-2" />
                          )}
                        </button>
                      ))}
                    </div>
                  </div>
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
