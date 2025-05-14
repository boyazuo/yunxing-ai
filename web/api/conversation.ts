import { api } from '@/lib/api'
import type { Conversation, Message } from '@/types/chat'

/**
 * 会话服务
 */
export const conversationService = {
  /**
   * 获取用户在指定应用下的会话列表
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param appId 应用ID
   * @returns 会话列表
   */
  async getConversations(tenantId: string, userId: string, appId: string): Promise<Conversation[]> {
    try {
      const response = await api.get<Conversation[]>(
        `/conversations?tenantId=${tenantId}&userId=${userId}&appId=${appId}`,
      )
      return response.data
    } catch (error) {
      console.error('获取会话列表失败', error)
      throw new Error('获取会话列表失败')
    }
  },

  /**
   * 获取指定会话的消息列表
   * @param conversationId 会话ID
   * @returns 消息列表
   */
  async getConversationMessages(conversationId: string): Promise<Message[]> {
    try {
      const response = await api.get<Message[]>(`/messages/conversation/${conversationId}`)
      return response.data
    } catch (error) {
      console.error('获取会话消息失败', error)
      throw new Error('获取会话消息失败')
    }
  },

  /**
   * 创建新会话
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param appId 应用ID
   * @param title 会话标题
   * @returns 创建的会话
   */
  async createConversation(tenantId: string, userId: string, appId: string, title: string): Promise<Conversation> {
    try {
      const response = await api.post<Conversation>('/conversations', {
        tenantId,
        userId,
        appId,
        title,
      })
      return response.data
    } catch (error) {
      console.error('创建会话失败', error)
      throw new Error('创建会话失败')
    }
  },

  /**
   * 更新会话标题
   * @param conversationId 会话ID
   * @param title 新标题
   * @returns 更新后的会话
   */
  async updateConversationTitle(conversationId: string, title: string): Promise<Conversation> {
    try {
      const response = await api.put<Conversation>(`/conversations/${conversationId}/title`, { title })
      return response.data
    } catch (error) {
      console.error('更新会话标题失败', error)
      throw new Error('更新会话标题失败')
    }
  },

  /**
   * 删除会话
   * @param conversationId 会话ID
   */
  async deleteConversation(conversationId: string): Promise<void> {
    try {
      await api.delete<void>(`/conversations/${conversationId}`)
    } catch (error) {
      console.error('删除会话失败', error)
      throw new Error('删除会话失败')
    }
  },
}
