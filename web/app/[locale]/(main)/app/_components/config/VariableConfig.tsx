import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { useState } from 'react'

interface VariableConfigProps {
  appId: string
}

export default function VariableConfig({ appId }: VariableConfigProps) {
  const [variables, setVariables] = useState<
    Array<{
      id: string
      key: string
      name: string
      description: string
      required: boolean
    }>
  >([])

  const addVariable = () => {
    setVariables((prev) => [
      ...prev,
      {
        id: `var-${Date.now()}`,
        key: `var_${prev.length + 1}`,
        name: `变量 ${prev.length + 1}`,
        description: '',
        required: false,
      },
    ])
  }

  const removeVariable = (id: string) => {
    setVariables((prev) => prev.filter((variable) => variable.id !== id))
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button variant="outline" size="sm" onClick={addVariable}>
          添加变量
        </Button>
      </div>
      {variables.length > 0 ? (
        variables.map((variable) => (
          <div key={variable.id} className="border border-border rounded-md p-3">
            <div className="flex justify-between items-center mb-2">
              <h4 className="text-sm font-medium">变量 {variables.indexOf(variable) + 1}</h4>
              <Button variant="ghost" size="sm" className="h-7 w-7 p-0" onClick={() => removeVariable(variable.id)}>
                ×
              </Button>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label htmlFor={`varKey-${variable.id}`} className="text-xs font-medium mb-1 block">
                  变量键名
                </label>
                <Input
                  id={`varKey-${variable.id}`}
                  className="h-8"
                  placeholder="user_name"
                  defaultValue={variable.key}
                />
                <p className="text-xs text-muted-foreground mt-1">系统内部使用的唯一标识符</p>
              </div>
              <div>
                <label htmlFor={`varName-${variable.id}`} className="text-xs font-medium mb-1 block">
                  显示名称
                </label>
                <Input id={`varName-${variable.id}`} className="h-8" placeholder="姓名" defaultValue={variable.name} />
                <p className="text-xs text-muted-foreground mt-1">用户看到的变量名称</p>
              </div>
              <div>
                <label htmlFor={`varDesc-${variable.id}`} className="text-xs font-medium mb-1 block">
                  描述说明
                </label>
                <Input
                  id={`varDesc-${variable.id}`}
                  className="h-8"
                  placeholder="请填写您的真实姓名"
                  defaultValue={variable.description}
                />
              </div>
              <div className="flex items-center pt-5 justify-between">
                <label htmlFor={`varRequired-${variable.id}`} className="text-xs font-medium">
                  必填项
                </label>
                <Switch
                  id={`varRequired-${variable.id}`}
                  checked={variable.required}
                  onCheckedChange={(checked) => {
                    setVariables((prev) => prev.map((v) => (v.id === variable.id ? { ...v, required: checked } : v)))
                  }}
                />
              </div>
            </div>
          </div>
        ))
      ) : (
        <div className="py-6 text-center text-muted-foreground">
          <p className="text-sm">暂无变量，点击"添加变量"来开始</p>
        </div>
      )}
    </div>
  )
}
