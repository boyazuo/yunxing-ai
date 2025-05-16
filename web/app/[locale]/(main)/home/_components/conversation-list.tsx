'use client'

import { conversationService } from '@/api/conversation'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Form, FormControl, FormField, FormItem, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import type { App } from '@/types/app'
import type { Conversation } from '@/types/chat'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, MessageSquare, MoreHorizontal, Pencil, Plus, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import * as z from 'zod'

// 表单验证Schema
const renameFormSchema = z.object({
  title: z.string().min(1, {
    message: '会话标题不能为空',
  }),
})

type RenameFormValues = z.infer<typeof renameFormSchema>

interface ConversationListProps {
  conversations: Conversation[]
  loadingConversations: boolean
  activeApp: App | null
  activeConversationId: string | null
  handleConversationClick: (conversation: Conversation) => void
  handleNewConversation: () => void
  refreshConversations?: () => void
}

export function ConversationList({
  conversations,
  loadingConversations,
  activeApp,
  activeConversationId,
  handleConversationClick,
  handleNewConversation,
  refreshConversations,
}: ConversationListProps) {
  const [renameDialogOpen, setRenameDialogOpen] = useState(false)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [currentConversation, setCurrentConversation] = useState<Conversation | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 重命名表单
  const form = useForm<RenameFormValues>({
    resolver: zodResolver(renameFormSchema),
    defaultValues: {
      title: '',
    },
  })

  // 处理重命名
  const handleRename = (conversation: Conversation) => {
    setCurrentConversation(conversation)
    form.setValue('title', conversation.title)
    setRenameDialogOpen(true)
  }

  // 处理删除确认对话框
  const handleDeleteDialog = (conversation: Conversation) => {
    setCurrentConversation(conversation)
    setDeleteDialogOpen(true)
  }

  // 执行删除操作
  const handleDelete = async () => {
    if (!currentConversation) return

    try {
      await conversationService.deleteConversation(currentConversation.conversationId)

      // 如果删除的是当前活动会话，创建新会话
      if (currentConversation.conversationId === activeConversationId) {
        handleNewConversation()
      }

      // 刷新会话列表
      if (refreshConversations) {
        refreshConversations()
      }
    } catch (error) {
      console.error('删除会话失败', error)
      alert('删除会话失败，请稍后重试')
    } finally {
      setDeleteDialogOpen(false)
    }
  }

  // 提交重命名表单
  const onSubmitRename = async (values: RenameFormValues) => {
    if (!currentConversation) return

    try {
      setIsSubmitting(true)
      await conversationService.updateConversationTitle(currentConversation.conversationId, values.title)

      // 刷新会话列表
      if (refreshConversations) {
        refreshConversations()
      }

      setRenameDialogOpen(false)
    } catch (error) {
      console.error('重命名失败', error)
      form.setError('title', {
        type: 'manual',
        message: '重命名失败，请稍后重试',
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="w-64 border-r flex flex-col bg-muted/10">
      <div className="p-4 border-b">
        <div className="flex items-center justify-between">
          <h2 className="font-semibold">会话历史</h2>
          <Button
            size="sm"
            variant="outline"
            className="h-8 px-2"
            disabled={!activeApp}
            onClick={handleNewConversation}
          >
            <Plus className="h-4 w-4 mr-1" />
            新建会话
          </Button>
        </div>
      </div>

      <div className="flex-1 overflow-auto">
        <ul className="space-y-1 p-2">
          {loadingConversations ? (
            <div className="flex justify-center items-center h-20">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : !activeApp ? (
            <div className="text-center p-4 text-muted-foreground text-sm">请选择一个应用</div>
          ) : conversations.length === 0 ? (
            <div className="text-center p-4 text-muted-foreground text-sm">暂无会话记录</div>
          ) : (
            conversations.map((conversation) => {
              const isActive = conversation.conversationId === activeConversationId
              return (
                <li key={conversation.conversationId}>
                  <div
                    className={`group relative w-full px-3 py-2.5 rounded-md cursor-pointer transition-colors ${
                      isActive ? 'bg-primary text-primary-foreground' : 'hover:bg-accent/50'
                    }`}
                  >
                    <button
                      type="button"
                      className="w-full flex items-center"
                      onClick={() => handleConversationClick(conversation)}
                      aria-pressed={isActive}
                    >
                      <MessageSquare
                        className={`h-4 w-4 mr-2 flex-shrink-0 ${isActive ? 'text-primary-foreground' : 'text-muted-foreground'}`}
                      />
                      <h3 className="font-medium text-sm truncate">{conversation.title}</h3>
                    </button>

                    {/* 更多操作按钮（仅在悬停和选中状态显示） */}
                    <div
                      className={`absolute right-2 top-1/2 -translate-y-1/2 ${isActive ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'} transition-opacity`}
                    >
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className={`h-7 w-7 ${isActive ? 'text-primary-foreground hover:bg-primary/90' : 'text-muted-foreground hover:bg-accent'}`}
                          >
                            <MoreHorizontal className="h-4 w-4" />
                            <span className="sr-only">操作菜单</span>
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-36">
                          <DropdownMenuItem
                            onClick={(e) => {
                              e.preventDefault()
                              e.stopPropagation()
                              handleRename(conversation)
                            }}
                          >
                            <Pencil className="h-4 w-4 mr-2" />
                            <span>重命名</span>
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            variant="destructive"
                            onClick={(e) => {
                              e.preventDefault()
                              e.stopPropagation()
                              handleDeleteDialog(conversation)
                            }}
                          >
                            <Trash2 className="h-4 w-4 mr-2" />
                            <span>删除</span>
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>
                  </div>
                </li>
              )
            })
          )}
        </ul>
      </div>

      {/* 重命名对话框 */}
      <Dialog open={renameDialogOpen} onOpenChange={setRenameDialogOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle className="text-base">重命名会话</DialogTitle>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmitRename)} className="space-y-4">
              <FormField
                control={form.control}
                name="title"
                render={({ field }) => (
                  <FormItem>
                    <FormControl>
                      <Input autoFocus placeholder="请输入会话标题" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <DialogFooter>
                <Button
                  variant="outline"
                  type="button"
                  onClick={() => setRenameDialogOpen(false)}
                  disabled={isSubmitting}
                >
                  取消
                </Button>
                <Button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin mr-1" /> : null}
                  确定
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* 删除确认对话框 */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent
          onCloseAutoFocus={(event) => {
            // 阻止默认的焦点处理
            event.preventDefault()
            document.body.style.pointerEvents = ''
          }}
        >
          <AlertDialogHeader>
            <AlertDialogTitle>删除会话</AlertDialogTitle>
            <AlertDialogDescription>
              确定要删除该会话吗？此操作不可撤销，会话中的所有消息将被永久删除。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
