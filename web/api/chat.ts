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
  /** 模型ID */
  modelId: string
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
 * 提供流式和同步两种方式的聊天接口
 */
export const chatService = {
  /**
   * 发送聊天消息（流式响应）
   * @param request 聊天请求参数
   * @returns Promise<Response> 原始响应对象
   */
  async streamMessage(request: ChatRequest): Promise<Response> {
    return sseClient.fetchEventStream('/ai/chat', request)
  },

  /**
   * 发送聊天消息（流式响应）并处理流数据
   * 封装了请求发送和事件处理的完整流程
   * @param request 聊天请求参数
   * @param callbacks 回调函数集合，包含onMessage、onError、onComplete
   */
  async streamMessageWithHandling(request: ChatRequest, callbacks: SSECallbacks): Promise<void> {
    try {
      const response = await this.streamMessage(request)
      await sseClient.processEventStream(response, callbacks)
    } catch (error) {
      callbacks.onError?.(new Error('发送消息失败'))
    }
  },

  /**
   * 发送聊天消息（同步响应）
   * 适用于不需要流式响应的场景
   * @param request 聊天请求参数
   * @returns 聊天响应对象
   */
  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    try {
      const response = await api.post<ChatResponse>('/ai/chat/sync', request)
      return response.data
    } catch (error) {
      throw new Error('发送消息失败')
    }
  },
}
