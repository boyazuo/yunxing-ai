'use client'

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { BadgeCheck, Bell, CreditCard, LogOut, Sparkles } from 'lucide-react'
import { useRouter } from 'next/navigation'

export default function UserAvatar() {
  const router = useRouter()

  // 这里我们创建一个模拟的用户，在实际项目中应该使用 next-auth
  const mockUser = {
    name: 'Boya',
    email: '6989006@qq.com',
    avatar: '',
  }

  const handleLogin = () => {
    router.push('/login')
  }

  const handleLogout = () => {
    // 在实际项目中应该调用 signOut
    router.replace('/login')
  }

  // 默认显示模拟用户
  // 在实际项目中可以使用 useSession() 替换这部分逻辑
  const user = mockUser

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className="relative border-none text-muted-foreground hover:bg-transparent focus:ring-0 focus:ring-offset-0 cursor-pointer"
        >
          <Avatar className="h-8 w-8">
            <AvatarImage src={user.avatar} alt={user.name} />
            <AvatarFallback>{user.name.slice(0, 2).toUpperCase()}</AvatarFallback>
          </Avatar>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56" align="end" forceMount>
        <DropdownMenuLabel className="font-normal">
          <div className="flex flex-col space-y-1">
            <p className="text-sm font-medium leading-none">{user.name}</p>
            <p className="text-xs leading-none text-muted-foreground">{user.email}</p>
          </div>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuGroup>
          <DropdownMenuItem onClick={() => router.push('/settings')}>
            <BadgeCheck className="mr-2 h-4 w-4" />
            <span>账号设置</span>
          </DropdownMenuItem>
          <DropdownMenuItem>
            <CreditCard className="mr-2 h-4 w-4" />
            <span>账单管理</span>
          </DropdownMenuItem>
          <DropdownMenuItem>
            <Bell className="mr-2 h-4 w-4" />
            <span>通知设置</span>
          </DropdownMenuItem>
        </DropdownMenuGroup>
        <DropdownMenuSeparator />
        <DropdownMenuItem>
          <Sparkles className="mr-2 h-4 w-4" />
          <span>升级到专业版</span>
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={handleLogout}>
          <LogOut className="mr-2 h-4 w-4" />
          <span>退出登录</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
