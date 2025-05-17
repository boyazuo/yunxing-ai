import { api } from '@/lib/api'
import { getSession } from 'next-auth/react'

interface ChatRequest {
  appId: string
  conversationId?: string | null
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

// 获取API基础URL
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/v1/api'

/**
 * 聊天服务
 */
export const chatService = {
  /**
   * 发送聊天消息（流式响应）
   * @param request 聊天请求参数
   * @returns Promise<Response>
   */
  async streamMessage(request: ChatRequest): Promise<Response> {
    try {
      // 获取认证令牌
      const session = await getSession()
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
      }

      // 如果有认证令牌，添加到请求头
      if (session?.accessToken) {
        headers.Authorization = session.accessToken
      }

      console.log('发送流式请求:', request)
      console.log('API基础URL:', API_BASE_URL)

      // 使用fetch API发送POST请求，获取流式响应
      // 这里我们不使用axios，因为axios的流处理在浏览器端不太直观
      // 对于流式响应，fetch API提供了更简单的处理方式
      const response = await fetch(`${API_BASE_URL}/ai/chat`, {
        method: 'POST',
        headers,
        body: JSON.stringify(request),
        credentials: 'include',
      })

      console.log('流式响应状态:', response.status, response.statusText, response.headers.get('Content-Type'))

      if (!response.ok) {
        throw new Error(`请求失败: ${response.status} ${response.statusText}`)
      }

      return response
    } catch (error) {
      console.error('创建流式请求失败', error)
      throw error
    }
  },

  /**
   * 发送聊天消息（同步响应）
   * @param request 聊天请求参数
   * @returns 聊天响应
   */
  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    try {
      const response = await api.post<ChatResponse>('/ai/chat/sync', request)
      return response.data
    } catch (error) {
      console.error('发送消息失败', error)
      throw new Error('发送消息失败')
    }
  },
}
