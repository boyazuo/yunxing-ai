import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Edit, MoreHorizontal, Plus, Trash, User } from 'lucide-react'

// 模拟应用数据
const mockApps = [
  {
    id: 1,
    name: '客户服务助手',
    type: '聊天机器人',
    description: '智能客服系统，提供7*24小时服务',
    author: '张三',
    authorAvatar: '/avatar/user1.png',
    updatedAt: '2023-12-01',
    logo: '/logos/customer-service.png',
  },
  {
    id: 2,
    name: '营销文案生成器',
    type: '内容生成',
    description: '自动生成符合品牌调性的营销文案',
    author: '李四',
    authorAvatar: '/avatar/user2.png',
    updatedAt: '2023-12-05',
    logo: '/logos/marketing.png',
  },
  {
    id: 3,
    name: '数据分析助手',
    type: '数据分析',
    description: '快速分析业务数据，生成洞察报告',
    author: '王五',
    authorAvatar: '/avatar/user3.png',
    updatedAt: '2023-12-10',
    logo: '/logos/data-analysis.png',
  },
  {
    id: 4,
    name: '知识库问答',
    type: '知识库',
    description: '基于企业知识库的智能问答系统',
    author: '赵六',
    authorAvatar: '/avatar/user4.png',
    updatedAt: '2023-12-15',
    logo: '/logos/knowledge-base.png',
  },
  {
    id: 5,
    name: '文档摘要工具',
    type: '文档处理',
    description: '自动提取文档关键信息，生成摘要',
    author: '钱七',
    authorAvatar: '/avatar/user5.png',
    updatedAt: '2023-12-20',
    logo: '/logos/document.png',
  },
]

export default function SpacePage() {
  return (
    <div className="container mx-auto py-6 space-y-6">
      {/* 顶部筛选和搜索区 */}
      <div className="flex justify-between items-center">
        <div className="flex items-center space-x-4">
          <Select defaultValue="all">
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="筛选应用" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部应用</SelectItem>
              <SelectItem value="recent">最近使用</SelectItem>
              <SelectItem value="created">我创建的</SelectItem>
              <SelectItem value="shared">共享给我的</SelectItem>
            </SelectContent>
          </Select>
          <Select defaultValue="all">
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="筛选应用" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部应用</SelectItem>
              <SelectItem value="chat">对话应用</SelectItem>
              <SelectItem value="agent">智能体</SelectItem>
              <SelectItem value="workflow">工作流</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="flex items-center space-x-2">
          <Input placeholder="搜索应用..." className="w-[250px]" />
          <Button>
            <Plus className="mr-2 h-4 w-4" />
            新建应用
          </Button>
        </div>
      </div>

      {/* 应用列表 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {mockApps.map((app) => (
          <Card key={app.id} className="cursor-pointer hover:shadow-md transition-shadow overflow-hidden">
            {/* 卡片头部 */}
            <div className="px-4 flex items-start justify-between">
              <div className="flex space-x-3">
                <div className="flex-shrink-0">
                  <Avatar className="h-10 w-10">
                    <AvatarImage src={app.logo} alt={app.name} />
                    <AvatarFallback>{app.name.slice(0, 2)}</AvatarFallback>
                  </Avatar>
                </div>
                <div>
                  <h3 className="font-medium text-sm">{app.name}</h3>
                  <span className="inline-block px-2 py-0.5 mt-1 text-xs bg-muted rounded-full">{app.type}</span>
                </div>
              </div>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="icon" className="h-8 w-8">
                    <MoreHorizontal className="h-4 w-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem>
                    <Edit className="mr-2 h-4 w-4" />
                    <span>编辑信息</span>
                  </DropdownMenuItem>
                  <DropdownMenuItem className="text-destructive">
                    <Trash className="mr-2 h-4 w-4" />
                    <span>删除应用</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>

            {/* 卡片内容 */}
            <CardContent className="px-4 h-12">
              <p className="text-xs text-muted-foreground line-clamp-3">{app.description}</p>
            </CardContent>

            {/* 卡片底部 */}
            <CardFooter className="px-4 flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Avatar className="h-6 w-6">
                  <AvatarImage src={app.authorAvatar} alt={app.author} />
                  <AvatarFallback>
                    <User className="h-3 w-3" />
                  </AvatarFallback>
                </Avatar>
                <span className="text-xs">{app.author}</span>
              </div>
              <span className="text-xs text-muted-foreground">更新于 {app.updatedAt}</span>
            </CardFooter>
          </Card>
        ))}
      </div>
    </div>
  )
}
