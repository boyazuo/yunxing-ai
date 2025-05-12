import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import type { VariableConfig as VariableConfigType } from '@/types/appConfig'
import { useEffect, useState } from 'react'

interface VariableConfigProps {
  appId: string
  variables?: VariableConfigType[]
  onChange?: (variables: VariableConfigType[]) => void
}

interface Variable extends VariableConfigType {
  id: string
}

export default function VariableConfig({ appId, variables = [], onChange }: VariableConfigProps) {
  const [variableList, setVariableList] = useState<Variable[]>([])

  // 初始化和同步外部传入的变量配置
  useEffect(() => {
    if (variables.length > 0) {
      setVariableList(
        variables.map((variable) => ({
          ...variable,
          id: variable.id || `var-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
        })),
      )
    }
  }, [variables])

  // 将内部状态转换为外部变量配置并通知父组件
  const updateParent = (newVariables: Variable[]) => {
    setVariableList(newVariables)

    if (onChange) {
      onChange(newVariables)
    }
  }

  const addVariable = () => {
    const newVariable = {
      id: `var-${Date.now()}`,
      key: `var_${variableList.length + 1}`,
      name: `变量 ${variableList.length + 1}`,
      description: '',
      required: false,
    }

    updateParent([...variableList, newVariable])
  }

  const removeVariable = (id: string) => {
    updateParent(variableList.filter((variable) => variable.id !== id))
  }

  const updateVariable = (id: string, field: keyof VariableConfigType, value: string | boolean) => {
    updateParent(variableList.map((variable) => (variable.id === id ? { ...variable, [field]: value } : variable)))
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button variant="outline" size="sm" onClick={addVariable}>
          添加变量
        </Button>
      </div>
      {variableList.length > 0 ? (
        variableList.map((variable) => (
          <div key={variable.id} className="border border-border rounded-md p-3">
            <div className="flex justify-between items-center mb-2">
              <h4 className="text-sm font-medium">变量 {variableList.indexOf(variable) + 1}</h4>
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
                  value={variable.key}
                  onChange={(e) => updateVariable(variable.id, 'key', e.target.value)}
                />
                <p className="text-xs text-muted-foreground mt-1">系统内部使用的唯一标识符</p>
              </div>
              <div>
                <label htmlFor={`varName-${variable.id}`} className="text-xs font-medium mb-1 block">
                  显示名称
                </label>
                <Input
                  id={`varName-${variable.id}`}
                  className="h-8"
                  placeholder="姓名"
                  value={variable.name}
                  onChange={(e) => updateVariable(variable.id, 'name', e.target.value)}
                />
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
                  value={variable.description}
                  onChange={(e) => updateVariable(variable.id, 'description', e.target.value)}
                />
              </div>
              <div className="flex items-center pt-5 justify-between">
                <label htmlFor={`varRequired-${variable.id}`} className="text-xs font-medium">
                  必填项
                </label>
                <Switch
                  id={`varRequired-${variable.id}`}
                  checked={variable.required}
                  onCheckedChange={(checked) => updateVariable(variable.id, 'required', checked)}
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
