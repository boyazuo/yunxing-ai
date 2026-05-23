import { api } from '@/lib/api'
import { type SSECallbacks, sseClient } from '@/lib/sse'

/**
 * 聊天请求参数
 */
export interface ChatRequest {
  /** 应用ID */
  appId: string
  /** 会话ID，新会话为null */
  conversationId?: string | null
  /** 用户输入的问题 */
  prompt: string
}

/**
 * 聊天响应
 */
export interface ChatResponse {
  /** 会话ID */
  conversationId: string
  /** 消息ID */
  messageId: string
  /** 回复内容 */
  content: string
}

/**
 * 聊天服务
 */
export const chatService = {
  async streamMessage(request: ChatRequest): Promise<Response> {
    return sseClient.fetchEventStream('/ai/chat', request)
  },

  async streamMessageWithHandling(request: ChatRequest, callbacks: SSECallbacks): Promise<void> {
    try {
      const response = await this.streamMessage(request)
      await sseClient.processEventStream(response, callbacks)
    } catch {
      callbacks.onError?.(new Error('发送消息失败'))
    }
  },

  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    try {
      const response = await api.post<ChatResponse>('/ai/chat/sync', request)
      return response.data
    } catch {
      throw new Error('发送消息失败')
    }
  },
}
