import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Textarea } from '@/components/ui/textarea'
import type { App } from '@/types/app'
import { MessageRole } from '@/types/chat'
import { ArrowRight, ChevronDown, FileText, Loader2, MessageSquare, Send, Settings, User } from 'lucide-react'
import { useEffect, useLayoutEffect, useRef, useState } from 'react'

// 定义类型
export interface ChatMessage {
  id: string
  role: MessageRole
  content: string
  time: string
}

export interface ChatModel {
  id: string
  modelId: string
  name: string
  provider: string
  temperature: number
  topP: number
  maxTokens: number
  isActive: boolean
}

export interface ChatInterfaceProps {
  activeApp: App | null
  messages: ChatMessage[]
  loadingMessages: boolean
  onSendMessage: (message: string, modelId: string) => Promise<void>
  models: ChatModel[]
  defaultModelId: string
  hasActiveConversation: boolean
  className?: string
}

export function ChatInterface({
  activeApp,
  messages,
  loadingMessages,
  onSendMessage,
  models = [],
  defaultModelId,
  hasActiveConversation,
  className = '',
}: ChatInterfaceProps) {
  const [userInput, setUserInput] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [selectedModelId, setSelectedModelId] = useState<string>(
    defaultModelId || (models.length > 0 ? models[0].modelId : ''),
  )

  // 确保在models数组更新时，如果selectedModelId为空，则设置为第一个可用的模型
  useEffect(() => {
    if (models.length > 0) {
      // 如果没有选择模型，或者选择的模型不在当前模型列表中，则选择第一个模型
      if (!selectedModelId || !models.find((m) => m.modelId === selectedModelId)) {
        setSelectedModelId(models[0].modelId)
      }
    }
  }, [models, selectedModelId])

  const handleModelSelect = (modelId: string) => {
    setSelectedModelId(modelId)
  }

  const handleSendMessage = async () => {
    if (!userInput.trim() || !activeApp || !selectedModelId) return

    setIsLoading(true)
    try {
      await onSendMessage(userInput.trim(), selectedModelId)
      setUserInput('')
    } finally {
      setIsLoading(false)
    }
  }

  const getSelectedModelName = () => {
    if (!models || models.length === 0) return '选择模型'
    const model = models.find((m) => m.modelId === selectedModelId)
    return model ? model.name : '选择模型'
  }

  return (
    <div className={`flex-1 flex flex-col bg-background ${className}`}>
      <ChatHeader
        activeApp={activeApp}
        models={models}
        selectedModelId={selectedModelId}
        getSelectedModelName={getSelectedModelName}
        onModelSelect={handleModelSelect}
      />
      <ChatMessages
        messages={messages}
        activeApp={activeApp}
        loadingMessages={loadingMessages}
        hasActiveConversation={hasActiveConversation}
      />
      <ChatInput
        userInput={userInput}
        setUserInput={setUserInput}
        isLoading={isLoading}
        activeApp={activeApp}
        selectedModelId={selectedModelId}
        handleSendMessage={handleSendMessage}
      />
    </div>
  )
}

// 子组件: 聊天头部
interface ChatHeaderProps {
  activeApp: App | null
  models: ChatModel[]
  selectedModelId: string
  getSelectedModelName: () => string
  onModelSelect: (modelId: string) => void
}

