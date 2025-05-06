'use client'

import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import {
  ArrowRight,
  ChevronLeft,
  ChevronRight,
  FileText,
  Loader2,
  MessageSquare,
  Search,
  Send,
  Settings,
  User,
} from 'lucide-react'
import { useState } from 'react'

// 模拟应用数据
const mockApps = [
  {
    id: 1,
    name: '客户服务助手',
    description: '智能客服系统，提供7*24小时服务',
    icon: '🤖',
    updatedAt: '2023-12-01',
    active: true,
  },
  {
    id: 2,
    name: '营销文案生成器',
    description: '自动生成符合品牌调性的营销文案',
    icon: '📝',
    updatedAt: '2023-12-05',
    active: false,
  },
  {
    id: 3,
    name: '数据分析助手',
    description: '快速分析业务数据，生成洞察报告',
    icon: '📊',
    updatedAt: '2023-12-10',
    active: false,
  },
  {
    id: 4,
    name: '知识库问答',
    description: '基于企业知识库的智能问答系统',
    icon: '🔍',
    updatedAt: '2023-12-15',
    active: false,
  },
]

// 模拟会话历史
const mockConversations = [
  {
    id: 1,
    title: '如何优化网站SEO',
    time: '10分钟前',
    excerpt: '我想了解如何提高我的网站在搜索引擎中的排名...',
    isActive: true,
  },
  {
    id: 2,
    title: '市场营销策略咨询',
    time: '1小时前',
    excerpt: '我需要为一个新产品制定营销策略...',
    isActive: false,
  },
  {
    id: 3,
    title: '数据分析报告生成',
    time: '昨天',
    excerpt: '请帮我分析这份销售数据并生成报告...',
    isActive: false,
  },
  {
    id: 4,
    title: '客户反馈分析',
    time: '3天前',
    excerpt: '我有一批客户反馈数据需要分析...',
    isActive: false,
  },
]

// 模拟聊天消息
const mockMessages = [
  {
    id: 1,
    role: 'user',
    content: '我想了解如何提高我的网站在搜索引擎中的排名？',
    time: '10:30',
  },
  {
    id: 2,
    role: 'assistant',
    content:
      '提高网站SEO排名需要从多个方面入手：\n\n1. 优质内容：创建原创、有价值且与用户搜索意图相关的内容\n2. 关键词优化：研究并使用相关关键词，包括长尾关键词\n3. 网站结构：确保网站有清晰的结构和导航\n4. 技术SEO：优化网站加载速度、移动友好性、HTTPS安全等\n5. 反向链接：获取高质量的外部链接\n6. 用户体验：提高页面停留时间和降低跳出率\n\n您希望我详细解释其中的哪个方面？',
    time: '10:32',
  },
  {
    id: 3,
    role: 'user',
    content: '请详细解释一下关键词优化的策略。',
    time: '10:35',
  },
  {
    id: 4,
    role: 'assistant',
    content:
      '关键词优化是SEO的基础，以下是详细的关键词优化策略：\n\n1. 关键词研究：\n   - 使用Google关键词规划师、SEMrush等工具找到相关关键词\n   - 分析竞争对手使用的关键词\n   - 关注搜索量和竞争度的平衡\n\n2. 长尾关键词：\n   - 定位更具体的长尾关键词，它们竞争较小但转化率高\n   - 围绕问题和特定需求构建长尾关键词\n\n3. 关键词布局：\n   - 在标题标签(H1)中使用主关键词\n   - 在URL中包含关键词\n   - 在前100词中自然地使用主关键词\n   - 在副标题(H2,H3)中使用相关关键词\n   - 在图片alt文本中使用关键词\n\n4. 语义SEO：\n   - 使用LSI关键词(潜在语义索引)补充主关键词\n   - 围绕主题而非单个关键词构建内容\n\n5. 关键词密度：\n   - 保持自然，避免关键词堆砌\n   - 一般建议密度在1-2%之间\n\n6. 内容更新：\n   - 定期更新内容以保持关键词相关性\n   - 跟踪关键词排名变化并调整策略\n\n您想了解这些策略中的哪些具体实施方法？',
    time: '10:40',
  },
]

