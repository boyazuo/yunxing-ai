import { Textarea } from '@/components/ui/textarea'
import { useEffect, useState } from 'react'

interface PromptConfigProps {
  appId: string
  sysPrompt?: string
  onChange?: (sysPrompt: string) => void
}

export default function PromptConfig({ appId, sysPrompt = '', onChange }: PromptConfigProps) {
  const [prompt, setPrompt] = useState(sysPrompt)

  // 当系统提示词属性变化时，更新内部状态
  useEffect(() => {
    setPrompt(sysPrompt)
  }, [sysPrompt])

  // 当内部状态变化时，通知父组件
  const handleChange = (value: string) => {
    setPrompt(value)
    onChange?.(value)
  }

  return (
    <div className="space-y-4">
      <div>
        <label htmlFor="sysPrompt" className="text-sm font-medium mb-1 block">
          系统提示词
        </label>
        <Textarea
          id="sysPrompt"
          placeholder="设置AI的系统提示词，定义AI的行为和限制..."
          className="min-h-[200px]"
          value={prompt}
          onChange={(e) => handleChange(e.target.value)}
        />
        <p className="text-xs text-muted-foreground mt-1">
          系统提示词用于指导AI的行为方式、专业领域和回答限制，对用户不可见
        </p>
      </div>
    </div>
  )
}
