import { Switch } from '@/components/ui/switch'
import { Database } from 'lucide-react'
import { useState } from 'react'

interface KnowledgebaseConfigProps {
  appId: string
}

export default function KnowledgebaseConfig({ appId }: KnowledgebaseConfigProps) {
  // 模拟可用知识库数据
  const [knowledgebases] = useState([
    { id: 'kb1', name: '公司产品手册', selected: false },
    { id: 'kb2', name: '技术文档库', selected: false },
    { id: 'kb3', name: '客户服务指南', selected: false },
    { id: 'kb4', name: '行业知识库', selected: false },
  ])

  const [selectedKbs, setSelectedKbs] = useState<string[]>([])

  const toggleKnowledgebase = (id: string) => {
    if (selectedKbs.includes(id)) {
      setSelectedKbs((prev) => prev.filter((kbId) => kbId !== id))
    } else {
      setSelectedKbs((prev) => [...prev, id])
    }
  }

  return (
    <div className="grid grid-cols-2 gap-3">
      {knowledgebases.map((kb) => (
        <div key={kb.id} className="flex items-center border rounded-md p-2.5 border-border">
          <Database className="h-4 w-4 text-muted-foreground mr-2" />
          <label htmlFor={kb.id} className="text-sm truncate cursor-pointer flex-1">
            {kb.name}
          </label>
          <Switch id={kb.id} checked={selectedKbs.includes(kb.id)} onCheckedChange={() => toggleKnowledgebase(kb.id)} />
        </div>
      ))}
    </div>
  )
}