export default function AppsPage() {
  const [userInput, setUserInput] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false)

  // 模拟发送消息
  const handleSendMessage = () => {
    if (!userInput.trim()) return
    setIsLoading(true)
    // 模拟API调用延迟
    setTimeout(() => {
      setUserInput('')
      setIsLoading(false)
    }, 1000)
  }

  // 切换侧边栏
  const toggleSidebar = () => {
    setIsSidebarCollapsed(!isSidebarCollapsed)
  }

  return (
    <div className="flex h-[calc(100vh-60px)]">
      {/* 左侧应用列表 */}
      <div
        className={`border-r bg-card flex flex-col transition-all duration-300 ${isSidebarCollapsed ? 'w-16' : 'w-64'}`}
      >
        <div className="p-4 flex items-center justify-between border-b">
          {!isSidebarCollapsed && <h2 className="font-semibold text-foreground">应用列表</h2>}
          <Button
            size="icon"
            variant="ghost"
            className={`h-8 w-8 ${isSidebarCollapsed ? 'mx-auto' : 'ml-auto'}`}
            onClick={toggleSidebar}
          >
            {isSidebarCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
          </Button>
        </div>

        {!isSidebarCollapsed && (
          <div className="relative px-4 pt-4 pb-2">
            <Search className="absolute left-6.5 top-6.5 h-4 w-4 text-muted-foreground" />
            <Input placeholder="搜索应用..." className="pl-9" />
          </div>
        )}

        <nav className="overflow-auto flex-1 p-2">
          {mockApps.map((app) => (
            <div
              key={app.id}
              className={`flex items-center rounded-md cursor-pointer transition-colors mb-1 ${
                app.active ? 'bg-accent text-accent-foreground' : 'hover:bg-muted'
              } ${isSidebarCollapsed ? 'justify-center py-3 px-2' : 'px-3 py-2'}`}
            >
              <div className={`text-xl flex-shrink-0 ${isSidebarCollapsed ? '' : 'mr-3'}`}>{app.icon}</div>
              {!isSidebarCollapsed && <div className="font-medium text-sm truncate">{app.name}</div>}
            </div>
          ))}
        </nav>
      </div>

      {/* 右侧聊天区域 */}
      <div className="flex-1 flex">
        {/* 会话历史 */}
        <div className="w-72 border-r flex flex-col bg-muted/10">
          <div className="p-4 border-b">
            <div className="flex items-center justify-between">
              <h2 className="font-semibold">会话历史</h2>
            </div>
          </div>

          <div className="space-y-1 overflow-auto flex-1 p-2">
            {mockConversations.map((conversation) => (
              <div
                key={conversation.id}
                className={`px-3 py-2.5 rounded-md cursor-pointer transition-colors ${
                  conversation.isActive ? 'bg-primary text-primary-foreground' : 'hover:bg-accent/50'
                }`}
              >
                <div className="flex items-center justify-between">
                  <h3 className="font-medium text-sm">{conversation.title}</h3>
                  <span
                    className={`text-xs ${conversation.isActive ? 'text-primary-foreground/70' : 'text-muted-foreground'}`}
                  >
                    {conversation.time}
                  </span>
                </div>
                <p
                  className={`text-xs mt-1 line-clamp-1 ${conversation.isActive ? 'text-primary-foreground/90' : 'text-muted-foreground'}`}
                >
                  {conversation.excerpt}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* 对话区域 */}
        <div className="flex-1 flex flex-col bg-background">
          {/* 对话头部 */}
          <div className="border-b p-4 flex items-center justify-between bg-card">
            <div className="flex items-center">
              <Avatar className="h-9 w-9 mr-3">
                <AvatarFallback>🤖</AvatarFallback>
              </Avatar>
              <div>
                <h3 className="font-medium">客户服务助手</h3>
                <p className="text-xs text-muted-foreground flex items-center">
                  <span className="bg-green-500 h-1.5 w-1.5 rounded-full inline-block mr-1.5" />
                  在线
                </p>
              </div>
            </div>
            <div className="flex gap-2">
              <Button variant="outline" size="icon" className="h-8 w-8">
                <Settings className="h-4 w-4" />
              </Button>
              <Button variant="outline" size="sm">
                <MessageSquare className="h-4 w-4 mr-2" />
                分享会话
              </Button>
            </div>
          </div>

          {/* 消息区域 */}
          <div className="flex-1 overflow-auto p-4 space-y-6">
            {mockMessages.map((message) => (
              <div key={message.id} className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div className={`flex gap-3 max-w-[85%] ${message.role === 'user' ? 'flex-row-reverse' : ''}`}>
                  <Avatar className={`h-8 w-8 ${message.role === 'user' ? 'mt-1' : ''}`}>
                    {message.role === 'user' ? (
                      <AvatarFallback className="bg-primary text-primary-foreground">
                        <User className="h-4 w-4" />
                      </AvatarFallback>
                    ) : (
                      <AvatarFallback className="bg-muted">🤖</AvatarFallback>
                    )}
                  </Avatar>
                  <div>
                    <div
                      className={`px-4 py-3 rounded-lg ${
                        message.role === 'user' ? 'bg-primary text-primary-foreground shadow-sm' : 'bg-muted shadow-sm'
                      }`}
                    >
                      <div className="whitespace-pre-line text-sm">{message.content}</div>
                    </div>
                    <div
                      className={`text-xs mt-1 text-muted-foreground ${message.role === 'user' ? 'text-right' : ''}`}
                    >
                      {message.time}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* 输入区域 */}
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
              />
              <div className="absolute right-2 bottom-2 flex gap-1.5">
                <Button size="icon" variant="ghost" className="h-7 w-7 rounded-full">
                  <FileText className="h-4 w-4 text-muted-foreground" />
                </Button>
                <Button
                  variant="ghost"
                  className="rounded-full h-7 w-7"
                  onClick={handleSendMessage}
                  disabled={!userInput.trim() || isLoading}
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
        </div>
      </div>
    </div>
  )
}
