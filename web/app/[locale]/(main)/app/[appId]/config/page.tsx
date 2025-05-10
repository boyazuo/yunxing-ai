'use client'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Bot,
  ChevronDown,
  ChevronLeft,
  ChevronUp,
  Database,
  MessageSquare,
  PlusCircle,
  RefreshCw,
  Save,
} from 'lucide-react'
import { useParams, useRouter } from 'next/navigation'
import { useState } from 'react'

import KnowledgebaseConfig from '@/app/[locale]/(main)/app/_components/config/KnowledgebaseConfig'
import ModelConfig from '@/app/[locale]/(main)/app/_components/config/ModelConfig'
import PromptConfig from '@/app/[locale]/(main)/app/_components/config/PromptConfig'
import VariableConfig from '@/app/[locale]/(main)/app/_components/config/VariableConfig'

export default function AppConfigPage() {
  const params = useParams()
  const router = useRouter()
  const appId = params.appId as string
  const [appName, setAppName] = useState('我的AI应用') // 假设这是应用名称，实际项目中应该从API获取

  // 控制各个部分是否展开的状态
  const [sectionState, setSectionState] = useState({
    promptSection: true,
    modelSection: false,
    variableSection: false,
    knowledgebaseSection: false,
  })

  // 切换某一部分的展开/折叠状态
  const toggleSection = (section: keyof typeof sectionState) => {
    setSectionState((prev) => ({
      ...prev,
      [section]: !prev[section],
    }))
  }

  // 调试预览区域的模拟对话
  const [chatInput, setChatInput] = useState('')
  const [chatMessages, setChatMessages] = useState([
    {
      id: 'welcome-msg',
      role: 'assistant',
      content: '您好！我是您的AI助手。请问有什么我可以帮助您的？',
    },
  ])

  // 返回应用列表页
  const handleBack = () => {
    router.push('/app')
  }

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault()
    if (!chatInput.trim()) return

    // 添加用户消息
    const userMsgId = `user-${Date.now()}`
    setChatMessages((prev) => [...prev, { id: userMsgId, role: 'user', content: chatInput }])

    // 模拟AI回复
    setTimeout(() => {
      const aiMsgId = `ai-${Date.now()}`
      setChatMessages((prev) => [
        ...prev,
        {
          id: aiMsgId,
          role: 'assistant',
          content: `我是AI助手，正在回复您的问题："${chatInput}"`,
        },
      ])
    }, 1000)

    setChatInput('')
  }

  // 保存配置
  const handleSaveConfig = () => {
    // 这里实现保存配置的逻辑
    console.log('保存配置')
  }

  return (
    <div className="flex flex-col h-full">
      {/* 顶部导航 */}
      <div className="p-4 border-b border-border flex justify-between items-center">
        <div className="flex items-center">
          <Button variant="ghost" size="icon" className="mr-2" onClick={handleBack}>
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <h1 className="font-medium">{appName}</h1>
        </div>
        <Button variant="default" size="sm" className="flex items-center gap-1" onClick={handleSaveConfig}>
          <Save className="h-4 w-4 mr-1" />
          保存配置
        </Button>
      </div>

      <div className="flex flex-1 h-[calc(100%-57px)]">
        {/* 左侧配置区域 */}
        <div className="w-[450px] border-r border-border h-full overflow-y-auto">
          <div className="p-4 space-y-2">
            {/* 提示词设置部分 */}
            <div className="bg-background rounded-lg overflow-hidden">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between bg-muted/50 text-left"
                onClick={() => toggleSection('promptSection')}
              >
                <div className="flex items-center">
                  <MessageSquare className="h-4 w-4 mr-2 text-primary" />
                  <span className="font-medium text-sm">提示词设置</span>
                </div>
                {sectionState.promptSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.promptSection && (
                <div className="p-4">
                  <PromptConfig appId={appId} />
                </div>
              )}
            </div>

            {/* AI 模型配置部分 */}
            <div className="bg-background rounded-lg overflow-hidden">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between bg-muted/50 text-left"
                onClick={() => toggleSection('modelSection')}
              >
                <div className="flex items-center">
                  <Bot className="h-4 w-4 mr-2 text-primary" />
                  <span className="font-medium text-sm">AI 模型配置</span>
                </div>
                {sectionState.modelSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.modelSection && (
                <div className="p-4">
                  <ModelConfig appId={appId} />
                </div>
              )}
            </div>

            {/* 变量配置部分 */}
            <div className="bg-background rounded-lg overflow-hidden">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between bg-muted/50 text-left"
                onClick={() => toggleSection('variableSection')}
              >
                <div className="flex items-center">
                  <RefreshCw className="h-4 w-4 mr-2 text-primary" />
                  <span className="font-medium text-sm">变量配置</span>
                </div>
                {sectionState.variableSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.variableSection && (
                <div className="p-4">
                  <VariableConfig appId={appId} />
                </div>
              )}
            </div>

            {/* 关联知识库部分 */}
            <div className="bg-background rounded-lg overflow-hidden">
              <button
                type="button"
                className="w-full py-3 px-4 flex items-center justify-between bg-muted/50 text-left"
                onClick={() => toggleSection('knowledgebaseSection')}
              >
                <div className="flex items-center">
                  <Database className="h-4 w-4 mr-2 text-primary" />
                  <span className="font-medium text-sm">关联知识库</span>
                </div>
                {sectionState.knowledgebaseSection ? (
                  <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                  <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              {sectionState.knowledgebaseSection && (
                <div className="p-4">
                  <KnowledgebaseConfig appId={appId} />
                </div>
              )}
            </div>
          </div>
        </div>

        {/* 右侧调试预览区域 */}
        <div className="flex-1 flex flex-col h-full">
          {/* 调试预览标题栏 */}
          <div className="flex justify-between items-center px-6 py-4 border-b border-border">
            <h2 className="font-medium">调试预览</h2>
            <Button variant="outline" size="sm" className="flex items-center gap-1 px-3 text-xs">
              <RefreshCw className="w-3.5 h-3.5 mr-1" />
              刷新
            </Button>
          </div>

          {/* 聊天容器 - 使用flex布局让输入框固定在底部 */}
          <div className="flex-1 flex flex-col">
            {/* 消息列表区域 - 使用flex-1让它占据所有可用空间 */}
            <div className="flex-1 overflow-y-auto px-6 py-4 bg-gray-50">
              {chatMessages.map((msg) => (
                <div key={msg.id} className={`flex mb-4 ${msg.role === 'assistant' ? 'justify-start' : 'justify-end'}`}>
                  <div
                    className={`max-w-[80%] p-3 rounded-lg ${
                      msg.role === 'assistant'
                        ? 'bg-white border border-gray-200 text-foreground'
                        : 'bg-primary text-primary-foreground'
                    }`}
                  >
                    {msg.content}
                  </div>
                </div>
              ))}
            </div>

            {/* 输入区域 - 固定在底部 */}
            <div className="px-6 py-4 border-t border-border bg-white">
              <form onSubmit={handleSendMessage} className="flex gap-2">
                <Input
                  placeholder="输入问题，按Enter键执行 [Ctrl(Alt/Shift) + Enter]"
                  value={chatInput}
                  onChange={(e) => setChatInput(e.target.value)}
                  className="flex-1 border-gray-200"
                />
                <Button
                  type="submit"
                  className="bg-primary text-primary-foreground rounded-full w-10 h-10 p-0 flex items-center justify-center"
                >
                  <PlusCircle className="h-5 w-5" />
                </Button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
