'use client'

import { useSession } from 'next-auth/react'
import { forwardRef, useCallback, useEffect, useId, useImperativeHandle, useRef, useState } from 'react'

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
import { RowActionsTrigger } from '@/components/blocks/row-actions'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Form, FormControl, FormField, FormItem, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { cn } from '@/lib/utils'
import type { App } from '@/types/app'
import type { Conversation } from '@/types/chat'
import { zodResolver } from '@hookform/resolvers/zod'
import { ChevronDown, Loader2, Pencil, Trash2 } from 'lucide-react'
import { useForm } from 'react-hook-form'
import * as z from 'zod'

const renameFormSchema = z.object({
  title: z.string().min(1, { message: '会话标题不能为空' }),
})

type RenameFormValues = z.infer<typeof renameFormSchema>

export interface ConversationListHandle {
  loadConversations: (appId: string) => Promise<void>
}

interface ConversationListProps {
  activeApp: App | null
  activeConversationId: string | null
  onConversationChange: (conversation: Conversation) => void
  onNewConversation: () => void
  className?: string
}

function ConversationSkeleton() {
  return (
    <div className="px-1.5 py-0.5">
      <Skeleton className="h-7 w-full rounded-md" />
    </div>
  )
}

export const ConversationList = forwardRef<ConversationListHandle, ConversationListProps>(
  ({ activeApp, activeConversationId, onConversationChange, onNewConversation, className }, ref) => {
    const { data: session } = useSession()
    const userId = session?.user?.userId || ''

    const skeletonIdPrefix = useId()
    const [renameDialogOpen, setRenameDialogOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
    const [currentConversation, setCurrentConversation] = useState<Conversation | null>(null)
    const [isSubmitting, setIsSubmitting] = useState(false)

    const [conversations, setConversations] = useState<Conversation[]>([])
    const [loadingConversations, setLoadingConversations] = useState(false)
    const [loadingMore, setLoadingMore] = useState(false)
    const [paginationData, setPaginationData] = useState({ current: 1, hasNext: false, total: 0 })

    const loadConversations = useCallback(
      async (appId: string) => {
        if (!userId || !appId) return
        try {
          setLoadingConversations(true)
          const data = await conversationService.getConversations(userId, appId)
          setConversations(data.records || [])
          setPaginationData({
            current: data.current,
            hasNext: Number(data.current) < Number(data.pages),
            total: data.total,
          })
        } catch (error) {
          console.error('加载会话失败', error)
        } finally {
          setLoadingConversations(false)
        }
      },
      [userId],
    )

    const loadMoreConversations = useCallback(
      async (appId: string) => {
        if (!userId || !appId || !paginationData.hasNext || loadingMore) return
        try {
          setLoadingMore(true)
          const nextPage = Number(paginationData.current) + 1
          const data = await conversationService.getConversations(userId, appId, nextPage)
          setConversations((prev) => [...prev, ...data.records])
          setPaginationData({ current: data.current, hasNext: data.hasNext, total: data.total })
        } catch (error) {
          console.error('加载更多会话失败', error)
        } finally {
          setLoadingMore(false)
        }
      },
      [userId, paginationData, loadingMore],
    )

    useImperativeHandle(ref, () => ({ loadConversations }), [loadConversations])

    useEffect(() => {
      if (activeApp) {
        loadConversations(activeApp.appId)
      } else {
        setConversations([])
        setPaginationData({ current: 1, hasNext: false, total: 0 })
      }
    }, [activeApp, loadConversations])

    const form = useForm<RenameFormValues>({
      resolver: zodResolver(renameFormSchema),
      defaultValues: { title: '' },
    })

    const handleConversationClick = (conversation: Conversation) => {
      if (conversation.conversationId === activeConversationId) return
      onConversationChange(conversation)
    }

    const handleNewConversation = () => {
      if (!activeApp) return
      onNewConversation()
    }

    const handleRename = (conversation: Conversation) => {
      setCurrentConversation(conversation)
      form.setValue('title', conversation.title)
      setRenameDialogOpen(true)
    }

    const handleDeleteDialog = (conversation: Conversation) => {
      setCurrentConversation(conversation)
      setDeleteDialogOpen(true)
    }

    const handleDelete = async () => {
      if (!currentConversation) return
      try {
        await conversationService.deleteConversation(currentConversation.conversationId)
        if (currentConversation.conversationId === activeConversationId) {
          handleNewConversation()
        }
        if (activeApp) loadConversations(activeApp.appId)
      } catch (error) {
        console.error('删除会话失败', error)
        alert('删除会话失败，请稍后重试')
      } finally {
        setDeleteDialogOpen(false)
      }
    }

    const onSubmitRename = async (values: RenameFormValues) => {
      if (!currentConversation) return
      try {
        setIsSubmitting(true)
        await conversationService.updateConversationTitle(currentConversation.conversationId, values.title)
        if (activeApp) loadConversations(activeApp.appId)
        setRenameDialogOpen(false)
      } catch (error) {
        console.error('重命名失败', error)
        form.setError('title', { type: 'manual', message: '重命名失败，请稍后重试' })
      } finally {
        setIsSubmitting(false)
      }
    }

    const listContainerRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
      const container = listContainerRef.current
      if (!container) return
      const handleScroll = () => {
        const { scrollTop, scrollHeight, clientHeight } = container
        if (scrollTop + clientHeight >= scrollHeight - 100 && activeApp && paginationData.hasNext && !loadingMore) {
          loadMoreConversations(activeApp.appId)
        }
      }
      container.addEventListener('scroll', handleScroll)
      return () => container.removeEventListener('scroll', handleScroll)
    }, [activeApp, paginationData.hasNext, loadingMore, loadMoreConversations])

    return (
      <div className={cn('flex flex-col min-h-0', className)}>
        <div ref={listContainerRef} className="flex-1 overflow-auto px-1.5 py-1.5">
          {loadingConversations ? (
            <div className="space-y-0.5">
              {['sk1', 'sk2', 'sk3', 'sk4', 'sk5'].map((id) => (
                <ConversationSkeleton key={`${skeletonIdPrefix}-${id}`} />
              ))}
            </div>
          ) : !activeApp ? (
            <p className="text-[11px] text-muted-foreground/70 text-center py-6 px-2">选择应用后开始对话</p>
          ) : conversations.length === 0 ? (
            <p className="text-[11px] text-muted-foreground/70 text-center py-6 px-2">暂无历史会话</p>
          ) : (
            <>
              <ul className="space-y-px">
                {conversations.map((conversation) => {
                  const isActive = conversation.conversationId === activeConversationId
                  return (
                    <li key={conversation.conversationId}>
                      <div
                        className={cn(
                          'group relative rounded-md transition-colors duration-150',
                          isActive ? 'bg-muted' : 'hover:bg-muted/60',
                        )}
                      >
                        <button
                          type="button"
                          className="w-full px-2 py-1.5 pr-7 text-left cursor-pointer"
                          onClick={() => handleConversationClick(conversation)}
                          aria-pressed={isActive}
                        >
                          <span
                            className={cn(
                              'block text-[13px] leading-snug truncate',
                              isActive ? 'font-medium text-foreground' : 'text-foreground/80',
                            )}
                          >
                            {conversation.title}
                          </span>
                        </button>

                        <div className="absolute right-0.5 top-1/2 -translate-y-1/2">
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <RowActionsTrigger
                                size="xs"
                                className="cursor-pointer"
                                label="操作菜单"
                              />
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-36">
                              <DropdownMenuItem
                                className="cursor-pointer"
                                onClick={(e) => {
                                  e.preventDefault()
                                  e.stopPropagation()
                                  handleRename(conversation)
                                }}
                              >
                                <Pencil className="h-3.5 w-3.5 mr-2" />
                                重命名
                              </DropdownMenuItem>
                              <DropdownMenuItem
                                variant="destructive"
                                className="cursor-pointer"
                                onClick={(e) => {
                                  e.preventDefault()
                                  e.stopPropagation()
                                  handleDeleteDialog(conversation)
                                }}
                              >
                                <Trash2 className="h-3.5 w-3.5 mr-2" />
                                删除
                              </DropdownMenuItem>
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </div>
                      </div>
                    </li>
                  )
                })}
              </ul>

              {paginationData.hasNext && (
                <div className="pt-1.5">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => activeApp && loadMoreConversations(activeApp.appId)}
                    disabled={loadingMore}
                    className="w-full text-[11px] text-muted-foreground/70 h-6 cursor-pointer"
                  >
                    {loadingMore ? (
                      <Loader2 className="h-3 w-3 animate-spin mr-1.5" />
                    ) : (
                      <ChevronDown className="h-3 w-3 mr-1.5" />
                    )}
                    {loadingMore ? '加载中...' : '加载更多'}
                  </Button>
                </div>
              )}
            </>
          )}
        </div>

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
                  <Button variant="outline" type="button" onClick={() => setRenameDialogOpen(false)} disabled={isSubmitting}>
                    取消
                  </Button>
                  <Button type="submit" disabled={isSubmitting} className="cursor-pointer">
                    {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin mr-1" /> : null}
                    确定
                  </Button>
                </DialogFooter>
              </form>
            </Form>
          </DialogContent>
        </Dialog>

        <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
          <AlertDialogContent
            onCloseAutoFocus={(event) => {
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
                className="bg-destructive text-destructive-foreground hover:bg-destructive/90 cursor-pointer"
              >
                删除
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>
    )
  },
)

ConversationList.displayName = 'ConversationList'
