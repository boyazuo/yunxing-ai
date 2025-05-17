import { getSession } from 'next-auth/react'

// 获取API基础URL
const DEFAULT_API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/v1/api'

/**
 * SSE事件类型
 */
export type SSEEvent = {
  name: string
  data: string
}

/**
 * SSE配置参数
 */
export type SSEOptions = {
  apiBaseUrl?: string
  defaultHeaders?: Record<string, string>
  credentials?: RequestCredentials
  debug?: boolean
}

/**
 * SSE回调函数类型
 */
export type SSECallbacks = {
  onMessage: (event: SSEEvent) => void
  onError?: (error: Error) => void
  onComplete?: () => void
}

/**
 * SSE（Server-Sent Events）客户端
 * 用于处理服务器发送的事件流
 */
export class SSEClient {
  private readonly apiBaseUrl: string
  private readonly defaultHeaders: Record<string, string>
  private readonly credentials: RequestCredentials

  /**
   * 构造函数
   * @param options SSE配置参数
   */
  constructor(options: SSEOptions = {}) {
    this.apiBaseUrl = options.apiBaseUrl || DEFAULT_API_BASE_URL
    this.defaultHeaders = options.defaultHeaders || {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
    }
    this.credentials = options.credentials || 'include'
  }

  /**
   * 创建请求头
   * @param additionalHeaders 额外的请求头
   * @returns 合并后的请求头
   */
  private async createHeaders(additionalHeaders?: Record<string, string>): Promise<Record<string, string>> {
    const headers = { ...this.defaultHeaders }

    // 获取认证令牌
    try {
      const session = await getSession()
      if (session?.accessToken) {
        headers.Authorization = session.accessToken
      }
    } catch (error) {
      console.error('获取会话令牌失败', error)
    }

    // 合并额外的请求头
    return { ...headers, ...additionalHeaders }
  }

  /**
   * 发送流式请求
   * @param url API路径，不包含基础URL
   * @param body 请求体
   * @param method 请求方法，默认为POST
   * @param additionalHeaders 额外的请求头
   * @returns Promise<Response>
   */
  async fetchEventStream(
    url: string,
    body: unknown,
    method = 'POST',
    additionalHeaders?: Record<string, string>,
  ): Promise<Response> {
    try {
      const headers = await this.createHeaders(additionalHeaders)
      const fullUrl = url.startsWith('http') ? url : `${this.apiBaseUrl}${url}`

      // 使用fetch API发送请求，获取流式响应
      const response = await fetch(fullUrl, {
        method,
        headers,
        body: JSON.stringify(body),
        credentials: this.credentials,
      })

      if (!response.ok) {
        throw new Error(`请求失败: ${response.status} ${response.statusText}`)
      }

      return response
    } catch (error) {
      console.error('创建流式请求失败', error)
      throw error
    }
  }

  /**
   * 处理事件流
   * @param response fetch返回的Response对象
   * @param callbacks 回调函数集合
   */
  async processEventStream(response: Response, callbacks: SSECallbacks): Promise<void> {
    const { onMessage, onError, onComplete } = callbacks

    try {
      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('无法获取响应流')
      }

      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()

        if (done) {
          // 处理最后可能剩余的数据
          if (buffer?.trim()) {
            this.processEvent(buffer.trim(), onMessage)
          }
          onComplete?.()
          break
        }

        // 解码二进制数据并追加到缓冲区
        buffer += decoder.decode(value, { stream: true })

        // 按SSE标准格式处理：双换行符分隔的事件
        const events = buffer.split('\n\n')

        // 保留最后一个可能不完整的事件
        buffer = events.pop() || ''

        // 处理所有完整的事件
        for (const event of events) {
          if (!event.trim()) continue
          this.processEvent(event, onMessage)
        }
      }
    } catch (error) {
      console.error('处理事件流失败', error)
      onError?.(error instanceof Error ? error : new Error(String(error)))
    }
  }

  /**
   * 处理单个事件
   * @param eventText 事件文本
   * @param onMessage 消息回调函数
   */
  private processEvent(eventText: string, onMessage: (event: SSEEvent) => void): void {
    try {
      // 解析事件
      const lines = eventText.split('\n')
      let eventName = 'message'
      let eventData = ''

      for (const line of lines) {
        if (line.startsWith('event:')) {
          eventName = line.substring(6).trim()
        } else if (line.startsWith('data:')) {
          eventData = line.substring(5).trim()
        }
      }

      // 如果没有找到data但有内容，可能是非标准格式，直接使用整个事件
      if (!eventData && eventText.trim()) {
        eventData = eventText.trim()
      }

      if (eventData) {
        const sseEvent: SSEEvent = { name: eventName, data: eventData }
        onMessage(sseEvent)
      }
    } catch (error) {
      console.error('处理单个事件失败', error)
    }
  }
}

// 导出默认实例，方便直接使用
export const sseClient = new SSEClient()
