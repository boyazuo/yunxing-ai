import { chatService } from '@/api/chat'
import { conversationService } from '@/api/conversation'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import type { App } from '@/types/app'
import { ChatStreamPhase, MessageRole } from '@/types/chat'
import { ArrowRight, ArrowUp, FileText, Loader2, Settings, Share2 } from 'lucide-react'
import { forwardRef, useCallback, useEffect, useImperativeHandle, useLayoutEffect, useRef, useState } from 'react'
import { Markdown } from './markdown'
import { ThinkingIndicator } from './thinking-indicator'

export interface ChatMessage {
  id: string
  role: MessageRole
  content: string
  time: string
}

export interface ChatInterfaceProps {
  activeApp: App | null
  hasActiveConversation: boolean
  activeConversationId: string | null
  userId?: string
  onNewConversation?: (conversationId: string) => void
  className?: string
}

export interface ChatInterfaceHandle {
  loadMessages: (conversationId: string) => Promise<void>
  cleanMessages: () => void
}

export const ChatInterface = forwardRef<ChatInterfaceHandle, ChatInterfaceProps>(
  (
    {
      activeApp,
      hasActiveConversation,
      activeConversationId,
      userId = '',
      onNewConversation,
      className = '',
    },
    ref,
  ) => {
    const [userInput, setUserInput] = useState('')
    const [isLoading, setIsLoading] = useState(false)
    const [messages, setMessages] = useState<ChatMessage[]>([])
    const [loadingMessages, setLoadingMessages] = useState(false)
    const [streamingPhase, setStreamingPhase] = useState<ChatStreamPhase>(ChatStreamPhase.UNDERSTANDING)

    const formatTime = useCallback((dateString: string) => {
      return new Date(dateString).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })
    }, [])

    const loadMessages = useCallback(
      async (conversationId: string) => {
        if (!conversationId) return

        try {
          setLoadingMessages(true)
          const data = await conversationService.getConversationMessages(conversationId)

          const formattedMessages: ChatMessage[] = data.flatMap((message) => [
            {
              id: `${message.messageId}-q`,
              role: MessageRole.USER,
              content: message.question,
              time: formatTime(message.createTime),
            },
            {
              id: `${message.messageId}-a`,
              role: MessageRole.ASSISTANT,
              content: message.answer,
              time: formatTime(message.createTime),
            },
          ])

          setMessages(formattedMessages)
        } catch (error) {
          console.error('加载消息失败', error)
        } finally {
          setLoadingMessages(false)
        }
      },
      [formatTime],
    )

    const cleanMessages = useCallback(() => {
      setMessages([])
    }, [])

    const sendMessage = async (question: string) => {
      if (!question || !activeApp) return
      try {
        const chatRequest = {
          appId: activeApp.appId,
          conversationId: activeConversationId || undefined,
          prompt: question,
          messages: messages.map((message) => ({
            role: message.role,
            content: message.content,
          })),
        }

        const currentTime = formatTime(new Date().toISOString())
        const tempUserId = `temp-${Date.now()}`
        const tempAssistantId = `temp-assistant-${Date.now()}`

        setMessages((prev) => [
          ...prev,
          {
            id: tempUserId,
            role: MessageRole.USER,
            content: question,
            time: currentTime,
          },
          {
            id: tempAssistantId,
            role: MessageRole.ASSISTANT,
            content: '',
            time: currentTime,
          },
        ])
        setStreamingPhase(ChatStreamPhase.UNDERSTANDING)

        let conversationId = activeConversationId
        let messageId = ''
        let messageContent = ''

        const updateMessageContent = (content: string, msgId: string) => {
          setMessages((prev) => {
            const updated = [...prev]
            const assistantMessageIndex = updated.findIndex((msg) => msg.id === msgId)
            if (assistantMessageIndex !== -1) {
              updated[assistantMessageIndex] = {
                ...updated[assistantMessageIndex],
                content: content,
              }
            }
            return updated
          })
        }

        await chatService.streamMessageWithHandling(chatRequest, {
          onMessage: (event) => {
            try {
              const data = event.data
              if (event.name === 'metadata') {
                try {
                  const metaData = JSON.parse(data)
                  if (metaData.conversationId) {
                    conversationId = metaData.conversationId
                  }
                  if (metaData.messageId) {
                    messageId = metaData.messageId
                  }
                  if (!activeConversationId) {
                    if (onNewConversation && conversationId) {
                      onNewConversation(conversationId)
                    }
                  }
                } catch (jsonError) {
                  console.error('解析metadata JSON失败:', jsonError, data)
                }
              } else if (event.name === 'status') {
                try {
                  const { phase } = JSON.parse(data) as { phase: string }
                  if (Object.values(ChatStreamPhase).includes(phase as ChatStreamPhase)) {
                    setStreamingPhase(phase as ChatStreamPhase)
                  }
                } catch (jsonError) {
                  console.error('解析status JSON失败:', jsonError, data)
                }
              } else if (event.name === 'end') {
                return
              } else {
                const chunk = JSON.parse(data).chunk
                messageContent += chunk
                updateMessageContent(messageContent, tempAssistantId)
              }
            } catch (error) {
              console.error('处理消息数据失败', error)
            }
          },
          onError: (error) => {
            console.error('处理流数据失败', error)
            updateMessageContent(`抱歉，${error.message || '请求发送失败，请重试。'}`, tempAssistantId)
          },
          onComplete: () => {
            try {
              if (conversationId && messageId) {
                setMessages((prev) => {
                  const updated = [...prev]
                  const userMsgIndex = updated.findIndex((msg) => msg.id === tempUserId)
                  const assistantMsgIndex = updated.findIndex((msg) => msg.id === tempAssistantId)

                  if (userMsgIndex !== -1) {
                    updated[userMsgIndex] = {
                      ...updated[userMsgIndex],
                      id: `${messageId}-q`,
                    }
                  }

                  if (assistantMsgIndex !== -1) {
                    updated[assistantMsgIndex] = {
                      ...updated[assistantMsgIndex],
                      id: `${messageId}-a`,
                    }
                  }

                  return updated
                })
              }
            } catch (completeError) {
              console.error('流完成处理失败', completeError)
            }
          },
        })

        return Promise.resolve()
      } catch (error) {
        console.error('发送消息失败', error)
        return Promise.reject(error)
      }
    }

    useImperativeHandle(
      ref,
      () => ({
        loadMessages,
        cleanMessages,
      }),
      [loadMessages, cleanMessages],
    )

    const handleSendMessage = async () => {
      if (!userInput.trim() || !activeApp) return

      const message = userInput.trim()
      setUserInput('')
      setIsLoading(true)

      try {
        await sendMessage(message)
      } catch (error) {
        console.error('发送消息失败', error)
      } finally {
        setIsLoading(false)
      }
    }

    const isEmpty = !loadingMessages && messages.length === 0

    return (
      <div className={`flex-1 flex flex-col bg-background min-w-0 ${className}`}>
        {!isEmpty && <ChatHeader activeApp={activeApp} />}

        {isEmpty ? (
          <EmptyChatState
            activeApp={activeApp}
            loadingMessages={loadingMessages}
            userInput={userInput}
            setUserInput={setUserInput}
            isLoading={isLoading}
            handleSendMessage={handleSendMessage}
          />
        ) : (
          <>
            <ChatMessages
              messages={messages}
              activeApp={activeApp}
              loadingMessages={loadingMessages}
              isLoading={isLoading}
              streamingPhase={streamingPhase}
            />
            <ChatInputDock
              userInput={userInput}
              setUserInput={setUserInput}
              isLoading={isLoading}
              activeApp={activeApp}
              handleSendMessage={handleSendMessage}
              variant="bottom"
            />
          </>
        )}
      </div>
    )
  },
)

