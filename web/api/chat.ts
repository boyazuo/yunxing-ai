import { api } from '@/lib/api'

interface ChatRequest {
  appId: string
  conversationId?: string
  modelId: string
  prompt: string
}

/**
 * 聊天响应
 */
interface ChatResponse {
  conversationId: string
  messageId: string
  content: string
}

/**
 * 聊天服务
 */
export const chatService = {
  /**
   * 发送聊天消息
   * @param request 聊天请求参数
   * @returns 聊天响应
   */
  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    try {
      const response = await api.post<ChatResponse>('/ai/chat', request)
      return response.data
    } catch (error) {
      console.error('发送消息失败', error)
      throw new Error('发送消息失败')
    }
  },
}
