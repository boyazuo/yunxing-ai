/**
 * 消息角色枚举
 */
export enum MessageRole {
  SYSTEM = 'system', // 系统消息
  USER = 'user', // 用户消息
  ASSISTANT = 'assistant', // 助手消息
}

/**
 * 消息状态枚举
 */
export enum MessageStatus {
  PENDING = 'pending', // 处理中
  SUCCESS = 'success', // 成功
  ERROR = 'error', // 失败
}

/**
 * 消息实体类型
 */
export interface Message {
  messageId: string
  conversationId: string
  question: string
  answer: string
  status: MessageStatus
  createTime: string
}

/**
 * 会话实体类型
 */
export interface Conversation {
  conversationId: string
  appId: string
  title: string
  createTime: string
  updateTime: string
  lastMessage?: string // 最后一条消息摘要，用于展示
}

/**
 * 前端消息类型 (UI展示用)
 */
export interface ChatMessage {
  id: string
  role: MessageRole
  content: string
  time: string
}