ChatInterface.displayName = 'ChatInterface'

interface ChatHeaderProps {
  activeApp: App | null
}

function ChatHeader({ activeApp }: ChatHeaderProps) {
  return (
    <div className="h-12 border-b border-border/40 px-5 flex items-center justify-between bg-background/80 backdrop-blur-sm shrink-0">
      <div className="min-w-0">
        <h3 className="font-semibold text-sm truncate">{activeApp?.appName || '请选择应用'}</h3>
      </div>
      <div className="flex gap-1">
        <Button variant="ghost" size="icon" className="h-8 w-8 text-muted-foreground cursor-pointer" disabled={!activeApp}>
          <Settings className="h-4 w-4" />
        </Button>
        <Button variant="ghost" size="icon" className="h-8 w-8 text-muted-foreground cursor-pointer" disabled={!activeApp}>
          <Share2 className="h-4 w-4" />
        </Button>
      </div>
    </div>
  )
}

interface EmptyChatStateProps {
  activeApp: App | null
  loadingMessages: boolean
  userInput: string
  setUserInput: (value: string) => void
  isLoading: boolean
  handleSendMessage: () => Promise<void>
}

function EmptyChatState({
  activeApp,
  loadingMessages,
  userInput,
  setUserInput,
  isLoading,
  handleSendMessage,
}: EmptyChatStateProps) {
  if (loadingMessages) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground/60" />
          <p className="text-xs text-muted-foreground">加载消息中...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex-1 flex flex-col items-center justify-center px-6 pb-16">
      <div className="w-full max-w-2xl flex flex-col items-center gap-8 animate-in fade-in duration-500">
        <div className="text-center space-y-2">
          <h1 className="text-2xl md:text-3xl font-semibold tracking-tight text-foreground">
            {activeApp ? '我能为你做什么？' : '欢迎使用云行 AI'}
          </h1>
          {activeApp?.intro && (
            <p className="text-sm text-muted-foreground max-w-md mx-auto leading-relaxed">{activeApp.intro}</p>
          )}
          {!activeApp && (
            <p className="text-sm text-muted-foreground">请从左侧选择一个应用，开始智能对话</p>
          )}
        </div>

        <ChatInputDock
          userInput={userInput}
          setUserInput={setUserInput}
          isLoading={isLoading}
          activeApp={activeApp}
          handleSendMessage={handleSendMessage}
          variant="center"
        />
      </div>
    </div>
  )
}