function ChatHeader({ activeApp, models, selectedModelId, getSelectedModelName, onModelSelect }: ChatHeaderProps) {
  return (
    <div className="border-b p-4 flex items-center justify-between bg-card">
      <div className="flex items-center">
        <Avatar className="h-9 w-9 mr-3">
          <AvatarFallback>{activeApp?.logo || '🤖'}</AvatarFallback>
        </Avatar>
        <div>
          <h3 className="font-medium">{activeApp?.appName || '请选择应用'}</h3>
          <p className="text-xs text-muted-foreground flex items-center">
            <span className="bg-green-500 h-1.5 w-1.5 rounded-full inline-block mr-1.5" />
            在线
          </p>
        </div>
      </div>
      <div className="flex gap-2">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" size="sm" className="gap-1" disabled={!activeApp || models.length === 0}>
              {getSelectedModelName()}
              <ChevronDown className="h-3.5 w-3.5 ml-1" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {models.map((model) => (
              <DropdownMenuItem
                key={model.modelId}
                onClick={() => onModelSelect(model.modelId)}
                className={selectedModelId === model.modelId ? 'bg-muted' : ''}
              >
                {model.name}
              </DropdownMenuItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>
        <Button variant="outline" size="icon" className="h-8 w-8">
          <Settings className="h-4 w-4" />
        </Button>
        <Button variant="outline" size="sm">
          <MessageSquare className="h-4 w-4 mr-2" />
          分享会话
        </Button>
      </div>
    </div>
  )
}

// 子组件: 消息区域
interface ChatMessagesProps {
  messages: ChatMessage[]
  activeApp: App | null
  loadingMessages: boolean
  hasActiveConversation: boolean
}

function ChatMessages({ messages, activeApp, loadingMessages, hasActiveConversation }: ChatMessagesProps) {
  const messagesContainerRef = useRef<HTMLDivElement>(null)

  // 使用 useLayoutEffect 确保在DOM更新后但在浏览器绘制前执行滚动
  // 创建一个安全的客户端专用钩子，以避免服务器端渲染问题
  const useIsomorphicLayoutEffect = typeof window !== 'undefined' ? useLayoutEffect : useEffect

  useIsomorphicLayoutEffect(() => {
    const container = messagesContainerRef.current
    if (container) {
      // 滚动到底部
      container.scrollTop = container.scrollHeight
    }
  })

  if (loadingMessages) {
    return (
      <div ref={messagesContainerRef} className="flex-1 overflow-auto p-4">
        <div className="flex justify-center items-center h-20">
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
        </div>
      </div>
    )
  }

  if (!hasActiveConversation && (!activeApp || messages.length === 0)) {
    return (
      <div ref={messagesContainerRef} className="flex-1 overflow-auto p-4">
        <div className="text-center p-4 text-muted-foreground">
          {activeApp ? '开始新的对话，或选择一个已有会话' : '请先选择一个应用'}
        </div>
      </div>
    )
  }

  if (messages.length === 0) {
    return (
      <div ref={messagesContainerRef} className="flex-1 overflow-auto p-4">
        <div className="text-center p-4 text-muted-foreground">暂无消息记录</div>
      </div>
    )
  }

  return (
    <div ref={messagesContainerRef} className="flex-1 overflow-auto p-4 space-y-6">
      {messages.map((message) => (
        <ChatMessageItem key={message.id} message={message} activeApp={activeApp} />
      ))}
    </div>
  )
}

// 子组件: 单条消息
interface ChatMessageItemProps {
  message: ChatMessage
  activeApp: App | null
}

function ChatMessageItem({ message, activeApp }: ChatMessageItemProps) {
  const isUserMessage = message.role === MessageRole.USER

  return (
    <div className={`flex ${isUserMessage ? 'justify-end' : 'justify-start'}`}>
      <div className={`flex gap-3 max-w-[85%] ${isUserMessage ? 'flex-row-reverse' : ''}`}>
        <Avatar className={`h-8 w-8 ${isUserMessage ? 'mt-1' : ''}`}>
          {isUserMessage ? (
            <AvatarFallback className="bg-primary text-primary-foreground">
              <User className="h-4 w-4" />
            </AvatarFallback>
          ) : (
            <AvatarFallback className="bg-muted">{activeApp?.logo || '🤖'}</AvatarFallback>
          )}
        </Avatar>
        <div>
          <div
            className={`px-4 py-3 rounded-lg ${
              isUserMessage ? 'bg-primary text-primary-foreground shadow-sm' : 'bg-muted shadow-sm'
            }`}
          >
            <div className="whitespace-pre-line text-sm">{message.content}</div>
          </div>
          <div className={`text-xs mt-1 text-muted-foreground ${isUserMessage ? 'text-right' : ''}`}>
            {message.time}
          </div>
        </div>
      </div>
    </div>
  )
}

// 子组件: 输入区域
interface ChatInputProps {
  userInput: string
  setUserInput: (value: string) => void
  isLoading: boolean
  activeApp: App | null
  selectedModelId: string
  handleSendMessage: () => Promise<void>
}

function ChatInput({
  userInput,
  setUserInput,
  isLoading,
  activeApp,
  selectedModelId,
  handleSendMessage,
}: ChatInputProps) {
  const isInputDisabled = !activeApp || isLoading
  const isSendDisabled = !userInput.trim() || isLoading || !activeApp || !selectedModelId

  return (
    <div className="border-t p-4 bg-card">
      <div className="flex items-center gap-2 relative">
        <Textarea
          placeholder="输入消息..."
          className="flex-1 pr-24 min-h-[4.5rem] resize-none"
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
        <div className="absolute right-2 bottom-2 flex gap-1.5">
          <Button size="icon" variant="ghost" className="h-7 w-7 rounded-full">
            <FileText className="h-4 w-4 text-muted-foreground" />
          </Button>
          <Button
            variant="ghost"
            className="rounded-full h-7 w-7"
            onClick={handleSendMessage}
            disabled={isSendDisabled}
          >
            {isLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
          </Button>
        </div>
      </div>
      <div className="flex justify-between mt-2 text-xs">
        <div className="text-muted-foreground">支持 Markdown 格式</div>
        <div className="text-muted-foreground flex items-center hover:text-foreground cursor-pointer">
          <span>开启知识库增强</span>
          <ArrowRight className="h-3 w-3 ml-1" />
        </div>
      </div>
    </div>
  )
}
