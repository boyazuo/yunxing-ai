import { Textarea } from '@/components/ui/textarea'

interface PromptConfigProps {
  appId: string
}

export default function PromptConfig({ appId }: PromptConfigProps) {
  return (
    <div className="space-y-4">
      <div>
        <label htmlFor="systemPrompt" className="text-sm font-medium mb-1 block">
          系统提示词
        </label>
        <Textarea id="systemPrompt" placeholder="设置AI的系统提示词，定义AI的行为和限制..." className="min-h-[200px]" />
        <p className="text-xs text-muted-foreground mt-1">
          系统提示词用于指导AI的行为方式、专业领域和回答限制，对用户不可见
        </p>
      </div>
    </div>
  )
}