interface ChatMessagesProps {
  messages: ChatMessage[]
  activeApp: App | null
  loadingMessages: boolean
  isLoading: boolean
  streamingPhase: ChatStreamPhase
}

function ChatMessages({ messages, loadingMessages, isLoading, streamingPhase }: ChatMessagesProps) {
  const messagesContainerRef = useRef<HTMLDivElement>(null)
  const useIsomorphicLayoutEffect = typeof window !== 'undefined' ? useLayoutEffect : useEffect

  useIsomorphicLayoutEffect(() => {
    const container = messagesContainerRef.current
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })

  if (loadingMessages) {
    return (
      <div ref={messagesContainerRef} className="flex-1 overflow-auto">
        <div className="flex flex-col items-center justify-center h-full gap-3">
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground/60" />
          <p className="text-xs text-muted-foreground">加载消息中...</p>
        </div>
      </div>
    )
  }

  return (
    <div ref={messagesContainerRef} className="flex-1 overflow-auto">
      <div className="max-w-3xl mx-auto px-4 md:px-6 py-8 space-y-8">
        {messages.map((message, index) => (
          <ChatMessageItem
            key={message.id}
            message={message}
            isThinking={
              isLoading &&
              index === messages.length - 1 &&
              message.role === MessageRole.ASSISTANT &&
              !message.content.trim()
            }
            streamingPhase={streamingPhase}
          />
        ))}
      </div>
    </div>
  )
}

interface ChatMessageItemProps {
  message: ChatMessage
  isThinking?: boolean
  streamingPhase: ChatStreamPhase
}

function ChatMessageItem({ message, isThinking = false, streamingPhase }: ChatMessageItemProps) {
  const isUserMessage = message.role === MessageRole.USER

  if (isUserMessage) {
    return (
      <div className="flex justify-end">
        <div className="max-w-[85%] min-w-0">
          <div className="px-4 py-3 rounded-2xl rounded-br-md bg-muted/80 border border-border/40 text-sm leading-relaxed text-foreground">
            <div className="whitespace-pre-line">{message.content}</div>
          </div>
        </div>
      </div>
    )
  }

  if (isThinking) {
    return (
      <div className="min-w-0">
        <ThinkingIndicator phase={streamingPhase} />
      </div>
    )
  }

  return (
    <div className="min-w-0">
      <div className="text-sm leading-relaxed text-foreground prose-sm max-w-none">
        <Markdown>{message.content}</Markdown>
      </div>
    </div>
  )
}

interface ChatInputDockProps {
  userInput: string
  setUserInput: (value: string) => void
  isLoading: boolean
  activeApp: App | null
  handleSendMessage: () => Promise<void>
  variant: 'center' | 'bottom'
}

function ChatInputDock({
  userInput,
  setUserInput,
  isLoading,
  activeApp,
  handleSendMessage,
  variant,
}: ChatInputDockProps) {
  const isInputDisabled = !activeApp || isLoading
  const isSendDisabled = !userInput.trim() || isLoading || !activeApp

  const placeholder =
    variant === 'center'
      ? activeApp
        ? '分配一个任务或提问任何问题'
        : '请先选择应用...'
      : activeApp
        ? '提出后续问题...'
        : '请先选择应用...'

  const wrapperClass =
    variant === 'center'
      ? 'w-full'
      : 'border-t border-border/40 p-4 md:p-5 bg-background shrink-0'

  return (
    <div className={wrapperClass}>
      <div
        className={`relative mx-auto ${variant === 'center' ? 'max-w-2xl' : 'max-w-3xl'}`}
      >
        <div
          className={`relative rounded-2xl border border-border/70 bg-card shadow-sm transition-shadow duration-200 focus-within:shadow-md focus-within:border-border ${
            variant === 'center' ? 'shadow-md' : ''
          }`}
        >
          <Textarea
            placeholder={placeholder}
            className="min-h-[56px] max-h-40 resize-none rounded-2xl border-0 bg-transparent px-4 pt-4 pb-12 text-sm focus-visible:ring-0 shadow-none"
            value={userInput}
            onChange={(e) => setUserInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault()
                handleSendMessage()
              }
            }}
            disabled={isInputDisabled}
          />
          <div className="absolute left-3 bottom-3 flex items-center gap-1">
            <Button
              size="icon"
              variant="ghost"
              className="h-8 w-8 rounded-lg text-muted-foreground cursor-pointer"
              disabled={isInputDisabled}
            >
              <FileText className="h-4 w-4" />
            </Button>
          </div>
          <div className="absolute right-3 bottom-3">
            <Button
              size="icon"
              className="h-8 w-8 rounded-full cursor-pointer"
              onClick={handleSendMessage}
              disabled={isSendDisabled}
            >
              {isLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <ArrowUp className="h-4 w-4" />}
            </Button>
          </div>
        </div>

        {variant === 'bottom' && (
          <div className="flex justify-between mt-2 text-[11px] px-1">
            <span className="text-muted-foreground">Enter 发送 · Shift+Enter 换行</span>
            <button
              type="button"
              className="text-muted-foreground hover:text-foreground flex items-center gap-0.5 transition-colors duration-200 cursor-pointer"
            >
              知识库增强
              <ArrowRight className="h-3 w-3" />
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
